from flask import Flask
from flask import jsonify
import get_from_db as gdb
from flask import request, render_template
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# Get information about the race
@app.route("/raceinfo", methods=['GET'])
def get_race_info():
  race_info = gdb.get_race_info()
  return jsonify(race_info)


# Get elapsed time, section, section time, given the car number and lap num string
@app.route("/sectiontiminginfo", methods=['GET'])
def get_section_timing_details():
  car_num = int(request.args.get('car_num'))
  lap_num_str = request.args.get('lap_num')
  sec_timing_results = gdb.get_section_details(car_num, lap_num_str) 

  #resp = flask.Response(jsonify(sec_timing_results))
  #resp.headers['Access-Control-Allow-Origin'] = '*'

  #return resp
  return jsonify(sec_timing_results)


# Get details (length, start end label, name) for all the sections in the track
@app.route("/sectioninfo", methods=['GET'])
def get_section_info():
  section_details = gdb.get_section_list_info()["section_arr"]
  return jsonify(section_details)


# Get list of cars who participated in the race
@app.route("/carslist", methods=["GET"])
def get_cars_list():
  cars_list = gdb.get_cars_list()
  return jsonify(cars_list)


# Get the valid laps for the car
@app.route("/getvalidlaps", methods=["GET"])
def get_valid_laps():
  car_num = int(request.args.get('car_num'))
  valid_laps = gdb.get_valid_laps(car_num)
  return jsonify(valid_laps)


# Get the overall rank of the driver, given the car number
@app.route("/getoverallrank", methods=["GET"])
def get_overall_rank():
  car_num = int(request.args.get('car_num'))
  rank = gdb.get_overall_rank(car_num)
  return jsonify(rank)


# Get time and distance from start of lap, given the car number
@app.route("/gettelemetrytiming", methods=["GET"])
def get_telemetry_timing_details():
  car_num = int(request.args.get('car_num'))
  timing_details = gdb.get_timing_details(car_num)

  return jsonify(timing_details)


# Get weather data
@app.route("/weather_data", methods=["GET"])
def get_weather_data():
  weather_data = gdb.get_weather_data()
  
  return jsonify(weather_data)


# Get Entry Info
@app.route("/getentryinfo", methods=["GET"])
def get_entry_info():
  car_num = int(request.args.get('car_num'))
  entry_info = gdb.get_entry_info(car_num)
  
  return jsonify(entry_info)

# Get Section Timing for All Laps
@app.route("/getalllaptiminginfo", methods=["GET"])
def get_all_lap_timing_info():
  car_num = int(request.args.get('car_num'))
  # Get all laps
  laps = gdb.get_laps_list_for_car(car_num)
  #laps = gdb.get_valid_laps(car_num)  

  lap_timings = []
  for lap in laps:
    sec_timing_results = gdb.get_section_details(car_num, lap)
    lap_timing = {}
    lap_timing["lap_num"] = lap
    lap_timing["num_of_sections"] = len(sec_timing_results)
    lap_timing["section_timing"] = sec_timing_results
    lap_timings.append(lap_timing)

  return jsonify(lap_timings)


# Get Section Timing Selected Lap Range
@app.route("/gettiminginfoinlaprange", methods=["GET"])
def get_lap_timing_info_in_range():
  car_num = int(request.args.get('car_num'))
  lap_beg = int(request.args.get('lap_beg'))
  lap_end = int(request.args.get('lap_end'))

  lap_timings = []
  for lap in range(lap_beg, (lap_end + 1)):
    sec_timing_results = gdb.get_section_details(car_num, str(lap))
    lap_timing = {}
    lap_timing["lap_num"] = lap
    lap_timing["num_of_sections"] = len(sec_timing_results)
    lap_timing["section_timing"] = sec_timing_results
    lap_timings.append(lap_timing)

  return jsonify(lap_timings)


# Get Car Lap Section Statistics
@app.route("/getstatistics", methods=["GET"])
def get_car_lap_section_statistics():
  stat = gdb.get_car_lap_section_statistics()

  return jsonify(stat)

# Get Rank Information
@app.route("/getrankinfo", methods=["GET"])
def get_rank_info():
  car_num = int(request.args.get('car_num'))
  lap_beg = int(request.args.get('lap_beg'))
  lap_end = int(request.args.get('lap_end'))
 
  ranks = [] 
  for i in range(lap_beg, lap_end+1):
    rank = gdb.get_rank_info(car_num, str(i))
    rank_info = {}
    rank_info['lap_num'] = i
    rank_info['rank'] = rank['lap_rank']
    rank_info['driver_name'] = rank['driver_name']
    ranks.append(rank_info)

  return jsonify(ranks)

# Get Car Info with Lap Times
@app.route("/getcarinfo", methods=["GET"])
def get_carinfo():
  car_num = int(request.args.get('car_num'))
  
  return jsonify(gdb.get_car_info(car_num))

# Index Page
@app.route("/", methods=["GET"])
def display_index():
  return render_template('indexstyled.html')


if __name__ == '__main__':
  app.run(host='0.0.0.0') #, ssl_context='adhoc')
