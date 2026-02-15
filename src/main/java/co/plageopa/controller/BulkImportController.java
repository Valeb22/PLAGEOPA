package co.plageopa.controller;

import co.plageopa.DTO.ImportPreviewResponse;
import co.plageopa.DTO.ImportResult;
import co.plageopa.service.BulkImportService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
@CrossOrigin(
		  origins = "http://localhost:4200",
		  allowCredentials = "true",
		  allowedHeaders = {"Content-Type","X-User-Id","Authorization","Accept"},
		  methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
		)

@RestController
@RequestMapping("/api/import")
public class BulkImportController {

  private final BulkImportService bulk;

  public BulkImportController(BulkImportService bulk) {
    this.bulk = bulk;
  }

  // 1) Descargar plantilla (ponla en: src/main/resources/static/plantillas/PLAGEOPA_Plantilla.xlsx)
  @GetMapping("/template.xlsx")
  public void template(HttpServletResponse resp) throws Exception {
    var res = new ClassPathResource("static/plantillas/PLAGEOPA_Plantilla.xlsx");
    resp.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PLAGEOPA_Plantilla.xlsx");
    resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    try (InputStream is = res.getInputStream()) {
      resp.getOutputStream().write(is.readAllBytes());
    }
    resp.flushBuffer();
  }

  // 2) Preview/validación (sin guardar)
  @PostMapping(value="/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ImportPreviewResponse preview(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value="limit", defaultValue="200") int limit) throws Exception {
    return bulk.preview(file, limit);
  }

  // 3) Aplicar/importar (guarda)
  @PostMapping(value="/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ImportResult apply(@RequestParam("file") MultipartFile file) throws Exception {
    return bulk.apply(file);
  }
}
