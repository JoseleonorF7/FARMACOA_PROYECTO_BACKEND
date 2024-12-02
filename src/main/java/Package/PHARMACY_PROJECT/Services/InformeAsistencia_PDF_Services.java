package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.InformeAsistencia_Model;
import Package.PHARMACY_PROJECT.Models.Reportes.ComparativaAsistencia_DTO;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteEmpleado_DTO;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteMensual_DTO;
import Package.PHARMACY_PROJECT.Models.Reportes.EstadisticasEmpleado_DTO;



import Package.PHARMACY_PROJECT.Repository.InformeAsistencia_Repository;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.awt.Color;  // Asegúrate de importar esta clase para Color

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Service
public class InformeAsistencia_PDF_Services {

    @Autowired
    private Empleado_Services empleadoServices;

    private final InformeAsistencia_Repository informeAsistenciaRepository;

    @Autowired
    public InformeAsistencia_PDF_Services(InformeAsistencia_Repository informeAsistenciaRepository) {
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
        table.addHeaderCell(new Cell().add(new Paragraph("Estado")));
        table.addHeaderCell(new Cell().add(new Paragraph("Diferencia Tiempo")));

        // Agregar las filas de la tabla
        for (Asistencia_Model asistencia : asistencias) {
            // Obtener horarios del turno del empleado
            LocalTime horaInicio1 = asistencia.getEmpleado().getHorario().getHoraInicio1();
            LocalTime horaFin1 = asistencia.getEmpleado().getHorario().getHoraFin1();
            LocalTime horaInicio2 = asistencia.getEmpleado().getHorario().getHoraInicio2();
            LocalTime horaFin2 = asistencia.getEmpleado().getHorario().getHoraFin2();

            // Calcular diferencias de tiempo para entrada y salida
            String diferenciaEntrada = asistencia.calcularDiferenciaTiempoEntrada(horaInicio1, horaInicio2);


            // Agregar celdas de la tabla
            table.addCell(new Cell().add(new Paragraph(asistencia.getFecha().toString())));
            table.addCell(new Cell().add(new Paragraph(asistencia.getTipoRegistro())));
            table.addCell(new Cell().add(new Paragraph(asistencia.getHoraEntrada() != null ? asistencia.getHoraEntrada().toString() : "No disponible")));
            table.addCell(new Cell().add(new Paragraph(asistencia.getEstado())));
        }

        return table;
    }



//--------------------------------------------------------------------------------------------

    public byte[] generateReporteMensualPdf(ReporteMensual_DTO reporteMensual) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Crear un escritor y documento PDF
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.setMargins(40, 40, 40, 40);  // Margen superior, izquierdo, inferior, derecho

