package co.plageopa.DTO;

import java.util.List;
import jakarta.validation.Valid;

public class RegistroUpdateDto {
@Valid private ProductorUpdateDto productor;
@Valid private FincaUpdateDto finca;

// manejo de cultivos
@Valid private List<CultivoUpsertDto> cultivosUpsert; // crear/editar
private List<Integer> cultivosDeleteIds;              // borrar por id
public ProductorUpdateDto getProductor() {
	return productor;
}
public void setProductor(ProductorUpdateDto productor) {
	this.productor = productor;
}
public FincaUpdateDto getFinca() {
	return finca;
}
public void setFinca(FincaUpdateDto finca) {
	this.finca = finca;
}
public List<CultivoUpsertDto> getCultivosUpsert() {
	return cultivosUpsert;
}
public void setCultivosUpsert(List<CultivoUpsertDto> cultivosUpsert) {
	this.cultivosUpsert = cultivosUpsert;
}
public List<Integer> getCultivosDeleteIds() {
	return cultivosDeleteIds;
}
public void setCultivosDeleteIds(List<Integer> cultivosDeleteIds) {
	this.cultivosDeleteIds = cultivosDeleteIds;
}


}
