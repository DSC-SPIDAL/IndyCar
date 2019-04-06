import matplotlib.pyplot as plt
import numpy as np

import csv

node1 = []

# with open('/Users/sahiltyagi/Desktop/HTMsubset1car.csv') as csvfile:
with open('/Users/sahiltyagi/Desktop/totalSink33.csv') as csvfile:
	readCSV = csv.reader(csvfile, delimiter=',')
	recordnum=0
	for row in readCSV:
		#print(row[0])
		latency = float(row[3])
		if latency > 2000:
			recordnum+=1
			print(row[0], ",", row[1], ",", row[3])
			#print(row[1])

		node1.append(latency)

print('recordnums:', recordnum)
# arr1 = [v for v in range(0, 1000, 50)]
# plt.xticks(arr1, rotation='vertical')
xbins = 300
plt.hist(node1, bins=xbins, alpha=0.15, histtype='bar', ec='black')
fig = plt.figure()
ax = fig.add_subplot(2, 1, 1)

plt.xlabel('latency (milliseconds)', fontsize=20)
plt.ylabel('frequency (log)', fontsize=20)
ax.set_yscale('log')
plt.show()