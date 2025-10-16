package co.plageopa.DTO;

public class CultivoDto {
    private Integer id;              // <-- NUEVO
    private String  nombreCultivo;
    private String  variedad;
    private Double  area;

    // getters/setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombreCultivo() { return nombreCultivo; }
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; }

    public String getVariedad() { return variedad; }
    public void setVariedad(String variedad) { this.variedad = variedad; }

    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }
}
