package com.finance.backend.repository;

import com.finance.backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;   // ✅ ADD THIS
import java.util.List;  

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("""
SELECT MONTH(e.date), SUM(e.amount)
FROM Expense e
WHERE YEAR(e.date) = YEAR(CURRENT_DATE)
GROUP BY MONTH(e.date)
ORDER BY MONTH(e.date)
""")
List<Object[]> monthlyTotals();

}
