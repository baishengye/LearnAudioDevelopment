package com.baishengye.audiorecorderdemo

import android.annotation.SuppressLint
import com.baishengye.audiorecorderdemo.databinding.ActivityLameTestBinding
import com.baishengye.libbase.base.BaseViewBindingActivity
import com.baishengye.liblame.LameLoader

class LameTestActivity : BaseViewBindingActivity<ActivityLameTestBinding>() {
    private val lameLoader: LameLoader by lazy { LameLoader() }

    override fun getViewBinding(): ActivityLameTestBinding =
        ActivityLameTestBinding.inflate(layoutInflater)

    override fun initViews() {

    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        binding.tvLameVersion.text = "Lame的版本为${lameLoader.lameVersion}"
    }

    override fun initListeners() {

    }
}