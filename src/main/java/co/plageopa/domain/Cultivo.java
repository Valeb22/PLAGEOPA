package co.plageopa.domain;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cultivos",
       indexes = {
           @Index(name = "cultivos_fk_finca_idx", columnList = "id_finca")
       })
@Getter @Setter
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cultivos") 
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_finca", nullable = false)
    private Finca finca;

    @Column(name = "nombre_cultivo", nullable = false, length = 100)
    private String nombreCultivo;

    @Column(length = 100)
    private String variedad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal area;
    
    public Cultivo() {
		// TODO Auto-generated constructor stub
	}

	

	public Integer getId() {
		return id;
	}



	public void setId(Integer id) {
		this.id = id;
	}



	public Finca getFinca() {
		return finca;
	}

	public void setFinca(Finca finca) {
		this.finca = finca;
	}

	public String getNombreCultivo() {
		return nombreCultivo;
	}

	public void setNombreCultivo(String nombreCultivo) {
		this.nombreCultivo = nombreCultivo;
	}

	public String getVariedad() {
		return variedad;
	}

	public void setVariedad(String variedad) {
		this.variedad = variedad;
	}

	public BigDecimal getArea() {
		return area;
	}

	public void setArea(BigDecimal area) {
		this.area = area;
	}



	public Cultivo(Integer id, Finca finca, String nombreCultivo, String variedad, BigDecimal area) {
		super();
		this.id = id;
		this.finca = finca;
		this.nombreCultivo = nombreCultivo;
		this.variedad = variedad;
		this.area = area;
	}

}
