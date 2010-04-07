LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := glkjni
LOCAL_SRC_FILES := event.c fileref.c fstream.c gi_blorb.c gi_dispa.c glkjni.c \
jcall.c latin1.c main.c mstream.c sound.c stream.c unicode.c \
win_gfx.c win_text.c window.c
LOCAL_CFLAGS    := -DANDROID
LOCAL_LDLIBS    := -L$(SYSROOT)/usr/lib -llog

include $(BUILD_STATIC_LIBRARY)
