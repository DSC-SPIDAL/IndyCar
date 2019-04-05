import matplotlib.pyplot as plt
import numpy as np
from matplotlib.legend_handler import HandlerLine2D

import csv

counters = []
node1 = []

with open('/Users/sahiltyagi/Desktop/htmoutput.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	recordnum=0
	for row in readCSV:
		counter = float(row[2])
		latency = float(row[0])
		node1.append(latency)
		counters.append(counter)

fig, ax1 = plt.subplots()
ax1.plot(node1, counters, label='car#11', c='blue', linewidth=0.7)
ax1.set_xlabel('record #')
ax1.set_ylabel('Engine RPM (rotations per minute)', color='b')

arr1 = [v for v in range(0, 15000, 500)]
print(arr1)

ax1.set_yticks(arr1)
#ax1.tick_params('y', colors='b')
for i in np.arange(0, 120000, 1000):
    ax1.axvline(i, color='grey', alpha=0.3)
for j in np.arange(0, 15000, 250):
	ax1.axhline(j, color='grey', alpha=0.3)

ax1.legend(loc='lower right', fontsize=12)

# arr1 = [v for v in range(0, 132000, 2000)]
# print(arr1)

ax2 = ax1.twinx()
counters = []
node1 = []
with open('/Users/sahiltyagi/Desktop/htmoutput.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	recordnum =0
	for row in readCSV:
		recordnum +=1
		counter = float(row[0])
		latency = float(row[3])
		node1.append(latency)
		counters.append(counter)

ax2.plot(counters, node1, c='red', linewidth=0.4)
#ax2.scatter(counters, node1, c='red', s=0.5)
ax2.set_ylabel('anomaly score', color='red')
ax2.set_yticks([0.0,0.05,0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95,1.0])
# for i in np.arange(0, 130000, 1000):
#     ax2.axvline(i, color='grey', alpha=0.3)
# for j in np.arange(0, 1, 0.025):
# 	ax2.axhline(j, color='grey', alpha=0.3)
fig.tight_layout()
#plt.legend(loc='lower right', fontsize=12)
plt.show()

# plt.ylim(ymax=240, ymin=0)
# plt.yticks([-10, 0, 10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190,200,210,220,230,240])
# plt.xlim(xmax=132000, xmin=0)
# plt.xticks(arr1, rotation='vertical')
# plt.legend(loc='lower right', fontsize=12)
# plt.show()