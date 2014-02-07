LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := glkmain
LOCAL_SRC_FILES := main.c

LOCAL_CFLAGS := -I$(LOCAL_PATH)/../glkjni -DANDROID
LOCAL_STATIC_LIBRARIES := glkjni

include $(BUILD_STATIC_LIBRARY)
