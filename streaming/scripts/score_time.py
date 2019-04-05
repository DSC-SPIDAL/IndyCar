import matplotlib.pyplot as plt
import numpy as np

import csv

tod = []
node1 = []
counters = []
axiscounters = []
axistod=[]
axisscore = []

with open('/Users/sahiltyagi/Desktop/benchmarks/HTMjava/-100to300range/car13/HTMseq_final.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		counter = float(row[0])
		latency = float(row[1])
		timeofday = row[2]
		score = float(row[3])
		counters.append(counter)
		node1.append(latency)
		tod.append(timeofday)
		axisscore.append(score)
		if(counter % 1000 == 0):
			axiscounters.append(counter)
			print(counter)
			axistod.append(timeofday)
			print(timeofday)

fig, ax1 = plt.subplots()
arr1 = [v for v in range(0, 105, 5)]
ax1.set_yticks(arr1)
ax1.set_yticklabels(arr1, rotation=0, fontsize=10)
ax1.set_xticks(axiscounters)
ax1.set_xticklabels(axistod, rotation=90, fontsize=10)
ax1.grid(linestyle='-', linewidth='0.1', color='grey')
#ax1.set_xlabel('time of day', fontsize=22)
ax1.set_ylabel('car #13 execution time (milliseconds)', fontsize=22)
#for car #20
# ax1.plot(counters, node1, label='avg:3.4, min=1.0, max.=99.5 anomaly/pitstop time: 16:44:35.128, 17:00:59.299, 18:09:23.310', c='red', linewidth=0.7)

#for car #13
ax1.plot(counters, node1, label='latency(ms) avg:2.89,min=1,max.=97.3 anomaly/pitstop time: 16:43:52.401,17:01:11.554,17:22:56.037', c='blue', linewidth=0.7)
ax1.legend(loc = 'upper right', bbox_to_anchor=(1, 1), fontsize=13, borderaxespad=0.)

ax2 = ax1.twinx()
ax2.set_ylabel('anomaly score', fontsize=25)
# ax2.scatter(counters, axisscore, label='anomaly score (0 is normal/expected event)', c='green', s=0.6)
ax2.plot(counters, axisscore, label='anomaly score (0 is normal/expected event)', c='red', linewidth=0.3)
ax2.legend(loc = 'upper left', bbox_to_anchor=(0, 1), fontsize=13, borderaxespad=0.)

plt.show()