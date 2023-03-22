package com.example.examenuf2

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity()
{
    lateinit var startTV: TextView
    lateinit var stopTV: TextView
    lateinit var playTV: TextView
    lateinit var stopplayTV: TextView
    lateinit var statusTV: TextView

    private var recordingTime = 0

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    var mFileName: File? = null
    private val REQUEST_AUDIO_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mPlayer = MediaPlayer()

        statusTV = findViewById(R.id.idTVstatus)
        startTV = findViewById(R.id.btnRecord)
        stopTV = findViewById(R.id.btnStop)
        playTV = findViewById(R.id.btnPlay)
        stopplayTV = findViewById(R.id.btnStopPlay)

        startTV.setOnClickListener {
            startRecording()
        }

        stopTV.setOnClickListener {
            pauseRecording()
        }

        playTV.setOnClickListener {
            playAudio()
        }

        stopplayTV.setOnClickListener {
            pausePlaying()
        }
    }

    private fun startRecording()
    {
        // Check permissions
        if (checkPermissions())
        {
            if (mRecorder == null) {
                // Save file
                mFileName = File(getExternalFilesDir("")?.absolutePath, "Record.3gp")

                // If file exists then increment counter
                var n = 0
                while (mFileName!!.exists()) {
                    n++
                    mFileName = File(getExternalFilesDir("")?.absolutePath, "Record$n.3gp")
                }

                // Initialize the class MediaRecorder
                mRecorder = MediaRecorder()

                // Set source to get audio
                mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)

                // Set the format of the file
                mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

                // Set the audio encoder
                mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                // Set the save path
                mRecorder!!.setOutputFile(mFileName!!.absolutePath)
                //mRecorder!!.setOutputFile(mFileName)
                try {
                    // Preparation of the audio file
                    mRecorder!!.prepare()
                } catch (e: IOException) {
                    Log.e("TAG", "prepare() failed")
                }
                // Start the audio recording
                mRecorder!!.start()
                statusTV.text = "Recording in progress"
            }
            else
            {
                //Stop Recording
                mRecorder!!.stop()

                // Release the class mRecorder
                mRecorder!!.release()
                mRecorder = null
                statusTV.text = "Recording interrupted"
                //Change the button logos
                startTV.setBackgroundResource(R.drawable.btn_rec_start)
            }
        }
        else
        {
            // Request permissions
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permissions accepted ->
        when (requestCode)
        {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.isNotEmpty())
            {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord)
                {
                    // Message
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG).show()
                }
                else
                {
                    // Message
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkPermissions(): Boolean
    {
        // Check permissions
        val result = ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions()
    {
        // Request permissions
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_AUDIO_PERMISSION_CODE)
    }

    private fun playAudio()
    {
        // Use the MediaPlayer class to listen to recorded audio files
        try {
            if(!mPlayer!!.isPlaying && recordingTime == 0)
            {
                // Preleva la fonte del file audio
                mPlayer!!.setDataSource(mFileName.toString())

                // Fetch the source of the mPlayer
                mPlayer!!.prepare()

                // Start the mPlayer
                mPlayer!!.start()
                statusTV.text = "Listening recording"

                //Change the button logos
                playTV.setBackgroundResource(R.drawable.btn_rec_pause)
            }
            else if(mPlayer!!.isPlaying && recordingTime == 0)
            {
                //Save current reproduction time in miliseconds
                recordingTime = mPlayer!!.currentPosition

                mPlayer!!.pause()
                statusTV.text = "Playing Paused"

                //Change the button logos
                playTV.setBackgroundResource(R.drawable.btn_rec_play)
            }
            else if(recordingTime != 0)
            {
                //Reanude playing where we paused
                mPlayer!!.seekTo(recordingTime)
                mPlayer!!.start()

                recordingTime = 0

                statusTV.text = "Playing Continued"
                //Change the button logos
                playTV.setBackgroundResource(R.drawable.btn_rec_pause)
            }
            //The Player has reached the end of the track, restarted
        }
        catch (e: Exception)
        {
            //Bug Preventer
            mPlayer!!.release()
            recordingTime = 0

            statusTV.text = "Bug Evaded"
        }
    }

    private fun pauseRecording()
    {
        // Stop recording
        if (mFileName == null)
        {

            // Message
            Toast.makeText(applicationContext, "Registration not started", Toast.LENGTH_LONG).show()

        } else
        {
            mRecorder!!.stop()

            // Message to confirm save file
            val savedUri = Uri.fromFile(mFileName)
            val msg = "File saved: " + savedUri!!.lastPathSegment
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()

            // Release the class mRecorder
            mRecorder!!.release()
            mRecorder = null
            statusTV.text = "Recording interrupted"
        }
    }

    private fun pausePlaying()
    {
        // Stop playing the audio file
        if(mPlayer!!.isPlaying)
        {
            //Player Stopped
            mPlayer!!.release()
            recordingTime = 0
            // Stop playing the audio file
            statusTV.text = "Recording stopped"

            //Change the button logos
            playTV.setBackgroundResource(R.drawable.btn_rec_play)
        }
        else
        {
            statusTV.text = "Playing nothing"
        }
    }

}