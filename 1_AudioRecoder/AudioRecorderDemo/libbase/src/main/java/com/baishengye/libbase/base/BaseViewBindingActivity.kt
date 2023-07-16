package com.baishengye.libbase.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import java.io.File

abstract class BaseViewBindingActivity<T : ViewBinding> : AppCompatActivity() {

    private lateinit var mBinding: T
    val binding : T
    get() = mBinding

    abstract fun getViewBinding():T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding  = getViewBinding()
        setContentView(binding.root)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        initPermission()
        initViews()
        initData()
        initListeners()
    }

    protected open fun initPermission(){}

    protected abstract fun initViews()

    protected abstract fun initData()

    protected abstract fun initListeners()

    protected open suspend fun getAllPcmFiles(dirPath: String,suffix:String=".pcm"):MutableList<String>{
        val directory = File(dirPath)
        val newPcmFileList = mutableListOf<String>()

        if (directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        // 递归调用，遍历子目录
                        newPcmFileList.addAll(getAllPcmFiles(file.absolutePath))
                    } else if (file.isFile && file.name.endsWith(suffix)) {
                        // 筛选出 .pcm 文件
                        newPcmFileList.add(file.name)
                    }
                }
            }
        }
        return newPcmFileList
    }
}