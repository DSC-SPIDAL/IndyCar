import numpy as np
import os
import six.moves.urllib as urllib
import sys
import tarfile
import tensorflow as tf
import zipfile
import cv2

from distutils.version import StrictVersion
from collections import defaultdict
from io import StringIO
from matplotlib import pyplot as plt
from PIL import Image
import time
import os
os.environ["CUDA_VISIBLE_DEVICES"]="0"

# to run on cpu
# os.environ["CUDA_VISIBLE_DEVICES"]=""

device_used = 'CPU'
if tf.test.gpu_device_name():
    device_used = 'GPU'
print(device_used)


# This is needed since the notebook is stored in the object_detection folder.
sys.path.append("..")

from object_detection.utils import ops as utils_ops

from object_detection.utils import label_map_util

from object_detection.utils import visualization_utils as vis_util

# What model to download.
MODEL_NAME = 'ssd_mobilenet_v1_coco_2017_11_17'
MODEL_FILE = MODEL_NAME + '.tar.gz'

# Path to frozen detection graph. This is the actual model that is used for the object detection.
PATH_TO_FROZEN_GRAPH = "exported_model" + '/frozen_inference_graph.pb'

# List of the strings that is used to add correct label for each box.
PATH_TO_LABELS = os.path.join('../indycar_data_prepare', 'pascal_label_map.pbtxt')


od_graph_def = tf.GraphDef()
with tf.gfile.GFile(PATH_TO_FROZEN_GRAPH, 'rb') as fid:
    serialized_graph = fid.read()
    od_graph_def.ParseFromString(serialized_graph)
    tf.import_graph_def(od_graph_def, name='')

category_index = label_map_util.create_category_index_from_labelmap(PATH_TO_LABELS, use_display_name=True)

def load_image_into_numpy_array(image):
  (im_width, im_height) = image.size
  return np.array(image.getdata()).reshape(
      (im_height, im_width, 3)).astype(np.uint8)

sess = tf.Session()
# Get handles to input and output tensors
ops = tf.get_default_graph().get_operations()
all_tensor_names = {output.name for op in ops for output in op.outputs}
tensor_dict = {}
for key in [
    'num_detections', 'detection_boxes', 'detection_scores',
    'detection_classes', 'detection_masks']:
    tensor_name = key + ':0'
    if tensor_name in all_tensor_names:
        tensor_dict[key] = tf.get_default_graph().get_tensor_by_name(tensor_name)
if 'detection_masks' in tensor_dict:
    # The following processing is only for single image
    detection_boxes = tf.squeeze(tensor_dict['detection_boxes'], [0])
    detection_masks = tf.squeeze(tensor_dict['detection_masks'], [0])
    # Reframe is required to translate mask from box coordinates to image coordinates and fit the image size.
    real_num_detection = tf.cast(tensor_dict['num_detections'][0], tf.int32)
    detection_boxes = tf.slice(detection_boxes, [0, 0], [real_num_detection, -1])
    detection_masks = tf.slice(detection_masks, [0, 0, 0], [real_num_detection, -1, -1])
    detection_masks_reframed = utils_ops.reframe_box_masks_to_image_masks(
        detection_masks, detection_boxes, image.shape[1], image.shape[2])
    detection_masks_reframed = tf.cast(
        tf.greater(detection_masks_reframed, 0.5), tf.uint8)
        # Follow the convention by adding back the batch dimension
    tensor_dict['detection_masks'] = tf.expand_dims(
        detection_masks_reframed, 0)
image_tensor = tf.get_default_graph().get_tensor_by_name('image_tensor:0')



cap = cv2.VideoCapture('<video-file-path>')
total_time = 0
capture_time = 0
preproc_time = 0
infer_time = 0
postproc_time = 0

image_count = 0
target_im_count = 10000

while image_count < target_im_count:
    start_time = time.time()
    ret, frame = cap.read()
    capture_time += time.time() - start_time
    if not ret:
        print('Input source error!')
        break
    temp_time = time.time()
    image_np = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    image_np_expanded = np.expand_dims(image_np, axis=0)
    preproc_time += time.time() - temp_time
    temp_time = time.time()
    # Actual detection.
    output_dict = sess.run(tensor_dict,
        feed_dict={image_tensor: image_np_expanded})
    infer_time += time.time() - temp_time
    temp_time = time.time()
    # all outputs are float32 numpy arrays, so convert types as appropriate
    output_dict['num_detections'] = int(output_dict['num_detections'][0])
    output_dict['detection_classes'] = output_dict[
        'detection_classes'][0].astype(np.uint8)
    output_dict['detection_boxes'] = output_dict['detection_boxes'][0]
    output_dict['detection_scores'] = output_dict['detection_scores'][0]
    if 'detection_masks' in output_dict:
        output_dict['detection_masks'] = output_dict['detection_masks'][0]
    vis_util.visualize_boxes_and_labels_on_image_array(
        image_np,
        output_dict['detection_boxes'],
        output_dict['detection_classes'],
        output_dict['detection_scores'],
        category_index,
        instance_masks=output_dict.get('detection_masks'),
        use_normalized_coordinates=True, line_thickness=8)
    frame = cv2.cvtColor(image_np, cv2.COLOR_RGB2BGR)
    postproc_time += time.time() - temp_time
    total_time += time.time() - start_time
    image_count += 1
    if image_count % 50 == 0:
        print('num of inferred images: ', image_count)

time_file = open('time_video_bench_' + device_used  + '.txt', 'w')
time_file.write('total images used for benchmark: ' + str(image_count) + '\n')
time_file.write('total time (s): ' + str(total_time) + '\n')
time_file.write('total capture time (s): ' + str(capture_time) + '\n')
time_file.write('total preprocess time (s): ' + str(preproc_time) + '\n')
time_file.write('total inference time (s): ' + str(infer_time) + '\n')
time_file.write('total postrocess time (s): ' + str(postproc_time) + '\n')

time_file.write('average total time (s): ' + str(total_time / image_count) + '\n')
time_file.write('average preprocess time (s): ' + str(preproc_time / image_count) + '\n')
time_file.write('average inference time (s): ' + str(infer_time / image_count) + '\n')
time_file.write('average postrocess time (s): ' + str(postproc_time / image_count) + '\n')

time_file.write('average FPS: ' + str(1/(total_time / image_count)) + '\n')
