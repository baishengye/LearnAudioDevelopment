package com.baishengye.liblame;

public class LameLoader {
    static {
        System.loadLibrary("LameLoader");
    }

    /**
     * 获取Lame版本号
     */
    public native static String getLameVersion();

    /**
     * 初始化Lame*/

}
