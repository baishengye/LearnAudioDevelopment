package com.baishengye.liblame.record

import android.media.AudioRecord
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.baishengye.liblame.LameLoader
import java.io.IOException
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

class MP3EncodeThread(threadName: String) : HandlerThread(threadName),
    AudioRecord.OnRecordPositionUpdateListener {

    companion object {
        const val STOP_THREAD_WHAT = 1001

        const val DATA_STOP = 1002
        const val DATA_IDLE = 1003
        const val DATA_PROGRESSING = 1004
    }

    private var mp3Buffer: ByteArray? = null

    private var dataState = DATA_IDLE

    private var outputStream: OutputStream? = null

    private val dataQueue = ConcurrentLinkedQueue<AudioChunk>()
    fun addData(audioChunk: AudioChunk) {
        dataQueue.offer(audioChunk)
    }

    inner class StopHandler(looper: Looper, encodeThread: MP3EncodeThread) :
        Handler(looper) {
        var encodeThread: WeakReference<MP3EncodeThread>

        init {
            this.encodeThread = WeakReference<MP3EncodeThread>(encodeThread)
        }

        override fun handleMessage(msg: Message) {
            if (msg.what == STOP_THREAD_WHAT) {
                val threadRef: MP3EncodeThread? = encodeThread.get()
                // Process all data in ring buffer and flush
                // left data to file
                while (threadRef?.progressData() == true);
                // Cancel any event left in the queue
                removeCallbacksAndMessages(null)
                threadRef?.flushAndRelease()

                dataState = DATA_STOP
            }
            super.handleMessage(msg)
        }
    }

    var stopHandler: Handler? = null

    override fun run() {
        super.run()

        stopHandler = StopHandler(this.looper, this)

        while (true) {
            if (dataState == DATA_STOP) {
                break
            }
        }
    }

    override fun onMarkerReached(recorder: AudioRecord?) {}

    override fun onPeriodicNotification(recorder: AudioRecord?) {
        //处理一次数据
        progressData()
    }

    @Synchronized
    fun flushAndRelease() {
        val encodeFlush = LameLoader.lameEncodeFlush(mp3Buffer!!)
        if (encodeFlush > 0) {
            try {
                outputStream?.write(mp3Buffer, 0, encodeFlush)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                LameLoader.lameClose()
            }
        }

        if (outputStream != null) {
            try {
                outputStream!!.flush()
                outputStream!!.close()
                outputStream = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    /**
     * 处理数据,队列是否还有数据*/
    @Synchronized
    fun progressData(): Boolean {
        val chunk = dataQueue.poll()
        chunk?.let { audioChunk ->
            val encodeSize = LameLoader.lameEncodeBuffer(
                audioChunk.toShorts(),
                audioChunk.toShorts(),
                audioChunk.size(),
                mp3Buffer!!
            )
            if (encodeSize > 0) {
                try {
                    outputStream?.write(mp3Buffer!!, 0, encodeSize)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        return dataQueue.size > 0
    }

    fun setOutputStream(outputStream: OutputStream) {
        this.outputStream = outputStream
    }

    fun stopThread() {
        stopHandler?.sendEmptyMessage(STOP_THREAD_WHAT)
    }

    fun setBufferSize(pullSizeInBytes: Int) {
        mp3Buffer = ByteArray((7200 + (pullSizeInBytes) * 2 * 1.25).toInt())
    }
}