import pandas as pd
import csv
import pymongo
from pymongo import MongoClient
import datetime
import pprint

client = MongoClient('localhost', 27017)

db = client.indycar_database

announcements = db.announcement_info
# $A - Announcement Information

filename = 'small_file_A'
col_names = ["nan", "command", "type", "seq_num", "preamble", "msg_num", "action", "priority", "timestamp", "text"]

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
   "command" : row["command"],
   "type" : row["type"],
   "seq_num" : row["seq_num"],
   "preamble" : row["preamble"],
   "announcement_data" : {
      "msg_num" : row["msg_num"],
      "action": row["action"],
      "priority": row["priority"],
      "timestamp": row["timestamp"],
      "text": row["text"]
    }
} for _, row in df.iterrows()]

announcements.insert(documents)

# $C - Completed lap Results Information

filename = 'small_file_C'
col_names = ["nan", "command", "type", "seq_num", "preamble", "rank", "car_num", "unique_identification", 
             "completed_laps", "elapsed_time", "last_laptime", "lap_status", "fastest_laptime", "fastest_lap", 
             "time_behind_leader", "laps_behind_leader", "time_behind_prec", "laps_behind_prec", "overall_rank", 
             "overall_best_lap_time", "current_status", "track_status",    "pit_stop_count", "last_pitted_lap", 
             "start_pos", "laps_led"]

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
   "command" : row["command"],
   "type" : row["type"],
   "seq_num" : row["seq_num"],
   "preamble" : row["preamble"],
   "completed_lap_results_data" : {
      "rank": row["rank"],
      "car_num": row["car_num"],
      "unique_identification": row["unique_identification"],
      "completed_laps": row["completed_laps"],
      "elapsed_time": row["elapsed_time"],
      "last_laptime": row["last_laptime"],
      "lap_status": row["lap_status"],
      "fastest_laptime": row["fastest_laptime"],
      "fastest_lap": row["fastest_lap"],
      "time_behind_leader": row["time_behind_leader"],
      "laps_behind_leader": row["laps_behind_leader"],
      "time_behind_prec": row["time_behind_prec"],
      "laps_behind_prec": row["laps_behind_prec"],
      "overall_rank": row["overall_rank"],
      "overall_best_lap_time": row["overall_best_lap_time"],
      "current_status": row["current_status"],
      "track_status": row["track_status"],
      "pit_stop_count": row["pit_stop_count"],
      "last_pitted_lap": row["last_pitted_lap"],
      "start_pos": row["start_pos"],
      "laps_led": row["laps_led"]
    }
} for _, row in df.iterrows()]

completed_lap_results = db.completed_lap_results_info
completed_lap_results.insert(documents)

print("Finished inserting completed lap results")

# $D - Invalidated Lap Information

filename = 'small_file_D'
col_names = ["nan", "command", "type", "seq_num", "preamble", "result_id",
             "lap_type", "car_num", "lap", "elapsed_time"]

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
   "command" : row["command"],
   "type" : row["type"],
   "seq_num" : row["seq_num"],
   "preamble" : row["preamble"],
   "invalidated_lap_info_data" : {
      "result_id": row["result_id"],
      "lap_type": row["lap_type"],
      "car_num": row["car_num"],
      "lap": row["lap"],
      "elapsed_time": row["elapsed_time"]
    }
} for _, row in df.iterrows()]

invalidated_lap = db.invalidated_lap_info
invalidated_lap.insert(documents)

print("Finished inserting invalidated lap info")


# $E - Entry Information

