#filename = 'small_file_G'

#filename = 'small_file_T'

#filename = 'small_file_U'

filename = 'small_file_W'

with open(filename, encoding='utf-8') as f:
    line = f.readline()
    cnt = 1
    while line:
        line = line.replace('¦',',')
        line = line.replace('$','')
        with open('small_file_{0}_replaced'.format(line[0]), 'a+') as fout:
            fout.writelines(line)
        line = f.readline()
        cnt += 1
        print(cnt)
