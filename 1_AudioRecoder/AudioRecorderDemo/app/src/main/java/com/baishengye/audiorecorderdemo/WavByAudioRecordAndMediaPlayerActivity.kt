package com.baishengye.audiorecorderdemo

import android.annotation.SuppressLint
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.baishengye.audiorecorderdemo.databinding.ActivityWavBinding
import com.baishengye.audiorecorderdemo.databinding.ItemAudioFileBinding
import com.baishengye.libaudio.config.decode.MediaPlayState.*
import com.baishengye.libaudio.helper.WaveFileInfoHelper
import com.baishengye.libaudio.playhelper.MediaPlayHelper
import com.baishengye.libaudio.recordHelper.PullTransport
import com.baishengye.libaudio.recordHelper.RecordHelper
import com.baishengye.libaudio.recordHelper.RecordHelperCreator
import com.baishengye.libaudio.recordHelper.pcm.PcmEncodeConfig
import com.baishengye.libbase.base.BaseViewBindingActivity
import com.baishengye.libutil.utils.DateUtil
import com.baishengye.libutil.utils.DateUtil.formatTime
import com.baishengye.libutil.utils.FolderUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class WavByAudioRecordAndMediaPlayerActivity : BaseViewBindingActivity<ActivityWavBinding>() {
    private var wavRecordHelper: RecordHelper? = null
    private val wavPlayHelper: MediaPlayHelper = MediaPlayHelper()

    private var wavDirPath: String? = null
    private var recordFile: File? = null
    private var wavFilesName: MutableList<String> = mutableListOf()

    override fun getViewBinding(): ActivityWavBinding = ActivityWavBinding.inflate(layoutInflater)

    override fun initViews() {

        binding.rvAudioList.linear().setup {
            addType<String>(R.layout.item_audio_file)

            onBind {
                val binding = getBinding<ItemAudioFileBinding>()
                binding.tvAudio.text = getModel<String>()
            }

            onClick(R.id.flAudio){
                val fileName = getModel<String>()

                startPlay(fileName)
            }
        }.models = wavFilesName
    }

    private fun startPlay(fileName: String) {
        // 在协程中执行异步任务
        CoroutineScope(Dispatchers.Main).launch {
            var result:String?=null
            withContext(Dispatchers.IO) {
                result = getWavInfo(wavDirPath + fileName)
            }

            binding.tvWavInfo.text = result
        }

        wavPlayHelper.let { mediaPlayHelper ->
            mediaPlayHelper.setOnPlayStateChange { mediaPlayState ->
                when (mediaPlayState) {
                    STOP -> binding.llPlayer.visibility = View.GONE
                    PAUSE -> binding.tvPlay.text = "播放"
                    IDLE -> binding.llPlayer.visibility = View.VISIBLE
                    PLAYING -> {
                        binding.sbBar.max = mediaPlayHelper.getDur()
                        binding.tvRightTime.text = formatTime(mediaPlayHelper.getDur())
                        binding.tvPlay.text = "暂停"
                    }
                }
            }
            mediaPlayHelper.startPlaying(wavDirPath + fileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.sbBar.min = 0
            }
            binding.sbBar.progress = 0

            //----------定时器记录播放进度---------//
            val mTimerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    this@WavByAudioRecordAndMediaPlayerActivity.runOnUiThread {
                        if (mediaPlayHelper.isPlaying()) {
                            binding.sbBar.progress = mediaPlayHelper.getCurrPos()
                            binding.tvLeftTime.text = formatTime(mediaPlayHelper.getCurrPos())
                        }
                    }
                }
            }
            Timer().schedule(mTimerTask, 0, 1000)
        }
    }

    override fun initData() {
        wavDirPath = FolderUtils.getAudioFolderPath(this)
        Log.d("TEST","pcmDirPath:${wavDirPath}")
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    override fun initListeners() {
        binding.tvPlay.setOnClickListener {
            if (wavPlayHelper.isPlaying()) {
                wavPlayHelper.pausePlaying()
            } else {
                wavPlayHelper.resumePlaying()
            }
        }

        binding.tvCancel.setOnClickListener {
            if (wavPlayHelper.isPlaying() || wavPlayHelper.isPaused()) {
                wavPlayHelper.stopPlaying()
            }
        }

        binding.btnWavRecordStartStop.setOnClickListener {
            if(binding.btnWavRecordStartStop.text=="开始录音"&&!TextUtils.isEmpty(wavDirPath)) {
                binding.btnWavRecordStartStop.text = "停止录音"
                binding.btnWavRecordPauseResume.text = "暂停"
                binding.btnWavRecordPauseResume.visibility = View.VISIBLE

                recordFile =
                    File(wavDirPath + "record_${DateUtil.calenderToFormatString(Calendar.getInstance())}.wav")
                wavRecordHelper = RecordHelperCreator.wav(
                    recordFile!!,
                    PcmEncodeConfig(),
                    PullTransport.Default()
                )
                wavRecordHelper?.startRecording()

            }else if(!TextUtils.isEmpty(wavDirPath)){
                binding.btnWavRecordStartStop.text = "开始录音"
                binding.btnWavRecordPauseResume.visibility = View.GONE

                wavRecordHelper?.stopRecording()
                recordFile = null
                wavRecordHelper = null
            }
        }

        binding.btnWavRecordPauseResume.setOnClickListener {
            if(binding.btnWavRecordPauseResume.text=="暂停"&&!TextUtils.isEmpty(wavDirPath)){
                binding.btnWavRecordPauseResume.text = "继续"

                wavRecordHelper?.pauseRecording()

            }else if(!TextUtils.isEmpty(wavDirPath)){
                binding.btnWavRecordPauseResume.text = "暂停"

                wavRecordHelper?.resumeRecording()
            }
        }

        binding.btnWavRefreshAudio.setOnClickListener {
            // 在协程中执行异步任务
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    wavFilesName.clear()
                    // 在 IO 线程执行耗时操作，例如网络请求或数据库查询
                    wavFilesName.addAll(getAllPcmFiles(wavDirPath!!,".wav"))
                }

                binding.rvAudioList.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun getWavInfo(filePath: String):String {
        val filename = filePath
        val reader = WaveFileInfoHelper(filename)
        return if (reader.isSuccess) {
            ("读取wav文件信息：" + filename
                    + "\n采样率：" + reader.sampleRate
                    + "\n声道数：" + reader.numChannels
                    + "\n编码长度：" + reader.bitPerChannel * reader.numChannels
                    + "\n数据长度：" + reader.dataLen)
        } else {
            "不是一个正常的wav文件"
        }
    }
}