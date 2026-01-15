package com.finance.backend.service;

import com.finance.backend.model.Expense;
import com.finance.backend.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpendingAnalysisService {

    private final ExpenseRepository expenseRepository;

    public SpendingAnalysisService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Map<String, Double> categoryTotals() {
        List<Expense> expenses = expenseRepository.findAll();
        Map<String, Double> totals = new HashMap<>();

        for (Expense e : expenses) {
            String category = e.getFinalCategory() != null
                    ? e.getFinalCategory()
                    : e.getPredictedCategory();

            totals.put(category,
                    totals.getOrDefault(category, 0.0) + e.getAmount());
        }
        return totals;
    }

    public double totalSpend() {
        return expenseRepository.findAll()
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }
    public double monthOverMonthChange() {
    var data = expenseRepository.monthlyTotals();

    if (data.size() < 2) return 0;

    double lastMonth = ((Number) data.get(data.size() - 2)[1]).doubleValue();
    double currentMonth = ((Number) data.get(data.size() - 1)[1]).doubleValue();

    if (lastMonth == 0) return 0;

    return ((currentMonth - lastMonth) / lastMonth) * 100;
}

}
