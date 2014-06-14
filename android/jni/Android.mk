JNI_DIR := $(call my-dir)
include $(call all-subdir-makefiles)
include $(CLEAR_VARS)
LOCAL_PATH := $(JNI_DIR)
include $(CLEAR_VARS)
LOCAL_MODULE := appsupport
LOCAL_CFLAGS := -Werror
LOCAL_CPP_EXTENSION := .cpp .cc
LOCAL_C_INCLUDE := $(LOCAL_PATH)
LOCAL_SRC_FILES := appsupport.cpp SoundEngine.cpp ExtensionEngine.cpp
LOCAL_STATIC_LIBRARIES := libzip libpng
LOCAL_LDLIBS := -llog -lstdc++ -lOpenSLES -lGLESv2 -ljnigraphics
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libv8_base.a
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libv8_snapshot.a
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libv8_libbase.arm.a
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libicui18n.a
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libicuuc.a
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libicudata.a
LOCAL_LDLIBS += $(LOCAL_PATH)/libstlport_static.a

include $(BUILD_SHARED_LIBRARY)
