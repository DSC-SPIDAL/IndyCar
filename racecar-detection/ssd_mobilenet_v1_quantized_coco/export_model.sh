
if [ -d "exported_model" ]; then
    rm -rf exported_model
else 
	mkdir exported_model
fi

INPUT_TYPE=image_tensor
PIPELINE_CONFIG_PATH='pipeline.config'
TRAINED_CKPT_PREFIX='CP/model.ckpt-25000'
EXPORT_DIR='exported_model/'
python ../models/research/object_detection/export_inference_graph.py \
    --input_type=${INPUT_TYPE} \
    --pipeline_config_path=${PIPELINE_CONFIG_PATH} \
    --trained_checkpoint_prefix=${TRAINED_CKPT_PREFIX} \
    --output_directory=${EXPORT_DIR}