filename = 'small_file_E'
col_names = ["nan", "command", "type", "seq_num", "preamble", "car_num",
             "unique_identifier", "driver_name", "start_pos", "field_count",
             "class", "driver_id", "transponder_control_num", "equipment",
             "license", "team", "team_id", "engine", "entrant_id", "home_town", 
             "points_eligible", "competitor_identifier"]

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
   "command" : row["command"],
   "type" : row["type"],
   "seq_num" : row["seq_num"],
   "preamble" : row["preamble"],
   "entry_info_data" : {
      "car_num": row["car_num"],
      "unique_identifier": row["unique_identifier"],
      "driver_name": row["driver_name"],
      "start_pos": row["start_pos"],
      "field_count": row["field_count"],
      "class": row["class"],
      "driver_id": row["driver_id"],
      "transponder_control_num": row["transponder_control_num"],
      "equipment": row["equipment"],
      "license": row["license"],
      "team": row["team"],
      "team_id": row["team_id"],
      "engine": row["engine"],
      "entrant_id": row["entrant_id"],
      "points_eligible": row["points_eligible"],
      "home_town": row["home_town"],
      "competitor_identifier": row["competitor_identifier"]
    }
} for _, row in df.iterrows()]

entry = db.entry_info
entry.insert(documents)

print("Finished inserting entry info")

# $F - Flag Information

filename = 'small_file_F'
col_names = ["nan", "command", "type", "seq_num", "preamble", "track_status",
             "lap_num", "green_time", "green_laps", "yellow_time", "yellow_laps",
             "red_time", "num_of_yellows", "current_leader",
             "num_of_lead_changes", "avg_race_speed"]

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
   "command" : row["command"],
   "type" : row["type"],
   "seq_num" : row["seq_num"],
   "preamble" : row["preamble"],
   "flag_info_data" : {
      "track_status": row["track_status"],
      "lap_num": row["lap_num"],
      "green_time": row["green_time"],
      "green_laps": row["green_laps"],
      "yellow_time": row["yellow_time"],
      "yellow_laps": row["yellow_laps"],
      "red_time": row["red_time"],
      "num_of_yellows": row["num_of_yellows"],
      "current_leader": row["current_leader"],
      "num_of_lead_changes": row["num_of_lead_changes"],
      "avg_race_speed": row["avg_race_speed"]
    }
} for _, row in df.iterrows()]

flag = db.flag_info
flag.insert(documents)

print("Finished inserting flag info")


# $G - Car Display Pit Stop Timer Information

filename = 'small_file_G_replaced'

def create_nested_json(data):
    elements = data.split()
    laps = elements[::2]
    pit_stop_times = elements[1::2]

    pit_stop_data = [{"lap": x, "pit_stop_time": y} for x, y in zip(laps,
                                                                    pit_stop_times)]
    return pit_stop_data

rows = []
csv_header = ['command', 'type', 'seq_num', 'preamble', 'car_num',
              'num_of_pit_stops', 'pit_stop_data']
frame_header = ['command', 'type', 'seq_num', 'preamble', 'car_num',
                'num_of_pit_stops', 'pit_stop_data']

with open(filename, 'r') as f_input:
    for row in csv.DictReader(f_input, delimiter=',',
                              fieldnames=csv_header[:-1], restkey=csv_header[-1],
                              skipinitialspace=True):
        try:
            rows.append([row['command'], row['type'], row['seq_num'],
                         row['preamble'], row['car_num'],
                         row['num_of_pit_stops'],
                         ' '.join(row['pit_stop_data'])])
        except KeyError as e:
            rows.append([row['command'], row['type'], row['seq_num'],
                         row['preamble'], row['car_num'],
                         row['num_of_pit_stops'], ' '])

df = pd.DataFrame(rows, columns=frame_header)

df['pit_stop_data'] = df['pit_stop_data'].apply(lambda x: create_nested_json(x))

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "pit_stop_timer_info" : {
     "car_num": row["car_num"],
     "num_of_pit_stops": row["num_of_pit_stops"],
     "pit_stop_data": row["pit_stop_data"]
   }
} for _, row in df.iterrows()]

pit_stop = db.pit_stop_info
pit_stop.insert(documents)

print("Finished inserting pit_stop info")


# $H - Heartbeat information

