minikube ssh "sudo chmod -R 777 /nfs/indycar"
minikube ssh "mkdir /nfs/indycar/datalogs"
minikube ssh "mkdir /nfs/indycar/config/lib/"

# copy log file into minikube
# change the path of the log file accordingly.
scp -i $(minikube ssh-key) ~/Downloads/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log docker@$(minikube ip):/nfs/indycar/datalogs/

# copy LSTM model files into minikube
scp -i $(minikube ssh-key) -r models docker@$(minikube ip):/nfs/indycar/config/

# Following link is for Linux CPU only. For other platforms, check https://www.tensorflow.org/install/lang_java
wget https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow_jni-cpu-linux-x86_64-1.14.0.tar.gz
mkdir tf-lib
tar -xzvf libtensorflow_jni-cpu-linux-x86_64-1.14.0.tar.gz -C tf-lib
scp -i $(minikube ssh-key) tf-lib/* docker@$(minikube ip):/nfs/indycar/config/lib/
