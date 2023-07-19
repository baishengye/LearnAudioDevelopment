package com.baishengye.libaudio.config.encode

/**
 * 媒体录音异常*/
class MediaRecordException: Exception {

    constructor():super()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    constructor (
        message: String?, cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)
}