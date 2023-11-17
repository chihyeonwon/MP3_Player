package com.example.simplemusicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.mp3player.R

class MusicPlayerService : Service() {
    var mMediaPlayer: MediaPlayer? = null
    var mBinder: MusicPlayerBinder = MusicPlayerBinder()

    inner class MusicPlayerBinder: Binder() {
        fun getService(): MusicPlayerService {
            return this@MusicPlayerService
        }
    }

    // 서비스가 생성될 때 딱 한 번만 실행
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        // 포그라운드 서비스 시작: 상태표시줄에 앱이 실행되고 있다는 알림 생성
        startForegroundService()
    }

    // 바인드
    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    // 시작된 상태 & 백그라운드
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    // 서비스 종료: 상태표시줄에 보였던 알림 해제
    override fun onDestroy() {
        super.onDestroy()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startForegroundService() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val mChannel = NotificationChannel(
                "CHANNEL_ID",
                "CHANNEL_NAME",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(mChannel)
        }

        val notification: Notification = Notification.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle("뮤직 플레이어 앱")
            .setContentText("앱이 실행 중입니다.")
            .build()

        startForeground(1, notification)
    }

    fun isPlaying() : Boolean {
        return (mMediaPlayer != null && mMediaPlayer?.isPlaying ?: false)
    }

    fun play() {
        if(mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.chocolate)

            mMediaPlayer?.setVolume(1.0f, 1.0f)
            mMediaPlayer?.isLooping = true
            mMediaPlayer?.start()
        }
        else {
            if(mMediaPlayer!!.isPlaying) {
                Toast.makeText(this, "이미 음악이 실행 중입니다.", Toast.LENGTH_SHORT).show()
            }
            else {
                mMediaPlayer?.start()
            }
        }
    }

    fun pause() {
        mMediaPlayer?.let {
            if(it.isPlaying) {
                it.pause()
            }
        }
    }

    fun stop() {
        mMediaPlayer?.let {
            if(it.isPlaying) {
                it.stop()
                it.release()
                mMediaPlayer = null
            }
        }
    }
}