package com.baishengye.libaudio.config.decode

/**
 * 媒体播放异常*/
class MediaPlayException: Exception {

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