
if [ -d "exported_tflite" ]; then
    rm -rf exported_tflite
else 
	mkdir exported_tflite
fi


export CONFIG_FILE=pipeline.config
export CHECKPOINT_PATH=CP/model.ckpt-25000
export OUTPUT_DIR=`pwd`/exported_tflite
echo $OUTPUT_DIR
rm -rf $OUTPUT_DIR
mkdir $OUTPUT_DIR

python ../models/research/object_detection/export_tflite_ssd_graph.py --pipeline_config_path=$CONFIG_FILE --trained_checkpoint_prefix=$CHECKPOINT_PATH --output_directory=$OUTPUT_DIR --add_postprocessing_op=true

tflite_convert \
--graph_def_file=$OUTPUT_DIR/tflite_graph.pb \
--output_file=$OUTPUT_DIR/detect.tflite \
--input_shapes=1,300,300,3 \
--input_arrays=normalized_input_image_tensor \
--output_arrays='TFLite_Detection_PostProcess','TFLite_Detection_PostProcess:1','TFLite_Detection_PostProcess:2','TFLite_Detection_PostProcess:3' \
--inference_type=QUANTIZED_UINT8 \
--mean_values=128 \
--std_dev_values=128 \
--change_concat_input_ranges=false \
--allow_custom_ops
