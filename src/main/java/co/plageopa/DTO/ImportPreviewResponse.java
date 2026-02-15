// co.plageopa.DTO.ImportPreviewResponse
package co.plageopa.DTO;

import java.util.List;

public record ImportPreviewResponse(
  int totalRows,
  int okRows,
  int errorRows,
  List<ImportPreviewRow> rows
) {}
