package com.finance.backend.controller;

import com.finance.backend.model.Expense;
import com.finance.backend.repository.ExpenseRepository;
import com.finance.backend.service.MLService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final MLService mlService;

    public ExpenseController(ExpenseRepository expenseRepository, MLService mlService) {
        this.expenseRepository = expenseRepository;
        this.mlService = mlService;
    }

    // CREATE expense (AI prediction)
    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {
        System.out.println("🔥 API HIT: " + expense.getDescription());

        String predictedCategory = mlService.predictCategory(expense.getDescription());
        expense.setPredictedCategory(predictedCategory);

        expense.setDate(LocalDate.now());
        return expenseRepository.save(expense);
    }

    // READ all expenses
    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    // HUMAN OVERRIDE (final category)
    @PutMapping("/{id}/final-category")
    public Expense updateFinalCategory(
            @PathVariable Long id,
            @RequestBody String finalCategory) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setFinalCategory(finalCategory);
        return expenseRepository.save(expense);
    }
}
