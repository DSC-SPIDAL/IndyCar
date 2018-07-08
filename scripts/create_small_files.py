import io

filename='eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log'

with io.open(filename, encoding='latin1') as f:
    line = f.readline()
    cnt = 1
    fs = {}
    while line:
        char = line[1]
        if line[1] in fs:
            fout = fs[line[1]]
            fout.writelines(line)
        else: 
            fout = io.open('small_file_{0}'.format(line[1]), 'a+')
            fs[line[1]] = fout
        line = f.readline()
        cnt += 1
        print(cnt)

    for key, value in fs.iteritems():
        value.close()
