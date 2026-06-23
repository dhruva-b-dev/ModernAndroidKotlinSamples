package com.dhruva.voicecontrollertimer

import android.Manifest
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruva.voicecontrollertimer.ui.theme.VoiceControllerTimerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceControllerTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    VoiceTimerScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceTimerScreen() {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    //state variables
    var timeLeftInMillis by remember { mutableLongStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Press 'Tap to Speak' to command") }
    var countDownTimer by remember { mutableStateOf<CountDownTimer?>(null) }

    //Audio Engines Setup
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    //Initialize speech Recognizer framework
    val speechRecognizer = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
    }
    val speechRecognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            // Some devices require this for permissions to be properly evaluated
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
    }

    //Helper functions for Text-to-Speech audio confirmations
    fun speakText(message: String) {
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    //Initialize TTS engine & Media Player on App Startup
    LaunchedEffect(Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            statusText = "Speech recognition not available on this device"
        }

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }

        //Load default system alarm sound
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )
        mediaPlayer = MediaPlayer.create(context, alarmUri)
    }

    //clean up timer when component leaves screen
    DisposableEffect(Unit) {
        onDispose {
            countDownTimer?.cancel()
            tts?.stop()
            tts?.shutdown()
            mediaPlayer?.release()
            speechRecognizer.destroy()
        }
    }

    //Timer logic functions
    fun stopTimer() {
        countDownTimer?.cancel()
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.prepare() //reset audio stream for next play
        }
        isTimerRunning = false
        statusText = "Timer stopped"
        speakText("Timer stopped")
    }

    fun startTimer(durationInMillis: Long, speechConfirmationText: String) {
        stopTimer()
        timeLeftInMillis = durationInMillis
        isTimerRunning = true
        statusText = "Timer Active"
        speakText(speechConfirmationText)

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onFinish() {
                isTimerRunning = false
                timeLeftInMillis = 0
                statusText = "Timer finished"
                mediaPlayer?.start()
            }

            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
            }
        }.start()
    }

    fun parseAndExecuteVoiceCommand(text: String) {
        val command = text.lowercase()

        when {
            command.contains("stop") || command.contains("pause") -> {
                stopTimer()
            }

            command.contains("start") -> {
                //Regex pattern to extract digits preceding the word "minute"
                val minutePattern = "(\\d+)\\s*minute".toRegex()
                val secondPattern = "(\\d+)\\s*second".toRegex()

                val minuteMatch = minutePattern.find(command)
                val secondMatch = secondPattern.find(command)

                var totalMillis = 0L
                var speechConfirmationText = "Starting timer for "

                if (minuteMatch != null) {
                    totalMillis += minuteMatch.groupValues[1].toLong() * 60 * 1000
                    speechConfirmationText += "${minuteMatch.groupValues[1]} minutes"
                }
                if (secondMatch != null) {
                    totalMillis += secondMatch.groupValues[1].toLong() * 1000
                    speechConfirmationText += "${secondMatch.groupValues[1]} seconds"
                }

                //default fallback if someone just says "start" without a specific duration
                if (totalMillis == 0L) {
                    totalMillis = 60000L // 1 minute baseline
                    speechConfirmationText = "Starting default one minute timer"
                }

                startTimer(totalMillis, speechConfirmationText)
            }

            else -> {
                statusText = "Command not recognized"
                speakText("Command not recognized. Try saying start timer for two minutes.")
            }
        }
    }

    //Initialize speech Recognizer framework
    //Setting up the voice listener instance
    LaunchedEffect(speechRecognizer) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onBeginningOfSpeech() {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                statusText = "Error hearing you: $error"
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onReadyForSpeech(params: Bundle?) {
                statusText = "Listening..."
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    parseAndExecuteVoiceCommand(text)
                }
            }

            override fun onRmsChanged(rmsdB: Float) {}
        })
    }

    //UI Layout Definition
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Formatting calculations for UI representation
        val totalSeconds = timeLeftInMillis / 1000
        val displayMinutes = totalSeconds / 60
        val displaySeconds = totalSeconds % 60
        val timerString = String.format(
            LocalLocale.current.platformLocale, "%02d:%02d", displayMinutes, displaySeconds
        )

        Text(
            text = timerString,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = statusText, fontSize = 16.sp, color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (permissionState.status.isGranted) {
                    //Turn off sounding alarm instantly when tapping to provide a new spoken voice command
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.stop()
                        mediaPlayer?.prepare()
                    }
                    speechRecognizer.startListening(speechRecognizerIntent)
                } else {
                    permissionState.launchPermissionRequest()
                }
            }, modifier = Modifier.size(width = 200.dp, height = 56.dp)
        ) {
            Text(text = "Tap to Speak", fontSize = 18.sp)
        }
    }
}