#include <string>
#include "../include/lame.h"
#include "com_baishengye_liblame_LameLoader.h"

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_baishengye_liblame_LameLoader
 * Method:    getLameVersion
 * Signature: ()Ljava/lang/String;
 */
extern "C" jstring Java_com_baishengye_liblame_LameLoader_getLameVersion
        (JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(get_lame_version());
}

#ifdef __cplusplus
}
#endif