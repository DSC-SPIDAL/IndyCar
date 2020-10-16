cp create_pascal_tf_record.py ../models/research/object_detection/dataset_tools/

python ../models/research/object_detection/dataset_tools/create_pascal_tf_record.py --data_dir=train_dataset --annotations_dir=Annotations --output_path=train.tfrecord --label_map_path=pascal_label_map.pbtxt

echo 'train.tfrecord is ready!!!!'

python ../models/research/object_detection/dataset_tools/create_pascal_tf_record.py --data_dir=val_dataset --annotations_dir=Annotations --output_path=val.tfrecord --label_map_path=pascal_label_map.pbtxt

echo 'val.tfrecord is ready!!!!'