filename = 'small_file_H'
col_names = ["nan", "command", "type", "seq_num", "preamble", "track_status",
             "date", "elapsed_time", "laps_to_go", "time_to_go"]

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "heartbeat_data" : {
     "track_status": row["track_status"],
     "date": row["date"],
     "elapsed_time": row["elapsed_time"],
     "laps_to_go": row["laps_to_go"],
     "time_to_go": row["time_to_go"]
   }
} for _, row in df.iterrows()]

h_heartbeat = db.h_heartbeat_info
h_heartbeat.insert(documents)

print("Finished inserting h heartbeat info")


# $I - Invalidated Lap Information

col_names = ["nan", "command", "type", "seq_num", "preamble", "car_num",
             "unique_identifier", "elapsed_time"]

filename = 'small_file_I'

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "invalidated_lap_data" : {
     "car_num": row["car_num"],
     "unique_identifier": row["unique_identifier"],
     "elapsed_time": row["elapsed_time"]
   }
} for _, row in df.iterrows()]

i_invalidated_lap = db.i_invalidated_lap_info
i_invalidated_lap.insert(documents)

print("Finished inserting i invalidated lap info")


# $L - Line Crossing Information

col_names = ["nan", "command", "type", "seq_num", "preamble", "car_num",
             "unique_identifier", "time_line", "source", "elapsed_time", 
             "track_status", "crossing_status"]

filename = 'small_file_L'

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "line_crossing_info" : {
     "car_num": row["car_num"],
     "unique_identifier": row["unique_identifier"],
     "time_line": row["time_line"],
     "source": row["source"],
     "elapsed_time": row["elapsed_time"],
     "track_status": row["track_status"],
     "crossing_status": row["crossing_status"]
   }
} for _, row in df.iterrows()]

line_crossing = db.line_crossing_info
line_crossing.insert(documents)

print("Finished inserting line crossing info")


# $M - Messages

col_names = ["nan", "command", "type", "seq_num", "preamble", "timestamp",
             "priority", "message"]

filename = 'small_file_M'

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "message_data" : {
     "timestamp": row["timestamp"],
     "priority": row["priority"],
     "message": row["message"]
   }
} for _, row in df.iterrows()]

messages = db.messages_info
messages.insert(documents)

print("Finished inserting messages info")


# $N - New Leader Information

col_names = ["nan", "command", "type", "seq_num", "preamble", "car_num",
             "unique_identifier", "lap_num", "elapsed_time",
             "lead_change_index"]

filename = 'small_file_N'

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "new_leader_info" : {
     "car_num": row["car_num"],
     "unique_identifier": row["unique_identifier"],
     "lap_num": row["lap_num"],
     "elapsed_time": row["elapsed_time"],
     "lead_change_index": row["lead_change_index"]
   }
} for _, row in df.iterrows()]

new_leader = db.new_leader_info
new_leader.insert(documents)

print("Finished inserting new leader info")


# $O - Overall Results

col_names = ["nan", "command", "type", "seq_num", "preamble", "result_id",
             "deleted", "marker", "rank", "overall_rank", "start_pos", 
             "best_lap_time", "best_lap", "last_lap_time", "laps", 
             "total_time", "last_warm_up_qual_time", "lap1_qual_time",
             "lap2_qual_time", "lap3_qual_time", "lap4_qual_time",
             "total_qual_time", "status", "diff", "gap", "on_track", 
             "pit_stops", "last_pit_lap", "since_pit_laps", "flag_status", 
             "no", "first_name", "last_name", "class", "equipment", 
             "license", "team", "total_entrant_points", 
             "total_driver_points", "comment", "total_chasis_points", 
             "total_engine_points", "off_track", "non_tow_rank", 
             "non_tow_lap", "non_tow_time", "on_track_passes", 
             "on_track_times_passed", "overtake_remaining", "tire_type", 
             "driver_id", "qualifying_speed"]

