
# https://github.com/log0/video_streaming_with_flask_example

import numpy as np
import cv2
from flask import Flask, render_template, Response
import time

app = Flask(__name__)




@app.route('/')
def index():
    return render_template('index.html')

def gen():
    for i in range(5):
        cap = cv2.VideoCapture("short_video.mp4")
        start_frame_number = 0
        cap.set(cv2.CAP_PROP_POS_FRAMES, start_frame_number)
        while True:
            ret, frame = cap.read()
            if not ret:
                break
            ret, jpeg = cv2.imencode('.jpg', frame)
            frame = jpeg.tobytes()
            time.sleep(0.02)
            #frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n\r\n')

@app.route('/video_feed')
def video_feed():
    return Response(gen(),
                    mimetype='multipart/x-mixed-replace; boundary=frame')


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=61521)
