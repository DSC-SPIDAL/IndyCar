import matplotlib.pyplot as plt
import numpy as np

import csv

counters = []
cpusage = []
memoryusage = []

with open('/Users/sahiltyagi/Desktop/resource_consumption.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		counter = float(row[0])
		memory = float(row[1])
		cpu = float(row[2])
		counters.append(counter)
		memoryusage.append(memory)
		cpusage.append(cpu)

plt.plot(counters, cpusage, color="red", label='CPU' , linewidth=2)
plt.plot(counters, memoryusage, color="blue", label='Mem' , linewidth=2)
plt.legend(loc='best', fontsize=20)
plt.show()