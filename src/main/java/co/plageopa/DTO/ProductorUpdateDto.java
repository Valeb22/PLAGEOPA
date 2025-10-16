package co.plageopa.DTO;

import jakarta.validation.constraints.Size;

public class ProductorUpdateDto {
@Size(max=250) private String nombre;
@Size(max=10)  private String telefono;
private Boolean perteneceAsociacion;
@Size(max=100) private String nombreAsociacion;
public String getNombre() {
	return nombre;
}
public void setNombre(String nombre) {
	this.nombre = nombre;
}
public String getTelefono() {
	return telefono;
}
public void setTelefono(String telefono) {
	this.telefono = telefono;
}
public Boolean getPerteneceAsociacion() {
	return perteneceAsociacion;
}
public void setPerteneceAsociacion(Boolean perteneceAsociacion) {
	this.perteneceAsociacion = perteneceAsociacion;
}
public String getNombreAsociacion() {
	return nombreAsociacion;
}
public void setNombreAsociacion(String nombreAsociacion) {
	this.nombreAsociacion = nombreAsociacion;
}


}
