import pymongo
from pymongo import MongoClient
import pprint
from bson.json_util import loads
from datetime import datetime, date, time, timedelta
import pytz

client = MongoClient('localhost', 27017)
global db
db = client.indycar_database


def get_completed_lap_results(car_num):
  completed_lap_results = db.completed_lap_results_info
  cursor = completed_lap_results.find({"completed_lap_results_data.car_num": car_num})
  completed_lap_results_arr = []

  for doc in cursor:
    completed_lap_results_arr.append(doc)

  return completed_lap_results_arr


def get_race_info():
  run_info_col = db.run_info
  run_info_record = run_info_col.find_one() # firs record is New (N), the rest is the Repeat (R) of it
  run_info = run_info_record.get("run_info")
  start_date = run_info.get("start_date")
  secs = int(start_date, 16)

  race_info = {}
  race_info["start_date"] = datetime.fromtimestamp(secs).strftime('%Y-%m-%d %H:%M:%S') # uses local timezone
  race_info["event_name"] = run_info.get("event_name")
  race_info["run_name"] = run_info.get("run_name")

  return race_info


def get_time_string(time_in_ten_thousandth_secs):
  num_of_secs = int(time_in_ten_thousandth_secs, 16) / 10000
  rem = int(time_in_ten_thousandth_secs, 16) % 10000
  m, s = divmod(num_of_secs, 60)
  h, m = divmod(m, 60)
  time_str = ("%d:%02d:%02d.%04d" % (h, m, s, rem))
 
  return time_str


def get_section_details(car_num, lap_num_str):
  completed_section_info = db.completed_section_info
  cursor = completed_section_info.find({ "$and" : [{"completed_section_data.car_num": car_num}, {"completed_section_data.last_lap": lap_num_str}]})
  completed_sec_results = [] # for lap and car num

  for doc in cursor:
    section_data = doc.get("completed_section_data")
    elapsed_time = section_data.get("elapsed_time")
    elapsed_time_str = get_time_string(elapsed_time)
    last_section_time = section_data.get("last_section_time")
    last_section_t_str = get_time_string(last_section_time)
    section_id = section_data.get("section_identifier")  

    section_info = {}
    section_info["section_id"] = section_id
    section_info["elapsed_time"] = elapsed_time_str
    section_info["last_section_time"] = last_section_t_str

    completed_sec_results.append(section_info)

  return completed_sec_results

 
def get_section_list_info():
  track_info_coll = db.track_info
  track_info_record = track_info_coll.find_one()
  track_info = track_info_record.get("track_info")
  sections_arr = track_info.get("section_data")
  track_name = track_info.get("track_name")
  track_length = track_info.get("track_length")
  num_of_sections = track_info.get("num_of_sections")

  section_info = {}
  section_info["track_name"] = track_name
  section_info["track_length"] = track_length
  section_info["num_of_sections"] = num_of_sections
  section_info["section_arr"] = sections_arr

  return section_info


def get_cars_list():
  entry_coll = db.entry_info

  cursor = entry_coll.distinct("entry_info_data.car_num")
  cars_list = []
  for doc in cursor:
    cars_list.append(doc)

  return cars_list


def get_laps_list_for_car(car_num):
  completed_lap_coll = db.completed_lap_results_info
  cursor = completed_lap_coll.distinct("completed_lap_results_data.completed_laps", {"completed_lap_results_data.car_num": car_num})
  laps_list = []

  for doc in cursor:
    laps_list.append(doc)

  return laps_list


def get_invalidated_laps_for_car(car_num):
  laps = get_laps_list_for_car(car_num)

  invalidated_lap_coll = db.invalidated_lap_info
  cursor = invalidated_lap_coll.distinct("invalidated_lap_info_data.lap", {"invalidated_lap_info_data.car_num": car_num})
  invalidated_laps_list = []

  for doc in cursor:
    invalidated_laps_list.append(doc)
   
  return invalidated_laps_list


def get_valid_laps(car_num):
  laps = get_laps_list_for_car(car_num)
  invalidated_laps = get_invalidated_laps_for_car(car_num)

  for x in invalidated_laps:
    for y in laps:
      if (str(x)==y):
        laps.remove(y)

  return laps


def get_driver_id_for_car(db, car_num):
  entry_coll = db.entry_info

  entry_for_car = entry_coll.find_one({"entry_info_data.car_num": car_num}) # since the record in irrelevant fields such as N,R,U or start_pos
  driver_id = entry_for_car.get("entry_info_data").get("driver_id")

  return driver_id


def get_overall_rank(car_num):
  driver_id = get_driver_id_for_car(db, car_num)

  overall_coll = db.overall_results
  cursor = overall_coll.distinct("overall_results.laps", {"overall_results.driver_id": driver_id})

  overall_laps = []
  for doc in cursor:
    overall_laps.append(int(doc, 16))
  
  max_lap_val = max(overall_laps)
  hex_val = hex(max_lap_val)[2:].upper()

  cursor = overall_coll.distinct("overall_results.overall_rank", {"overall_results.laps": hex_val})
  overall_ranks = []

  for doc in cursor:
    overall_ranks.append(int(doc, 16))

  rank = max(overall_ranks)

  return rank

def get_timing_details(car_num):
  telemetry_data_coll = db.telemetry_data
  cursor = telemetry_data_coll.find({"car_num": car_num})

  timing_details = []
  for doc in cursor:
    telem_data = {}
    telem_data["time_of_day"] = doc.get("time_of_day")
    telem_data["meters_since_start_of_lap"] = doc.get("distance_since_start_of_lap")

    timing_details.append(telem_data)

  return timing_details


def get_weather_data():
  weather_coll = db.weather_data
  cursor = weather_coll.find()
  weather_data_arr = []

  for doc in cursor:
    weather_info = {}
    weather_data = doc.get("weather_data")
    weather_info["time_of_day"] = get_time_string(weather_data.get("time_of_day")) # uses local timezone
    weather_info["ambient_temp"] = int(weather_data.get("ambient_temp"), 16)
    weather_info["relative_humidity"] = int(weather_data.get("relative_humidity"), 16)
    weather_info["barometric_pressure"] = int(weather_data.get("barometric_pressure"), 16)
   
    weather_data_arr.append(weather_info)

  return weather_data_arr


def get_entry_info(car_num):
  entry_coll = db.entry_info

  entry_record = entry_coll.find({"entry_info_data.car_num": car_num}, {'_id': False}).sort([("$natural", -1)]).limit(1)
  
  return entry_record[0]


def get_car_lap_section_statistics():
  cars_list = get_cars_list()

  cars_info_arr = []
  for car in cars_list:
    car_info = {}
    car_info["car_num"] = car
    laps = get_laps_list_for_car(car)
  
    lap_info_arr = []
    for lap in laps:
      sec_timing_results = get_section_details(car, lap)
      lap_info = {}
      lap_info["lap_num"] = lap
      lap_info["num_of_sections"] = len(sec_timing_results)
      lap_info_arr.append(lap_info)

    car_info["lap_and_section_info"] = lap_info_arr
    cars_info_arr.append(car_info)
  
  return cars_info_arr
