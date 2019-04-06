# import matplotlib.pyplot as plt
# import numpy as np
# from matplotlib.legend_handler import HandlerLine2D

# import csv

# counters = []
# node1 = []

# with open('/Users/sahiltyagi/Desktop/nozeroes.csv') as csvfile:
# 	readCSV = csv.reader(csvfile, delimiter=',')
# 	recordnum=0
# 	for row in readCSV:
# 		print(row[0])
# 		recordnum +=1
# 		counter = float(row[0])
# 		latency = float(recordnum)
# 		node1.append(latency)
# 		counters.append(counter)

# plt.plot(node1, counters, label='speed car#9')
# plt.xlabel('record #', fontsize=20)
# plt.ylabel('input', fontsize=20)
# ax = plt.subplot(111)
# plt.ylabel('Input value (mph)', fontsize=20)
# for i in np.arange(0, 20754, 100):
#     ax.axvline(i, color='grey', alpha=0.3)
# for j in np.arange(0, 250, 10):
# 	ax.axhline(j, color='grey', alpha=0.3)

# plt.ylim(ymax=240, ymin=0)
# plt.yticks([10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190,200,210,220,230,240])
# plt.xlim(xmax=20754, xmin=0)
# plt.xticks([1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,11000,12000,13000,14000,15000,16000,17000,18000,19000,20000])

# plt.legend(loc='lower right', fontsize=18)
# plt.show()

## injected speed plot
# import matplotlib.pyplot as plt
# import numpy as np
# from matplotlib.legend_handler import HandlerLine2D

# import csv

# counters = []
# node1 = []

# with open('/Users/sahiltyagi/Desktop/injectedspeed.csv') as csvfile:
# 	readCSV = csv.reader(csvfile, delimiter=',')
# 	recordnum=0
# 	for row in readCSV:
# 		print(row[0])
# 		recordnum +=1
# 		counter = float(row[0])
# 		latency = float(recordnum)
# 		node1.append(latency)
# 		counters.append(counter)

# plt.plot(node1, counters, label='speed car#9')
# plt.xlabel('record #', fontsize=20)
# plt.ylabel('input', fontsize=20)
# ax = plt.subplot(111)
# plt.ylabel('Input value (mph)', fontsize=20)
# for i in np.arange(0, 20854, 100):
#     ax.axvline(i, color='grey', alpha=0.3)
# for j in np.arange(0, 240, 10):
# 	ax.axhline(j, color='grey', alpha=0.3)

# arr1 = [v for v in range(0, 21000, 500)]

# print(arr1)

# plt.ylim(ymax=240, ymin=0)
# plt.yticks([10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190,200,210,220,230,240])
# plt.xlim(xmax=20854, xmin=0)
# plt.xticks(arr1, rotation='vertical')

# plt.legend(loc='upper left', fontsize=14)
# plt.show()

import matplotlib.pyplot as plt
import numpy as np
from matplotlib.legend_handler import HandlerLine2D

import csv

counters = []
node1 = []

with open('/Users/sahiltyagi/Desktop/nozeroes.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	recordnum=0
	for row in readCSV:
		print(row[0])
		recordnum +=1
		counter = float(row[0])
		latency = float(recordnum)
		node1.append(latency)
		counters.append(counter)

plt.plot(node1, counters, label='speed car#9')

counters = []
node1 = []
#add scatter plot indices for 0.00 mph speed
with open('/Users/sahiltyagi/Desktop/zeroindex.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		counter = float(row[0])
		latency = 0
		node1.append(latency)
		counters.append(counter)

plt.scatter(counters, node1, label='injected 0s',s=2.5,c='red')

plt.xlabel('record #', fontsize=20)
plt.ylabel('input', fontsize=20)
ax = plt.subplot(111)
plt.ylabel('Input value (mph)', fontsize=20)
for i in np.arange(0, 20854, 100):
    ax.axvline(i, color='grey', alpha=0.3)
for j in np.arange(0, 250, 10):
	ax.axhline(j, color='grey', alpha=0.3)

arr1 = [v for v in range(0, 21500, 500)]

print(arr1)

plt.ylim(ymax=240, ymin=0)
plt.yticks([-10, 0, 10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190,200,210,220,230,240])
plt.xlim(xmax=20854, xmin=0)
plt.xticks(arr1, rotation='vertical')
plt.legend(loc='center', fontsize=18)
plt.show()