filename = 'small_file_O'

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "overall_results" : {
    "result_id": row["result_id"],
    "deleted": row["deleted"],
    "marker": row["marker"],
    "rank": row["rank"],
    "overall_rank": row["overall_rank"],
    "start_pos": row["start_pos"],
    "best_lap_time": row["best_lap_time"],
    "best_lap": row["best_lap"],
    "last_lap_time": row["last_lap_time"],
    "laps": row["laps"],
    "total_time": row["total_time"],
    "last_warm_up_qual_time": row["last_warm_up_qual_time"],
    "lap1_qual_time": row["lap1_qual_time"],
    "lap2_qual_time": row["lap2_qual_time"],
    "lap3_qual_time": row["lap3_qual_time"],
    "lap4_qual_time": row["lap4_qual_time"],
    "total_qual_time": row["total_qual_time"],
    "status": row["status"],
    "diff": row["diff"],
    "gap": row["gap"],
    "on_track": row["on_track"],
    "pit_stops": row["pit_stops"],
    "last_pit_lap": row["last_pit_lap"],
    "since_pit_laps": row["since_pit_laps"],
    "flag_status": row["flag_status"],
    "no": row["no"],
    "first_name": row["first_name"],
    "last_name": row["last_name"],
    "class": row["class"],
    "equipment": row["equipment"],
    "license": row["license"],
    "team": row["team"],
    "total_entrant_points": row["total_entrant_points"],
    "total_driver_points": row["total_driver_points"],
    "comment": row["comment"],
    "total_chasis_points": row["total_chasis_points"],
    "total_engine_points": row["total_engine_points"],
    "off_track": row["off_track"],
    "non_tow_rank": row["non_tow_rank"],
    "non_tow_lap": row["non_tow_lap"],
    "non_tow_time": row["non_tow_time"],
    "on_track_passes": row["on_track_passes"],
    "on_track_times_passed": row["on_track_times_passed"],
    "overtake_remaining": row["overtake_remaining"],
    "tire_type": row["tire_type"],
    "driver_id": row["driver_id"],
    "qualifying_speed": row["qualifying_speed"]
  }
} for _, row in df.iterrows()]

overall = db.overall_results
overall.insert(documents)

print("Finished inserting overall results")


# $R - Run Information

col_names = ["nan", "command", "type", "seq_num", "preamble", "event_name",
             "event_round", "run_name", "run_type", "start_date"]

filename = 'small_file_R'

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "run_info" : {
    "event_name": row["event_name"],
    "event_round": row["event_round"],
    "run_name": row["run_name"],
    "run_type": row["run_type"],
    "start_date": row["start_date"]
  }
} for _, row in df.iterrows()]

run = db.run_info
run.insert(documents)

print("Finished inserting run info")


# $S - Completed Section Results

col_names = ["nan", "command", "type", "seq_num", "preamble", "car_num",
             "unique_identifier", "section_identifier", "elapsed_time",
             "last_section_time", "last_lap"]

filename = 'small_file_S'

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "completed_section_data" : {
    "car_num": row["car_num"],
    "unique_identifier": row["unique_identifier"],
    "section_identifier": row["section_identifier"],
    "elapsed_time": row["elapsed_time"],
    "last_section_time": row["last_section_time"],
    "last_lap": row["last_lap"]
  }
} for _, row in df.iterrows()]

completed_section = db.completed_section_info
completed_section.insert(documents)

print("Finished inserting completed section info")


# $T - Track Information

def create_nested_json_T(data):
    elements = data.split()
    section_names = elements[::4]
    section_lengths = elements[1::4]
    section_start_labels = elements[2::4]
    section_end_labels = elements[3::4]

    section_data = [{"section_name": w, "section_length": x,
                      "section_start_label": y, "section_end_label": z} for
                      w, x, y, z in zip( section_names, section_lengths,
                                         section_start_labels, section_end_labels)]
    return section_data

file_name = 'small_file_T_replaced'
rows = []
csv_header = ['command', 'type', 'seq_num', 'preamble', 'track_name',
              'track_type', 'track_length', 'num_of_sections', 'section_data']
