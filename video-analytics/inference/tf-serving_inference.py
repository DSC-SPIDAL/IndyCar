
# https://github.com/log0/video_streaming_with_flask_example

import numpy as np
import cv2
from flask import Flask, render_template, Response
import time
import cv2
import time
import core.utils as utils
import tensorflow as tf
from PIL import Image
from tensorflow_serving.apis import predict_pb2
from tensorflow_serving.apis import prediction_service_pb2_grpc
import threading
import grpc


channel = grpc.insecure_channel("localhost:8500")
stub = prediction_service_pb2_grpc.PredictionServiceStub(channel)
request = predict_pb2.PredictRequest()
request.model_spec.name = 'yolo_v3'


num_classes     = 20
input_size      = 416


app = Flask(__name__)



@app.route('/')
def index():
    return render_template('index.html')

def gen():
    for i in range(5):
        count = 0
        cap = cv2.VideoCapture("full_video.mp4")
        start_frame_number = 1000
        cap.set(cv2.CAP_PROP_POS_FRAMES, start_frame_number)
        while True:
            cap.set(cv2.CAP_PROP_POS_MSEC,(count*125))   
            ret, frame = cap.read()
            count += 1
            if not ret:
                break

            frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            image = Image.fromarray(frame)



            frame_size = frame.shape[:2]
            image_data = utils.image_preporcess(np.copy(frame), [input_size, input_size])
            image_data = image_data[np.newaxis, ...]
            image_data= image_data.astype(np.float32)
            prev_time = time.time()
            request.inputs['input'].CopyFrom(tf.contrib.util.make_tensor_proto(image_data, shape=[1,input_size,input_size, 3]))
            result_future = stub.Predict.future(request, 10.25)
            pred_sbbox = np.asarray(result_future.result().outputs['pred_sbbox'].float_val)
            pred_mbbox = np.asarray(result_future.result().outputs['pred_mbbox'].float_val)
            pred_lbbox = np.asarray(result_future.result().outputs['pred_lbbox'].float_val)
            
            
            
            pred_bbox = np.concatenate([np.reshape(pred_sbbox, (-1, 5 + num_classes)), 
                np.reshape(pred_mbbox, (-1, 5 + num_classes)),
                np.reshape(pred_lbbox, (-1, 5 + num_classes))], axis=0)

            bboxes = utils.postprocess_boxes(pred_bbox, frame_size, input_size, 0.3)
            bboxes = utils.nms(bboxes, 0.45, method='nms')
            image = utils.draw_bbox(frame, bboxes)

            curr_time = time.time()
            exec_time = curr_time - prev_time
            print(exec_time)           
            result = np.asarray(image)
            frame = cv2.cvtColor(result, cv2.COLOR_RGB2BGR)
            #result = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            #format should be bgr
            ret, jpeg = cv2.imencode('.jpg', frame)
            frame = jpeg.tobytes()
            yield (b'--frame\r\n'
                  b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n\r\n')

@app.route('/video_feed')
def video_feed():
    return Response(gen(),
                    mimetype='multipart/x-mixed-replace; boundary=frame')


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=61521)
