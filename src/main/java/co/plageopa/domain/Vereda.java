package co.plageopa.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(name = "veredas",
       indexes = {
           @Index(name = "veredas_codigo_corto_idx", columnList = "codigo_corto", unique = false),
           @Index(name = "veredas_geom_gix", columnList = "geom")
       })
@Getter @Setter
public class Vereda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vereda")
    private Integer id;


    @Column(name = "codigo_corto", nullable = false, length = 9)
    private String codigoCorto;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(columnDefinition = "geometry(MULTIPOLYGON,4326)", nullable = false)
    private MultiPolygon geom;
    
    public Vereda() {
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}



	public void setId(Integer id) {
		this.id = id;
	}



	public String getCodigoCorto() {
		return codigoCorto;
	}

	public void setCodigoCorto(String codigoCorto) {
		this.codigoCorto = codigoCorto;
	}

	public MultiPolygon getGeom() {
		return geom;
	}

	public void setGeom(MultiPolygon geom) {
		this.geom = geom;
	}

	public Vereda(Integer id, String codigoCorto, MultiPolygon geom) {
		super();
		this.id = id;
		this.codigoCorto = codigoCorto;
		this.geom = geom;
	}
   
}
