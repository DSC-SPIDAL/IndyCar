
# https://github.com/log0/video_streaming_with_flask_example

import os
import numpy as np
import cv2
from flask import Flask, render_template, Response
import time
import cv2
import core.utils as utils
import tensorflow as tf
from PIL import Image
import threading
import itertools


app = Flask(__name__)

os.environ["CUDA_VISIBLE_DEVICES"] = "7"

return_elements = ["input/input_data:0", "pred_sbbox/concat_2:0",
                   "pred_mbbox/concat_2:0", "pred_lbbox/concat_2:0"]
pb_file = "./feb2020_yolov3_coco.pb"
num_classes = 20
input_size = 416
graph = tf.Graph()
return_tensors = utils.read_pb_return_tensors(graph, pb_file, return_elements)

# , config=tf.ConfigProto(device_count={'GPU': 0}))
sess = tf.Session(graph=graph)

buffer_size = 10
byte_frame = []
for i in range(0, buffer_size):
    byte_frame.append(None)
current_buffer_index = 0
current_frame_index = -1

condtition = threading.Condition()


@app.route('/')
def index():
    return render_template('index.html')


def loop():

    global byte_frame
    global current_buffer_index
    global current_frame_index

    myrtmp_addr = "rtmp://j-093.juliet.futuresystems.org/live/indycar live=1"
    cap = cv2.VideoCapture(myrtmp_addr)

    infer_flag = True
    t1 = time.time()
    while True:
        # cap.set(cv2.CAP_PROP_POS_MSEC,(count*125))
        ret, frame = cap.read()
        if not ret:
            print('Input source error!')
            cap = cv2.VideoCapture(myrtmp_addr)
            continue

        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        image = Image.fromarray(frame)

        if infer_flag:

            frame_size = frame.shape[:2]
            image_data = utils.image_preporcess(
                np.copy(frame), [input_size, input_size])
            image_data = image_data[np.newaxis, ...]
            prev_time = time.time()
            pred_sbbox, pred_mbbox, pred_lbbox = sess.run([return_tensors[1], return_tensors[2], return_tensors[3]],
                                                          feed_dict={return_tensors[0]: image_data})
            #print('time:',time.time() - prev_time)
            pred_bbox = np.concatenate([np.reshape(pred_sbbox, (-1, 5 + num_classes)),
                                        np.reshape(
                                            pred_mbbox, (-1, 5 + num_classes)),
                                        np.reshape(pred_lbbox, (-1, 5 + num_classes))], axis=0)

            bboxes = utils.postprocess_boxes(
                pred_bbox, frame_size, input_size, 0.3)
            bboxes = utils.nms(bboxes, 0.45, method='nms')
        infer_flag = not infer_flag

        image = utils.draw_bbox(frame, bboxes)

        curr_time = time.time()
        exec_time = curr_time - prev_time
        # print(exec_time)
        result = np.asarray(image)
        frame = cv2.cvtColor(result, cv2.COLOR_RGB2BGR)
        #result = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        # format should be bgr
        ret, jpeg = cv2.imencode('.jpg', frame)
        frame = jpeg.tobytes()
        #with condtition:
        byte_frame[current_buffer_index] = (b'--frame\r\n'
                                                b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n\r\n')
        current_frame_index = current_frame_index+1
            #condtition.notifyAll()
        current_buffer_index = current_buffer_index+1
        if current_buffer_index >= buffer_size:
            current_buffer_index = 0

        # yield byte_frame[current_buffer_index-1]
        # print(time.time() - t1)
        # t1 = time.time()


def gen():
    frame_to_be_sent = current_frame_index
    last_frame_sent = -1
    print("Starting...", frame_to_be_sent)

    t1 = time.time()
    while True:
        t2 = time.time()
        while last_frame_sent is current_frame_index or frame_to_be_sent > current_frame_index or frame_to_be_sent is -1:
            time.sleep(0.001)
            if frame_to_be_sent is -1:
                frame_to_be_sent = current_frame_index

        #print(time.time()-t2)

        #print("Trying...", frame_to_be_sent, current_buffer_index)
        yield byte_frame[frame_to_be_sent % buffer_size]
        #print(time.time() - t1)
        t1 = time.time()
        last_frame_sent = frame_to_be_sent
        frame_to_be_sent = frame_to_be_sent+1


@app.route('/video_feed')
def video_feed():
    return Response(gen(),
                    mimetype='multipart/x-mixed-replace; boundary=frame')


if __name__ == '__main__':
    looper = threading.Thread(target=loop, args=())
    looper.start()
    app.run(host='0.0.0.0', port=61521)
