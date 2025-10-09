package co.plageopa.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios",
       indexes = { @Index(name = "usuarios_correo_idx", columnList = "correo", unique = true) })
@Getter @Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String nombre;

    @NotBlank @Email
    @Column(nullable = false, length = 150, unique = true)
    private String correo;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String contraseña;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String rol; 
    
    public Usuario() {
		// TODO Auto-generated constructor stub
	}

	

	public Integer getId() {
		return id;
	}



	public void setId(Integer id) {
		this.id = id;
	}



	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getContraseña() {
		return contraseña;
	}

	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}



	public Usuario(Integer id, @NotBlank String nombre, @NotBlank @Email String correo, @NotBlank String contraseña,
			@NotBlank String rol) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.correo = correo;
		this.contraseña = contraseña;
		this.rol = rol;
	}

	
}
