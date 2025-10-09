package co.plageopa.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs",
       indexes = {
           @Index(name = "logs_usuario_idx", columnList = "id_usuario"),
           @Index(name = "logs_fecha_idx", columnList = "fecha_hora"),
           @Index(name = "logs_tabla_registro_idx", columnList = "tabla_afectada,id_registro_afectado")
       })
@Getter @Setter
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario") // puede ser null
    private Usuario usuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "tabla_afectada", nullable = false, length = 50)
    private String tablaAfectada;

    @Column(nullable = false, length = 10)
    private String operacion; // INSERT/UPDATE/DELETE/LOGIN...

    @Column(name = "id_registro_afectado", nullable = false)
    private Integer idRegistroAfectado;
    
    public Log() {
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getTablaAfectada() {
		return tablaAfectada;
	}

	public void setTablaAfectada(String tablaAfectada) {
		this.tablaAfectada = tablaAfectada;
	}

	public String getOperacion() {
		return operacion;
	}

	public void setOperacion(String operacion) {
		this.operacion = operacion;
	}

	public Integer getIdRegistroAfectado() {
		return idRegistroAfectado;
	}

	public void setIdRegistroAfectado(Integer idRegistroAfectado) {
		this.idRegistroAfectado = idRegistroAfectado;
	}

	public Log(Integer id, Usuario usuario, LocalDateTime fechaHora, String tablaAfectada, String operacion,
			Integer idRegistroAfectado) {
		super();
		this.id = id;
		this.usuario = usuario;
		this.fechaHora = fechaHora;
		this.tablaAfectada = tablaAfectada;
		this.operacion = operacion;
		this.idRegistroAfectado = idRegistroAfectado;
	}
    	
}
