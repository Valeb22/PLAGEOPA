package co.plageopa.DTO;

import java.util.LinkedHashMap;

public record StatsReport(
    long totalProductores,
    long totalFincas,
    long totalCultivos,
    long cultivosUnicos,

    double areaTotalFincas,
    double areaTotalCultivada,
    double usoSueloPct,
    double areaPromedioPorFinca,

    double p25AreaFinca,
    double medianaAreaFinca,
    double p75AreaFinca,

    LinkedHashMap<String, Double> topCultivosArea,   // cultivo -> ha
    LinkedHashMap<String, Long>   topCultivosFincas, // cultivo -> #fincas

    LinkedHashMap<String, Long> generoProductores,     // Masculino/Femenino
    LinkedHashMap<String, Long> asociacionProductores  // Sí/No
) {}
