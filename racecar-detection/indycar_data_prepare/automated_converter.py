#!/usr/bin/env python
# coding: utf-8

#dependencies
#apt-get install -y protobuf-compiler python-pil python-lxml python-tk
#pip install --user Cython
#pip install --user contextlib2


import glob
import os
import random
import shutil

from xml.etree import ElementTree as et


#get file names
image_names = glob.glob("images/*.jpg")
image_names = [x.replace('images/', '').replace('.jpg', '') for x in image_names]



# train, validation
val_ratio = 0.15
train_ratio = 1 - val_ratio # 0.85


#shuffle
random.shuffle(image_names)

num_train = round(len(image_names) * train_ratio)
num_val = round(len(image_names) * val_ratio)


train_images = image_names[:num_train]
val_images = image_names[num_train:]


print('Number of training images: ', len(train_images))
print('Number of validation images: ', len(val_images))



## Create directories
def created_dirs(path):
  if os.path.exists(path):
    shutil.rmtree(path)

  os.makedirs(path)
  os.makedirs(path + '/' + 'Annotations')
  os.makedirs(path + '/' +'JPEGImages')


# copy images:
def copy_images(images, source_path, dest_path):
    for name in images:
        shutil.copyfile(source_path + '/' + name + '.jpg', dest_path + '/'+ 'JPEGImages/' + name + '.jpg')
    



created_dirs('train_dataset')
copy_images(train_images,'images', 'train_dataset')

created_dirs('val_dataset')
copy_images(val_images,'images', 'val_dataset')





def fix_annotations_save(source_dir, file_name, target_dir):
    
    # some files have problematic annotations. first fix them
    # Read in the file
    with open(source_dir + file_name + '.xml') as file :
        filedata = file.read().replace('\n', '').replace('<path', '<path>').replace('annotations', 'annotation')
    # Write the file out again
    with open(source_dir + file_name + '.xml', 'w') as file:
        file.write(filedata)

        
        
    tree = et.parse(source_dir + file_name + '.xml')
    root = tree.getroot()

    
    # some old files have unneccessary tags, and these give error. Remove them
    for path in root.findall('path'):
        root.remove(path)
    for source in root.findall('source'):
        root.remove(source)
    
    for elem in root.getiterator():
        # some cars labelled as dogs. Fix them
        if elem.text == "dog":
            elem.text = elem.text.replace('dog', 'car')
        if elem.text == "images":
            elem.text = elem.text.replace('images', 'JPEGImages')
        # some files start with annotation tag.This should be annotations
        if elem.text == "annotations":
            elem.text = elem.text.replace('annotations', 'annotation')
        
    tree.write(target_dir + file_name + '.xml')
    


for name in train_images:
    fix_annotations_save('annotations/', name, 'train_dataset/Annotations/')

for name in val_images:
    fix_annotations_save('annotations/', name, 'val_dataset/Annotations/')
    




#convert to tfrecord
os.system("./create_tfrecord.sh")





