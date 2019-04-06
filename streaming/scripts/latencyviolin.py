import matplotlib.pyplot as plt
import csv
import numpy as np

noreuse =[]
noreuse_file = '/Users/sahiltyagi/Desktop/out11.csv'
with open(noreuse_file) as csvfile:
    readCSV = csv.reader(csvfile, delimiter=',')
    for row in readCSV:
        noreuse.append(float(row[1]))

print(len(noreuse))

v1 = plt.violinplot(noreuse,[0], widths=0.8,showmeans=True, showextrema=True, showmedians=True)
for pc in v1['bodies']:
    pc.set_facecolor('blue')
v1['cmedians'].set_color('g')
v1['cmeans'].set_color('m')


fig = plt.figure(1, figsize=(6, 6))
ax = plt.subplot(111)
#xlabels = ['NoReuse','Reuse','MR','MPR']
xlabels = ['car #11']
#plt.xticks([0,1,2,3], xlabels)
plt.xticks([0], xlabels)

#plt.tick_params(labelsize=19)
plt.grid(b=True, which='major', color='grey', linestyle='-')

# for i in np.arange(0, 8000, 500):
#     ax.axhline(i, color='grey', alpha=0.3)
plt.ylabel('latency (millisec.)', fontsize=15)
#plt.ylim(ymax=15000, ymin=1000)
plt.ylim(ymax=11, ymin=0)

arr1 = [v for v in range(0, 11, 1)]
print(arr1)

plt.yticks(arr1)
plotfilename = '/Users/sahiltyagi/Desktop/latency11.png'
plt.savefig(plotfilename, bbox_inches='tight')
# plt.show()

medians=[np.median(noreuse)]
print(medians)
print('Execution completed')