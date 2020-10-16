# Copyright 2019 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
r"""A demo for object detection.

For Raspberry Pi, you need to install 'feh' as image viewer:
sudo apt-get install feh

Example (Running under edgetpu repo's root directory):

  - Face detection:
    python3 examples/object_detection.py \
    --model='test_data/mobilenet_ssd_v2_face_quant_postprocess_edgetpu.tflite' \
    --keep_aspect_ratio

  - Pet detection:
    python3 examples/object_detection.py \
    --model='test_data/ssd_mobilenet_v1_fine_tuned_edgetpu.tflite' \
    --label='test_data/pet_labels.txt' \
    --keep_aspect_ratio

"""

import argparse
import platform
import subprocess
from edgetpu.detection.engine import DetectionEngine
from edgetpu.utils import dataset_utils
from PIL import Image
from PIL import ImageDraw
import glob
import time
import cv2

def main():
  parser = argparse.ArgumentParser()
  parser.add_argument(
      '--model',
      help='Path of the detection model, it must be a SSD model with postprocessing operator.',
      required=True)
  parser.add_argument('--label', help='Path of the labels file.')
  
  parser.add_argument(
      '--keep_aspect_ratio',
      dest='keep_aspect_ratio',
      action='store_true',
      help=(
          'keep the image aspect ratio when down-sampling the image by adding '
          'black pixel padding (zeros) on bottom or right. '
          'By default the image is resized and reshaped without cropping. This '
          'option should be the same as what is applied on input images during '
          'model training. Otherwise the accuracy may be affected and the '
          'bounding box of detection result may be stretched.'))
  parser.set_defaults(keep_aspect_ratio=False)
  args = parser.parse_args()


  # Initialize engine.
  engine = DetectionEngine(args.model)
  labels = dataset_utils.read_label_file(args.label) if args.label else None

  cap = cv2.VideoCapture('fast_forward.mp4')
  
  total_time = 0
  load_time = 0
  preproc_time = 0
  infer_time = 0
  postproc_time = 0
  image_count = 0
  for i in range(10):
      while image_count < 10000:
        # Open image.
        start_time = time.time()
        ret, img = cap.read()
        load_time += time.time() - start_time
        temp_time = time.time()
        img = Image.fromarray(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
        im_width, im_height = img.size
        img_resized = img.resize((300,300))
        preproc_time += time.time() - temp_time
        # Run inference.
        temp_time = time.time()
        ans = engine.detect_with_image(
            img_resized,
            threshold=0.5,
            keep_aspect_ratio=args.keep_aspect_ratio,
            relative_coord=False,
            top_k=15)
        infer_time += time.time() - temp_time
        temp_time = time.time()

        draw = ImageDraw.Draw(img)
        # Display result.
        if ans:
            for obj in ans:
                box = obj.bounding_box.flatten().tolist()
                # Draw a rectangle.
                draw.rectangle([box[0]* im_width / 300, box[1]* im_height /300, box[2]* im_width /300, box[3] * im_height / 300], outline='red')
        #img.save('output/' + file_name[file_name.rfind('/'):])
        postproc_time += time.time() - temp_time
        total_time += time.time() - start_time
        image_count += 1
        if image_count % 50 == 0:
            print('num of inferred images: ', image_count)

  print('total images used for benchmark: ' + str(image_count))
  print('total time (s): ' + str(total_time))
  print('total load time (s): ' + str(load_time))
  print('total preprocess time (s): ' + str(preproc_time))
  print('total inference time (s): ' + str(infer_time) )
  print('total postrocess time (s): ' + str(postproc_time))

  print('average total time (s): ' + str(total_time / image_count))
  print('average load time (s): ' + str(load_time / image_count))
  print('average preprocess time (s): ' + str(preproc_time / image_count))
  print('average inference time (s): ' + str(infer_time / image_count))
  print('average postrocess time (s): ' + str(postproc_time / image_count))

if __name__ == '__main__':
  main()
