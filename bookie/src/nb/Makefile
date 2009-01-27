include ../tools/Makefile.base

# Makefile.base overrides
FO_XSLTPROC_OPTS = --stringparam paper.type A4
L10N_REVISION = revisjon

# Stuff below here is added for building the Norwegian version.

COLLAB=http://svn.red-bean.com/svnbook

BUILDTMP_DIR = build.tmp
BOOKFILES = appa.xml appb.xml appc.xml book.xml ch00.xml ch01.xml \
			ch02.xml ch03.xml ch04.xml ch05.xml ch06.xml ch07.xml \
			ch08.xml ch09.xml copyright.xml foreword.xml

BDTMP = bd.tmp
BDFILES = book.xml foreword.xml ch00.xml ch01.xml ch02.xml ch03.xml \
          ch04.xml appa.xml ch05.xml ch06.xml ch07.xml ch08.xml \
          ch09.xml appb.xml appc.xml copyright.xml

build: dircheck htmlbook copytobuild

dircheck:
	mkdir $(BUILDTMP_DIR)
	rmdir $(BUILDTMP_DIR)

htmlbook:
	$(MAKE) clean && $(MAKE) all-html

copytobuild:
	svn co http://svn.sunbase.org/repos/svnbook_nb/build $(BUILDTMP_DIR)
	find $(BUILDTMP_DIR) -type f | grep -v '/\.svn/' | xargs rm
	cp -a $(BOOK_HTML_CHUNK_DIR) $(BUILDTMP_DIR)/
	cp -a $(BOOK_HTML_TARGET) $(BUILDTMP_DIR)/
	cp -a $(BOOK_IMAGES) $(BUILDTMP_DIR)/images/
	cp -a $(BOOK_DIR)/styles.css $(BUILDTMP_DIR)/

editmode:
	$(MAKE) convfiles CONV_PARAM=-e

commitmode:
	$(MAKE) convfiles CONV_PARAM=

convfiles:
	@for _prc in $(BOOKFILES); do \
		echo -n "$$_prc: convert..."; \
		dest=book/$$_prc; \
		swp=book/.$$_prc.swp; \
		if [ -e $$swp ]; then \
			echo "$$swp: Swap file found. Is the file being edited?" >&2; \
			exit 1; \
		fi; \
		bin/clean_files $(CONV_PARAM) $$dest >$$dest.tmp || exit 1; \
		echo -n "move..."; \
		mv $$dest.tmp $$dest; \
		echo "OK."; \
	done

bookdiff:
	@mkdir $(BDTMP) || { \
		echo -n "\"$(BDTMP)\" already exists. Press ENTER to delete it, or CTRL-C to abort..." >&2; \
		read; \
		rm -rf $(BDTMP); \
		mkdir $(BDTMP); \
	}
	$(MAKE) engdir
	cp -a book/eng $(BDTMP)/
	@for _ptd in $(BDFILES); do \
		cat $(BDTMP)/eng/$$_ptd | bin/clean_files -e >>$(BDTMP)/eng.txt; \
		cat book/$$_ptd | bin/clean_files -e >>$(BDTMP)/norw.txt; \
	done
	@echo
	@echo Checking files against the English version. If everything is fine,
	@echo you should see only one line of output between the ===== lines.
	@echo =====
	@diff -du $(BDTMP)/eng.txt $(BDTMP)/norw.txt | grep ^-
	@echo =====
	@echo
	@echo The temporary files are still in $(BDTMP)/ for you to play with.
	@echo
	@echo -n Press Enter to launch vimdiff or CTRL-C to exit...
	@read
	vimdiff $(BDTMP)/norw.txt $(BDTMP)/eng.txt

engdir:
	@if [ -d book/eng ]; then \
		svn up -r`cat LAST_UPDATED` book/eng; \
	else \
		svn co -r`cat LAST_UPDATED` $(COLLAB)/trunk/src/en/book book/eng; \
	fi

sync:
	mkdir sync.LOCK
	svn update
	@echo ======= START svn status =======
	@svn status -q
	@echo ======== END svn status ========
	@echo -n Press Enter to continue or CTRL-C to abort...
	@read
	@if [ "$(HEAD)" = "" ]; then \
		svnversion . | tr -d M >HEADREV; \
	else \
		echo $(HEAD) >HEADREV; \
		echo "Note: Merging up to r$(HEAD) instead of actual repository HEAD"; \
	fi;
	$(MAKE) editmode
	svn merge -r$$(cat LAST_UPDATED):$$(cat HEADREV) $(COLLAB)/trunk/src/en .
	mv HEADREV LAST_UPDATED
	$(MAKE) commitmode
	rmdir sync.LOCK

# $Id: Makefile 1994 2006-02-12 01:37:34Z sunny256 $
# vim: set ts=4 sw=4 sts=4 noet :
