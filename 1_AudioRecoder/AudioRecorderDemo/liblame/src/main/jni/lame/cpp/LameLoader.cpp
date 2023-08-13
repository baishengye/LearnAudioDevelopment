/*****************************************************************************/
#include "LameLoader.h"
#include "../include/lame.h"
#include <cstdlib>
#include <cstring>
#include <cmath>
#include <cstdio>
#include <android/log.h>

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif


static lame_global_flags *lame_context;
static hip_t hip_context;
static mp3data_struct *mp3data;
static int enc_delay, enc_padding;

/**
 * 获取Lame版本号
 * @return 版本号*/
jstring
Java_com_baishengye_liblame_LameLoader_getLameVersion(JNIEnv *env, jobject clazz) {
    return env->NewStringUTF(get_lame_version());
}


jint Java_com_baishengye_liblame_LameLoader_initializeEncoder(JNIEnv *env, jobject clazz,
                                                              jint in_sample_rate,
                                                              jint num_channels,
                                                              jint out_sample_rate,
                                                              jint out_bitrate,
                                                              jint quality) {
    if (!lame_context) {
        lame_context = lame_init();
        if (lame_context) {
            lame_set_in_samplerate(lame_context, in_sample_rate);
            lame_set_num_channels(lame_context, num_channels);
            lame_set_out_samplerate(lame_context, out_sample_rate);
            lame_set_brate(lame_context, out_bitrate);
            lame_set_quality(lame_context, quality);
            int ret = lame_init_params(lame_context);
            __android_log_print(ANDROID_LOG_DEBUG, "LameLoader.so", "initialized lame with code %d",
                                ret);
            return ret;
        }
    }
    return -1;
}


void Java_com_baishengye_liblame_LameLoader_setEncoderPreset
        (JNIEnv *env, jobject clazz, jint preset) {
    switch (preset) {
        case baishengye_liblame_LameLoader_LAME_PRESET_MEDIUM:
            lame_set_VBR_q(lame_context, 4);
            lame_set_VBR(lame_context, vbr_rh);
            break;
        case baishengye_liblame_LameLoader_LAME_PRESET_STANDARD:
            lame_set_VBR_q(lame_context, 2);
            lame_set_VBR(lame_context, vbr_rh);
            break;
        case baishengye_liblame_LameLoader_LAME_PRESET_EXTREME:
            lame_set_VBR_q(lame_context, 0);
            lame_set_VBR(lame_context, vbr_rh);
            break;
        case baishengye_liblame_LameLoader_LAME_PRESET_DEFAULT:
        default:
            break;
    }
}


jint Java_com_baishengye_liblame_LameLoader_encode
        (JNIEnv *env, jobject clazz, jshortArray leftChannel, jshortArray rightChannel,
         jint channelSamples, jbyteArray mp3Buffer, jint bufferSize) {
    int encoded_samples;
    short *left_buf, *right_buf;
    jbyte *mp3_buf;

    left_buf = env->GetShortArrayElements(leftChannel, nullptr);
    right_buf = env->GetShortArrayElements(rightChannel, nullptr);
    mp3_buf = env->GetByteArrayElements(mp3Buffer, nullptr);

    encoded_samples = lame_encode_buffer(lame_context, left_buf, right_buf, channelSamples,
                                         (unsigned char *) mp3_buf, bufferSize);

// mode 0 means free left/right buf, write changes back to left/rightChannel
    env->ReleaseShortArrayElements(leftChannel, left_buf, 0);
    env->ReleaseShortArrayElements(rightChannel, right_buf, 0);

    if (encoded_samples < 0) {
// don't propagate changes back up if we failed
        env->ReleaseByteArrayElements(mp3Buffer, mp3_buf, JNI_ABORT);
        return -1;
    }

    env->ReleaseByteArrayElements(mp3Buffer, mp3_buf, 0);
    return encoded_samples;
}


jint Java_com_baishengye_liblame_LameLoader_flushEncoder
        (JNIEnv *env, jobject clazz, jbyteArray mp3Buffer, jint bufferSize) {
// call lame_encode_flush when near the end of pcm buffer
    int num_bytes;
    jbyte *mp3_buf;

    mp3_buf = env->GetByteArrayElements(mp3Buffer, nullptr);

    num_bytes = lame_encode_flush(lame_context, (unsigned char *) mp3_buf, bufferSize);
    if (num_bytes < 0) {
// some kind of error occurred, don't propagate changes to buffer
        env->ReleaseByteArrayElements(mp3Buffer, mp3_buf, JNI_ABORT);
        return num_bytes;
    }

    env->ReleaseByteArrayElements(mp3Buffer, mp3_buf, 0);
    return num_bytes;
}


