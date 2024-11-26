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
    private static final String BACKEND_URL_HUELLA = "http://localhost:8080/empleado/registrarHuella";
    private static final String BACKEND_URL_ASISTENCIA = "http://localhost:8080/asistencia/entrada/";
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
                        System.out.println("Dato recibido del Arduino: " + data);

                        // Filtrar mensajes no deseados antes de procesar
                        if (data.equalsIgnoreCase("Imagen capturada correctamente.") ||
                                data.equalsIgnoreCase("Huella identificada exitosamente") ||
                                data.contains("Intentando identificar huella")) {
                            System.out.println("Mensaje intermedio ignorado: " + data);
                            continue;
                        }

                        // Procesar datos válidos
                        processBufferedData(data);
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

    private void processBufferedData(String data) {
        System.out.println("Entrando a processBufferedData con el dato: " + data);

        // Normalización: eliminar puntos y espacios innecesarios
        data = data.replaceAll("\\.+", "").trim();
        System.out.println("Dato normalizado: " + data);

        // Ignorar mensajes específicos que no se procesan
        if (data.equalsIgnoreCase("Imagen capturada correctamente") ||
                data.equalsIgnoreCase("Huella identificada exitosamente") ||
                data.contains("Intentando identificar huella")) {
            System.out.println("Mensaje intermedio ignorado: " + data);
            return; // Salir del método sin procesar
        }

        if (data.startsWith("Huella registrada exitosamente ID:")) {
            System.out.println("Detectado inicio de registro de nueva huella.");
            handleRegistroHuella(data);
        } else if (data.startsWith("Huella identificada ID:")) {
            System.out.println("Detectada identificación de huella.");
            handleHuellaIdentificada(data);
        } else {
            System.out.println("Mensaje no reconocido: " + data);
        }
    }

    private void handleRegistroHuella(String data) {
        System.out.println("Entrando a handleRegistroHuella con el dato: " + data);
        try {
            String fingerprintId = extractIdFromRegistroMessage(data);
            System.out.println("ID extraído del mensaje de registro: " + fingerprintId);
            if (fingerprintId != null) {
                System.out.println("Enviando ID al backend para registro.");
                sendFingerprintToBackend(fingerprintId); // Enviar al endpoint de registro
            } else {
                System.out.println("Advertencia: ID de registro no válido. Ignorando.");
            }
        } catch (Exception e) {
            System.err.println("Error al manejar el registro de huella: " + e.getMessage());
        }
    }

    private String extractIdFromRegistroMessage(String data) {
        System.out.println("Entrando a extractIdFromRegistroMessage con el dato: " + data);
        try {
            if (data.startsWith("Huella registrada exitosamente ID:")) {
                String id = data.split(":")[1].trim();
                System.out.println("ID extraído exitosamente: " + id);
                return id; // Extraer el ID después de '#'
            }
            System.out.println("El dato no contiene un ID válido para registro.");
            return null;
        } catch (Exception e) {
            System.err.println("Error al extraer ID de registro del mensaje: " + e.getMessage());
            return null;
        }
    }

    private String extractIdFromMessage(String data) {
        System.out.println("Entrando a extractIdFromMessage con el dato: " + data);
        try {
            if (data.startsWith("Huella identificada ID:")) {
                String id = data.split(":")[1].trim();
                System.out.println("ID extraído exitosamente: " + id);
                return id; // Extraer el ID después de los dos puntos
            }
            System.out.println("El dato no contiene un ID válido para identificación.");
            return null;
        } catch (Exception e) {
            System.err.println("Error al extraer ID de huella del mensaje: " + e.getMessage());
            return null;
        }
    }

    private void handleHuellaIdentificada(String data) {
        System.out.println("Entrando a handleHuellaIdentificada con el dato: " + data);
        try {
            String fingerprintId = extractIdFromMessage(data);
            System.out.println("ID extraído del mensaje de identificación: " + fingerprintId);
            if (fingerprintId != null) {
                System.out.println("Enviando ID al backend para asistencia.");
                sendFingerprintToAsistencia(fingerprintId);
            } else {
                System.out.println("Advertencia: ID duplicado o no válido detectado. Ignorando.");
            }
        } catch (Exception e) {
            System.err.println("Error al manejar huella identificada: " + e.getMessage());
        }
    }

    private void sendFingerprintToAsistencia(String id) {
        try {
            String asistenciaUrl = BACKEND_URL_ASISTENCIA + id;
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
            Response<Empleado_Model> response = restTemplate.postForObject(BACKEND_URL_HUELLA, empleado, Response.class);

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




