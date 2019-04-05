import os
import subprocess

for cars in range(1, 34):
    print("Starting run for ", cars)
    args = ["java", "-jar", "target/web-socket-1.0-SNAPSHOT-jar-with-dependencies.jar",
            "/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log", str(cars)]
    subprocess.run(args)
    print("Ended run for ", cars)