frame_header = ['command', 'type', 'seq_num', 'preamble', 'track_name',
                'track_type', 'track_length', 'num_of_sections', 'section_data']

with open(file_name, 'r') as f_input:
    for row in csv.DictReader(f_input, delimiter=',',
                              fieldnames=csv_header[:-1], restkey=csv_header[-1],
                              skipinitialspace=True):
        try:
            rows.append([row['command'], row['type'], row['seq_num'],
                         row['preamble'], row['track_name'],
                         row['track_type'], row['track_length'],
                         row['num_of_sections'], ' '.join(row['section_data'])])
        except KeyError as e:
            rows.append([row['command'], row['type'], row['seq_num'],
                         row['preamble'], row['track_name'],
                         row['track_type'], row['track_length'],
                         row['num_of_sections'], ' '])

df = pd.DataFrame(rows, columns=frame_header)

df['section_data'] = df['section_data'].apply(lambda x: create_nested_json_T(x))


documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "track_info" : {
    "track_name" : row["track_name"],
    "track_type" : row["track_type"],
    "track_length" : row["track_length"],
    "num_of_sections" : row["num_of_sections"],
    "section_data": row["section_data"]
  }
} for _, row in df.iterrows()]

track = db.track_info
track.insert(documents)

print("Finished inserting track info")


# $U - Track Information - Time Lines

def create_nested_json_U(data):
    elements = data.split()
    timeline_names = elements[::3]
    types = elements[1::3]
    distances = elements[2::3]

    timeline_data = [{"timeline_name": x, "type": y, "distance_from_sf": z} for
                      x, y, z in zip(timeline_names, types, distances)]
    return timeline_data

file_name = 'small_file_U_replaced'
rows = []
csv_header = ['command', 'type', 'seq_num', 'preamble', 'track_name',
              'track_type', 'track_length', 'num_of_timelines', 'timeline_data']
frame_header = ['command', 'type', 'seq_num', 'preamble', 'track_name',
                'track_type', 'track_length', 'num_of_timelines',
                'timeline_data']

with open(file_name, 'r') as f_input:
    for row in csv.DictReader(f_input, delimiter=',',
                              fieldnames=csv_header[:-1], restkey=csv_header[-1],
                              skipinitialspace=True):
        try:
            rows.append([row['command'], row['type'], row['seq_num'],
                         row['preamble'], row['track_name'],
                         row['track_type'], row['track_length'],
                         row['num_of_timelines'], ' '.join(row['timeline_data'])])
        except KeyError as e:
            rows.append([row['command'], row['type'], row['seq_num'],
                         row['preamble'], row['track_name'],
                         row['track_type'], row['track_length'],
                         row['num_of_timelines'], ' '])

df = pd.DataFrame(rows, columns=frame_header)

df['timeline_data'] = df['timeline_data'].apply(lambda x:
                                                create_nested_json_U(x))

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "timeline_info" : {
    "track_name" : row["track_name"],
    "track_type" : row["track_type"],
    "track_length" : row["track_length"],
    "num_of_timelines" : row["num_of_timelines"],
    "timeline_data": row["timeline_data"]
  }
} for _, row in df.iterrows()]

track_timeline = db.track_timeline_info
track_timeline.insert(documents)

print("Finished inserting track timeline info")



# $V - Announcement Information

col_names = ["nan", "command", "type", "seq_num", "preamble", "major",
             "minor", "info"]

filename = 'small_file_V'

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "announcement_info" : {
    "major": row["major"],
    "minor": row["minor"],
    "info": row["info"]
  }
} for _, row in df.iterrows()]

v_announcement = db.v_announcement_info
v_announcement.insert(documents)

print("Finished inserting v announcement info")


# $W - Weather Data


