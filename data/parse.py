f = open('jdb2011/train.txt')
for line in f:
	tokens = line.rstrip().split('\t')
	#print tokens
	if tokens[0] != tokens[1]:
		print tokens[0], "=>", tokens[1]

