

if [ -d "CP" ]; then
    echo "Using existing checkpoint"
else 
	mkdir CP
	cd CP
	wget http://download.tensorflow.org/models/object_detection/ssd_resnet50_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03.tar.gz
	tar -xzvf ssd_resnet50_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03.tar.gz
	mv ssd_resnet50_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03/* .
	rm -rf ssd_resnet50_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03
	rm -rf ssd_resnet50_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03.tar.gz
	rm pipeline.config
	rm checkpoint
	cd ..
fi



# From the tensorflow/models/research/ directory
PIPELINE_CONFIG_PATH=`pwd`/pipeline.config
MODEL_DIR=`pwd`/CP
NUM_TRAIN_STEPS=25000
SAMPLE_1_OF_N_EVAL_EXAMPLES=1
python ../models/research/object_detection/model_main.py \
    --pipeline_config_path=${PIPELINE_CONFIG_PATH} \
    --model_dir=${MODEL_DIR} \
    --num_train_steps=${NUM_TRAIN_STEPS} \
    --sample_1_of_n_eval_examples=$SAMPLE_1_OF_N_EVAL_EXAMPLES \
    --alsologtostderr

