LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := twistyterps
LOCAL_SRC_FILES := automap.c solve.c infix.c copying.c debug.c inform.c \
	quetzal.c undo.c op_call.c decode.c errmesg.c globals.c iff.c init.c \
	io.c z_io.c op_jmp.c op_math.c op_save.c op_table.c op_v6.c oplist.c \
	stack.c zscii.c tokenise.c struct.c objects.c portfunc.c hash.c \
	sound.c graphics.c blorb.c main.c startunix.c twisty.c

LOCAL_CFLAGS := -I$(LOCAL_PATH)/../glkjni -DANDROID -DSMART_TOKENISER -DFAST \
	-DUSE_INLINE -DNO_TICK
LOCAL_STATIC_LIBRARIES := glkjni
LOCAL_LDLIBS    := -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)