jint Java_com_baishengye_liblame_LameLoader_closeEncoder
        (JNIEnv *env, jobject clazz) {
    if (lame_context) {
        int ret = lame_close(lame_context);
        lame_context = nullptr;
        __android_log_print(ANDROID_LOG_DEBUG, "LameLoader.so", "freed lame with code %d", ret);
        return ret;
    }
    return -1;
}

void Java_com_baishengye_liblame_LameLoader_wav2mp3(JNIEnv *env, jobject clazz,
                                                    jstring wav_path, jstring mp3_path) {
    const char *wavPath = env->GetStringUTFChars(wav_path, nullptr);
    const char *mp3Path = env->GetStringUTFChars(mp3_path, nullptr);
    //open input file and output file
    FILE *fInput = fopen(wavPath, "rb");
    FILE *fMp3 = fopen(mp3Path, "wb");
    //将起始字节定位到45个字节处，因为前44个字节是Wav的头文件
    fseek(fInput, 44, SEEK_SET);
    short int inputBuffer[baishengye_liblame_LameLoader_MP3_BUFFER_SIZE * 2];
    unsigned char mp3Buffer[baishengye_liblame_LameLoader_MP3_BUFFER_SIZE];//You must specified at least 7200
    int read = 0; // number of bytes in inputBuffer, if in the end return 0
    int write = 0;// number of bytes output in mp3buffer.  can be 0
    long total = 44; // the bytes of reading input file
    int nowConvertBytes = 0;

    //convert to mp3
    do {
        read = fread(inputBuffer, sizeof(short int) * 2,
                     baishengye_liblame_LameLoader_MP3_BUFFER_SIZE, fInput);
        total += read * 2;
        nowConvertBytes = total;
        if (read != 0) {
            write = lame_encode_buffer_interleaved(lame_context, inputBuffer, read, mp3Buffer,
                                                   baishengye_liblame_LameLoader_MP3_BUFFER_SIZE);
            //write the converted buffer to the file
            fwrite(mp3Buffer, sizeof(unsigned char), write, fMp3);
        }
        //if in the end flush
        if (read == 0) {
            lame_encode_flush(lame_context, mp3Buffer,
                              baishengye_liblame_LameLoader_MP3_BUFFER_SIZE);
        }
    } while (read != 0);

    fclose(fInput);
    fclose(fMp3);
    env->ReleaseStringUTFChars(wav_path, wavPath);
    env->ReleaseStringUTFChars(mp3_path, mp3Path);
    nowConvertBytes = -1;
}

void Java_com_baishengye_liblame_LameLoader_wav2mp3Speed(JNIEnv *env, jobject clazz,
                                                         jstring wav_path,
                                                         jstring mp3_path, jint speed) {
    lame_set_out_samplerate(lame_context, lame_get_out_samplerate(lame_context) * speed);
    lame_init_params(lame_context);

    Java_com_baishengye_liblame_LameLoader_wav2mp3(env, clazz, wav_path, mp3_path);
}

void Java_com_baishengye_liblame_LameLoader_wav2mp3shear(JNIEnv *env, jobject clazz,
                                                         jstring wav_path,
                                                         jstring mp3_path, jfloat start,
                                                         jfloat end) {
    const char *wavPath = env->GetStringUTFChars(wav_path, nullptr);
    const char *mp3Path = env->GetStringUTFChars(mp3_path, nullptr);
    //open input file and output file
    FILE *fInput = fopen(wavPath, "rb");
    FILE *fMp3 = fopen(mp3Path, "wb");
    //将起始字节定位到45个字节处，因为前44个字节是Wav的头文件
    fseek(fInput, 44, SEEK_SET);
    short int inputBuffer[baishengye_liblame_LameLoader_MP3_BUFFER_SIZE * 2];
    unsigned char mp3Buffer[baishengye_liblame_LameLoader_MP3_BUFFER_SIZE];//You must specified at least 7200
    int read = 0; // number of bytes in inputBuffer, if in the end return 0
    int write = 0;// number of bytes output in mp3buffer.  can be 0
    long total = 44; // the bytes of reading input file
    int nowConvertBytes = 0;


    //convert to mp3
    do {
        read = fread(inputBuffer, sizeof(short int) * 2,
                     baishengye_liblame_LameLoader_MP3_BUFFER_SIZE, fInput);
        if (total)
            total += read * 2;
        nowConvertBytes = total;
        if (read != 0) {
            write = lame_encode_buffer_interleaved(lame_context, inputBuffer, read, mp3Buffer,
                                                   baishengye_liblame_LameLoader_MP3_BUFFER_SIZE);
            //write the converted buffer to the file
            fwrite(mp3Buffer, sizeof(unsigned char), write, fMp3);
        }
        //if in the end flush
        if (read == 0) {
            lame_encode_flush(lame_context, mp3Buffer,
                              baishengye_liblame_LameLoader_MP3_BUFFER_SIZE);
        }
    } while (read != 0);

    fclose(fInput);
    fclose(fMp3);
    env->ReleaseStringUTFChars(wav_path, wavPath);
    env->ReleaseStringUTFChars(mp3_path, mp3Path);
    nowConvertBytes = -1;
}


