package com.example.exoplayer

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.gesture.Gesture
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.exoplayer.databinding.ActivityMainBinding
import java.lang.Runnable
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import androidx.core.content.edit
import androidx.core.view.isVisible


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var playerview: PlayerView
    private lateinit var exoplayer: ExoPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var singleTapGesture: GestureDetector
    private lateinit var lefttouch: GestureDetector
    private lateinit var righttouch: GestureDetector

    private lateinit var sp: SharedPreferences
    private var restoredFlag = false
    val handler = Handler(Looper.getMainLooper())

    private val videos = listOf(
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4"
    )
    val TAG = "VIDEO_PLAYER_TAG"


    private fun initPlayer() {

        exoplayer = ExoPlayer.Builder(this).build()
        playerview.player = exoplayer


        val mediaItems = videos.map { uri -> MediaItem.fromUri(uri.toUri()) }

        exoplayer.setMediaItems(mediaItems)
        exoplayer.prepare()
        exoplayer.playWhenReady = true
        exoplayer.play()


        exoplayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayPasueIcon(isPlaying)
            }
        })
        exoplayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        binding.playBtn.isVisible = true
                        seekBar.max = exoplayer.duration.toInt()
                        binding.currentTime.text = formateDate(exoplayer.currentPosition)
                        binding.duration.text = formateDate(exoplayer.duration)
                        if (!restoredFlag && sp.contains("Track") && sp.contains("pos")) {
                            val index = sp.getInt("Track", 0)
                            val position = sp.getLong("pos", 0L)
                            exoplayer.seekTo(index, position)
                            restoredFlag = true
                        }
                    }

                    Player.STATE_BUFFERING -> {
                        binding.playBtn.visibility = View.INVISIBLE
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayPasueIcon(isPlaying)
            }
        })


        startSeekBarUpdater()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        playerview = binding.exoPlayer
        seekBar = binding.seekbar


        sp = getSharedPreferences("PlayerInfo", MODE_PRIVATE)
        initPlayer()
        setUpControls()

        singleTapGesture =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    toggleControls()
                    return true
                }


            })
        binding.playerview.setOnTouchListener { _, event ->
            singleTapGesture.onTouchEvent(event)
            true
        }

        var isFullscreen = false


        binding.fullscreenBtn.setOnClickListener {

            requestedOrientation = if (!isFullscreen) {
                // Enable sensor-based landscape (normal + reverse)
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

            } else {
                // Reset to user orientation (sensors enabled again)
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            isFullscreen = !isFullscreen

        }

        lefttouch = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                exoplayer.seekTo(exoplayer.currentPosition - 10_000)
                return true
            }
        })

        righttouch = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                exoplayer.seekTo(exoplayer.currentPosition + 10_000)
                return true
            }
        })

        binding.leftside.setOnTouchListener { _, event ->
            lefttouch.onTouchEvent(event)
            true
        }

        binding.rightside.setOnTouchListener { _, event ->
            righttouch.onTouchEvent(event)
            true
        }

        binding.settingsBtn.setOnClickListener {
            showPlayBackSettings(it)
        }

    }


    private fun showPlayBackSettings(anchor : View) {
        val popupMenu = PopupMenu(this,anchor)
        popupMenu.menu.apply {
            add("0.5x")
            add("1x")
            add("1.5x")
            add("2x")
        }

        popupMenu.setOnMenuItemClickListener {
            item ->
            val speed = item.title.toString().removeSuffix("x").toFloat()
            exoplayer.setPlaybackSpeed(speed)
            true
        }

        popupMenu.show()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val layoutParams = binding.playerview.layoutParams

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Fullscreen: expand to full height
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            // Portrait: restore default height
            layoutParams.height = resources.getDimensionPixelSize(R.dimen.default_player_height)
        }

        binding.playerview.layoutParams = layoutParams
    }

    private fun toggleControls() {
        val isvisible = binding.controllerLayout.isVisible
        binding.controllerLayout.isVisible = !isvisible
        binding.detailslayout.isVisible = !isvisible
        if (!isvisible) setAutoHideControls()
    }

    private val hideUIContols = Runnable {
        binding.controllerLayout.isVisible = false
        binding.detailslayout.isVisible = false
    }

    private fun setAutoHideControls() {
        handler.removeCallbacks(hideUIContols)
        handler.postDelayed(hideUIContols, 3000)
    }


    @SuppressLint("DefaultLocale")
    private fun formateDate(duration: Long): String {
        val totalsec = duration / 1000
        val min = totalsec / 60
        val sec = totalsec % 60
        return String.format("%02d:%02d", min, sec)
    }


    private fun setUpControls() {
        binding.playBtn.setOnClickListener {
            if (exoplayer.isPlaying) {
                exoplayer.pause()
            } else {
                exoplayer.play()
            }
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoplayer.seekTo(progress.toLong())
                    binding.currentTime.text = formateDate(progress.toLong())
                }
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onStartTrackingTouch(p0: SeekBar?) {}
        })


        binding.forward10Btn.setOnClickListener {
            exoplayer.seekTo(exoplayer.currentPosition + 10000)
        }

        binding.back10btn.setOnClickListener {
            exoplayer.seekTo(exoplayer.currentPosition - 10000)
        }

        binding.nextBtn.setOnClickListener {
            exoplayer.seekToNext()
        }

        binding.prevBtn.setOnClickListener {
            exoplayer.seekToPrevious()
        }
    }

    private fun startSeekBarUpdater() {

        handler.post(object : Runnable {
            override fun run() {
                if (exoplayer.isPlaying) {
                    seekBar.max = exoplayer.duration.toInt()
                    binding.seekbar.progress = exoplayer.currentPosition.toInt()
                    binding.currentTime.text = formateDate(exoplayer.currentPosition)
                }
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun updatePlayPasueIcon(isplaying: Boolean) {
        val icon =
            if (isplaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        binding.playBtn.setImageResource(icon)
    }

    override fun onStart() {
        super.onStart()
        exoplayer.playWhenReady = true
        exoplayer.play()
    }

    override fun onResume() {
        super.onResume()
        exoplayer.playWhenReady = true
        exoplayer.play()
    }

    override fun onPause() {
        super.onPause()
        sp.edit()
            .putInt("Track", exoplayer.currentMediaItemIndex)
            .putLong("pos", exoplayer.currentPosition)
            .apply()
        exoplayer.pause()
    }

    override fun onStop() {
        super.onStop()
        exoplayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        exoplayer.release()
    }

}