        // Título del reporte con mes y año
        String mesNombre = obtenerNombreMes(reporteMensual.getMes());
        String fechaReporte = mesNombre + " " + reporteMensual.getAño();
        // Título del documento
        document.add(new Paragraph("Reporte de Asistencia Mensual- "+ fechaReporte)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));

        // Resumen General de Asistencias
        document.add(new Paragraph("Resumen General de Asistencia").setFontSize(14).setBold());
        document.add(new Paragraph("Mes: " + reporteMensual.getMes() + " - Año: " + reporteMensual.getAño()));

        // Resumen de Asistencia por Estado
        document.add(new Paragraph("Resumen de Asistencia por Estado").setFontSize(14).setBold());
        Table table3 = new Table(2);  // Dos columnas: Estado, Total
        table3.addCell("Estado");
        table3.addCell("Total");

        // Información de Asistencias Tardes y Puntuales
        table3.addCell("Tardes");
        table3.addCell(String.valueOf(reporteMensual.getAsistenciasPorEstado().get("tardes")));
        table3.addCell("Puntuales");
        table3.addCell(String.valueOf(reporteMensual.getAsistenciasPorEstado().get("puntuales")));

        document.add(table3);
        document.add(new Paragraph("\n"));


        // Tabla de Estadísticas por Empleado
        document.add(new Paragraph("Estadísticas por Empleado").setFontSize(14).setBold());
        Table table1 = new Table(4);  // Cuatro columnas: Nombre, Total Tarde, Llegadas Tarde, Total Puntual
        table1.addCell("Empleado");
        table1.addCell("Total Tarde");
        table1.addCell("Llegadas Tarde");
        table1.addCell("Total Puntual");

        // Verifica que 'estadisticasPorEmpleado' no sea nulo y tiene datos
        if (reporteMensual.getEstadisticasPorEmpleado() != null) {
            for (EstadisticasEmpleado_DTO empleado : reporteMensual.getEstadisticasPorEmpleado()) {
                table1.addCell(empleado.getEmpleadoNombre());
                table1.addCell(String.valueOf(empleado.getTotalTarde()));
                table1.addCell(String.valueOf(empleado.getLlegadasTarde()));
                table1.addCell(String.valueOf(empleado.getTotalPuntual()));
            }
        }

        document.add(table1);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("\n"));



        // Cerrar documento PDF
        document.close();

        // Retornar los bytes del PDF generado
        return baos.toByteArray();
    }

    public byte[] generateReporteEmpleadoPdf(ReporteEmpleado_DTO reporteEmpleado) throws IOException {
        Logger logger = LoggerFactory.getLogger(getClass());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            logger.info("Generando reporte mensual de asistencia para el empleado: {}", reporteEmpleado.getEmpleadoNombre());

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.setMargins(40, 40, 40, 40);

            String mesNombre = obtenerNombreMes(reporteEmpleado.getMes());
            String fechaReporte = mesNombre + " " + reporteEmpleado.getAño();

            document.add(new Paragraph("Reporte de Asistencia Mensual - " + reporteEmpleado.getEmpleadoNombre() + " - " + fechaReporte)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n\n"));

            document.add(new Paragraph("Resumen General de Asistencia").setFontSize(14).setBold());
            document.add(new Paragraph("Mes: " + reporteEmpleado.getMes() + " - Año: " + reporteEmpleado.getAño()));
            document.add(new Paragraph("Total de Asistencias: " + reporteEmpleado.getTotalAsistencias()));
            document.add(new Paragraph("Total de Tarde: " + (reporteEmpleado.getTotalTarde() != null ? reporteEmpleado.getTotalTarde() : "0 minutos y 0 segundos")));
            document.add(new Paragraph("Llegadas Tarde: " + reporteEmpleado.getLlegadasTarde()));
            document.add(new Paragraph("Llegadas Puntuales: " + reporteEmpleado.getLlegadasPuntuales()));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Detalles de Asistencias").setFontSize(14).setBold());
            Table table = new Table(5);
            table.addCell("Fecha");
            table.addCell("Hora Entrada");
            table.addCell("Tipo Registro");
            table.addCell("Diferencia de Tiempo");
            table.addCell("Estado");

            for (Asistencia_Model asistencia : reporteEmpleado.getAsistencias()) {
                if (asistencia == null) continue;

                table.addCell(asistencia.getFecha() != null ? asistencia.getFecha().toString() : "N/A");
                table.addCell(asistencia.getHoraEntrada() != null ? asistencia.getHoraEntrada().toString() : "N/A");
                table.addCell(asistencia.getTipoRegistro() != null ? asistencia.getTipoRegistro() : "N/A");
                table.addCell(asistencia.getDiferenciaTiempoEntrada() != null ? asistencia.getDiferenciaTiempoEntrada() : "N/A");
                table.addCell(asistencia.getDiferenciaTiempoEntrada() != null && asistencia.getDiferenciaTiempoEntrada().contains("Tarde") ? "Tarde" : "Puntual");
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.close();

            logger.info("Reporte mensual generado correctamente.");
        } catch (Exception e) {
            logger.error("Error al generar el reporte mensual de asistencia: ", e);
            throw new IOException("No se pudo generar el reporte mensual del PDF.", e);
        } finally {
            baos.close();
        }

        return baos.toByteArray();
    }

    public byte[] generateReporteEmpleadoFechaPdf(ReporteEmpleado_DTO reporteEmpleado) throws IOException {
        Logger logger = LoggerFactory.getLogger(getClass());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            logger.info("Generando reporte de asistencia para el empleado: {} en la fecha: {}",
                    reporteEmpleado.getEmpleadoNombre(), reporteEmpleado.getFecha());

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.setMargins(40, 40, 40, 40);

            String fechaReporte = convertirFechaANombreDeMes(reporteEmpleado.getFecha());

            document.add(new Paragraph("Reporte de Asistencia - " + reporteEmpleado.getEmpleadoNombre() + " - " + fechaReporte)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\nResumen de Asistencia").setFontSize(14).setBold());
            document.add(new Paragraph("Fecha: " + reporteEmpleado.getFecha()));
            document.add(new Paragraph("\n"));

            Table table = new Table(5);
            table.addCell("Fecha");
            table.addCell("Hora Entrada");
            table.addCell("Tipo Registro");
            table.addCell("Diferencia de Tiempo");
            table.addCell("Estado");

            for (Asistencia_Model asistencia : reporteEmpleado.getAsistencias()) {
                if (asistencia == null) continue;

                table.addCell(asistencia.getFecha() != null ? asistencia.getFecha().toString() : "N/A");
                table.addCell(asistencia.getHoraEntrada() != null ? asistencia.getHoraEntrada().toString() : "N/A");
                table.addCell(asistencia.getTipoRegistro() != null ? asistencia.getTipoRegistro() : "N/A");
                table.addCell(asistencia.getDiferenciaTiempoEntrada() != null ? asistencia.getDiferenciaTiempoEntrada() : "N/A");
                table.addCell(asistencia.getDiferenciaTiempoEntrada() != null && asistencia.getDiferenciaTiempoEntrada().contains("Tarde") ? "Tarde" : "Puntual");
            }

            document.add(table);
            document.close();

            logger.info("Reporte por fecha generado correctamente.");
        } catch (Exception e) {
            logger.error("Error al generar el reporte por fecha: ", e);
            throw new IOException("No se pudo generar el reporte por fecha del PDF.", e);
        } finally {
            baos.close();
        }

        return baos.toByteArray();
    }

    public String convertirFechaANombreDeMes(LocalDate fecha) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return fecha.format(formatter);  // Devuelve el mes y el año
    }

    public byte[] generarReporteComparativoPdf(int cantidadTardanzas, int cantidadPuntualidades,
                                               List<ComparativaAsistencia_DTO> datos,
                                               Integer mes, Integer anio) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = null;
        PdfDocument pdf = null;
        Document document = null;

        try {
            // Validación de los datos
            if (datos == null || datos.isEmpty()) {
                throw new IllegalArgumentException("No se encontraron registros de asistencia para el mes y año proporcionados.");
            }

            writer = new PdfWriter(baos);
             pdf = new PdfDocument(writer);
            document = new Document(pdf);
            document.setMargins(40, 40, 40, 40);

            // Título
            String mesNombre = obtenerNombreMes(mes);
            String fechaReporte = mesNombre + " " + anio;
            document.add(new Paragraph("Reporte Comparativo de Asistencia - " + fechaReporte)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // Totales generales
            document.add(new Paragraph("Totales Generales:")
                    .setFontSize(14)
                    .setBold());
            document.add(new Paragraph("Tardanzas totales: " + cantidadTardanzas)
                    .setFontSize(12));
            document.add(new Paragraph("Puntualidades totales: " + cantidadPuntualidades)
                    .setFontSize(12));
            document.add(new Paragraph("\n"));


            // Gráfica de pastel
            if (cantidadTardanzas > 0 || cantidadPuntualidades > 0) {
                byte[] graficaBytes = generarGraficaPastel(cantidadTardanzas, cantidadPuntualidades);
                ImageData imageData = ImageDataFactory.create(graficaBytes);
                Image image = new Image(imageData);
                image.setWidth(400);
                image.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(image);
            }

            // Tabla con detalles por empleado
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Detalle por Empleado:")
                    .setFontSize(14)
                    .setBold());
            Table table = new Table(new float[]{3, 4, 2, 2});
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(new Cell().add(new Paragraph("ID Empleado").setBold()).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nombre Empleado").setBold()).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Puntualidades").setBold()).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Tardanzas").setBold()).setTextAlignment(TextAlignment.CENTER));

            for (ComparativaAsistencia_DTO empleado : datos) {
                table.addCell(String.valueOf(empleado.getEmpleadoId()));
                table.addCell(empleado.getEmpleadoNombre());
                table.addCell(String.valueOf(empleado.getPuntualidades()));
                table.addCell(String.valueOf(empleado.getTardanzas()));
            }
            document.add(table);
            document.close();
        } catch (IllegalArgumentException e) {
            // Captura de errores en los datos, como cuando no se encuentran registros
            throw new IllegalArgumentException("Error al generar el reporte: " + e.getMessage(), e);

        } catch (Exception e) {
            // Captura de cualquier otro tipo de error inesperado
            throw new RuntimeException("Error inesperado al generar el reporte: " + e.getMessage(), e);
        } finally {
            // Cierre de recursos en el bloque finally para asegurar que siempre se liberen
            try {
                if (document != null) {
                    document.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                // Manejo de errores durante el cierre de recursos
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
        return baos.toByteArray();
    }

    private String obtenerNombreMes(int mes) {
        try {
            String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            if (mes < 1 || mes > 12) {
                throw new IllegalArgumentException("El número del mes debe estar entre 1 y 12.");
            }
            return meses[mes - 1];
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el nombre del mes: " + e.getMessage(), e);
        }
    }

    private byte[] generarGraficaPastel(int cantidadTardanzas, int cantidadPuntualidades) throws IOException {
        try {
            DefaultPieDataset dataset = new DefaultPieDataset();
            dataset.setValue("Tardanzas", cantidadTardanzas);
            dataset.setValue("Puntualidades", cantidadPuntualidades);
            JFreeChart chart = ChartFactory.createPieChart("Comparativa de Asistencia", dataset, true, true, false);
            ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);
            return chartStream.toByteArray();
        } catch (Exception e) {
            throw new IOException("Error al generar la gráfica de pastel: " + e.getMessage(), e);
        }
    }

    private byte[] generarGraficaComparativaTardanzas(List<ComparativaAsistencia_DTO> datos) throws IOException {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (ComparativaAsistencia_DTO dato : datos) {
                dataset.addValue(dato.getTardanzas(), "Tardanzas", dato.getEmpleadoNombre());
            }
            JFreeChart chart = ChartFactory.createBarChart("Tardanzas por Empleado", "Empleado",
                    "Cantidad de Tardanzas", dataset, PlotOrientation.HORIZONTAL, true, true, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 800, 600);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IOException("Error al generar la gráfica comparativa de tardanzas: " + e.getMessage(), e);
        }
    }

    private byte[] generarGraficaComparativaPuntualidades(List<ComparativaAsistencia_DTO> datos) throws IOException {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (ComparativaAsistencia_DTO dato : datos) {
                dataset.addValue(dato.getPuntualidades(), "Puntualidades", dato.getEmpleadoNombre());
            }
            JFreeChart chart = ChartFactory.createBarChart("Puntualidades por Empleado", "Empleado",
                    "Cantidad de Puntualidades", dataset, PlotOrientation.HORIZONTAL, true, true, false);
            CategoryPlot plot = chart.getCategoryPlot();
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, java.awt.Color.BLUE);
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            plot.setDomainGridlinePaint(java.awt.Color.GRAY);
            plot.setRangeGridlinePaint(java.awt.Color.GRAY);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 800, 600);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IOException("Error al generar la gráfica comparativa de puntualidades: " + e.getMessage(), e);
        }
    }

    //----------------------------------------------------------------------------------------------


}