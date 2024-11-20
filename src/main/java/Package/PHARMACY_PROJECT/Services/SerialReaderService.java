package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

@Service
public class SerialReaderService {

    private static final String ARDUINO_PORT = "COM8";
    private static final String BACKEND_URL = "http://localhost:8080/empleado/registrarHuella";
    private SerialPort port;
    private final RestTemplate restTemplate = new RestTemplate();
    private Set<String> processedFingerprints = new HashSet<>(); // Para evitar duplicados

    public void startSerialCommunication() {
        try {
            port = configureSerialPort(ARDUINO_PORT);

            if (port == null || !port.openPort()) {
                System.err.println("Error: No se pudo abrir el puerto " + ARDUINO_PORT);
                return;
            }

            System.out.println("Conectado al puerto serie: " + ARDUINO_PORT);

            new Thread(this::readSerialData).start();
        } catch (Exception e) {
            System.err.println("Error al iniciar la comunicación serial: " + e.getMessage());
        }
    }

    private SerialPort configureSerialPort(String portName) {
        SerialPort port = SerialPort.getCommPort(portName);
        port.setBaudRate(9600);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 2000);
        return port;
    }

    private void readSerialData() {
        System.out.println("Iniciando lectura de datos serial...");

        try (var inputStream = port.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while (true) {
                if (port.isOpen() && inputStream.available() > 0) {
                    bytesRead = inputStream.read(buffer);
                    if (bytesRead > 0) {
                        String data = new String(buffer, 0, bytesRead).trim();
                        System.out.println("Dato recibido del Arduino: " + data + " (hashCode: " + data.hashCode() + ")");

                        // Procesar datos válidos
                        processSerialData(data);
                    }
                }

                Thread.sleep(100); // Pequeña pausa
            }
        } catch (Exception e) {
            System.err.println("Error durante la comunicación serial: " + e.getMessage());
        } finally {
            closeSerialPort();
        }
    }

    private void processSerialData(String data) {
        // Verifica si el mensaje contiene un JSON de huella dactilar
        if (data.contains("{") && data.contains("}")) {
            String jsonPart = data.substring(data.indexOf("{"), data.indexOf("}") + 1);

            if (jsonPart.matches("\\{\\s*\"huellaDactilar\"\\s*:\\s*\"\\d+\"\\s*\\}")) {
                String fingerprintId = extractFingerprintId(jsonPart);
                if (fingerprintId != null && !processedFingerprints.contains(fingerprintId)) {
                    processedFingerprints.add(fingerprintId);
                    sendFingerprintToBackend(fingerprintId);
                } else if (data.contains("Huella identificada. ID:")) {
                    sendFingerprintToAsistencia(fingerprintId);
                } else {
                    System.err.println("Advertencia: Huella duplicada detectada. Ignorando.");
                }
            } else {
                System.err.println("Advertencia: JSON no válido detectado.");
            }
        } else {
            System.out.println("Mensaje recibido: " + data);
        }
    }

    private String extractFingerprintId(String data) {
        try {
            // Extraer ID de huella dactilar del JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            return node.get("huellaDactilar").asText();
        } catch (Exception e) {
            System.err.println("Error al procesar JSON: " + e.getMessage());
            return null;
        }
    }

    private void sendFingerprintToAsistencia(String huellaDactilar) {
        try {
            String asistenciaUrl = "http://localhost:8080/asistencia/entrada/" + huellaDactilar;
            System.out.println("Enviando ID de huella dactilar a la URL de asistencia: " + asistenciaUrl);
            restTemplate.postForObject(asistenciaUrl, null, Void.class);
            System.out.println("Envío a URL de asistencia exitoso.");
        } catch (HttpClientErrorException e) {
            System.err.println("Error del backend al enviar a asistencia (HTTP error): " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error al enviar la huella al URL de asistencia: " + e.getMessage());
        }
    }


    private void sendFingerprintToBackend(String huellaDactilar) {
        try {
            Empleado_Model empleado = new Empleado_Model();
            empleado.setHuellaDactilar(huellaDactilar);

            System.out.println("Enviando huella dactilar al backend: " + huellaDactilar);
            Response<Empleado_Model> response = restTemplate.postForObject(BACKEND_URL, empleado, Response.class);

            if (response != null) {
                System.out.println("Respuesta del backend: " + response.getMessage());
            } else {
                System.err.println("Error: No se recibió respuesta del backend.");
            }
        } catch (HttpClientErrorException.BadRequest e) {
            System.err.println("Error del backend (BadRequest): " + e.getResponseBodyAsString());
        } catch (HttpClientErrorException e) {
            System.err.println("Error del backend (HTTP error): " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error al enviar la huella al backend: " + e.getMessage());
        }
    }

    private void closeSerialPort() {
        if (port != null && port.isOpen()) {
            port.closePort();
            System.out.println("Puerto serial cerrado.");
        }
    }


}
