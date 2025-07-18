package com.example.exoplayer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var playerview: PlayerView
    private lateinit var exoplayer: ExoPlayer
    private lateinit var seekBar: SeekBar

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
        Log.d(TAG, "Player Build")
        playerview.player = exoplayer
        Log.d(TAG, "Player Set on View")



        val mediaItems = videos.map { uri -> MediaItem.fromUri(uri.toUri()) }
        Log.d(TAG, "Media Item List Prepared")

        exoplayer.setMediaItems(mediaItems)
        Log.d(TAG, "Media Item set")
        exoplayer.prepare()
        Log.d(TAG, "Player Prepared")
        exoplayer.playWhenReady = true
        Log.d(TAG, "player when ready true")
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
                        Log.d(TAG, "Player is READY — duration: ${exoplayer.duration}")
                        seekBar.max = exoplayer.duration.toInt()
                    }
                    Player.STATE_BUFFERING -> Log.d(TAG, "Player BUFFERING")
                    Player.STATE_ENDED -> Log.d(TAG, "Player ENDED")
                    Player.STATE_IDLE -> Log.d(TAG, "Player IDLE")
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "onIsPlayingChanged: $isPlaying")
                updatePlayPasueIcon(isPlaying)
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Playback ERROR: ${error.message}", error)
            }
        })

        Log.d(TAG, "Player Listener Added")

        startSeekBarUpdater()
        Log.d(TAG, "seekBarUpdater Called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerview = binding.exoPlayer
        seekBar = binding.seekbar


        Log.d(TAG, "Calling initPLayer")
        initPlayer()
        Log.d(TAG, "Calling setUpContols")
        setUpControls()
    }



    private fun setUpControls() {
        binding.playBtn.setOnClickListener {
            if (exoplayer.isPlaying) {
                exoplayer.pause()
            } else {
                exoplayer.play()
            }
            Log.d(TAG, "Play button pushed")
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    exoplayer.seekTo(progress.toLong())
                    Log.d(TAG, "seekbar pos changed")
                }
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {}
            override fun onStartTrackingTouch(p0: SeekBar?) {}
        })

        binding.nextBtn.setOnClickListener {
            exoplayer.seekToNext()
        }

        binding.prevBtn.setOnClickListener {
            exoplayer.seekToPrevious()
        }
    }

    private fun startSeekBarUpdater() {

        handler.post(object : Runnable{
            override fun run() {
                if(exoplayer.isPlaying){
                    seekBar.max = exoplayer.duration.toInt()
                    binding.seekbar.progress = exoplayer.currentPosition.toInt()
                }
                handler.postDelayed(this,500)
            }
        })
        Log.d(TAG, "seekbar updater funciton done")
    }

    private fun updatePlayPasueIcon(isplaying: Boolean) {
        val icon = if(isplaying) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause
        binding.playBtn.setImageResource(icon)
    }

    override fun onStart() {
        super.onStart()
        exoplayer.playWhenReady=true
        exoplayer.play()
    }

    override fun onResume() {
        super.onResume()
        exoplayer.play()
    }

    override fun onStop() {
        super.onStop()
        exoplayer.release()
    }

}

