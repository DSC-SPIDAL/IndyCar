import tensorflow as tf

count = 0
file = open('example.txt','w')
for example in tf.python_io.tf_record_iterator("train.tfrecord"):
    file.write(str(tf.train.Example.FromString(example)))
    
    count += 1
    if count == 2:
        break

