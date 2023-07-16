package com.baishengye.audiorecorderdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baishengye.audiorecorderdemo.databinding.ActivityMainBinding
import com.baishengye.libbase.base.BaseViewBindingActivity

class MainActivity : BaseViewBindingActivity<ActivityMainBinding>() {

    companion object{
        const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1001
    }

    override fun getViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun initPermission() {
if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

} else {
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
        RECORD_AUDIO_PERMISSION_REQUEST_CODE
    )
}
    }

    override fun initViews() {

    }

    override fun initData() {

    }

    override fun initListeners() {
        binding.btnPcm.setOnClickListener {
            startActivity(Intent(this,PcmActivity::class.java))
        }
        binding.btnWav.setOnClickListener {
            startActivity(Intent(this,WavActivity::class.java))
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //有权限啥也不干
                } else {
                    finish()
                }
            }
        }
    }
}