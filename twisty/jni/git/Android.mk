LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := gitterp
LOCAL_SRC_FILES := accel.c compiler.c gestalt.c git.c git_unix.c glkop.c \
	heap.c memory.c opcodes.c operands.c peephole.c savefile.c saveundo.c \
	search.c terp.c twisty.c

LOCAL_CFLAGS := -I$(LOCAL_PATH)/../glkjni -DANDROID -DSMART_TOKENISER -DFAST \
	-DUSE_INLINE -DNO_TICK -g
LOCAL_STATIC_LIBRARIES := glkjni
LOCAL_LDLIBS    := -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)

