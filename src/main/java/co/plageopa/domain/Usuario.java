package co.plageopa.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
    name = "usuarios",
    indexes = {
        @Index(name = "usuarios_correo_idx", columnList = "correo", unique = true)
    }
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String nombre;

    @NotBlank
    @Email
    @Column(nullable = false, length = 150, unique = true)
    private String correo;

    @NotBlank
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String rol;

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    public Usuario() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
