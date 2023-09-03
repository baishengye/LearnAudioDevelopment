package com.baishengye.libopensl

object OpenslLoader {

    init {
        System.loadLibrary("OpenslLoader")
    }

    external fun startRecord(filePath: String): Boolean

    external fun stopRecord()
}