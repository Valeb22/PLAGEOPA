package co.plageopa.DTO;

import jakarta.validation.constraints.DecimalMin;

public class CultivoDto {
    private Integer id;             
    private String  nombreCultivo;
    private String  variedad;
    @DecimalMin("0.0")
    private Double area;


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombreCultivo() { return nombreCultivo; }
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; }

    public String getVariedad() { return variedad; }
    public void setVariedad(String variedad) { this.variedad = variedad; }

    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }
}
