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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Crear un escritor y documento PDF
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.setMargins(40, 40, 40, 40);  // Margen superior, izquierdo, inferior, derecho

        // Título del reporte con mes y año
        String mesNombre = obtenerNombreMes(reporteEmpleado.getMes());
        String fechaReporte = mesNombre + " " + reporteEmpleado.getAño();
        // Título del documento
        document.add(new Paragraph("Reporte de Asistencia Mensual - " + reporteEmpleado.getEmpleadoNombre()+"- "+ fechaReporte)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));

        // Resumen General de Asistencias
        document.add(new Paragraph("Resumen General de Asistencia").setFontSize(14).setBold());
        document.add(new Paragraph("Mes: " + reporteEmpleado.getMes() + " - Año: " + reporteEmpleado.getAño()));
        document.add(new Paragraph("Total de Asistencias: " + reporteEmpleado.getTotalAsistencias()));
        document.add(new Paragraph("Total de Tarde: " + (reporteEmpleado.getTotalTarde() != null ? reporteEmpleado.getTotalTarde() : "0 minutos y 0 segundos")));
        document.add(new Paragraph("Llegadas Tarde: " + reporteEmpleado.getLlegadasTarde()));
        document.add(new Paragraph("Llegadas Puntuales: " + reporteEmpleado.getLlegadasPuntuales()));
        document.add(new Paragraph("\n"));

        // Tabla con los Detalles de las Asistencias
        document.add(new Paragraph("Detalles de Asistencias").setFontSize(14).setBold());
        Table table = new Table(5);  // Cinco columnas: Fecha, Hora Entrada, Tipo Registro, Diferencia Tiempo, Estado
        table.addCell("Fecha");
        table.addCell("Hora Entrada");
        table.addCell("Tipo Registro");
        table.addCell("Diferencia de Tiempo");
        table.addCell("Estado");

        // Añadir los detalles de cada asistencia
        for (Asistencia_Model asistencia : reporteEmpleado.getAsistencias()) {
            // Verificar si la fecha y hora están disponibles y formatear correctamente
            String fecha = asistencia.getFecha() != null ? asistencia.getFecha().toString() : "N/A";
            String horaEntrada = asistencia.getHoraEntrada() != null ? String.valueOf(asistencia.getHoraEntrada()) : "N/A";
            String tipoRegistro = asistencia.getTipoRegistro() != null ? asistencia.getTipoRegistro() : "N/A";
            String diferenciaTiempo = asistencia.getDiferenciaTiempoEntrada() != null ? asistencia.getDiferenciaTiempoEntrada() : "N/A";
            String estado = diferenciaTiempo.contains("Tarde") ? "Tarde" : "Puntual";

            // Agregar las celdas a la tabla
            table.addCell(fecha);
            table.addCell(horaEntrada);
            table.addCell(tipoRegistro);
            table.addCell(diferenciaTiempo);
            table.addCell(estado);
        }

        document.add(table);
        document.add(new Paragraph("\n"));

        // Cerrar documento PDF
        document.close();

        // Retornar los bytes del PDF generado
        return baos.toByteArray();
    }

    public byte[] generateReporteEmpleadoFechaPdf(ReporteEmpleado_DTO reporteEmpleado) throws IOException {
        Logger logger = LoggerFactory.getLogger(getClass());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            logger.info("Creando documento PDF...");
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Agregar contenido al PDF
            document.add(new Paragraph("Hola Mundo"));

            String fechaReporte = convertirFechaANombreDeMes(reporteEmpleado.getFecha());
            document.add(new Paragraph("Reporte de Asistencia - " + reporteEmpleado.getEmpleadoNombre() + "- " + fechaReporte)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\nResumen de Asistencia").setFontSize(14).setBold());
            document.add(new Paragraph("Fecha: " + reporteEmpleado.getFecha()));

            // Tabla
            Table table = new Table(5); // Cinco columnas
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

            logger.info("PDF generado correctamente.");
        } catch (Exception e) {
            logger.error("Error al generar el PDF: ", e);
            throw new IOException("No se pudo generar el PDF.", e);
        } finally {
            baos.close();
        }

        return baos.toByteArray();
    }

    public byte[] generarReporteComparativoPdf(int cantidadTardanzas, int cantidadPuntualidades, List<ComparativaAsistencia_DTO> datos, Integer mes, Integer anio) throws IOException {
        // Validación de si no hay empleados
        if (datos == null || datos.isEmpty()) {
            // Lanza una excepción o maneja el caso de error aquí
            throw new IllegalArgumentException("No se encontraron registros de asistencia para el mes y año proporcionados.");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Agregar márgenes al documento
        document.setMargins(40, 40, 40, 40);  // Margen superior, izquierdo, inferior, derecho

        // Título del reporte con mes y año
        String mesNombre = obtenerNombreMes(mes);
        String fechaReporte = mesNombre + " " + anio;
        document.add(new Paragraph("Reporte Comparativo de Asistencia - " + fechaReporte)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
        ); // Usando conversión

        document.add(new Paragraph("\n"));

        // Mostrar los totales con estilo
        document.add(new Paragraph("Totales Generales:")
                .setFontSize(14)
                .setBold()
        ); // Usando conversión
        document.add(new Paragraph("Tardanzas totales: " + cantidadTardanzas)
                .setFontSize(12)
        );
        document.add(new Paragraph("Puntualidades totales: " + cantidadPuntualidades)
                .setFontSize(12)
        );

        document.add(new Paragraph("\n"));

        // Insertar gráfica de pastel (tardanzas vs puntualidades)
        if (cantidadTardanzas > 0 || cantidadPuntualidades > 0) {
            byte[] graficaBytes = generarGraficaPastel(cantidadTardanzas, cantidadPuntualidades); // Método para generar la gráfica
            ImageData imageData = ImageDataFactory.create(graficaBytes);
            Image image = new Image(imageData);
            image.setWidth(400);  // Ajustar el tamaño de la imagen
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(image);
        }

        document.add(new Paragraph("\n"));

        // Insertar gráfica para Tardanzas
        byte[] graficaTardanzasBytes = generarGraficaComparativaTardanzas(datos); // Generar gráfica para tardanzas
        if (graficaTardanzasBytes != null && graficaTardanzasBytes.length > 0) {
            ImageData imageData = ImageDataFactory.create(graficaTardanzasBytes);
            Image image = new Image(imageData);
            image.setWidth(400);  // Ajustar el tamaño de la imagen
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(image);
        }

        document.add(new Paragraph("\n"));

        // Insertar gráfica para Puntualidades
        byte[] graficaPuntualidadesBytes = generarGraficaComparativaPuntualidades(datos); // Generar gráfica para puntualidades
        if (graficaPuntualidadesBytes != null && graficaPuntualidadesBytes.length > 0) {
            ImageData imageData = ImageDataFactory.create(graficaPuntualidadesBytes);
            Image image = new Image(imageData);
            image.setWidth(400);  // Ajustar el tamaño de la imagen
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(image);
        }

        document.add(new Paragraph("\n"));


        // Tabla con los detalles por empleado
        document.add(new Paragraph("Detalle por Empleado:")
                .setFontSize(14)
                .setBold()
        ); // Usando conversión

        // Crear una tabla con las proporciones deseadas
        Table table = new Table(new float[]{3, 4, 2, 2}); // Columnas: ID, Nombre, Puntualidades, Tardanzas
        table.setWidth(UnitValue.createPercentValue(100)); // Ancho de la tabla al 100% del documento
        table.setBorder(Border.NO_BORDER);  // Eliminar el borde exterior para un look limpio

        // Encabezados de la tabla
        table.addHeaderCell(new Cell().add(new Paragraph("ID Empleado").setBold()).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Nombre Empleado").setBold()).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Puntualidades").setBold()).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Tardanzas").setBold()).setTextAlignment(TextAlignment.CENTER));

        // Rellenar la tabla con los datos de empleados
        for (ComparativaAsistencia_DTO empleado : datos) {
            table.addCell(String.valueOf(empleado.getEmpleadoId()));
            table.addCell(empleado.getEmpleadoNombre());
            table.addCell(String.valueOf(empleado.getPuntualidades()));
            table.addCell(String.valueOf(empleado.getTardanzas()));
        }

        document.add(table);

        // Cerrar el documento
        document.close();

        return baos.toByteArray();
    }


    public String convertirFechaANombreDeMes(LocalDate fecha) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return fecha.format(formatter);  // Devuelve el mes y el año
    }

    private String obtenerNombreMes(int mes) {
        String[] meses = {
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El número del mes debe estar entre 1 y 12.");
        }
        return meses[mes - 1];  // Ajusta porque el índice de los meses comienza en 0
    }

    private byte[] generarGraficaPastel(int cantidadTardanzas, int cantidadPuntualidades) throws IOException {
        // Generar una gráfica de pastel para las tardanzas vs puntualidades
        // Utiliza una librería como JFreeChart, iText7 para generar el gráfico en bytes
        // Este método solo es un placeholder para que sepas dónde generar el gráfico
        // Asegúrate de usar la librería adecuada para generar la gráfica de pastel y devolverla en formato byte[]

        // Ejemplo de cómo usar JFreeChart para crear un gráfico de pastel (modificar según tu caso)
        // Puedes instalar y usar la librería JFreeChart para esta tarea

        // Ejemplo simplificado:
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Tardanzas", cantidadTardanzas);
        dataset.setValue("Puntualidades", cantidadPuntualidades);

        JFreeChart chart = ChartFactory.createPieChart(
                "Comparativa de Asistencia", dataset, true, true, false);

        // Convertir el gráfico a bytes
        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);  // Ajusta el tamaño según necesidad
        return chartStream.toByteArray();
    }


    private byte[] generarGraficaComparativaTardanzas(List<ComparativaAsistencia_DTO> datos) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (ComparativaAsistencia_DTO dato : datos) {
            dataset.addValue(dato.getTardanzas(), "Tardanzas", dato.getEmpleadoNombre());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Tardanzas por Empleado",
                "Empleado",
                "Cantidad de Tardanzas",
                dataset,
                PlotOrientation.HORIZONTAL,  // Cambiar a orientación horizontal
                true,
                true,
                false
        );


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 800, 600);
        return baos.toByteArray();
    }

    // Método para generar la gráfica de Puntualidades con barras horizontales
    private byte[] generarGraficaComparativaPuntualidades(List<ComparativaAsistencia_DTO> datos) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (ComparativaAsistencia_DTO dato : datos) {
            dataset.addValue(dato.getPuntualidades(), "Puntualidades", dato.getEmpleadoNombre());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Puntualidades por Empleado",
                "Empleado",
                "Cantidad de Puntualidades",
                dataset,
                PlotOrientation.HORIZONTAL,  // Cambiar a orientación horizontal
                true,
                true,
                false
        );

        // Acceder al renderizador de la gráfica para cambiar los colores
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Establecer el color azul para las barras
        renderer.setSeriesPaint(0, Color.BLUE);  // 0 es el índice de la serie de datos (solo una en este caso)

        // Ajustar la apariencia general del gráfico
        plot.setBackgroundPaint(Color.WHITE);  // Color de fondo del gráfico
        plot.setDomainGridlinePaint(Color.GRAY);  // Color de las líneas de la cuadrícula en el eje X
        plot.setRangeGridlinePaint(Color.GRAY);  // Color de las líneas de la cuadrícula en el eje Y

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 800, 600);
        return baos.toByteArray();
    }
    //----------------------------------------------------------------------------------------------


}