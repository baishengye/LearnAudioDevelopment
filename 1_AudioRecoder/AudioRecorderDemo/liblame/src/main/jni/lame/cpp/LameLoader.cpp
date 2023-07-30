#include <string>
#include "../include/lame.h"
#include "com_baishengye_liblame_LameLoader.h"

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

#define BUFFER_SIZE 8192

static lame_global_flags *lameGlobalFlags = nullptr;


/**
 * 获取Lame版本号
 * @return 版本号*/
jstring
Java_com_baishengye_liblame_LameLoader_00024Companion_getLameVersion(JNIEnv *env, jobject clazz) {
    return env->NewStringUTF(get_lame_version());
}

/**
 * 默认方式初始化Lame
 * @return  -1：初始化失败*/
jint
Java_com_baishengye_liblame_LameLoader_00024Companion_initLameDefault(JNIEnv *env, jobject clazz) {
    lameGlobalFlags = lame_init();
    return lame_init_params(lameGlobalFlags);
}

/**配置参数初始化Lame*/
jint
Java_com_baishengye_liblame_LameLoader_00024Companion_initLame(JNIEnv *env, jobject clazz,
                                                               jint in_sample_rate,
                                                               jint out_channel,
                                                               jint out_sample_rate,
                                                               jint out_bitrate,
                                                               jfloat scale_input,
                                                               jint mode,
                                                               jint vbr_mode,
                                                               jint quality,
                                                               jint vbr_quality,
                                                               jint abr_mean_bitrate,
                                                               jint lowpass_freq,
                                                               jint highpass_freq,
                                                               jstring id3tag_title,
                                                               jstring id3tag_artist,
                                                               jstring id3tag_album,
                                                               jstring id3tag_year,
                                                               jstring id3tag_comment) {
    lameGlobalFlags = lame_init();
    lame_set_in_samplerate(lameGlobalFlags, in_sample_rate);
    lame_set_num_channels(lameGlobalFlags, out_channel);
    lame_set_out_samplerate(lameGlobalFlags, out_sample_rate);
    lame_set_brate(lameGlobalFlags, out_bitrate);
    lame_set_quality(lameGlobalFlags, quality);
    lame_set_scale(lameGlobalFlags, scale_input);
    lame_set_VBR_q(lameGlobalFlags, vbr_quality);
    lame_set_VBR_mean_bitrate_kbps(lameGlobalFlags, abr_mean_bitrate);
    lame_set_lowpassfreq(lameGlobalFlags, lowpass_freq);
    lame_set_highpassfreq(lameGlobalFlags, highpass_freq);

    switch (mode) {
        case 0:
            lame_set_mode(lameGlobalFlags, STEREO);
            break;
        case 1:
            lame_set_mode(lameGlobalFlags, JOINT_STEREO);
            break;
        case 3:
            lame_set_mode(lameGlobalFlags, MONO);
            break;
        case 4:
            lame_set_mode(lameGlobalFlags, NOT_SET);
            break;
        default:
            lame_set_mode(lameGlobalFlags, NOT_SET);
            break;
    }

    switch (vbr_mode) {
        case 0:
            lame_set_VBR(lameGlobalFlags, vbr_off);
            break;
        case 2:
            lame_set_VBR(lameGlobalFlags, vbr_rh);
            break;
        case 3:
            lame_set_VBR(lameGlobalFlags, vbr_abr);
            break;
        case 4:
            lame_set_VBR(lameGlobalFlags, vbr_mtrh);
            break;
        case 6:
            lame_set_VBR(lameGlobalFlags, vbr_default);
            break;
        default:
            lame_set_VBR(lameGlobalFlags, vbr_off);
            break;

    }

    const jchar *title = nullptr;
    const jchar *artist = nullptr;
    const jchar *album = nullptr;
    const jchar *year = nullptr;
    const jchar *comment = nullptr;
    if (id3tag_title) {
        title = env->GetStringChars(id3tag_title, nullptr);
    }
    if (id3tag_artist) {
        artist = env->GetStringChars(id3tag_artist, nullptr);
    }
    if (id3tag_album) {
        album = env->GetStringChars(id3tag_album, nullptr);
    }
    if (id3tag_year) {
        year = env->GetStringChars(id3tag_year, nullptr);
    }
    if (id3tag_comment) {
        comment = env->GetStringChars(id3tag_comment, nullptr);
    }

    if (title || artist || album || year || comment) {
        id3tag_init(lameGlobalFlags);

        if (title != nullptr) {
            id3tag_set_title(lameGlobalFlags, (const char *) title);
            env->ReleaseStringChars(id3tag_title, title);
        }
        if (artist != nullptr) {
            id3tag_set_artist(lameGlobalFlags, (const char *) artist);
            env->ReleaseStringChars(id3tag_artist, artist);
        }
        if (album != nullptr) {
            id3tag_set_album(lameGlobalFlags, (const char *) album);
            env->ReleaseStringChars(id3tag_album, album);
        }
        if (year != nullptr) {
            id3tag_set_year(lameGlobalFlags, (const char *) year);
            env->ReleaseStringChars(id3tag_year, year);
        }
        if (comment != nullptr) {
            id3tag_set_comment(lameGlobalFlags, (const char *) comment);
            env->ReleaseStringChars(id3tag_comment, comment);
        }
    }

    return lame_init_params(lameGlobalFlags);
}

/*回收lame的编码缓冲区
 * 返回值是回收时缓冲区中的MP3数据Bytes*/
