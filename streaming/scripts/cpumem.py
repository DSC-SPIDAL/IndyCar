import matplotlib.pyplot as plt
import numpy as np
from matplotlib.legend_handler import HandlerLine2D

import csv

memoryarr = []
cpuarr = []
indexarr = []

with open('/Users/sahiltyagi/Desktop/seqCPUMEM.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		# print(row[1])
		recordnum = float(row[0])
		# counter = float(row[0])
		cpu = float(row[1])
		mem = float(row[2])
		indexarr.append(recordnum)
		cpuarr.append(cpu)
		memoryarr.append(mem)

fig, ax1 = plt.subplots()
ax1.plot(indexarr, cpuarr, label='cpu_sequential', c='red', linewidth=2.0)
ax1.set_xlabel('record #')
ax1.set_ylabel('% CPU usage', color='r')

arr1 = [v for v in range(0, 5000, 200)]
ax1.set_yticks(arr1)
for i in np.arange(0, 250, 5):
    ax1.axvline(i, color='grey', alpha=0.3)
for j in np.arange(0, 5000, 100):
	ax1.axhline(j, color='grey', alpha=0.3)

ax1.legend(loc='lower left', fontsize=12)

ax2 = ax1.twinx()

ax2.plot(indexarr, memoryarr, label='mem_sequential', c='blue', linewidth=2.0)
ax2.set_ylabel('% Memory usage', color='blue')
ax2.set_yticks([1,2,3,4,5,6,7,8,9,10,11,12,13,14])
ax2.legend(loc='upper right', fontsize=12)
fig.tight_layout()
plt.show()