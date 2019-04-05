import matplotlib.pyplot as plt
import csv
import numpy as np

noreuse =[]
noreuse_file = '/Users/sahiltyagi/Desktop/Indy500/executiontime.csv'
with open(noreuse_file) as csvfile:
    readCSV = csv.reader(csvfile, delimiter=',')
    for row in readCSV:
        cost = float(row[0])
        noreuse.append(cost)

print(len(noreuse))

v1 = plt.violinplot(noreuse,[0], widths=0.8,showmeans=True, showextrema=False, showmedians=True)
for pc in v1['bodies']:
    pc.set_facecolor('blue')
v1['cmedians'].set_color('g')
v1['cmeans'].set_color('m')


fig = plt.figure(1, figsize=(6, 6))
ax = plt.subplot(111)
#xlabels = ['NoReuse','Reuse','MR','MPR']
xlabels = ['streetlight']
#plt.xticks([0,1,2,3], xlabels)
plt.xticks([0], xlabels)
plt.tick_params(labelsize=19)
plt.grid(b=True, which='major', color='grey', linestyle='-')
# ax = plt.gca()
# for i in np.arange(0, 15000, 500):
#     ax.axhline(i, color='grey', alpha=0.3)
for i in np.arange(0, 8000, 500):
    ax.axhline(i, color='grey', alpha=0.3)
plt.ylabel('latency in milliec', fontsize=20)
#plt.ylim(ymax=15000, ymin=1000)
#plt.yticks([1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,11000,12000,13000,14000,15000])
plotfilename = '/Users/sahiltyagi/Desktop/broker-latency.pdf'
plt.savefig(plotfilename, bbox_inches='tight')
# plt.show()

medians=[np.median(noreuse)]
print(medians)
print('Execution completed')