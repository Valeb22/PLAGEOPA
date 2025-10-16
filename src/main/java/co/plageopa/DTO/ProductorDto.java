package co.plageopa.DTO;

import jakarta.validation.constraints.*;

public class ProductorDto {
  @NotBlank @Size(max=10)
  private String cedula;

  @NotBlank @Size(max=250)
  private String nombre;

  @Size(max=10)
  private String telefono;

  @NotBlank @Pattern(regexp="Femenino|Masculino")
  private String genero;

  private boolean perteneceAsociacion;

  @Size(max=100)
  private String nombreAsociacion;

  public String getCedula() { return cedula; }
  public void setCedula(String cedula){ this.cedula = cedula; }
  public String getNombre(){ return nombre; }
  public void setNombre(String nombre){ this.nombre = nombre; }
  public String getTelefono(){ return telefono; }
  public void setTelefono(String telefono){ this.telefono = telefono; }
  public String getGenero(){ return genero; }
  public void setGenero(String genero){ this.genero = genero; }
  public boolean isPerteneceAsociacion(){ return perteneceAsociacion; }
  public void setPerteneceAsociacion(boolean v){ this.perteneceAsociacion = v; }
  public String getNombreAsociacion(){ return nombreAsociacion; }
  public void setNombreAsociacion(String v){ this.nombreAsociacion = v; }
}
