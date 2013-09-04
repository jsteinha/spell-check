from collections import defaultdict
from pprint import pprint

import codecs
import locale
import sys

# Wrap sys.stdout into a StreamWriter to allow writing unicode.
sys.stdout = codecs.getwriter(locale.getpreferredencoding())(sys.stdout) 


hanzi = open('inputHanzi.txt')
pinyin = open('inputPinyin.txt')
histogram = defaultdict(int)
for (line1, line2) in zip(hanzi, pinyin):
	words_hanzi = line1.rstrip("\n").split(" ")
	words_pinyin = line2.rstrip("\n").split("+")
	sanitized_words = map(lambda word : unicode(word, 'utf-8'), words_hanzi)
	for source, target in zip(sanitized_words, words_pinyin):
		print source, target
	#print words1
	#print words2
	#for word in words1:
	#	histogram[word] += 1
	#	#sanitized_word = list(unicode(word, 'utf-8'))
	#	#for char in sanitized_word:
	#	#	print "char:", char
	#	#	histogram[char] += 1

#counts = histogram.values()
#hist2 = defaultdict(int)
#total = 0
#for c in counts:
#	hist2[c] += 1
#	total += c
#for k, v in sorted(hist2.items()):
#	print "%d: %d (%.2f)" % (k, v, k*v/float(total))
