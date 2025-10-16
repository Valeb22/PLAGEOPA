package co.plageopa.domain;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_usuario") // FK
    private Usuario usuario;

    @Column(name = "usuario_nombre", length = 200)
    private String usuarioNombre;

    @Column(name = "usuario_correo", length = 150)
    private String usuarioCorreo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "tabla_afectada", nullable = false, length = 50)
    private String tablaAfectada;

    @Column(name = "operacion", nullable = false, length = 10)
    private String operacion;

    @Column(name = "id_registro_afectado", nullable = false)
    private Integer idRegistroAfectado;

    @Column(name = "detalle", columnDefinition = "TEXT")
    private String detalle;

    // ---------- Constructores ----------
    public Log() {}

    public Log(Integer id, Usuario usuario, String usuarioNombre, String usuarioCorreo,
               LocalDateTime fechaHora, String tablaAfectada, String operacion,
               Integer idRegistroAfectado, String detalle) {
        this.id = id;
        this.usuario = usuario;
        this.usuarioNombre = usuarioNombre;
        this.usuarioCorreo = usuarioCorreo;
        this.fechaHora = fechaHora;
        this.tablaAfectada = tablaAfectada;
        this.operacion = operacion;
        this.idRegistroAfectado = idRegistroAfectado;
        this.detalle = detalle;
    }

    // ---------- Getters y Setters ----------
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getUsuarioCorreo() { return usuarioCorreo; }
    public void setUsuarioCorreo(String usuarioCorreo) { this.usuarioCorreo = usuarioCorreo; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getTablaAfectada() { return tablaAfectada; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }

    public String getOperacion() { return operacion; }
    public void setOperacion(String operacion) { this.operacion = operacion; }

    public Integer getIdRegistroAfectado() { return idRegistroAfectado; }
    public void setIdRegistroAfectado(Integer idRegistroAfectado) { this.idRegistroAfectado = idRegistroAfectado; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
