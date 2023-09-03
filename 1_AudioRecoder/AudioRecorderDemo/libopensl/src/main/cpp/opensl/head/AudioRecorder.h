//
// Created by baishengye on 2023-09-03.
//
#ifndef AUDIORECORDERDEMO_AUDIORECORDER_H
#define AUDIORECORDERDEMO_AUDIORECORDER_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <stdio.h>
#include "../source/AudioEngine.hpp"

class AudioRecorder {
private:
    FILE *mFile;

    AudioEngine *mAudioEngine;
    SLObjectItf mRecorderObj;
    SLRecordItf mRecorder;
    SLAndroidSimpleBufferQueueItf mBufferQueue;

    unsigned mBufSize;
    short *mBuffers[2];
    int mIndex;

    bool mIsRecording;
    bool mIsInitialized;

private:
    bool initRecorder();

    void release();

    static void recorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context);

public:
    AudioRecorder(const char *filePath);

    bool start();

    void stop();

    virtual ~AudioRecorder();
};


#endif //AUDIORECORDERDEMO_AUDIORECORDER_H
