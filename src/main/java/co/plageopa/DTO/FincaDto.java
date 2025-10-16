package co.plageopa.DTO;

public class FincaDto {
    private Double  areaTotal;
    private String  tipoActividad;
    private Double  lon;
    private Double  lat;
    private String  veredaCodigo;
    private String  globalid;   // <-- NUEVO

    // getters/setters
    public Double getAreaTotal() { return areaTotal; }
    public void setAreaTotal(Double areaTotal) { this.areaTotal = areaTotal; }

    public String getTipoActividad() { return tipoActividad; }
    public void setTipoActividad(String tipoActividad) { this.tipoActividad = tipoActividad; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public String getVeredaCodigo() { return veredaCodigo; }
    public void setVeredaCodigo(String veredaCodigo) { this.veredaCodigo = veredaCodigo; }

    public String getGlobalid() { return globalid; }
    public void setGlobalid(String globalid) { this.globalid = globalid; }
}
