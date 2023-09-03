//
// Created by baishengye on 2023-09-03.
//
#include <android/log.h>

#ifndef LOG_TAG
#define LOG_TAG "ANDROID_LAB"    //log信息的标签
#define LOGD(TAG, ...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(TAG, ...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(TAG, ...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(TAG, ...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(TAG, ...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型
#endif

#ifndef AUDIORECORDERDEMO_ANDROIDLOG_HPP
#define AUDIORECORDERDEMO_ANDROIDLOG_HPP

#endif //AUDIORECORDERDEMO_ANDROIDLOG_HPP
