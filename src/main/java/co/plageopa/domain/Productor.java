package co.plageopa.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "productores",
       indexes = {
           @Index(name = "productores_cedula_idx", columnList = "cedula", unique = true),
           @Index(name = "productores_nombre_idx", columnList = "nombre")
       })
@Getter @Setter
public class Productor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_productor")
    private Integer id;

    @NotBlank
    @Column(nullable = false, length = 10, unique = true)
    private String cedula;

    @NotBlank
    @Column(nullable = false, length = 250)
    private String nombre;

    @Column(length = 10)
    private String telefono;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String genero; // 'Femenino' | 'Masculino' (restricción en BD)

    @Column(name = "pertenece_asociacion", nullable = false)
    private boolean perteneceAsociacion = false;

    @Column(name = "nombre_asociacion", length = 100)
    private String nombreAsociacion;

public Productor() {
	// TODO Auto-generated constructor stub
}

public Integer getId() {
	return id;
}

public void setId(Integer id) {
	this.id = id;
}



public String getCedula() {
	return cedula;
}

public void setCedula(String cedula) {
	this.cedula = cedula;
}

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

public String getGenero() {
	return genero;
}

public void setGenero(String genero) {
	this.genero = genero;
}

public boolean isPerteneceAsociacion() {
	return perteneceAsociacion;
}

public void setPerteneceAsociacion(boolean perteneceAsociacion) {
	this.perteneceAsociacion = perteneceAsociacion;
}

public String getNombreAsociacion() {
	return nombreAsociacion;
}

public void setNombreAsociacion(String nombreAsociacion) {
	this.nombreAsociacion = nombreAsociacion;
}

public Productor(Integer id, @NotBlank String cedula, @NotBlank String nombre, String telefono, @NotBlank String genero,
		boolean perteneceAsociacion, String nombreAsociacion) {
	super();
	this.id = id;
	this.cedula = cedula;
	this.nombre = nombre;
	this.telefono = telefono;
	this.genero = genero;
	this.perteneceAsociacion = perteneceAsociacion;
	this.nombreAsociacion = nombreAsociacion;
}

}
