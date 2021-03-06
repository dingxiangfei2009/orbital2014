JNI_DIR := $(call my-dir)
include $(call all-subdir-makefiles)
include $(CLEAR_VARS)
LOCAL_PATH := $(JNI_DIR)
include $(CLEAR_VARS)
LOCAL_MODULE := appsupport
LOCAL_CFLAGS := -g -Werror -Ofast# -frtti -fexceptions
LOCAL_CPP_EXTENSION := .cpp .cc
LOCAL_C_INCLUDES := $(LOCAL_PATH) \
	$(LOCAL_PATH)/jsoncpp \
	$(LOCAL_PATH)/libpng \
	$(LOCAL_PATH)/libzip \
	$(LOCAL_PATH)/v8
LOCAL_SRC_FILES := appsupport.cpp SoundEngine.cpp ExtensionEngine.cpp ResourceManager.cpp GridRendererGraphicsImpl.cpp
LOCAL_STATIC_LIBRARIES := libzip libpng jsoncpp libwebsockets
LOCAL_LDLIBS := -llog -lstdc++ -lOpenSLES -lGLESv2 -ljnigraphics -lz -landroid -lm
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libv8_base.a
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libv8_snapshot.a
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libv8_libbase.arm.a
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libicui18n.a
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libicuuc.a
LOCAL_LDLIBS += $(LOCAL_PATH)/v8/libicudata.a

include $(BUILD_SHARED_LIBRARY)
