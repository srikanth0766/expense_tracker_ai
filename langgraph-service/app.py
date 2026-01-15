from dotenv import load_dotenv
load_dotenv()

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Dict, TypedDict, Optional

from langgraph.graph import StateGraph, END
from langchain_mistralai import ChatMistralAI
from langchain_core.messages import HumanMessage

# =========================================================
# APP
# =========================================================
app = FastAPI(title="Financial Intelligence Engine", version="3.0")

# =========================================================
# CORS - Allow frontend to make requests
# =========================================================
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://127.0.0.1:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# =========================================================
# LLM (EXPLANATION ONLY)
# =========================================================
llm = ChatMistralAI(
    model="mistral-large-latest",
    temperature=0.2
)

# =========================================================
# INPUT MODELS
# =========================================================
class ExpenseItem(BaseModel):
    category: str
    amount: float

class MonthlySnapshot(BaseModel):
    month: str
    income: float
    expenses: List[ExpenseItem]

class FinancialHistory(BaseModel):
    current: MonthlySnapshot
    previous: Optional[MonthlySnapshot] = None

# =========================================================
# GRAPH STATE
# =========================================================
class ExplainState(TypedDict):
    features: Dict
    explanation: str

# =========================================================
# FEATURE EXTRACTION
# =========================================================
def extract_features(snapshot: MonthlySnapshot) -> Dict:
    total_expenses = sum(e.amount for e in snapshot.expenses)

    breakdown = {}
    for e in snapshot.expenses:
        breakdown[e.category] = breakdown.get(e.category, 0) + e.amount

    expense_ratio = round(total_expenses / snapshot.income, 2)
    savings = snapshot.income - total_expenses

    dominant_category = max(breakdown, key=breakdown.get)
    dominant_share = round(breakdown[dominant_category] / total_expenses, 2)

    risk_flags = []
    if expense_ratio > 0.85:
        risk_flags.append("HIGH_EXPENSE_RATIO")
    if dominant_share > 0.4:
        risk_flags.append("CATEGORY_DOMINANCE")
    if savings < 0:
        risk_flags.append("NEGATIVE_SAVINGS")

    return {
        "month": snapshot.month,
        "income": snapshot.income,
        "total_expenses": total_expenses,
        "savings": savings,
        "expense_ratio": expense_ratio,
        "dominant_category": dominant_category,
        "dominant_share": dominant_share,
        "category_breakdown": breakdown,
        "risk_flags": risk_flags,
    }

# =========================================================
# TREND & ANOMALY ANALYSIS
# =========================================================
def compare_months(curr: Dict, prev: Dict) -> Dict:
    delta_expenses = curr["total_expenses"] - prev["total_expenses"]
    delta_ratio = round(curr["expense_ratio"] - prev["expense_ratio"], 2)

    trend = "stable"
    if delta_ratio > 0.05:
        trend = "worsening"
    elif delta_ratio < -0.05:
        trend = "improving"

    anomalies = []
    for cat, amt in curr["category_breakdown"].items():
        prev_amt = prev["category_breakdown"].get(cat, 0)
        if prev_amt > 0:
            change = (amt - prev_amt) / prev_amt
            if change > 0.5:
                anomalies.append(f"SPIKE_{cat.upper()}")
            elif change < -0.4:
                anomalies.append(f"DROP_{cat.upper()}")

    return {
        "expense_change": delta_expenses,
        "ratio_change": delta_ratio,
        "trend": trend,
        "anomalies": anomalies,
    }

# =========================================================
# RULE ENGINE (FINAL DECISION)
# =========================================================
def interpret(features: Dict, comparison: Optional[Dict]) -> Dict:
    severity = "low"
    confidence = 0.5

    if "NEGATIVE_SAVINGS" in features["risk_flags"]:
        severity = "high"
        confidence = 0.9
    elif "HIGH_EXPENSE_RATIO" in features["risk_flags"]:
        severity = "medium"
        confidence = 0.75

    if comparison:
        if comparison["trend"] == "worsening":
            confidence = min(confidence + 0.1, 1.0)
        if comparison["trend"] == "improving":
            confidence = max(confidence - 0.1, 0.4)

    headline = "Spending looks balanced."
    action = "Maintain current budget."
    note = "No immediate financial risk detected."

    if severity == "medium":
        headline = "Spending trend needs attention."
        action = "Review discretionary expenses."
        note = "Expenses are increasing faster than income."

    if severity == "high":
        headline = "Expenses exceed sustainable limits."
        action = "Reduce spending immediately."
        note = "Negative savings increase financial risk."

    return {
        "headline": headline,
        "action": action,
        "note": note,
        "severity": severity,
        "confidence": round(confidence, 2),
    }

# =========================================================
# LLM EXPLANATION NODE
# =========================================================
def explanation_agent(state: ExplainState):
    f = state["features"]

    prompt = f"""
    Explain the financial situation briefly (2 sentences max).
    No advice, no commands.

    Expense ratio: {f['expense_ratio']}
    Savings: {f['savings']}
    Dominant category: {f['dominant_category']}
    Risk flags: {f['risk_flags']}
    Trend: {f.get('trend', 'N/A')}
    Anomalies: {f.get('anomalies', [])}
    """

    result = llm.invoke([HumanMessage(content=prompt)])
    return {"explanation": result.content.strip()}

# =========================================================
# LANGGRAPH
# =========================================================
graph = StateGraph(ExplainState)
graph.add_node("explain", explanation_agent)
graph.set_entry_point("explain")
graph.add_edge("explain", END)
explain_graph = graph.compile()

# =========================================================
# API
# =========================================================
@app.post("/reason")
def reason(payload: FinancialHistory):
    current_features = extract_features(payload.current)

    comparison = None
    if payload.previous:
        previous_features = extract_features(payload.previous)
        comparison = compare_months(current_features, previous_features)
        current_features.update(comparison)

    decision = interpret(current_features, comparison)

    explanation = explain_graph.invoke(
        {"features": current_features}
    )["explanation"]

    return {
        "summary": {
            **decision,
            "trend": comparison["trend"] if comparison else "N/A",
            "explanation": explanation
        },
        "debug": current_features
    }

# =========================================================
# HEALTH
# =========================================================
@app.get("/")
def health():
    return {"status": "backend running"}
