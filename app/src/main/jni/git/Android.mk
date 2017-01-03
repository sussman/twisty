LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := git
LOCAL_SRC_FILES := accel.c compiler.c gestalt.c git.c git_unix.c glkop.c \
	heap.c memory.c opcodes.c operands.c peephole.c savefile.c saveundo.c \
	search.c terp.c twisty.c

# Rename the glk main/shutdown hooks so they don't conflict with other terps.
LOCAL_CFLAGS := -I$(LOCAL_PATH)/../glkjni -DANDROID -DFAST -DUSE_INLINE \
	-Dglkunix_arguments=glkunix_arguments_git \
	-Dglkunix_startup_code=glkunix_startup_code_git \
	-Dglk_main=glk_main_git \
	-Dglk_shutdown=glk_shutdown_git \
	-g
LOCAL_STATIC_LIBRARIES := glkjni

include $(BUILD_STATIC_LIBRARY)
