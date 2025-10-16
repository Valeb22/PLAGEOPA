package co.plageopa.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.plageopa.repository.LogRepository;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class LogsController {

    private final LogRepository logRepo;

    public LogsController(LogRepository logRepo) {
        this.logRepo = logRepo;
    }

    // GET /api/logs?from=YYYY-MM-DD&to=YYYY-MM-DD
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listByDateRange(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(logRepo.findByRangeMap(from, to));
    }
}
