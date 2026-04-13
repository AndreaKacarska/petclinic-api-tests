import streamlit as st
import pandas as pd

df = pd.read_csv("emissions.csv")

st.title("CodeCarbon Results")
st.line_chart(df.set_index("timestamp")[["emissions", "energy_consumed"]])

# streamlit run dashboard.py