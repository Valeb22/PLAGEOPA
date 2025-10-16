package co.plageopa.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity @Table(name="logs")
public class Log {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
private Integer idLog;

@ManyToOne(optional = true, fetch = FetchType.LAZY)
@JoinColumn(name = "id_usuario")
private Usuario usuario; 

@Column(name="usuario_nombre") private String usuarioNombre;   // NUEVO
@Column(name="usuario_correo") private String usuarioCorreo;   // NUEVO

@Column(name="fecha_hora") private LocalDateTime fechaHora;
@Column(name="tabla_afectada") private String tablaAfectada;
@Column(name="operacion") private String operacion;
@Column(name="id_registro_afectado") private Integer idRegistroAfectado;


public Log() {
	// TODO Auto-generated constructor stub
}


public Log(Integer idLog, Usuario usuario, String usuarioNombre, String usuarioCorreo, LocalDateTime fechaHora,
		String tablaAfectada, String operacion, Integer idRegistroAfectado) {
	super();
	this.idLog = idLog;
	this.usuario = usuario;
	this.usuarioNombre = usuarioNombre;
	this.usuarioCorreo = usuarioCorreo;
	this.fechaHora = fechaHora;
	this.tablaAfectada = tablaAfectada;
	this.operacion = operacion;
	this.idRegistroAfectado = idRegistroAfectado;
}


public Integer getIdLog() {
	return idLog;
}


public void setIdLog(Integer idLog) {
	this.idLog = idLog;
}


public Usuario getUsuario() {
	return usuario;
}


public void setUsuario(Usuario usuario) {
	this.usuario = usuario;
}


public String getUsuarioNombre() {
	return usuarioNombre;
}


public void setUsuarioNombre(String usuarioNombre) {
	this.usuarioNombre = usuarioNombre;
}


public String getUsuarioCorreo() {
	return usuarioCorreo;
}


public void setUsuarioCorreo(String usuarioCorreo) {
	this.usuarioCorreo = usuarioCorreo;
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
}
