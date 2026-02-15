// co.plageopa.DTO.ImportResult
package co.plageopa.DTO;

public record ImportResult(
  int totalRows,
  int insertedProductores,
  int updatedProductores,
  int insertedFincas,
  int updatedFincas,
  int insertedCultivos,
  int errorRows
) {}
