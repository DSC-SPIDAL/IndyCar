## tfrecord instructions

TensorFlow Detection Model Zoo requires tfrecord files. Follow the instructions to create tfrecord files for this task.

1. Add images into images folder
2. Add annotations into annotations folder.
3. Adjust training, val, and test ratios in `automated_converter.py`
    3.1 It shuffles the data. If you don't want it, just remove shuffling.
4. Run the script, it will generate tfrecords files in main directory.
    4.1. I haven't tested if it overrides the old file or appends. So, test it before using it.

p.s. Some dependencies are listed  in `automated_converter.py`. However, there could be extra dependencies.