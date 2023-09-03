//
// Created by baishengye on 2023-09-03.
//
#include "jni.h"

#ifndef AUDIORECORDERDEMO_AUDIOBUFFER_H
#define AUDIORECORDERDEMO_AUDIOBUFFER_H

class AudioBuffer {
public:
    //short类型的二维数组,short:2byte
    short **buffer;
    int index;//下标

public:
    AudioBuffer();//构造函数
    ~AudioBuffer();//析构函数

    short *getUnprocessedBuffer();//获取未处理过的数据
    short *getProgressedBuffer();//获取处理完了的数据
};

#endif //AUDIORECORDERDEMO_AUDIOBUFFER_H
