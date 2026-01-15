package com.finance.backend.service;

import com.finance.backend.model.Advice;
import com.finance.backend.repository.AdviceRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AdvisorService {

    private final SpendingAnalysisService analysisService;
    private final AdviceRepository adviceRepository;

    public AdvisorService(SpendingAnalysisService analysisService,
                           AdviceRepository adviceRepository) {
        this.analysisService = analysisService;
        this.adviceRepository = adviceRepository;
    }

    public Advice generateAdvice() {

        double total = analysisService.totalSpend();
        Map<String, Double> categories = analysisService.categoryTotals();
        double trend = analysisService.monthOverMonthChange();

if (trend > 15) {
    Advice advice = new Advice();
    advice.setCategory("GENERAL");
    advice.setMessage(
        "Your overall spending increased by " + Math.round(trend) + "% compared to last month."
    );
    advice.setSuggestion(
        "Would you like help controlling expenses this month?"
    );
    advice.setConfidence("HIGH");

    return adviceRepository.save(advice);
}


        for (String category : categories.keySet()) {
            double percent = (categories.get(category) / total) * 100;

            if (percent > 40) {
                Advice advice = new Advice();
                advice.setCategory(category);
                advice.setMessage(
                        "You spent " + Math.round(percent) + "% on " + category
                );
                advice.setSuggestion(
                        "Would you like to reduce spending on " + category + "?"
                );
                advice.setConfidence("HIGH");

                return adviceRepository.save(advice);
            }
        }

        Advice neutral = new Advice();
        neutral.setCategory("GENERAL");
        neutral.setMessage("Your spending is balanced.");
        neutral.setSuggestion("Keep tracking your expenses.");
        neutral.setConfidence("LOW");

        return adviceRepository.save(neutral);
    }
}
