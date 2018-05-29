import pandas as pd
import pymongo
from pymongo import MongoClient

client = MongoClient('localhost', 27017)
db = client.indycar_database

filename = 'small_file_P'

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python')
df = df.loc[:, 1:4]
df = df.drop_duplicates(keep='last') # remove duplicate rows
df.columns = ["command", "car_num", "time_of_day", "distance_since_start_of_lap"]

print(df)

documents = [{
  "command" : row["command"],
  "car_num" : row["car_num"],
  "time_of_day" : row["time_of_day"],
  "distance_since_start_of_lap" : row["distance_since_start_of_lap"]
} for _, row in df.iterrows()]

telemetry = db.telemetry_data
telemetry.insert(documents)

print("Finished inserting telemetry data (first three columns)")

