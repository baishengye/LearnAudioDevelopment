package com.baishengye.libaudio.recorder

/**
 * 录音机接口
 *
 * **实现该接口的类将提供:**
 * - 开始「startRecording」
 * - 暂停「pauseRecording」
 * - 继续「resumeRecording」
 * - 停止「stopRecording」方法。
 *
 * @author baishengye
 */
interface Recorder {
    /**
     * 开始，请确保当前app有 RECORD_AUDIO 权限
     */
    fun startRecording()

    /**
     * 暂停
     */
    fun pauseRecording()

    /**
     * 继续
     */
    fun resumeRecording()

    /**
     * 停止
     */
    fun stopRecording()
}