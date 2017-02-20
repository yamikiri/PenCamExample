#include <jni.h>
#include <string>
#include <android/log.h>

#ifdef __ANDROID__

#include "include/libuvc/libuvc.h"

#define LOG_TAG "penCam-JNI"
#define LOGI(...) ((void)__android_log_print( ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__))
#define LOGD(...) ((void)__android_log_print( ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print( ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))

#else
#include "xu.h"
#endif

int32_t Solidyear_SetLed(int32_t fd, uint8_t bLedSwitch) {
#ifdef __ANDROID__
    return 0;
#else
    return XU_RegSet(SOLIDYEAR_LED_SWITCH, bLedSwitch, fd);
#endif
}

int32_t Solidyear_GetHotkey(int32_t fd, uint8_t *pHotkeyStatus) {
#ifdef __ANDROID__
    return 0;
#else
    return XU_RegGet(SOLIDYEAR_HOTKEY_STATUS, pHotkeyStatus, fd);
#endif
}

int32_t Solidyear_GetSnapshot(int32_t fd, uint8_t *pSnapshotStatus) {
#ifdef __ANDROID__
    return 0;
#else
    return XU_RegGet(SOLIDYEAR_SNAPSHOT_STATUS, pSnapshotStatus, fd);
#endif
}

int32_t Solidyear_SetSnapshot(int32_t fd, uint8_t bSnapshotStatus) {
#ifdef __ANDROID__
    return 0;
#else
    return XU_RegSet(SOLIDYEAR_SNAPSHOT_STATUS, bSnapshotStatus, fd);
#endif
}

typedef struct _customI2Ccmd {
    uint32_t action;
    uint16_t addr;
    uint8_t value;
} i2c_cmd;

enum _action {
    I2C_WRITE,
    I2C_READ
};

extern "C"
jstring
Java_tw_com_solidyear_pencamexample_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    int32_t fd;
    int32_t ret;
    uint32_t tmp;
    uint8_t value;
    i2c_cmd cmd;
    char uid[128];

    uvc_context_t *ctx;
    uvc_device_t *dev;
    uvc_device_handle_t *devh;
    uvc_stream_ctrl_t ctrl;
    uvc_error_t res;

    res = uvc_init(&ctx, NULL);
    if (res < 0) {
        LOGE("uvc_init");
        //return res;
    }

    LOGI("UVC initialized\n");
    /* Locates the first attached UVC device, stores in dev */
    res = uvc_find_device(
            ctx, &dev,
            0x60b/*0*/, 0, NULL); /* filter devices: vendor_id, product_id, "serial_num" */

    if (res < 0) {
        LOGE("uvc_find_device"); /* no devices found */
    } else {
        LOGI("Device found\n");

        /* Try to open the device: requires exclusive access */
        res = uvc_open(dev, &devh);

        if (res < 0) {
            LOGE("uvc_open"); /* unable to open device */
        } else {
            LOGI("Device opened\n");

            /* Print out a message containing all the information that libuvc
                  * knows about the device */
            uvc_print_diag(devh, stderr);

            uvc_print_stream_ctrl(&ctrl, stderr);
            if (res < 0) {
                LOGE("get_mode"); /* device doesn't provide a matching stream */
            } else {

            }
        }
    }
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
