package co.plageopa.DTO;

import java.util.List;

import lombok.Data;

//co.plageopa.dto.RegistroResponseDto
@Data
public class RegistroResponseDto {
private ProductorDto productor;
private FincaDto finca;
private List<CultivoDto> cultivos;
public ProductorDto getProductor() {
	return productor;
}
public void setProductor(ProductorDto productor) {
	this.productor = productor;
}
public FincaDto getFinca() {
	return finca;
}
public void setFinca(FincaDto finca) {
	this.finca = finca;
}
public List<CultivoDto> getCultivos() {
	return cultivos;
}
public void setCultivos(List<CultivoDto> cultivos) {
	this.cultivos = cultivos;
}


}
