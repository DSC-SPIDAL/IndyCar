import matplotlib.pyplot as plt
import numpy as np

import csv

counters = []
node1 = []

with open('/Users/sahiltyagi/Desktop/delta.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		print(row[0])
		counter = float(row[0])
		latency = float(row[1])
		node1.append(latency)
		counters.append(counter)

#plt.plot(kind='scatter', counters, node1)
plt.scatter(counters, node1,label='delta(mph)')
plt.xlabel('record index', fontsize=20)
plt.ylabel('delta change (mph)', fontsize=20)
ax = plt.subplot(111)
for i in np.arange(-250, 250, 50):
    ax.axhline(i, color='grey', alpha=0.3)
for j in np.arange(0,18113, 1250):
	ax.axvline(j, color='grey', alpha=0.3)
#plt.legend(loc='upper left', fontsize=20)
plt.show()