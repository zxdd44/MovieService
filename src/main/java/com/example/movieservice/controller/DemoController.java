package com.example.movieservice.controller;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.service.DemoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/demo")
public class DemoController {
    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    // URL: POST http://localhost:8080/demo/rollback
    @PostMapping("/rollback")
    public void testRollback(@RequestBody MovieDto dto) {
        demoService.createWithTransaction(dto);
    }

    // URL: POST http://localhost:8080/demo/no-rollback
    @PostMapping("/no-rollback")
    public void testNoRollback(@RequestBody MovieDto dto) {
        demoService.createWithoutTransaction(dto);
    }

    // URL: POST http://localhost:8080/demo/noBulk
    @PostMapping("/noBulk")
    public void testBulkNoTx(@RequestBody List<MovieDto> dtos) {
        demoService.createBulkWithoutTransaction(dtos);
    }

    // URL: GET http://localhost:8080/demo/NPO
    @GetMapping("/NPO")
    public String testN1() {
        demoService.demonstrateNPO();
        return "Смотри в консоль IDEA!";
    }

    // URL: GET http://localhost:8080/demo/race-condition-unsafe
    @GetMapping("/race-condition-unsafe")
    public Map<String, Integer> demoRaceConditionUnsafe() {
        return demoService.runUnsafeRaceConditionDemo();
    }
}
