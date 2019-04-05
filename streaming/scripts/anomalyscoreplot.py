import matplotlib.pyplot as plt
import numpy as np

import csv

counters = []
node1 = []

with open('/Users/sahiltyagi/Desktop/injectedanomaly.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		print(row[0])
		counter = float(row[0])
		latency = float(row[1])
		node1.append(latency)
		counters.append(counter)

#plt.plot(kind='scatter', counters, node1)
plt.scatter(counters, node1,label='anomaly score')
plt.xlabel('record index', fontsize=20)
plt.ylabel('anomaly score', fontsize=20)
#plt.legend(loc='upper left', fontsize=20)
plt.show()