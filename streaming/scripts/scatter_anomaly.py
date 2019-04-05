import matplotlib.pyplot as plt
import numpy as np
from matplotlib.legend_handler import HandlerLine2D

import csv

counters = []
node1 = []

with open('/Users/sahiltyagi/Desktop/out.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		print(row[0])
		counter = float(row[0])
		latency = float(row[1])
		node1.append(latency)
		counters.append(counter)

#plt.plot(kind='scatter', counters, node1)
plt.scatter(counters, node1, label='latency')
plt.xlabel('Record index #', fontsize=20)
ax = plt.subplot(111)
plt.ylabel('Execution Time (millisec)', fontsize=20)
for i in np.arange(0, 100, 5):
    ax.axhline(i, color='grey', alpha=0.3)
for j in np.arange(0,17263, 1250):
	ax.axvline(j, color='grey', alpha=0.3)
plt.legend(loc='best', fontsize=20)
plt.show()
