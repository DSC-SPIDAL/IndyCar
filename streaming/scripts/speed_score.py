import matplotlib.pyplot as plt
import numpy as np

import csv

tod = []
node1 = []
counters = []
axiscounters = []
axistod=[]
axisscore = []
axisspeed = []

with open('/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/HTMseqTime13_rawscore.csv') as csvfile:
#with open('/Users/sahiltyagi/Desktop/benchmarks/HTMjava/-100to300range/car13/HTMseq_final.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		# counter = float(row[0])
		# latency = float(row[1])
		# timeofday = row[2]
		# score = float(row[3])
		# speed = float(row[4])
		# counters.append(counter)
		# node1.append(latency)
		# tod.append(timeofday)
		# axisscore.append(score)
		# axisspeed.append(speed)
		# if(counter % 3000 == 0):
		# 	axiscounters.append(counter)
		# 	print(counter)
		# 	axistod.append(timeofday)
		# 	print(timeofday)

		counter = float(row[0])
		#speed = float(row[4])
		speed = float(row[1])
		timeofday = row[2]
		score = float(row[3])
		counters.append(counter)
		axisspeed.append(speed)
		tod.append(timeofday)
		axisscore.append(score)
		if(counter % 3000 == 0):
			axiscounters.append(counter)
			print(counter)
			axistod.append(timeofday)
			print(timeofday)

fig, ax1 = plt.subplots()
arr1 = [v for v in range(0, 250, 25)]
ax1.set_yticks(arr1)
ax1.set_yticklabels(arr1, rotation=0, fontsize=8)
ax1.set_xticks(axiscounters)
ax1.set_xticklabels(axistod, rotation=90, fontsize=8)
ax1.grid()
ax1.set_xlabel('time of day', fontsize=15)
ax1.set_ylabel('speed (MPH)', fontsize=15)
ax1.plot(counters, axisspeed, label='car #13 HTM.java', c='blue', linewidth=0.7)
ax1.legend(loc = 'upper right')

ax2 = ax1.twinx()
ax2.set_ylabel('anomaly score', fontsize=15)
# ax2.scatter(counters, axisscore, label='anomaly score (0 is normal/expected event)', c='green', s=0.04)
ax2.plot(counters, axisscore, label='anomaly score (0 is normal/expected event)', c='red', linewidth=0.5)
ax2.legend(loc = 'upper left')

plt.show()