filename='eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log'

with open(filename, encoding='latin1') as f:
    line = f.readline()
    cnt = 1
    while line:
        char = line[1]
        with open('small_file_{0}'.format(line[1]), 'a+') as fout:
            fout.writelines(line)
        line = f.readline()
        cnt += 1
        print(cnt)
