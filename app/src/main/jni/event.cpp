#include <jni.h>

#include <mpv/client.h>

#include "globals.h"
#include "jni_utils.h"
#include "log.h"

static void sendPropertyUpdateToJava(JNIEnv *env, mpv_event_property *prop) {
    jstring jprop = env->NewStringUTF(prop->name);
    jstring jvalue = NULL;
    switch (prop->format) {
    case MPV_FORMAT_NONE:
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_S, jprop);
        break;
    case MPV_FORMAT_FLAG:
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_Sb, jprop,
            (jboolean) (*(int*)prop->data != 0));
        break;
    case MPV_FORMAT_INT64:
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_Sl, jprop,
            (jlong) *(int64_t*)prop->data);
        break;
    case MPV_FORMAT_DOUBLE:
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_Sd, jprop,
            (jdouble) *(double*)prop->data);
        break;
    case MPV_FORMAT_STRING:
        jvalue = env->NewStringUTF(*(const char**)prop->data);
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_SS, jprop, jvalue);
        break;
    default:
        ALOGV("sendPropertyUpdateToJava: Unknown property update format received in callback: %d!", prop->format);
        break;
    }
    if (jprop)
        env->DeleteLocalRef(jprop);
    if (jvalue)
        env->DeleteLocalRef(jvalue);
}

static void sendEventToJava(JNIEnv *env, int event) {
    env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_event, event);
}

static void sendEefErrorToJava(JNIEnv *env, mpv_event_end_file *eef) {
    if (eef->error) {
        jstring jerr = env->NewStringUTF(mpv_error_string(eef->error));
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_efEvent, jerr);
        if (jerr)
            env->DeleteLocalRef(jerr);
    }
}

static inline bool invalid_utf8(unsigned char c) {
    return c == 0xc0 || c == 0xc1 || c >= 0xf5;
}

static void sendLogMessageToJava(JNIEnv *env, mpv_event_log_message *msg) {
    // filter the most obvious cases of invalid utf-8
    int invalid = 0;
    for (int i = 0; msg->text[i]; i++)
        invalid |= invalid_utf8((unsigned char) msg->text[i]);
    if (invalid)
        return;

    jstring jprefix = env->NewStringUTF(msg->prefix);
    jstring jtext = env->NewStringUTF(msg->text);

    env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_logMessage_SiS,
        jprefix, (jint) msg->log_level, jtext);

    if (jprefix)
        env->DeleteLocalRef(jprefix);
    if (jtext)
        env->DeleteLocalRef(jtext);
}

void *event_thread(void *arg) {
    JNIEnv *env = NULL;
    acquire_jni_env(g_vm, &env);
    if (!env) {
        die("failed to acquire java env");
        return NULL;
    }

    while (1) {
        mpv_event *mp_event;
        mpv_event_property *mp_property = NULL;
        mpv_event_log_message *msg = NULL;
        mpv_event_end_file *eef = NULL;

        mp_event = mpv_wait_event(g_mpv, -1.0);

        if (g_event_thread_request_exit)
            break;

        if (mp_event->event_id == MPV_EVENT_NONE)
            continue;

        switch (mp_event->event_id) {
        case MPV_EVENT_LOG_MESSAGE:
            msg = (mpv_event_log_message*)mp_event->data;
            sendLogMessageToJava(env, msg);
            break;
        case MPV_EVENT_PROPERTY_CHANGE:
            mp_property = (mpv_event_property*)mp_event->data;
            sendPropertyUpdateToJava(env, mp_property);
            break;
        case MPV_EVENT_END_FILE:
            sendEventToJava(env, mp_event->event_id);
            eef = (mpv_event_end_file*)mp_event->data;
            sendEefErrorToJava(env, eef);
            break;
        default:
            sendEventToJava(env, mp_event->event_id);
            break;
        }
    }

    g_vm->DetachCurrentThread();

    return NULL;
}
