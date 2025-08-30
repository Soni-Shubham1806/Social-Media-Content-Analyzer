package com.analyzer.backend.controller;

import com.analyzer.backend.model.TextExtractionResponse;
import com.analyzer.backend.service.TextExtractionService;
import com.analyzer.backend.service.TextExtractionService.ExtractResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/extract")
@CrossOrigin(origins = "*") // relax during dev; tighten in prod
public class TextExtractionController {

    private final TextExtractionService extractionService;

    public TextExtractionController(TextExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    @PostMapping(value = "/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TextExtractionResponse> extractText(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ExtractResult r = extractionService.extractText(file);

        TextExtractionResponse resp = new TextExtractionResponse(
                r.sourceType,
                r.text,
                r.pageCount,
                r.durationMs
        );

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