jint
Java_com_baishengye_liblame_LameLoader_00024Companion_lameEncodeFlush(JNIEnv *env, jobject clazz,
                                                                      jbyteArray mp3buf) {
    const jsize mp3buf_size = env->GetArrayLength(mp3buf);
    jbyte *j_mp3buf = env->GetByteArrayElements(mp3buf, nullptr);

    int result = lame_encode_flush(lameGlobalFlags, (u_char *) j_mp3buf, mp3buf_size);

    env->ReleaseByteArrayElements(mp3buf, j_mp3buf, 0);

    return result;
}

/*
 * 编码*/
jint
Java_com_baishengye_liblame_LameLoader_00024Companion_lameEncodeBuffer(JNIEnv *env, jobject clazz,
                                                                       jshortArray buffer_l,
                                                                       jshortArray buffer_r,
                                                                       jint samples,
                                                                       jbyteArray mp3buf) {
    jshort *j_buffer_l = env->GetShortArrayElements(buffer_l, nullptr);

    jshort *j_buffer_r = env->GetShortArrayElements(buffer_r, nullptr);

    const jsize mp3buf_size = env->GetArrayLength(mp3buf);
    jbyte *j_mp3buf = env->GetByteArrayElements(mp3buf, nullptr);

    int result = lame_encode_buffer(lameGlobalFlags, j_buffer_l, j_buffer_r,
                                    samples, (u_char *) j_mp3buf, mp3buf_size);

    env->ReleaseShortArrayElements(buffer_l, j_buffer_l, 0);
    env->ReleaseShortArrayElements(buffer_r, j_buffer_r, 0);
    env->ReleaseByteArrayElements(mp3buf, j_mp3buf, 0);

    return result;
}

/*左右声道混合编码*/
jint
Java_com_baishengye_liblame_LameLoader_00024Companion_lameEncodeBufferInterleaved(JNIEnv *env,
                                                                                  jobject clazz,
                                                                                  jshortArray pcm,
                                                                                  jint samples,
                                                                                  jbyteArray mp3buf) {
    jshort *j_pcm = env->GetShortArrayElements(pcm, nullptr);

    const jsize mp3buf_size = env->GetArrayLength(mp3buf);
    jbyte *j_mp3buf = env->GetByteArrayElements(mp3buf, nullptr);

    int result = lame_encode_buffer_interleaved(lameGlobalFlags, j_pcm,
                                                samples, (u_char *) j_mp3buf, mp3buf_size);

    env->ReleaseShortArrayElements(pcm, j_pcm, 0);
    env->ReleaseByteArrayElements(mp3buf, j_mp3buf, 0);

    return result;
}

/*销毁Lame*/
void
Java_com_baishengye_liblame_LameLoader_00024Companion_lameClose(JNIEnv *env, jobject clazz) {
    lame_close(lameGlobalFlags);
    lameGlobalFlags = nullptr;
}

void
Java_com_baishengye_liblame_LameLoader_00024Companion_wav2mp3(JNIEnv *env, jobject clazz,
                                                              jstring wav_path, jstring mp3_path) {
    const char *wavPath = env->GetStringUTFChars(wav_path, nullptr);
    const char *mp3Path = env->GetStringUTFChars(mp3_path, nullptr);
    //open input file and output file
    FILE *fInput = fopen(wavPath, "rb");
    FILE *fMp3 = fopen(mp3Path, "wb");
    short int inputBuffer[BUFFER_SIZE * 2];
    unsigned char mp3Buffer[BUFFER_SIZE];//You must specified at least 7200
    int read = 0; // number of bytes in inputBuffer, if in the end return 0
    int write = 0;// number of bytes output in mp3buffer.  can be 0
    long total = 0; // the bytes of reading input file
    int nowConvertBytes = 0;

    //convert to mp3
    do {
        read = static_cast<int>(fread(inputBuffer, sizeof(short int) * 2, BUFFER_SIZE, fInput));
        total += read * sizeof(short int) * 2;
        nowConvertBytes = total;
        if (read != 0) {
            write = lame_encode_buffer_interleaved(lameGlobalFlags, inputBuffer, read, mp3Buffer,
                                                   BUFFER_SIZE);
            //write the converted buffer to the file
            fwrite(mp3Buffer, sizeof(unsigned char), static_cast<size_t>(write), fMp3);
        }
        //if in the end flush
        if (read == 0) {
            lame_encode_flush(lameGlobalFlags, mp3Buffer, BUFFER_SIZE);
        }
    } while (read != 0);

    //release resources
    if (lameGlobalFlags != nullptr) {
        lame_close(lameGlobalFlags);
        lameGlobalFlags = nullptr;
    }
    fclose(fInput);
    fclose(fMp3);
    env->ReleaseStringUTFChars(wav_path, wavPath);
    env->ReleaseStringUTFChars(mp3_path, mp3Path);
    nowConvertBytes = -1;
}

void
Java_com_baishengye_liblame_LameLoader_00024Companion_wav2mp3Speed(JNIEnv *env, jobject clazz,
                                                                   jstring wav_path,
                                                                   jstring mp3_path, jint speed) {
    lame_set_out_samplerate(lameGlobalFlags, lame_get_out_samplerate(lameGlobalFlags) * speed);
    lame_init_params(lameGlobalFlags);

    Java_com_baishengye_liblame_LameLoader_00024Companion_wav2mp3(env, clazz, wav_path, mp3_path);
}
#ifdef __cplusplus
}
#endif