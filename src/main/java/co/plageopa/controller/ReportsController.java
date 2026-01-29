package co.plageopa.controller;

import co.plageopa.DTO.StatsReport;
import co.plageopa.service.StatsService;
import co.plageopa.service.LogsReportService;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/reports")
public class ReportsController {

  private final StatsService statsService;
  private final LogsReportService logsReportService;

  public ReportsController(StatsService statsService, LogsReportService logsReportService) {
    this.statsService = statsService;
    this.logsReportService = logsReportService;
  }

  // ✅ JSON para el front
  @GetMapping("/stats")
  public StatsReport statsJson() {
    return statsService.getStats();
  }

  // ✅ PDF bonito backend
  @GetMapping("/stats.pdf")
  public void statsPdf(HttpServletResponse response) throws Exception {

    StatsReport stats = statsService.getStats();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    Document doc = new Document(PageSize.A4, 36, 36, 60, 50);
    PdfWriter writer = PdfWriter.getInstance(doc, baos);

    PageNumberEvent event = new PageNumberEvent();
    writer.setPageEvent(event);

    doc.open();

    addHeader(doc);
    addMetaBlock(doc);

    doc.add(spacer(8));
    doc.add(sectionTitle("Resumen de indicadores"));
    doc.add(spacer(6));
    doc.add(kpiTable(stats));

    doc.add(spacer(10));
    doc.add(sectionTitle("Top cultivos por área (ha)"));
    doc.add(spacer(6));
    doc.add(topTable(stats));

    doc.add(spacer(10));
    doc.add(sectionTitle("Distribuciones"));
    doc.add(spacer(6));
    doc.add(distTable("Género de productores", stats.generoProductores()));
    doc.add(spacer(6));
    doc.add(distTable("Pertenencia a asociación", stats.asociacionProductores()));

    doc.close();
    event.writeTotalPages(writer);

    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Estadisticas_PLAGEOPA.pdf");
    response.setContentType("application/pdf");
    response.getOutputStream().write(baos.toByteArray());
    response.flushBuffer();
  }

