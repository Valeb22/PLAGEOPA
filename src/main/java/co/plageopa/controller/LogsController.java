package co.plageopa.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.plageopa.repository.LogRepository;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class LogsController {

  private final LogRepository logRepo;

  public LogsController(LogRepository logRepo) { this.logRepo = logRepo; }

  @GetMapping
  public ResponseEntity<List<Map<String,Object>>> list(@RequestParam(defaultValue = "100") int limit) {
    return ResponseEntity.ok(logRepo.findLastLogs(limit));
  }
}
