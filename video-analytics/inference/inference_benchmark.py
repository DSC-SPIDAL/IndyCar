import numpy as np
import cv2
import time
import cv2
import time
import core.utils as utils
import tensorflow as tf
from PIL import Image
import os


#os.environ["CUDA_VISIBLE_DEVICES"]="0"

return_elements = ["input/input_data:0", "pred_sbbox/concat_2:0", "pred_mbbox/concat_2:0", "pred_lbbox/concat_2:0"]
pb_file         = "./yolov3_coco.pb"
num_classes     = 80
input_size      = 416
graph           = tf.Graph()
return_tensors  = utils.read_pb_return_tensors(graph, pb_file, return_elements)

sess = tf.Session(graph=graph)#, config=tf.ConfigProto(device_count={'GPU': 1}))


def gen():
    for i in range(5):
        count = 0
        cap = cv2.VideoCapture("crash_video.mp4")
        start_frame_number = 0
        cap.set(cv2.CAP_PROP_POS_FRAMES, start_frame_number)
        while True:
            cap.set(cv2.CAP_PROP_POS_MSEC,(count*2))   
            ret, frame = cap.read()
            count += 1
            if not ret:
                break

            frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            image = Image.fromarray(frame)



            frame_size = frame.shape[:2]
            image_data = utils.image_preporcess(np.copy(frame), [input_size, input_size])
            image_data = image_data[np.newaxis, ...]
            prev_time = time.time()
            pred_sbbox, pred_mbbox, pred_lbbox = sess.run([return_tensors[1], return_tensors[2], return_tensors[3]],
                feed_dict={ return_tensors[0]: image_data})
                #print('time:',time.time() - prev_time)
            pred_bbox = np.concatenate([np.reshape(pred_sbbox, (-1, 5 + num_classes)), 
                np.reshape(pred_mbbox, (-1, 5 + num_classes)),
                np.reshape(pred_lbbox, (-1, 5 + num_classes))], axis=0)

            bboxes = utils.postprocess_boxes(pred_bbox, frame_size, input_size, 0.1)
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



gen()
