package com.baishengye.audiorecorderdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baishengye.audiorecorderdemo.databinding.ActivityPcmBinding
import com.baishengye.audiorecorderdemo.databinding.ItemAudioFileBinding
import com.baishengye.libaudio.player.PcmPlayHelper
import com.baishengye.libaudio.config.AudioRecordConfig
import com.baishengye.libaudio.recorder.PullTransport
import com.baishengye.libaudio.recorder.Recorder
import com.baishengye.libaudio.recorder.RecorderCreator
import com.baishengye.libbase.base.BaseViewBindingActivity
import com.baishengye.libutil.utils.DateUtil
import com.baishengye.libutil.utils.FolderUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class PcmActivity : BaseViewBindingActivity<ActivityPcmBinding>() {
    private var pcmRecorder: Recorder ?= null
    private var pcmPlayer: PcmPlayHelper ?= null

    private var pcmDirPath:String ?= null
    private var recordFile:File ?= null
    private var pcmFilesName : MutableList<String> = mutableListOf()

    override fun getViewBinding(): ActivityPcmBinding {
        return ActivityPcmBinding.inflate(layoutInflater)
    }

    override fun initPermission() {

    }

    override fun initViews() {
        binding.rvAudioList.linear().setup {
            addType<String>(R.layout.item_audio_file)

            onBind {
                val binding = getBinding<ItemAudioFileBinding>()
                binding.tvAudio.text = getModel<String>()
            }

            onClick(R.id.flAudio){
                val fileName = getModel<String>()
                pcmPlayer = PcmPlayHelper(){
                    binding.btnPcmStopPlay.text = "播放"
                    binding.btnPcmStopPlay.isEnabled = false
                }
                binding.btnPcmStopPlay.text = "停止"
                binding.btnPcmStopPlay.isEnabled = true
                pcmPlayer?.startPlaying(pcmDirPath + fileName)
            }
        }.models = pcmFilesName
    }

    override fun initData() {
        pcmDirPath = FolderUtils.getAudioFolderPath(this)
        Log.d("TEST","pcmDirPath:${pcmDirPath}")
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    override fun initListeners() {
        binding.btnPcmRecordStartStop.setOnClickListener {
            if(binding.btnPcmRecordStartStop.text=="开始录音"&&!TextUtils.isEmpty(pcmDirPath)){
                binding.btnPcmRecordStartStop.text = "停止录音"
                binding.btnPcmRecordPauseResume.text = "暂停"
                binding.btnPcmRecordPauseResume.visibility = View.VISIBLE

                recordFile = File(pcmDirPath + "record_${DateUtil.calenderToFormatString(Calendar.getInstance())}.pcm")
                pcmRecorder = RecorderCreator.pcm(recordFile!!, AudioRecordConfig(),PullTransport.Default())
                pcmRecorder?.startRecording()

            }else if(!TextUtils.isEmpty(pcmDirPath)){
                binding.btnPcmRecordStartStop.text = "开始录音"
                binding.btnPcmRecordPauseResume.visibility = View.GONE

                pcmRecorder?.stopRecording()
                recordFile = null
                pcmRecorder = null
            }
        }

        binding.btnPcmRecordPauseResume.setOnClickListener {
            if(binding.btnPcmRecordPauseResume.text=="暂停"&&!TextUtils.isEmpty(pcmDirPath)){
                binding.btnPcmRecordPauseResume.text = "继续"

                pcmRecorder?.pauseRecording()

            }else if(!TextUtils.isEmpty(pcmDirPath)){
                binding.btnPcmRecordPauseResume.text = "暂停"

                pcmRecorder?.resumeRecording()
            }
        }

        binding.btnPcmRefreshAudio.setOnClickListener {
            // 在协程中执行异步任务
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    pcmFilesName.clear()
                    // 在 IO 线程执行耗时操作，例如网络请求或数据库查询
                    pcmFilesName.addAll(getAllPcmFiles(pcmDirPath!!,".pcm"))
                }

                binding.rvAudioList.adapter?.notifyDataSetChanged()
            }
        }

        binding.btnPcmStopPlay.setOnClickListener {
            if(binding.btnPcmStopPlay.text=="停止" && binding.btnPcmStopPlay.isEnabled){
                binding.btnPcmStopPlay.text = "播放"
                binding.btnPcmStopPlay.isEnabled = false
                pcmPlayer?.stopPlaying()
            }
        }
    }
}