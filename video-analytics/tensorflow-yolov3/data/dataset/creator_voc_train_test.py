#! /usr/bin/env python
# coding=utf-8
#================================================================
#   Copyright (C) 2019 * Ltd. All rights reserved.
#
#   Editor      : VIM
#   File name   : convert_weight.py
#   Author      : Sahaj
#   Created date: 2019-05-30 02:41:31
#   Description :
#
#================================================================

file=open("voc_test.txt","r")
new_file=open("new_voc_test.txt","w+")
data=file.read()
data=data.split('\n')



def rounding(a):
	if a%1<0.5:
		return int(a//1)
	elif a%1>=0.5:
		return int(a//1)+1

def take_section_fix(sen):
	a=sen.split(',')
	for i in range(0,len(a)):
		a[i]=str(rounding(float(a[i])))
	a=','.join(a)
	return a



if data[len(data)-1]=="" or data[len(data)-1]==" ":
	data.pop()

k=[]

for i in data:  #change the next line
	if i.startswith('VOC/test/VOCdevkit/VOC2007/JPEGImages/102') or i.startswith('VOC/test/VOCdevkit/VOC2007/JPEGImages/ff'):		
		line=i.split(' ')
		for i in range(1,len(line)):
			line[i]=take_section_fix(line[i])
		line=' '.join(line)
		line=line+'\n'
		if line.isspace():
			print("there is spaces line")
			break
		new_file.write(line)
	else:
		line=i
		line=line+'\n'
		new_file.write(line)

print(len(data))
print("completed")