package co.plageopa.DTO;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class RegistroCreateDto {
  @NotNull @Valid
  private ProductorDto productor;

  @NotNull @Valid
  private FincaDto finca;

  @NotNull @Valid
  private List<@Valid CultivoDto> cultivos;

  // getters/setters
  // ...
  public ProductorDto getProductor(){ return productor; }
  public void setProductor(ProductorDto v){ this.productor = v; }
  public FincaDto getFinca(){ return finca; }
  public void setFinca(FincaDto v){ this.finca = v; }
  public List<CultivoDto> getCultivos(){ return cultivos; }
  public void setCultivos(List<CultivoDto> v){ this.cultivos = v; }
}
