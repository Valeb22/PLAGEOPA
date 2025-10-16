package co.plageopa.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public class FincaUpdateDto {
@DecimalMin("0.0") private Double areaTotal;
@Size(max=200)     private String tipoActividad;
private Double lon;  // si cambian, revalidamos punto único
private Double lat;
private String codigoVereda; // opcional
private String globalid;     // opcional para elegir qué finca actualizar (si hay varias)
public Double getAreaTotal() {
	return areaTotal;
}
public void setAreaTotal(Double areaTotal) {
	this.areaTotal = areaTotal;
}
public String getTipoActividad() {
	return tipoActividad;
}
public void setTipoActividad(String tipoActividad) {
	this.tipoActividad = tipoActividad;
}
public Double getLon() {
	return lon;
}
public void setLon(Double lon) {
	this.lon = lon;
}
public Double getLat() {
	return lat;
}
public void setLat(Double lat) {
	this.lat = lat;
}
public String getCodigoVereda() {
	return codigoVereda;
}
public void setCodigoVereda(String codigoVereda) {
	this.codigoVereda = codigoVereda;
}
public String getGlobalid() {
	return globalid;
}
public void setGlobalid(String globalid) {
	this.globalid = globalid;
}


}

