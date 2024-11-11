package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.InformeAsistencia_Model;
import Package.PHARMACY_PROJECT.Repository.InformeAsistencia_Repository;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InformeAsistencia_Services {

    private final InformeAsistencia_Repository informeAsistenciaRepository;

    @Autowired
    public InformeAsistencia_Services(InformeAsistencia_Repository informeAsistenciaRepository) {
        this.informeAsistenciaRepository = informeAsistenciaRepository;
    }

    public InformeAsistencia_Model save(InformeAsistencia_Model informe) {
        return informeAsistenciaRepository.save(informe);
    }

    public List<InformeAsistencia_Model> findAll() {
        return informeAsistenciaRepository.findAll();
    }

    public void deleteById(Long id) {
        informeAsistenciaRepository.deleteById(id);
    }

    public byte[] generateEmployeeAttendancePdf(Empleado_Model empleado, List<Asistencia_Model> asistencias, Integer mes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        // Título del documento
        document.add(new Paragraph("Informe de Asistencia de Empleado")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        // Información del empleado
        addEmployeeInfo(document, empleado);

        // Filtrar y agrupar asistencias por mes
        List<Asistencia_Model> asistenciasFiltradas = filterAsistenciasByMonth(asistencias, mes);
        Map<Integer, List<Asistencia_Model>> asistenciasPorMes = groupAsistenciasByMonth(asistenciasFiltradas);

        // Crear el reporte
        generateAttendanceReport(document, asistenciasPorMes);

        document.close();
        return baos.toByteArray();
    }

    // Método para agregar la información del empleado
    private void addEmployeeInfo(Document document, Empleado_Model empleado) {
        document.add(new Paragraph("Información del Empleado:")
                .setFontSize(16)
                .setBold());
        document.add(new Paragraph("Nombre: " + empleado.getNombre()));
        document.add(new Paragraph("ID del Empleado: " + empleado.getIdentificacion()));
        document.add(new Paragraph("Fecha de Contratación: " + (empleado.getFechaContratacion() != null ? empleado.getFechaContratacion().toString() : "No disponible")));
        document.add(new Paragraph("Rol: " + (empleado.getRol() != null ? empleado.getRol() : "No disponible")));
        document.add(new Paragraph("Estado Activo: " + (empleado.getActivo() != null && empleado.getActivo() ? "Sí" : "No")));
        document.add(new Paragraph("\n"));
    }

    // Método para filtrar asistencias por mes
    private List<Asistencia_Model> filterAsistenciasByMonth(List<Asistencia_Model> asistencias, Integer mes) {
        if (mes != null) {
            return asistencias.stream()
                    .filter(asistencia -> asistencia.getFecha().getMonthValue() == mes)
                    .collect(Collectors.toList());
        }
        return asistencias; // Si no se especifica mes, devuelve todas las asistencias
    }

    // Método para agrupar las asistencias por mes
    private Map<Integer, List<Asistencia_Model>> groupAsistenciasByMonth(List<Asistencia_Model> asistencias) {
        Map<Integer, List<Asistencia_Model>> asistenciasPorMes = new HashMap<>();
        for (Asistencia_Model asistencia : asistencias) {
            int mes = asistencia.getFecha().getMonthValue();
            asistenciasPorMes.computeIfAbsent(mes, k -> new ArrayList<>()).add(asistencia);
        }
        return asistenciasPorMes;
    }

    // Método para generar el reporte de asistencias por mes
    private void generateAttendanceReport(Document document, Map<Integer, List<Asistencia_Model>> asistenciasPorMes) {
        for (Map.Entry<Integer, List<Asistencia_Model>> entry : asistenciasPorMes.entrySet()) {
            int mes = entry.getKey();
            List<Asistencia_Model> asistenciasDelMes = entry.getValue();

            // Obtener el nombre del mes
            String mesNombre = obtenerNombreMes(mes);

            // Crear el encabezado para el mes
            document.add(new Paragraph("Mes: " + mesNombre)
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // Crear la tabla de asistencia para el mes
            Table table = createAttendanceTable(asistenciasDelMes);
            document.add(table);
            document.add(new Paragraph("\n"));
        }
    }

    // Método para crear la tabla de asistencia
    private Table createAttendanceTable(List<Asistencia_Model> asistencias) {
        Table table = new Table(new float[]{2, 2, 3, 3, 3, 3});
        table.addHeaderCell(new Cell().add(new Paragraph("Fecha")));
        table.addHeaderCell(new Cell().add(new Paragraph("Tipo de Registro")));
        table.addHeaderCell(new Cell().add(new Paragraph("Hora de Entrada")));
        table.addHeaderCell(new Cell().add(new Paragraph("Hora de Salida")));
        table.addHeaderCell(new Cell().add(new Paragraph("Estado")));
        table.addHeaderCell(new Cell().add(new Paragraph("Diferencia Tiempo")));

        // Agregar las filas de la tabla
        for (Asistencia_Model asistencia : asistencias) {
            table.addCell(new Cell().add(new Paragraph(asistencia.getFecha().toString())));
            table.addCell(new Cell().add(new Paragraph(asistencia.getTipoRegistro())));
            table.addCell(new Cell().add(new Paragraph(asistencia.getHoraEntrada() != null ? asistencia.getHoraEntrada().toString() : "No disponible")));
            table.addCell(new Cell().add(new Paragraph(asistencia.getHoraSalida() != null ? asistencia.getHoraSalida().toString() : "No disponible")));
            table.addCell(new Cell().add(new Paragraph(asistencia.getEstado())));
            table.addCell(new Cell().add(new Paragraph(
                    asistencia.calcularDiferenciaTiempoEntrada() + " / " +
                            (asistencia.getHoraSalida() != null ? asistencia.calcularDiferenciaTiempoSalida() : "No disponible")
            )));
        }
        return table;
    }

    public byte[] generateAllEmployeesAttendancePdf(List<Asistencia_Model> asistencias) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        // Cargar la imagen
        Image logo = new Image(ImageDataFactory.create("src/main/resources/images/DROG.png"));

        // Ajustar el tamaño de la imagen
        logo.scaleToFit(100, 100); // Cambia el tamaño de la imagen si es necesario
        logo.setFixedPosition(pdf.getDefaultPageSize().getWidth() - 110, pdf.getDefaultPageSize().getHeight() - 110); // Posicionarla en la esquina superior derecha

        // Agregar la imagen en la esquina superior derecha de cada página
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, event -> {
            document.add(logo); // Agregar la imagen al documento en cada página
        });

        // Título del documento
        document.add(new Paragraph("Informe de Asistencia de Todos los Empleados")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        // Agrupar asistencias por empleado
        Map<String, List<Asistencia_Model>> asistenciasPorEmpleado = asistencias.stream()
                .collect(Collectors.groupingBy(asistencia -> asistencia.getEmpleado().getIdentificacion()));

        for (Map.Entry<String, List<Asistencia_Model>> entry : asistenciasPorEmpleado.entrySet()) {
            String empleadoId = entry.getKey();
            List<Asistencia_Model> asistenciasDelEmpleado = entry.getValue();

            Empleado_Model empleado = asistenciasDelEmpleado.get(0).getEmpleado();  // Suponiendo que el empleado es el mismo para todas las asistencias

            // Información del empleado
            document.add(new Paragraph("Empleado: " + empleado.getNombre())
                    .setFontSize(16)
                    .setBold());
            document.add(new Paragraph("ID del Empleado: " + empleado.getIdentificacion()));
            document.add(new Paragraph("Rol: " + empleado.getRol()));
            document.add(new Paragraph("Estado: " + (empleado.getActivo() ? "Activo" : "Inactivo")));
            document.add(new Paragraph("\n"));

            // Crear la tabla para las asistencias del empleado
            Table table = new Table(new float[]{2, 3, 3, 3, 3, 3});
            table.addHeaderCell(new Cell().add(new Paragraph("Fecha")));
            table.addHeaderCell(new Cell().add(new Paragraph("Tipo de Registro")));
            table.addHeaderCell(new Cell().add(new Paragraph("Hora de Entrada")));
            table.addHeaderCell(new Cell().add(new Paragraph("Hora de Salida")));
            table.addHeaderCell(new Cell().add(new Paragraph("Estado")));
            table.addHeaderCell(new Cell().add(new Paragraph("Diferencia de Tiempo")));

            // Agregar las filas a la tabla
            for (Asistencia_Model asistencia : asistenciasDelEmpleado) {
                table.addCell(new Cell().add(new Paragraph(asistencia.getFecha().toString())));
                table.addCell(new Cell().add(new Paragraph(asistencia.getTipoRegistro())));
                table.addCell(new Cell().add(new Paragraph(asistencia.getHoraEntrada() != null ? asistencia.getHoraEntrada().toString() : "No disponible")));
                table.addCell(new Cell().add(new Paragraph(asistencia.getHoraSalida() != null ? asistencia.getHoraSalida().toString() : "No disponible")));
                table.addCell(new Cell().add(new Paragraph(asistencia.getEstado())));
                table.addCell(new Cell().add(new Paragraph(
                        asistencia.calcularDiferenciaTiempoEntrada() + " / " +
                                (asistencia.getHoraSalida() != null ? asistencia.calcularDiferenciaTiempoSalida() : "No disponible")
                )));
            }

            // Agregar la tabla al documento
            document.add(table);
            document.add(new Paragraph("\n"));
        }

        document.close();
        return baos.toByteArray();
    }

    // Método para obtener el nombre del mes
    private String obtenerNombreMes(int mes) {
        switch (mes) {
            case 1: return "Enero";
            case 2: return "Febrero";
            case 3: return "Marzo";
            case 4: return "Abril";
            case 5: return "Mayo";
            case 6: return "Junio";
            case 7: return "Julio";
            case 8: return "Agosto";
            case 9: return "Septiembre";
            case 10: return "Octubre";
            case 11: return "Noviembre";
            case 12: return "Diciembre";
            default: return "Mes desconocido";
        }
}}