# GCC Makefile for GlkJNI library.

CC = gcc -ansi

OPTIONS = -Wall -g
#OPTIONS = -O -DNDEBUG

## Windows JNI
#JNI_INCLUDES = \
#  -I"C:\Program Files\Java\jdk1.x.x\include" \
#  -I"C:\Program Files\Java\jdk1.x.x\include\win32"
#JVM_LIB = -LC:\Program\\ Files\Java\jdk1.x.x\lib -ljvm

## OS X JNI
#JNI_INCLUDES = -I/Library/Java/Home/include
#JVM_LIB = \
#  -L/System/Library/Frameworks/JavaVM.framework/Libraries \
#  -framework JavaVM -ljvm

## Linux JNI
#JNI_INCLUDES = \
#  -I/path/to/jdk/include \
#  -I/path/to/jdk/include/linux
#JVM_LIB = -L/path/to/jre/lib/i386/client -ljvm

JNI_INCLUDES = -I/opt/jdk/include -I/opt/jdk/include/linux
JVM_LIB = -L/opt/jdk/jre/lib/i386/client -ljvm

CFLAGS = $(OPTIONS) $(JNI_INCLUDES)

GLKLIB = libglkjni.a

BASE_OBJS = \
  main.o latin1.o glkjni.o jcall.o fileref.o event.o unicode.o \
  sound.o gi_dispa.o gi_blorb.o

WINDOW_OBJS = \
  window.o win_text.o win_gfx.o

STREAM_OBJS = \
  stream.o mstream.o fstream.o

HEADERS = \
  glk.h glkjni.h jcall.h

OBJS = $(BASE_OBJS) $(STREAM_OBJS) $(WINDOW_OBJS)

all: $(GLKLIB) Make.glkjni

unicode.o: unigen.c

$(GLKLIB): $(OBJS)
	ar r $(GLKLIB) $(OBJS)
	ranlib $(GLKLIB)

Make.glkjni:
	echo "LINKLIBS = $(JVM_LIB)" > Make.glkjni
	echo "GLKLIB = -lglkjni" >> Make.glkjni

$(OBJS): $(HEADERS)

$(STREAM_OBJS): stream.h $(HEADERS)

$(WINDOW_OBJS): window.h $(HEADERS)

clean:
	$(RM) *.o $(GLKLIB) Make.glkjni
