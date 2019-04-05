import matplotlib.pyplot as plt
import numpy as np

import csv

node1 = []

with open('/Users/sahiltyagi/Desktop/benchmarks/HTMjava/clean_env/HTMseqAvg.csv') as csvfile:
#with open('/Users/sahiltyagi/Desktop/stormHTM33.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	#recordnum=0
	for row in readCSV:
		#recordnum +=1
		latency = float(row[1])
		if latency > 100:
			print(row[0], ",", row[1])
			#print(row[1])

		node1.append(latency)

#arr1 = [v for v in range(0, 100, 1)]
#plt.xticks(arr1, rotation='vertical')
fig, ax1 = plt.subplots()
for j in np.arange(0, 120000, 6000):
	ax1.axhline(j, color='grey', alpha=0.1)

# for j in np.arange(0, 4500, 500):
for j in np.arange(0, 120000, 3000):
	ax1.axhline(j, color='grey', alpha=0.3)

for j in np.arange(0, 3000, 200):
	ax1.axvline(j, color='grey', alpha=0.3)

xbins = 200
plt.hist(node1, bins=xbins, alpha=0.2, histtype='bar', ec='black', label='avg:1.74, min:0.25, max:2040.25 ms')

arr1 = [v for v in range(0, 3000, 100)]
plt.xticks(arr1, rotation='horizontal', fontsize=7)
ax1.legend(loc='upper right', fontsize=20)
plt.xlabel('execution time (milliseconds)', fontsize=15)
plt.ylabel('frequency', fontsize=15)

node1=[]
index=[]
with open('/Users/sahiltyagi/Desktop/benchmarks/HTMjava/clean_env/HTMseqAvg.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		latency = float(row[1])
		if latency > 1000:
			print(row[0], ",", row[1])
			recordnum=float(row[0])
			node1.append(latency)
			index.append(recordnum)

print('recordnum', recordnum)
plt.scatter(node1, index, s=2.5,c='red')
plt.show()