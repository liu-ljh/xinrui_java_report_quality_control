package org.xinrui.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xinrui.dto.ApiResponse;
import org.xinrui.service.QualityService;
import org.xinrui.dto.DeepSeekRequestDTO;
import org.xinrui.service.model.QualityResult;

@Slf4j
@RestController
@RequestMapping("/api/v1/quality-control")
public class QualityControlController {
    private final QualityService qualityService;

    @Autowired
    public QualityControlController(QualityService qualityService) {
        this.qualityService = qualityService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello, World!");
    }

    @PostMapping("/deepseek")
    public ApiResponse<QualityResult> analyzeReport(@RequestBody DeepSeekRequestDTO request) {
        try {
            QualityResult result = qualityService.analyzeReport(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.fail(-1, e.getMessage());
        }
    }
}