default: html-chunk

include ../tools/Makefile.base

# Makefile.base overrides
FO_XSLTPROC_OPTS = --stringparam paper.type A4
L10N_REVISION = в редакции

dist-html-chunk:
	../tools/book-dist.py --html-chunk
