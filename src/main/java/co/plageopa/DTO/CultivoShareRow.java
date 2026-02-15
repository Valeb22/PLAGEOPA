package co.plageopa.DTO;

public record CultivoShareRow(
    String cultivo,
    double areaHa,
    double areaPct,
    long fincas,
    double fincasPct,
    long productores,
    double productoresPct
) {}