  private static void addHeader(Document doc) throws Exception {
    PdfPTable header = new PdfPTable(new float[]{1.2f, 3.8f});
    header.setWidthPercentage(100);

    PdfPCell logoCell = new PdfPCell();
    logoCell.setBorder(Rectangle.NO_BORDER);

    try (InputStream is = new ClassPathResource("static/logo.jpeg").getInputStream()) {
      Image logo = Image.getInstance(is.readAllBytes());
      logo.scaleToFit(70, 70);
      logoCell.addElement(logo);
    } catch (Exception ex) {
      logoCell.addElement(new Phrase(" "));
    }

    PdfPCell titleCell = new PdfPCell();
    titleCell.setBorder(Rectangle.NO_BORDER);

    Font title = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(20, 35, 60));
    Font subtitle = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(90, 90, 90));

    titleCell.addElement(new Paragraph("PLAGEOPA", title));
    titleCell.addElement(new Paragraph("Reporte de Estadísticas", subtitle));

    header.addCell(logoCell);
    header.addCell(titleCell);

    doc.add(header);

    LineSeparator line = new LineSeparator();
    line.setLineColor(new Color(210, 210, 210));
    doc.add(new Chunk(line));
  }

  private static void addMetaBlock(Document doc) throws Exception {
    Font meta = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(80, 80, 80));
    String exportedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    Paragraph metaP = new Paragraph("Fecha de exportación: " + exportedAt, meta);
    metaP.setSpacingBefore(8);
    metaP.setSpacingAfter(2);
    doc.add(metaP);
  }

  private static Paragraph sectionTitle(String text) {
    Font f = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(20, 35, 60));
    Paragraph p = new Paragraph(text, f);
    p.setSpacingBefore(6);
    return p;
  }

  private static Paragraph spacer(float pts) {
    Paragraph p = new Paragraph(" ");
    p.setSpacingBefore(pts);
    return p;
  }

  private static PdfPTable kpiTable(StatsReport s) {
    PdfPTable t = new PdfPTable(new float[]{3.2f, 2.0f});
    t.setWidthPercentage(100);

    addHeaderCell(t, "Métrica");
    addHeaderCell(t, "Valor");

    addBodyCell(t, "Total productores"); addBodyCell(t, String.valueOf(s.totalProductores()));
    addBodyCell(t, "Total fincas"); addBodyCell(t, String.valueOf(s.totalFincas()));
    addBodyCell(t, "Total cultivos (registros)"); addBodyCell(t, String.valueOf(s.totalCultivos()));
    addBodyCell(t, "Cultivos únicos"); addBodyCell(t, String.valueOf(s.cultivosUnicos()));

    addBodyCell(t, "Área total fincas (ha)"); addBodyCell(t, fmt2(s.areaTotalFincas()));
    addBodyCell(t, "Área total cultivada (ha)"); addBodyCell(t, fmt2(s.areaTotalCultivada()));
    addBodyCell(t, "Uso de suelo (%)"); addBodyCell(t, fmt2(s.usoSueloPct()));
    addBodyCell(t, "Área promedio por finca (ha)"); addBodyCell(t, fmt2(s.areaPromedioPorFinca()));

    addBodyCell(t, "Área finca P25 (ha)"); addBodyCell(t, fmt2(s.p25AreaFinca()));
    addBodyCell(t, "Mediana área finca (ha)"); addBodyCell(t, fmt2(s.medianaAreaFinca()));
    addBodyCell(t, "Área finca P75 (ha)"); addBodyCell(t, fmt2(s.p75AreaFinca()));

    return t;
  }

  private static PdfPTable topTable(StatsReport s) {
    PdfPTable t = new PdfPTable(new float[]{3.2f, 2.0f});
    t.setWidthPercentage(100);

    addHeaderCell(t, "Cultivo");
    addHeaderCell(t, "Área (ha)");

    Map<String, Double> top = s.topCultivosArea();
    if (top == null || top.isEmpty()) {
      PdfPCell c = new PdfPCell(new Phrase("No hay datos para mostrar."));
      c.setColspan(2);
      c.setPadding(8);
      c.setBorderColor(new Color(220, 220, 220));
      t.addCell(c);
      return t;
    }

    for (var e : top.entrySet()) {
      addBodyCell(t, e.getKey());
      addBodyCell(t, fmt2(e.getValue()));
    }
    return t;
  }

  private static PdfPTable distTable(String title, Map<String, Long> dist) {
    PdfPTable t = new PdfPTable(new float[]{2.6f, 1.2f, 3.2f});
    t.setWidthPercentage(100);

    PdfPCell cap = new PdfPCell(new Phrase(title, new Font(Font.HELVETICA, 10, Font.BOLD)));
    cap.setColspan(3);
    cap.setPadding(8);
    cap.setBorderColor(new Color(220,220,220));
    cap.setBackgroundColor(new Color(245,245,245));
    t.addCell(cap);

    addHeaderCell(t, "Categoría");
    addHeaderCell(t, "Valor");
    addHeaderCell(t, "Distribución");

    long total = 0;
    if (dist != null) for (var v : dist.values()) total += (v == null ? 0 : v);

    if (dist == null || dist.isEmpty()) {
      PdfPCell c = new PdfPCell(new Phrase("No hay datos para mostrar."));
      c.setColspan(3);
      c.setPadding(8);
      c.setBorderColor(new Color(220, 220, 220));
      t.addCell(c);
      return t;
    }

    for (var e : dist.entrySet()) {
      long v = e.getValue() == null ? 0 : e.getValue();
      double pct = total > 0 ? (v * 1.0 / total) : 0;

      addBodyCell(t, e.getKey());
      addBodyCell(t, String.valueOf(v));
      t.addCell(barCell(pct));
    }
    return t;
  }

  private static PdfPCell barCell(double pct0to1) {
    pct0to1 = Math.max(0, Math.min(1, pct0to1));
    int blocks = (int)Math.round(pct0to1 * 20);
    String bar = "█".repeat(blocks) + " ".repeat(20 - blocks);
    String label = String.format(java.util.Locale.US, " %s  (%.1f%%)", bar, pct0to1 * 100);

    Font f = new Font(Font.COURIER, 9, Font.NORMAL, new Color(40,40,40));
    PdfPCell cell = new PdfPCell(new Phrase(label, f));
    cell.setPadding(7);
    cell.setBorderColor(new Color(220,220,220));
    return cell;
  }

  private static void addHeaderCell(PdfPTable t, String text) {
    Font f = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    PdfPCell cell = new PdfPCell(new Phrase(text, f));
    cell.setBackgroundColor(new Color(20, 35, 60));
    cell.setPadding(8);
    cell.setBorderColor(new Color(20, 35, 60));
    t.addCell(cell);
  }

  private static void addBodyCell(PdfPTable t, String text) {
    Font f = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(40, 40, 40));
    PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, f));
    cell.setPadding(7);
    cell.setBorderColor(new Color(220, 220, 220));
    t.addCell(cell);
  }

  private static String fmt2(double v) {
    return String.format(java.util.Locale.US, "%.2f", v);
  }

  static class PageNumberEvent extends PdfPageEventHelper {
    private PdfTemplate total;
    private BaseFont bf;

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
      total = writer.getDirectContent().createTemplate(30, 16);
      try {
        bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
      } catch (Exception ignored) {}
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      PdfContentByte cb = writer.getDirectContent();
      cb.saveState();
      cb.setFontAndSize(bf, 9);

      String text = "Página " + writer.getPageNumber() + " de ";
      float x = document.right() - 120;
      float y = document.bottom() - 18;

      cb.beginText();
      cb.setRGBColorFill(120, 120, 120);
      cb.setTextMatrix(x, y);
      cb.showText(text);
      cb.endText();

      cb.addTemplate(total, x + bf.getWidthPoint(text, 9), y);
      cb.restoreState();
    }

    void writeTotalPages(PdfWriter writer) {
      total.beginText();
      total.setFontAndSize(bf, 9);
      total.setTextMatrix(0, 0);
      total.showText(String.valueOf(writer.getPageNumber() - 1));
      total.endText();
    }
  }
  
  @GetMapping(
		  value = "/logs.xlsx",
		  produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
		)
		public ResponseEntity<byte[]> logsExcel(
		    @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		    @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
		) {
		  byte[] bytes = logsReportService.buildLogsXlsx(from, to);

		  return ResponseEntity.ok()
		      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=logs.xlsx")
		      .contentType(MediaType.parseMediaType(
		          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
		      ))
		      .body(bytes);
		}
}
