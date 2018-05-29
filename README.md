
# IndyCar

1) Break the large log file according to the different types of records

   ```python createsmallfiles.py```

2) Replace characters of files having different length fields in their records

   ```python replacecharinfile.py```

3) Add all records (except telemetry data) as documents to the database

   ```python addtodb.py```

4) Add first four columns in telemetry data to the database

   ```python addtelemetrydatatodb.py```

5) Start the web app using following command

   ```python web_app.py```

6) Get general info such as race info, weather info using following commands respectively

- curl http://localhost:5000/raceinfo
- curl http://localhost:5000/weather_data

7) Get cars list, valid laps, section details (length, name), how much time has it taken to complete the sections in the lap using the following commands respectively 

- curl http://localhost:5000/carslist
- curl http://localhost:5000/getvalidlaps?car_num=9
- curl http://localhost:5000/sectioninfo
- curl http://localhost:5000/sectiontiminginfo?car_num=9&lap_num=3

8) Get time of day and distance in meters from start of lap using the telemetry data using the following commands respectively

   curl http://localhost:5000/gettelemetrytiming?car_num=9

9) Get overall rank of car and driver/car details using the following commands respectively

- curl http://localhost:5000/getoverallrank?car_num=9
- curl http://localhost:5000/getentryinfo?car_num=9
