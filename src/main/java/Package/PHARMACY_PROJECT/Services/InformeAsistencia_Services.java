package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.InformeAsistencia_Model;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteConsolidado;
import Package.PHARMACY_PROJECT.Models.Reportes.EmpleadoReporte;
import Package.PHARMACY_PROJECT.Models.Reportes.EmpleadoRanking;
import Package.PHARMACY_PROJECT.Models.Reportes.PromedioMensual;



import Package.PHARMACY_PROJECT.Repository.InformeAsistencia_Repository;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfPTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class InformeAsistencia_Services {

    @Autowired
    private Empleado_Services empleadoServices;

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


//--------------------------------------------------------------------------------------------


    public byte[] generateAllEmployeesAttendancePdf(ReporteConsolidado reporteConsolidado) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        // Título del documento
        document.add(new Paragraph("Reporte de Asistencia de Empleados")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        // 1. Resumen general de asistencia de todos los empleados
        document.add(new Paragraph("Resumen General de Asistencia de Empleados").setFontSize(14).setBold());
        document.add(createAttendanceSummaryTable(reporteConsolidado.getReportePorEmpleado()));

        // 2. Ranking de empleados por tardanza en entradas
        document.add(new Paragraph("Ranking de Empleados por Tardanza en Entradas").setFontSize(14).setBold());
        document.add(createRankingTable(reporteConsolidado.getRankingTardanzas(), "Tardanza"));

        // 3. Ranking de empleados por salidas tempranas
        document.add(new Paragraph("Ranking de Empleados por Salidas Tempranas").setFontSize(14).setBold());
        document.add(createRankingTable(reporteConsolidado.getRankingSalidasTempranas(), "Salida Temprana"));

        // 4. Promedios mensuales de asistencia
        document.add(new Paragraph("Promedios Mensuales de Asistencia").setFontSize(14).setBold());
        document.add(createMonthlyAveragesTable(reporteConsolidado.getPromediosMensuales()));

        // 5. Sugerencias de mejora
        document.add(new Paragraph("Sugerencias de Mejora").setFontSize(14).setBold());
        document.add(new Paragraph("Los empleados con mayores tiempos de tardanza y salidas tempranas pueden beneficiarse de políticas de revisión de asistencia o incentivos para mejorar su puntualidad."));

        document.close();
        return baos.toByteArray();
    }

    // Método para crear la tabla de resumen de asistencia
    private Table createAttendanceSummaryTable(Map<String, EmpleadoReporte> reportePorEmpleado) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 4, 2, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(new Cell().add(new Paragraph("ID Empleado").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Nombre Empleado").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Total Minutos Entrada Tarde").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Total Minutos Salida Temprana").setBold()));

        for (EmpleadoReporte empleado : reportePorEmpleado.values()) {
            String nombreEmpleado = empleadoServices.getNombreByIdentificacion(empleado.getIdentificacionEmpleado())
                    .orElse("Nombre no encontrado");

            table.addCell(new Cell().add(new Paragraph(empleado.getIdentificacionEmpleado())));
            table.addCell(new Cell().add(new Paragraph(nombreEmpleado)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(empleado.getTotalMinutosTardeEntrada()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(empleado.getTotalMinutosTempranoSalida()))));
        }

        return table;
    }

    // Método para crear la tabla de ranking
    private Table createRankingTable(List<EmpleadoRanking> rankingList, String tipo) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{3,3,3}));
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell("Posición");
        table.addHeaderCell("Nombre Empleado");
        table.addHeaderCell("Total Minutos " + tipo);

        int position = 1;
        for (EmpleadoRanking empleado : rankingList) {
            String nombreEmpleado = empleadoServices.getNombreByIdentificacion(empleado.getIdentificacionEmpleado())
                    .orElse("Nombre no encontrado");

            table.addCell(String.valueOf(position++));
            table.addCell(new Cell().add(new Paragraph(nombreEmpleado)));
            table.addCell(tipo.equals("Tardanza")
                    ? String.valueOf(empleado.getTotalMinutosTardeEntrada())
                    : String.valueOf(empleado.getTotalMinutosTempranoSalida()));
        }

        return table;
    }

    // Método para crear la tabla de promedios mensuales
// Método para crear la tabla de promedios mensuales
    private Table createMonthlyAveragesTable(Map<YearMonth, PromedioMensual> promediosMensuales) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 3, 3}));
        table.setWidth(UnitValue.createPercentValue(100)); // Ajustar el ancho al 100%

        // Encabezados
        table.addHeaderCell(new Cell().add(new Paragraph("Mes").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Promedio Minutos Entrada Tarde").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Promedio Minutos Salida Temprana").setBold()));

        // Iterar sobre los datos
        for (Map.Entry<YearMonth, PromedioMensual> entry : promediosMensuales.entrySet()) {
            String mes = entry.getKey().toString(); // Convertir YearMonth a String
            PromedioMensual promedio = entry.getValue();

            double promedioTarde = promedio.calcularPromedioTardanzaEntrada();
            double promedioSalidaTemprana = promedio.calcularPromedioSalidaTemprana();

            table.addCell(new Cell().add(new Paragraph(mes)));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", promedioTarde))));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", promedioSalidaTemprana))));
        }

        return table;
    }

//----------------------------------------------------------------------------------------------


}