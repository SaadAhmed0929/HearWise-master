package com.example.hearwise.feature.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearwise.engine.SineWaveGenerator
import com.example.hearwise.data.model.HearingProfile
import com.example.hearwise.data.model.HearingThresholds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Ear { LEFT, RIGHT }
enum class TestPhase { PREP, TESTING, DONE }

data class CalibrationUiState(
    val phase: TestPhase = TestPhase.PREP,
    val activeEar: Ear = Ear.LEFT,
    val currentFreq: Int = 1000,
    val currentDb: Int = 0,
    val finalProfile: HearingProfile? = null,
    val leftMap: Map<Int, Int> = emptyMap(),
    val rightMap: Map<Int, Int> = emptyMap()
)

class CalibrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()

    private val audioEngine = SineWaveGenerator()
    private var testJob: Job? = null
    private val frequencies = listOf(1000, 2000, 4000, 8000, 250, 500)
    private var freqIndex = 0

    private val MAX_SAFE_VOLUME_DB = 85

    fun startCalibration() {
        freqIndex = 0
        _uiState.update { 
            it.copy(
                phase = TestPhase.TESTING, 
                activeEar = Ear.LEFT, 
                currentFreq = frequencies[freqIndex],
                currentDb = 0,
                leftMap = emptyMap(),
                rightMap = emptyMap()
            )
        }
        startTestLoop()
    }

    private fun startTestLoop() {
        testJob?.cancel()
        testJob = viewModelScope.launch {
            val currentState = _uiState.value
            val freq = frequencies[freqIndex]
            val isLeft = currentState.activeEar == Ear.LEFT
            
            _uiState.update { it.copy(currentDb = 0, currentFreq = freq) }
            audioEngine.startTone(freq.toDouble(), isLeft)

            while (true) {
                audioEngine.setVolumeDb(_uiState.value.currentDb)
                delay(1500)
                
                if (_uiState.value.currentDb >= MAX_SAFE_VOLUME_DB) {
                    recordThreshold(MAX_SAFE_VOLUME_DB)
                    break
                }
                
                _uiState.update { it.copy(currentDb = it.currentDb + 5) }
            }
        }
    }

    private fun recordThreshold(db: Int) {
        testJob?.cancel()
        audioEngine.stopTone()
        
        val currentState = _uiState.value
        val freq = currentState.currentFreq
        val leftMap = currentState.leftMap.toMutableMap()
        val rightMap = currentState.rightMap.toMutableMap()
        
        if (currentState.activeEar == Ear.LEFT) {
            leftMap[freq] = db
        } else {
            rightMap[freq] = db
        }
        
        _uiState.update { it.copy(leftMap = leftMap, rightMap = rightMap) }
        advanceSequence()
    }

    private fun advanceSequence() {
        if (freqIndex < frequencies.size - 1) {
            freqIndex++
            startTestLoop()
        } else {
            if (_uiState.value.activeEar == Ear.LEFT) {
                freqIndex = 0
                _uiState.update { it.copy(activeEar = Ear.RIGHT) }
                startTestLoop()
            } else {
                finishCalibration()
            }
        }
    }

    private fun finishCalibration() {
        testJob?.cancel()
        audioEngine.stopTone()
        
        val currentState = _uiState.value
        val leftMap = currentState.leftMap
        val rightMap = currentState.rightMap
        
        val lt = HearingThresholds(
            hz_250 = leftMap[250] ?: 0, hz_500 = leftMap[500] ?: 0,
            hz_1000 = leftMap[1000] ?: 0, hz_2000 = leftMap[2000] ?: 0,
            hz_4000 = leftMap[4000] ?: 0, hz_8000 = leftMap[8000] ?: 0
        )
        val rt = HearingThresholds(
            hz_250 = rightMap[250] ?: 0, hz_500 = rightMap[500] ?: 0,
            hz_1000 = rightMap[1000] ?: 0, hz_2000 = rightMap[2000] ?: 0,
            hz_4000 = rightMap[4000] ?: 0, hz_8000 = rightMap[8000] ?: 0
        )
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val profile = HearingProfile(
            timestamp = sdf.format(Date()),
            leftEar = lt,
            rightEar = rt
        )
        
        _uiState.update { it.copy(phase = TestPhase.DONE, finalProfile = profile) }
    }

    fun onIHeardIt() {
        if (_uiState.value.phase == TestPhase.TESTING) {
            recordThreshold(_uiState.value.currentDb)
        }
    }

    fun onCannotHearIt() {
        if (_uiState.value.phase == TestPhase.TESTING) {
            recordThreshold(MAX_SAFE_VOLUME_DB)
        }
    }

    fun resetTest() {
        testJob?.cancel()
        audioEngine.stopTone()
        _uiState.update { CalibrationUiState() }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopTone()
    }
}
