package com.baishengye.libutil.utils

import android.content.Context
import java.io.File

object FolderUtils {
    private const val FOLDER_IMAGE = "folder_image"
    private const val FOLDER_VIDEO = "folder_video"
    private const val FOLDER_AUDIO = "folder_audio"

    fun getImageFolderPath(context: Context): String {
        return getFolderPath(context, FOLDER_IMAGE)
    }

    fun getVideoFolderPath(context: Context): String {
        return getFolderPath(context, FOLDER_VIDEO)
    }

    fun getAudioFolderPath(context: Context): String {
        return getFolderPath(context, FOLDER_AUDIO)
    }

    fun getFolderPath(context: Context, folder: String?): String {
        return context.getExternalFilesDir(folder)?.path + File.separator
    }
}