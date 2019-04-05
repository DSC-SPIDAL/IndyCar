import matplotlib.pyplot as plt
import matplotlib as mp
import numpy as np

import csv

node1 = []
recordnum=0

with open('/Users/sahiltyagi/Desktop/benchmarks/htmjava_sortedlatency.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	for row in readCSV:
		#print(row[0])
		latency = float(row[0])
		if latency > 2000:
			print(row[0])
			recordnum+=1

		node1.append(latency)

print('record count', recordnum)
fig, ax1 = plt.subplots()
#ax1.set_yticks([0,1,2,3,4,5,6,7,8,9,10,20,40,100,500,1000,1500,2000,2500,3000,3500,4000,4500,5000])

p = np.array([0.0, 25.0, 50.0, 75.0, 100.0])
perc = mp.mlab.prctile(node1, p=p)
plt.plot(node1, label='car #20 speed')
plt.plot((len(node1)-1) * p/100., perc, 'ro')
plt.xticks((len(node1)-1) * p/100., map(str, p), fontsize=22)

plt.xlabel('percentile', fontsize=30)
plt.ylabel('latency (milliseconds)', fontsize=20)
# for j in np.arange(0, 4500, 20):
for j in np.arange(0, 10000, 500):
	ax1.axhline(j, color='grey', alpha=0.1)

# for j in np.arange(0, 4500, 500):
for j in np.arange(0, 10000, 250):
	ax1.axhline(j, color='grey', alpha=0.3)

#arr1 = [v for v in range(0, 4500, 500)]
arr1 = [v for v in range(0, 10000, 1000)]
# plt.yticks([500,1000,1500,2000,2500,3000,3500,4000,4500])
plt.yticks(arr1, rotation='horizontal', fontsize=20)
ax1.legend(loc='upper left', fontsize=20)
plt.show()