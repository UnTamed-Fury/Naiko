#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <locale.h>
#include <atomic>

#include <mpv/client.h>

#include <pthread.h>

extern "C" {
    #include <libavcodec/jni.h>
}

#include "log.h"
#include "jni_utils.h"
#include "event.h"

#define ARRAYLEN(a) (sizeof(a)/sizeof(a[0]))

extern "C" {
    jni_func(void, create, jobject appctx, jstring log_lvl);
    jni_func(void, init);
    jni_func(void, destroy);

    jni_func(void, command, jobjectArray jarray);
};

JavaVM *g_vm;
mpv_handle *g_mpv;
std::atomic<bool> g_event_thread_request_exit(false);

static pthread_t event_thread_id;

static void prepare_environment(JNIEnv *env, jobject appctx) {
    setlocale(LC_NUMERIC, "C");

    if (!env->GetJavaVM(&g_vm) && g_vm)
        av_jni_set_java_vm(g_vm, NULL);
    init_methods_cache(env);
}

jni_func(void, create, jobject appctx, jstring log_lvl) {
    prepare_environment(env, appctx);

    if (g_mpv) {
        die("mpv is already initialized");
        return;
    }

    g_mpv = mpv_create();
    if (!g_mpv) {
        die("context init failed");
        return;
    }

    const char *log_lvl_string = env->GetStringUTFChars(log_lvl, 0);
    mpv_request_log_messages(g_mpv, log_lvl_string);
    env->ReleaseStringUTFChars(log_lvl, log_lvl_string);
}

jni_func(void, init) {
    if (!g_mpv) {
        die("mpv is not created");
        return;
    }

    if (mpv_initialize(g_mpv) < 0) {
        die("mpv init failed");
        return;
    }

#ifdef __aarch64__
    ALOGV("You're using the 64-bit build of mpv!");
#endif

    g_event_thread_request_exit = false;
    pthread_create(&event_thread_id, NULL, event_thread, NULL);
}

jni_func(void, destroy) {
    if (!g_mpv) {
        die("mpv destroy called but it's already destroyed");
        return;
    }

    // poke event thread and wait for it to exit
    g_event_thread_request_exit = true;
    mpv_wakeup(g_mpv);
    pthread_join(event_thread_id, NULL);

    mpv_terminate_destroy(g_mpv);
    g_mpv = NULL;
}

jni_func(void, command, jobjectArray jarray) {
    const char *arguments[128] = { 0 };
    int len = env->GetArrayLength(jarray);
    if (!g_mpv) {
        die("Cannot run command: mpv is not initialized");
        return;
    }
    if (len >= ARRAYLEN(arguments)) {
        die("Cannot run command: too many arguments");
        return;
    }

    for (int i = 0; i < len; ++i)
        arguments[i] = env->GetStringUTFChars((jstring)env->GetObjectArrayElement(jarray, i), NULL);

    mpv_command(g_mpv, arguments);

    for (int i = 0; i < len; ++i)
        env->ReleaseStringUTFChars((jstring)env->GetObjectArrayElement(jarray, i), arguments[i]);
}
