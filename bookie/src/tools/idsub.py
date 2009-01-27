#!/usr/bin/python

import sys, fileinput, re, os

def main(mapfile, xmlfiles):
  idmap = {}
  for line in fileinput.FileInput(mapfile):
    line = line.rstrip('\n')
    if len(line) == 0:
      continue
    idfrom, idto = line.split('\t')
    idmap[idfrom] = idto
    #print "%s: %s" % (idfrom, idto)
  for f in xmlfiles:
    print f,": ",
    ih = open(f, 'r')
    oh = open(f+".out", 'w')
    subs = 0
    for line in ih:
      line, num  = re.subn('( (?:id|linkend)=")(.*?)"',
          lambda m: m.group(1) + idmap[m.group(2)] + '"',
          line)
      subs += num
      oh.write(line)
    print "%d changes" % (subs,)
    ih.close()
    oh.close()
    if subs:
      os.rename(f+".out", f)
    else:
      os.unlink(f+".out")

if __name__ == '__main__':
  main(sys.argv[1], sys.argv[2:])

