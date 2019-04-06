import matplotlib.pyplot as plt
import numpy as np
from matplotlib.legend_handler import HandlerLine2D

import csv

counters = []
node1 = []

with open('/Users/sahiltyagi/Desktop/speedup.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		print(row[0])
		counter = float(row[0])
		latency = float(row[1])
		node1.append(latency)
		counters.append(counter)

#plt.plot(kind='scatter', counters, node1)
plt.plot(counters, node1, label='speedup')
plt.xlabel('parallelism', fontsize=20)
plt.ylabel('speedup', fontsize=20)
ax = plt.subplot(111)
plt.ylabel('Speedup', fontsize=20)
for i in np.arange(0, 3, 0.5):
    ax.axhline(i, color='grey', alpha=0.3)
for j in np.arange(0,5,1):
	ax.axvline(j, color='grey', alpha=0.3)
plt.legend(loc='upper left', fontsize=18)
plt.show()
