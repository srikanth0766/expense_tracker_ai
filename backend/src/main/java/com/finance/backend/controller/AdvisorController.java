package com.finance.backend.controller;

import com.finance.backend.model.Advice;
import com.finance.backend.repository.AdviceRepository;
import com.finance.backend.service.AdvisorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advisor")
@CrossOrigin
public class AdvisorController {

    private final AdvisorService advisorService;
    private final AdviceRepository adviceRepository;

    public AdvisorController(AdvisorService advisorService,
                              AdviceRepository adviceRepository) {
        this.advisorService = advisorService;
        this.adviceRepository = adviceRepository;
    }

    @GetMapping("/analyze")
    public Advice analyze() {
        return advisorService.generateAdvice();
    }

    @PutMapping("/{id}/feedback")
    public Advice feedback(@PathVariable Long id,
                           @RequestBody String decision) {

        Advice advice = adviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advice not found"));

        advice.setUserDecision(decision);
        return adviceRepository.save(advice);
    }
}
