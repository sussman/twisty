LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := twistyterps
LOCAL_SRC_FILES := model.c
LOCAL_CFLAGS := -I../glkjni -DANDROID
LOCAL_STATIC_LIBRARIES := glkjni
LOCAL_LDLIBS    := -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)

