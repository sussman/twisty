LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := nitfol
LOCAL_SRC_FILES := automap.c solve.c infix.c copying.c debug.c inform.c \
	quetzal.c undo.c op_call.c decode.c errmesg.c globals.c iff.c init.c \
	io.c z_io.c op_jmp.c op_math.c op_save.c op_table.c op_v6.c oplist.c \
	stack.c zscii.c tokenise.c struct.c objects.c portfunc.c hash.c \
	sound.c graphics.c blorb.c main.c startunix.c twisty.c

# Rename the glk main/shutdown hooks so they don't conflict with other terps.
LOCAL_CFLAGS := -I$(LOCAL_PATH)/../glkjni -DANDROID -DSMART_TOKENISER -DFAST \
	-DUSE_INLINE -DNO_TICK \
	-Dglkunix_arguments=glkunix_arguments_nitfol \
	-Dglkunix_startup_code=glkunix_startup_code_nitfol \
	-Dglk_main=glk_main_nitfol \
	-Dglk_shutdown=glk_shutdown_nitfol \
	-g
LOCAL_STATIC_LIBRARIES := glkjni

include $(BUILD_STATIC_LIBRARY)