/*****************************************************下面是解码操作******************************************************************************/


jint Java_com_baishengye_liblame_LameLoader_initializeDecoder
        (JNIEnv *env, jobject clazz) {
    if (!hip_context) {
        hip_context = hip_decode_init();
        if (hip_context) {
            mp3data = (mp3data_struct *) malloc(sizeof(mp3data_struct));
            memset(mp3data, 0, sizeof(mp3data_struct));
            enc_delay = -1;
            enc_padding = -1;
            return 0;
        }
    }
    return -1;
}


jint Java_com_baishengye_liblame_LameLoader_nativeConfigureDecoder
        (JNIEnv *env, jobject clazz, jbyteArray mp3Buffer, jint bufferSize) {
    int ret = -1;
    short left_buf[1152], right_buf[1152];
    jbyte *mp3_buf;

    if (mp3data) {
        mp3_buf = env->GetByteArrayElements(mp3Buffer, nullptr);
        if (mp3data->header_parsed) {
            mp3data->totalframes = mp3data->nsamp / mp3data->framesize;
            ret = 0;
            __android_log_print(ANDROID_LOG_DEBUG, "LameLoader.so",
                                "decoder configured successfully");
            __android_log_print(ANDROID_LOG_DEBUG, "LameLoader.so", "sample rate: %d, channels: %d",
                                mp3data->samplerate, mp3data->stereo);
            __android_log_print(ANDROID_LOG_DEBUG, "LameLoader.so", "bitrate: %d, frame size: %d",
                                mp3data->bitrate, mp3data->framesize);
        } else {
            ret = -1;
        }
        env->ReleaseByteArrayElements(mp3Buffer, mp3_buf, 0);
    }

    return ret;
}


jint Java_com_baishengye_liblame_LameLoader_getDecoderChannels
        (JNIEnv *env, jobject clazz) {
    return mp3data->stereo;
}


jint Java_com_baishengye_liblame_LameLoader_getDecoderSampleRate
        (JNIEnv *env, jobject clazz) {
    return mp3data->samplerate;
}


jint Java_com_baishengye_liblame_LameLoader_getDecoderDelay
        (JNIEnv *env, jobject clazz) {
    return enc_delay;
}


jint Java_com_baishengye_liblame_LameLoader_getDecoderPadding
        (JNIEnv *env, jobject clazz) {
    return enc_padding;
}


jint Java_com_baishengye_liblame_LameLoader_getDecoderTotalFrames
        (JNIEnv *env, jobject clazz) {
    return mp3data->totalframes;
}


jint Java_com_baishengye_liblame_LameLoader_getDecoderFrameSize
        (JNIEnv *env, jobject clazz) {
    return mp3data->framesize;
}


jint Java_com_baishengye_liblame_LameLoader_getDecoderBitrate
        (JNIEnv *env, jobject clazz) {
    return mp3data->bitrate;
}


jint Java_com_baishengye_liblame_LameLoader_nativeDecodeFrame
        (JNIEnv *env, jobject clazz, jbyteArray mp3Buffer, jint bufferSize,
         jshortArray rightChannel, jshortArray leftChannel) {
    int samples_read;
    short *left_buf, *right_buf;
    jbyte *mp3_buf;

    left_buf = env->GetShortArrayElements(leftChannel, nullptr);
    right_buf = env->GetShortArrayElements(rightChannel, nullptr);
    mp3_buf = env->GetByteArrayElements(mp3Buffer, nullptr);

    samples_read = hip_decode1_headers(hip_context, (unsigned char *) mp3_buf, bufferSize, left_buf,
                                       right_buf, mp3data);

    env->ReleaseByteArrayElements(mp3Buffer, mp3_buf, 0);

    if (samples_read < 0) {
// some sort of error occurred, don't propagate changes to buffers
        env->ReleaseShortArrayElements(leftChannel, left_buf, JNI_ABORT);
        env->ReleaseShortArrayElements(rightChannel, right_buf, JNI_ABORT);
        return samples_read;
    }

    env->ReleaseShortArrayElements(leftChannel, left_buf, 0);
    env->ReleaseShortArrayElements(rightChannel, right_buf, 0);

    return samples_read;
}


jint Java_com_baishengye_liblame_LameLoader_closeDecoder
        (JNIEnv *env, jobject clazz) {
    if (hip_context) {
        int ret = hip_decode_exit(hip_context);
        hip_context = nullptr;
        free(mp3data);
        mp3data = nullptr;
        enc_delay = -1;
        enc_padding = -1;
        return ret;
    }
    return -1;
}

#ifdef __cplusplus
}
#endif