def create_nested_json_W(data):
    elements = data.split()
    timeline_names = elements[::2]
    track_temperatures = elements[1::2]

    loop_temp_data = [{"timeline_name": x, "track_temperature": y} for
                      x, y in zip(timeline_names, track_temperatures)]
    return loop_temp_data

file_name = 'small_file_W_replaced'
rows = []
csv_header = ['command', 'type', 'seq_num', 'preamble', 'time_of_day',
              'ambient_temp', 'relative_humidity',
              'barometric_pressure', 'wind_speed', 'wind_direction',
              'num_of_loop_temps', 'loop_temp_data']
frame_header = ['command', 'type', 'seq_num', 'preamble', 'time_of_day',
              'ambient_temp', 'relative_humidity',
              'barometric_pressure', 'wind_speed', 'wind_direction',
              'num_of_loop_temps', 'loop_temp_data']

with open(file_name, 'r') as f_input:
    for row in csv.DictReader(f_input, delimiter=',',
                              fieldnames=csv_header[:-1], restkey=csv_header[-1],
                              skipinitialspace=True):
        try:
            rows.append([row['command'], row['type'], row['seq_num'],
                         row['preamble'], row['time_of_day'],
                         row['ambient_temp'], row['relative_humidity'],
                         row['barometric_pressure'], row['wind_speed'],
                         row['wind_direction'], row['num_of_loop_temps'],
                         ' '.join(row['loop_temp_data'])])
        except KeyError as e:
            rows.append([row['command'], row['type'], row['seq_num'],
                         row['preamble'], row['time_of_day'],
                         row['ambient_temp'], row['relative_humidity'],
                         row['barometric_pressure'], row['wind_speed'],
                         row['wind_direction'], row['num_of_loop_temps'], ' '])

df = pd.DataFrame(rows, columns=frame_header)

df['loop_temp_data'] = df['loop_temp_data'].apply(lambda x:
                                                create_nested_json_W(x))

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "weather_data" : {
    "time_of_day" : row["time_of_day"],
    "ambient_temp" : row["ambient_temp"],
    "relative_humidity" : row["relative_humidity"],
    "barometric_pressure" : row["barometric_pressure"],
    "wind_direction": row['wind_direction'],
    "num_of_loop_temps": row['num_of_loop_temps'],
    "loop_temp_data": row["loop_temp_data"]
  }
} for _, row in df.iterrows()]

weather = db.weather_data
weather.insert(documents)

print("Finished inserting weather info")


# $X - Heart Beat Data

col_names = ["nan", "command", "type", "seq_num", "preamble", "event_name",
             "track_name", "track_length", "track_type", "green_time", 
             "yellow_time", "red_time", "current_flag", 
             "time_of_most_recent_flag", "overall_time_to_go", "total_laps", 
             "date", "elapsed_time", "session_start_time"]

filename = 'small_file_X'

df = pd.read_csv(filename , encoding='utf-8', header=None, sep='[\u0024|\u00A6]', engine='python', names = col_names)

del df["nan"] # remove NaN column
df = df.drop_duplicates(keep='last') # remove duplicate rows

documents = [{
  "command" : row["command"],
  "type" : row["type"],
  "seq_num" : row["seq_num"],
  "preamble" : row["preamble"],
  "heartbeat_info" : {
    "event_name": row["event_name"],
    "track_name": row["track_name"],
    "track_length": row["track_length"],
    "track_type": row["track_type"],
    "green_time": row["green_time"],
    "yellow_time": row["yellow_time"],
    "red_time": row["red_time"],
    "current_flag": row["current_flag"],
    "time_of_most_recent_flag": row["time_of_most_recent_flag"],
    "overall_time_to_go": row["overall_time_to_go"],
    "total_laps": row["total_laps"],
    "date": row["date"],
    "elapsed_time": row["elapsed_time"],
    "session_start_time": row["session_start_time"]
  }
} for _, row in df.iterrows()]

x_heartbeat = db.x_heartbeat_info
x_heartbeat.insert(documents)

print("Finished inserting x heart beat info")
