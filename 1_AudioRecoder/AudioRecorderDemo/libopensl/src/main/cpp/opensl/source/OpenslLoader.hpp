//
// Created by baishengye on 2023-09-03.
//

#include "../head/AudioRecorder.h"

AudioRecorder *audioRecorder = nullptr;

extern "C"
JNIEXPORT jboolean  JNICALL
Java_com_baishengye_libopensl_OpenslLoader_startRecord(JNIEnv *env, jobject thiz,
                                                       jstring file_path) {
    const char *filePath = env->GetStringUTFChars(file_path, 0);

    if (audioRecorder) {
        audioRecorder->stop();
        delete audioRecorder;
    }
    audioRecorder = new AudioRecorder(filePath);

    env->ReleaseStringUTFChars(file_path, filePath);

    return (jboolean) audioRecorder->start();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_baishengye_libopensl_OpenslLoader_stopRecord(JNIEnv *env, jobject thiz) {
    if (!audioRecorder) {
        return;
    }
    audioRecorder->stop();
    delete audioRecorder;
    audioRecorder = nullptr;
}