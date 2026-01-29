package co.plageopa.service;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import co.plageopa.repository.LogRepository;

@Service
public class LogsReportService {

  private final LogRepository logRepo;

  public LogsReportService(LogRepository logRepo) {
    this.logRepo = logRepo;
  }

  public byte[] buildLogsXlsx(LocalDate from, LocalDate to) {

    List<Map<String, Object>> rows = logRepo.findByRangeMap(from, to);

    try (Workbook wb = new XSSFWorkbook();
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      Sheet sheet = wb.createSheet("Logs");

      /* =========================
         Styles
      ========================= */

      // Header
      CellStyle headerStyle = wb.createCellStyle();
      Font headerFont = wb.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);
      headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
      headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      headerStyle.setBorderBottom(BorderStyle.THIN);
      headerStyle.setBorderTop(BorderStyle.THIN);
      headerStyle.setBorderLeft(BorderStyle.THIN);
      headerStyle.setBorderRight(BorderStyle.THIN);

      // DateTime
      CellStyle dateTimeStyle = wb.createCellStyle();
      dateTimeStyle.setDataFormat(
        wb.createDataFormat().getFormat("yyyy-mm-dd hh:mm")
      );

      // Wrap text (detalle)
      CellStyle wrapStyle = wb.createCellStyle();
      wrapStyle.setWrapText(true);
      wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

      /* =========================
         Columns definition
      ========================= */

      // key en el Map -> header visible en Excel
      String[][] columns = new String[][] {
        { "fecha_hora",           "Fecha / Hora" },
        { "usuario_nombre",       "Usuario" },
        { "tabla_afectada",       "Tabla" },
        { "operacion",            "Operación" },
        { "id_registro_afectado", "ID afectado" },
        { "detalle",              "Detalle" }
      };

      /* =========================
         Header row
      ========================= */

      Row header = sheet.createRow(0);
      for (int c = 0; c < columns.length; c++) {
        Cell cell = header.createCell(c);
        cell.setCellValue(columns[c][1]);
        cell.setCellStyle(headerStyle);
      }

      /* =========================
         Body rows
      ========================= */

      int rowIdx = 1;
      for (Map<String, Object> rowMap : rows) {
        Row row = sheet.createRow(rowIdx++);

        for (int c = 0; c < columns.length; c++) {
          String key = columns[c][0];
          Object v = rowMap.get(key);
          Cell cell = row.createCell(c);

          if (v == null) {
            cell.setCellValue("");
          }
          else if (v instanceof Timestamp ts) {
            cell.setCellValue(ts);
            cell.setCellStyle(dateTimeStyle);
          }
          else if (v instanceof LocalDateTime ldt) {
            cell.setCellValue(Timestamp.valueOf(ldt));
            cell.setCellStyle(dateTimeStyle);
          }
          else if (v instanceof Number n) {
            cell.setCellValue(n.doubleValue());
          }
          else {
            cell.setCellValue(String.valueOf(v));
          }

          // Detalle: wrap text
          if ("detalle".equals(key)) {
            cell.setCellStyle(wrapStyle);
          }
        }
      }

      /* =========================
         Column widths
      ========================= */

      sheet.setColumnWidth(0, 22 * 256); // Fecha
      sheet.setColumnWidth(1, 20 * 256); // Usuario
      sheet.setColumnWidth(2, 18 * 256); // Tabla
      sheet.setColumnWidth(3, 14 * 256); // Operación
      sheet.setColumnWidth(4, 16 * 256); // ID
      sheet.setColumnWidth(5, 70 * 256); // Detalle

      wb.write(out);
      return out.toByteArray();

    } catch (Exception e) {
      throw new RuntimeException("Error generando Excel de logs", e);
    }
  }
}
