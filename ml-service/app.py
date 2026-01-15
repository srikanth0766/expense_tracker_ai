from fastapi import FastAPI
from pydantic import BaseModel
import pickle

app = FastAPI()

# Load trained model & vectorizer
model = pickle.load(open("model.pkl", "rb"))
vectorizer = pickle.load(open("vectorizer.pkl", "rb"))

class Expense(BaseModel):
    description: str

@app.post("/predict")
def predict_category(expense: Expense):
    vector = vectorizer.transform([expense.description])
    category = model.predict(vector)[0]
    return {"category": category}

