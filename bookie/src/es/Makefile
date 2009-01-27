# This altered default was present in the Spanish fork of Makefile when I
# performed the migration to an includable base form.
# I have preserved it. --maxb
default: all-html

include ../tools/Makefile.base

BOOK_ASPELL_FILES = appa appb book foreword ch00 ch01 ch03 ch04 ch06 ch07 ch08 ch09
OTHER_ASPELL_FILES = COORDINADOR glosario_traduccion LEAME TODO TRABAJO

aspell_add_words:
	@for file in $(BOOK_ASPELL_FILES); do \
		cat book/$$file.xml |aspell list -H --lang=es |sort|uniq> tmp.txt;\
		cat book/$$file.xml.aspell_ignore >> tmp.txt;\
		sort tmp.txt|uniq> book/$$file.xml.aspell_ignore;\
		rm tmp.txt;\
		svn diff book/$$file.xml.aspell_ignore;\
	done
	@for file in $(OTHER_ASPELL_FILES); do \
		cat $$file | aspell list --mode=url --lang=es |sort|uniq> tmp.txt;\
		cat .aspell_ignore >> tmp.txt;\
		sort tmp.txt|uniq> .aspell_ignore;\
		rm tmp.txt;\
		svn diff $$file;\
	done

aspell_check:
	@for file in $(BOOK_ASPELL_FILES); do \
		touch book/$$file.xml.aspell_ignore;\
		aspell -H --lang=es create master ./book/.aspell.$$file < book/$$file.xml.aspell_ignore;\
		aspell check book/$$file.xml -H --lang=es --add-extra-dicts ./book/.aspell.$$file;\
	done
	touch .aspell_ignore
	aspell --mode=url --lang=es create master ./.aspell.master < .aspell_ignore
	@for file in $(OTHER_ASPELL_FILES); do \
		aspell check $$file --mode=url --lang=es --add-extra-dicts ./.aspell.master;\
	done
