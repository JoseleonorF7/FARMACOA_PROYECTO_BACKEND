package Package.PHARMACY_PROJECT.Models.Reportes;

import lombok.Getter;
import lombok.Setter;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReporteConsolidado {
    private Map<String, EmpleadoReporte> reportePorEmpleado;
    private List<EmpleadoRanking> rankingTardanzas;
    private List<EmpleadoRanking> rankingSalidasTempranas;
    private Map<YearMonth, PromedioMensual> promediosMensuales;

    public ReporteConsolidado(Map<String, EmpleadoReporte> reportePorEmpleado,
                              List<EmpleadoRanking> rankingTardanzas,
                              List<EmpleadoRanking> rankingSalidasTempranas,
                              Map<YearMonth, PromedioMensual> promediosMensuales) {
        this.reportePorEmpleado = reportePorEmpleado;
        this.rankingTardanzas = rankingTardanzas;
        this.rankingSalidasTempranas = rankingSalidasTempranas;
        this.promediosMensuales = promediosMensuales;
    }}