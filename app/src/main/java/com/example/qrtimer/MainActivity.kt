package com.example.qrtimer

import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text

class MainActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var deleteButton: Button
    private lateinit var stopButton: Button
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timerInput = 0L
    private var ringtone: Ringtone? = null
    private var isRingtonePlaying = false
    private var shouldResumeRingtone = false

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.timerText)
        deleteButton = findViewById(R.id.deleteButton)
        stopButton = findViewById(R.id.stopButton)

        setupKeypadListeners()
        deleteButton.setOnClickListener { deleteLastInput() }
        stopButton.setOnClickListener {
            if (isTimerRunning || isRingtonePlaying) {
                shouldResumeRingtone = true
                openQrScanner()
            } else {
                Toast.makeText(this, "Timer is not running", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupKeypadListeners() {
        val buttonIds = listOf(
            R.id.button1, R.id.button2, R.id.button3,
            R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9,
            R.id.button00, R.id.button0, R.id.startButton
        )

        buttonIds.forEach { id ->
            findViewById<Button>(id).setOnClickListener { button ->
                when (id) {
                    R.id.startButton -> startTimer()
                    else -> updateTimerInput((button as Button).text.toString())
                }
            }
        }
    }

    private fun updateTimerInput(input: String) {
        if (timerInput.toString().length < 6) {
            timerInput = (timerInput * 10) + input.toLong()
            updateTimerDisplay()
        }
    }

    private fun deleteLastInput() {
        timerInput /= 10
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        val hours = timerInput / 10000
        val minutes = (timerInput % 10000) / 100
        val seconds = timerInput % 100

        timerText.text = String.format("%02dh %02dm %02ds", hours, minutes, seconds)
    }

    private fun startTimer() {
        if (timerInput <= 0) {
            Toast.makeText(this, "Please set a valid time", Toast.LENGTH_SHORT).show()
            return
        }

        val totalSeconds = (timerInput / 10000) * 3600 + ((timerInput % 10000) / 100) * 60 + (timerInput % 100)
        countDownTimer?.cancel()
        isTimerRunning = true

        countDownTimer = object : CountDownTimer(totalSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val hours = secondsRemaining / 3600
                val minutes = (secondsRemaining % 3600) / 60
                val seconds = secondsRemaining % 60
                timerText.text = String.format("%02dh %02dm %02ds", hours, minutes, seconds)
            }

            @RequiresApi(Build.VERSION_CODES.P)
            override fun onFinish() {
                isTimerRunning = false
                timerInput = 0L // Reset timerInput to 0
                timerText.text = "00h 00m 00s"
                playRingtone()
                Toast.makeText(this@MainActivity, "Timer Finished!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun openQrScanner() {
        val intent = Intent(this, QrScannerActivity::class.java)
        qrScanLauncher.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val qrScanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            stopTimer()
            shouldResumeRingtone = false
        } else {
            if (shouldResumeRingtone) {
                playRingtone()
                shouldResumeRingtone = false
            }
        }
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        stopRingtone()
        timerText.text = "00h 00m 00s"
        Toast.makeText(this, "Timer Stopped!", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun playRingtone() {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone?.isLooping = true
            ringtone?.play()
            isRingtonePlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Could not play ringtone", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
        isRingtonePlaying = false
    }

    override fun onPause() {
        super.onPause()
        stopRingtone()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()
        if (shouldResumeRingtone) {
            playRingtone()
        }
    }
}