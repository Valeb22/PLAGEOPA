package co.plageopa.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "fincas",
       indexes = {
           @Index(name = "fincas_globalid_idx", columnList = "globalid", unique = true),
           @Index(name = "fincas_id_productor_idx", columnList = "id_productor"),
           @Index(name = "fincas_id_vereda_idx", columnList = "id_vereda"),
           @Index(name = "fincas_geom_gix", columnList = "geom")
       })
@Getter @Setter
public class Finca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_finca")
    private Integer id;

    @Column(nullable = false, unique = true)
    private UUID globalid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_productor", nullable = false)
    private Productor productor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vereda") // puede ser null (ON DELETE SET NULL)
    private Vereda vereda;

    @Column(name = "area_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal areaTotal;

    @Column(name = "tipo_actividad", nullable = false, length = 200)
    private String tipoActividad;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(columnDefinition = "geometry(POINT,4326)", nullable = false)
    private Point geom;
    
    public Finca() {
		// TODO Auto-generated constructor stub
	}

	

	public Integer getId() {
		return id;
	}



	public void setId(Integer id) {
		this.id = id;
	}



	public UUID getGlobalid() {
		return globalid;
	}

	public void setGlobalid(UUID globalid) {
		this.globalid = globalid;
	}

	public Productor getProductor() {
		return productor;
	}

	public void setProductor(Productor productor) {
		this.productor = productor;
	}

	public Vereda getVereda() {
		return vereda;
	}

	public void setVereda(Vereda vereda) {
		this.vereda = vereda;
	}

	public BigDecimal getAreaTotal() {
		return areaTotal;
	}

	public void setAreaTotal(BigDecimal areaTotal) {
		this.areaTotal = areaTotal;
	}

	public String getTipoActividad() {
		return tipoActividad;
	}

	public void setTipoActividad(String tipoActividad) {
		this.tipoActividad = tipoActividad;
	}

	public Point getGeom() {
		return geom;
	}

	public void setGeom(Point geom) {
		this.geom = geom;
	}



	public Finca(Integer id, UUID globalid, Productor productor, Vereda vereda, BigDecimal areaTotal,
			String tipoActividad, Point geom) {
		super();
		this.id = id;
		this.globalid = globalid;
		this.productor = productor;
		this.vereda = vereda;
		this.areaTotal = areaTotal;
		this.tipoActividad = tipoActividad;
		this.geom = geom;
	}
}
