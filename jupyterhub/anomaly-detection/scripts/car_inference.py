import json
import sys
import paho.mqtt.client as mqtt
import time
import requests
import numpy as np
import logging



car_number = sys.argv[1]
serving_address = sys.argv[2]
mqtt_address = sys.argv[3].split(":")
input_topic = sys.argv[4]
output_topic = sys.argv[5]
series_len = int(sys.argv[6])


def on_message (client, userdata, msg):
    prediction(msg)

def on_connect (client, userdata, flags, rc):
    client.subscribe (input_topic)
    logging.info('Connected to topic ' + input_topic)

client = mqtt.Client ("testclient")
client.on_connect = on_connect
client.on_message = on_message
client.username_pw_set ("admin", "password") # TO DO; pass these as parameters
client.connect (mqtt_address[0], int(mqtt_address[1]), 60)

speed_data = []

headers = {"content-type": "application/json"}


def prediction(msg):
    raw_data = msg.payload.decode ("utf-8").split (',')
    speed = raw_data[0]
    rpm = raw_data[1]
    throttle = raw_data[2]
    record_counter = raw_data[3]
    lap_distance = raw_data[4]
    time_of_day = raw_data[5][8:] # we don't need the date. just get the time
    uuid = car_number + '_' + record_counter;
    recv_time = int(time.time() * 1000)

    speed_data.append(float(speed) / 240.0)
    if len(speed_data) > series_len:
        speed_data.pop(0)

    if len (speed_data) == series_len:
        input_data = np.reshape(speed_data, (1, series_len, 1)).astype(np.float32).tolist()
        input_json = json.dumps({"signature_name": "serving_default", "instances": input_data})

        json_response = requests.post (
            'http://'+ serving_address + '/v1/models/lstm_speed_model:predict', data=input_json,
            headers=headers)
        speed_prediction = json.loads(json_response.text)['predictions'][0][0]
        speed_anomaly = str(np.abs(speed_prediction - np.float32(speed)/240.0))
    else:
        speed_anomaly = 0

    send_message=  json.dumps({
        "carNumber":car_number,
        "throttle":throttle,
        "vehicleSpeedAnomaly": speed_anomaly,
        "vehicleSpeed": speed,
        "recvTime": recv_time,
        "throttleAnomaly": 0,
        "engineSpeedAnomaly": 0,
        "lapDistance":lap_distance,
        "UUID":uuid,
        "timeOfDay":time_of_day,
        "engineSpeed":rpm,
        "sendTime":int(time.time() * 1000) })
    client.publish(output_topic, payload=send_message, qos=0, retain=False)




    return client


logging.info('Listening the topic forever')
client.loop_forever ()
# call stream.loop_stop() in another cell to stop receiving a stream