import matplotlib.pyplot as plt
import numpy as np

import csv

tod = []
node1 = []
counters = []
axiscounters = []
axistod=[]
axisspeed = []

with open('/Users/sahiltyagi/Desktop/benchmarks/HTMjava/-100to300range/car13/HTMseq_final.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		counter = float(row[0])
		latency = float(row[1])
		timeofday = row[2]
		speed = float(row[4])
		counters.append(counter)
		node1.append(latency)
		tod.append(timeofday)
		axisspeed.append(speed)
		if(counter % 3000 == 0):
			axiscounters.append(counter)
			print(counter)
			axistod.append(timeofday)
			print(timeofday)

fig, ax1 = plt.subplots()
arr1 = [v for v in range(0, 105, 5)]
ax1.set_yticks(arr1)
ax1.set_yticklabels(arr1, rotation=0, fontsize=8)
ax1.set_xticks(axiscounters)
ax1.set_xticklabels(axistod, rotation=90, fontsize=8)
ax1.grid()
ax1.set_xlabel('time of day', fontsize=15)
ax1.set_ylabel('execution time (milliseconds)', fontsize=15)
ax1.plot(counters, node1, label='avg:3.4, min=1.0, max.=99.5 anomaly/pitstop time: 16:44:35.128, 17:00:59.299, 18:09:23.310', c='red', linewidth=0.7)

ax2 = ax1.twinx()
arr2 = [v for v in range(0, 250, 25)]
ax2.set_yticks(arr2)
ax2.set_yticklabels(arr2, rotation=0, fontsize=8)
ax2.set_ylabel('speed (MPH)', fontsize=15)
ax2.plot(counters, axisspeed, label='avg:3.4, min=1.0, max.=99.5 anomaly/pitstop time: 16:44:35.128, 17:00:59.299, 18:09:23.310', c='blue', linewidth=0.7)

ax1.legend(loc = 'upper right')
plt.show()