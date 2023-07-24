package com.baishengye.liblame;

public class LameLoader {
    static {
        System.loadLibrary("LameLoader");
    }

    public native String getLameVersion();
}
