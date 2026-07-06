package com.tvxargtec.online.utils

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.Equalizer
import android.util.Log
import kotlin.math.roundToInt

class EqualizerHelper(private val context: Context, private val audioSessionId: Int) {

    private var equalizer: Equalizer? = null
    private val prefs: SharedPreferences = context.getSharedPreferences("equalizer_prefs", Context.MODE_PRIVATE)
    var isEnabled: Boolean = false
        private set

    companion object {
        private const val TAG = "EqualizerHelper"
        private const val KEY_ENABLED = "eq_enabled"
        private const val KEY_PRESET = "eq_preset"

        val PRESETS = listOf(
            Preset("Plano", "Plano", floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)),
            Preset("Rock", "Rock", floatArrayOf(0.6f, 0.4f, 0.2f, -0.1f, -0.3f, -0.2f, 0.1f, 0.3f, 0.5f, 0.6f)),
            Preset("Pop", "Pop", floatArrayOf(-0.2f, 0.1f, 0.4f, 0.5f, 0.3f, -0.1f, -0.3f, -0.2f, 0.1f, 0.2f)),
            Preset("Jazz", "Jazz", floatArrayOf(0.5f, 0.3f, 0.1f, 0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f)),
            Preset("Clásico", "Clásico", floatArrayOf(0.6f, 0.5f, 0.3f, 0.1f, 0.0f, -0.1f, -0.1f, 0.0f, 0.2f, 0.4f)),
            Preset("Voz", "Voz", floatArrayOf(-0.3f, -0.2f, 0.2f, 0.5f, 0.6f, 0.5f, 0.2f, -0.1f, -0.3f, -0.4f)),
            Preset("Bajos", "Bajos++", floatArrayOf(0.8f, 0.6f, 0.4f, 0.1f, -0.2f, -0.4f, -0.5f, -0.4f, -0.2f, 0.0f)),
            Preset("Agudos", "Agudos++", floatArrayOf(-0.3f, -0.2f, -0.1f, 0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 0.7f, 0.8f))
        )

        data class Preset(val name: String, val displayName: String, val levels: FloatArray) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Preset) return false
                return name == other.name
            }
            override fun hashCode() = name.hashCode()
        }
    }

    private var _currentPreset: String = "Plano"
    val currentPreset: String get() = _currentPreset

    fun getPresetNames(): List<String> = PRESETS.map { it.displayName }
    fun getPresetKeys(): List<String> = PRESETS.map { it.name }

    fun init(): Boolean {
        return try {
            equalizer?.release()
            equalizer = Equalizer(0, audioSessionId)
            loadPreset()
            equalizer?.let { eq ->
                eq.enabled = isEnabled
                if (isEnabled) {
                    applyPresetByName(_currentPreset)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init equalizer: ${e.message}")
            false
        }
    }

    fun getNumberOfBands(): Int {
        return (equalizer?.numberOfBands ?: 0).toInt()
    }

    fun getBandLevelRange(): Pair<Short, Short> {
        val range = equalizer?.bandLevelRange
        return if (range != null && range.size >= 2) Pair(range[0], range[1]) else Pair(0.toShort(), 0.toShort())
    }

    fun getCenterFreq(band: Int): Int {
        return equalizer?.getCenterFreq(band.toShort()) ?: 0
    }

    fun getBandLevel(band: Int): Short {
        return equalizer?.getBandLevel(band.toShort()) ?: 0.toShort()
    }

    fun setBandLevel(band: Int, level: Short) {
        try {
            equalizer?.setBandLevel(band.toShort(), level)
            prefs.edit().putInt("band_$band", level.toInt()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set band level: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        try {
            equalizer?.enabled = enabled
            if (enabled) {
                loadBands(equalizer!!)
            }
            prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set enabled: ${e.message}")
        }
    }

    fun resetToFlat() {
        _currentPreset = "Plano"
        applyPresetByName("Plano")
        prefs.edit().putString(KEY_PRESET, "Plano").apply()
    }

    fun applyPresetByName(presetName: String) {
        val preset = PRESETS.find { it.name == presetName } ?: return
        _currentPreset = presetName
        val range = getBandLevelRange()
        val mid = ((range.first.toInt() + range.second.toInt()) / 2).toShort()
        val halfRange = (range.second - range.first) / 2
        val numBands = getNumberOfBands()
        for (i in 0 until numBands) {
            val presetIdx = (i * preset.levels.size) / numBands
            val rawLevel = if (presetIdx < preset.levels.size) preset.levels[presetIdx] else 0f
            val level = (mid + (halfRange * rawLevel).roundToInt()).toShort()
            try {
                equalizer?.setBandLevel(i.toShort(), level)
                prefs.edit().putInt("band_$i", level.toInt()).apply()
            } catch (_: Exception) {}
        }
        prefs.edit().putString(KEY_PRESET, presetName).apply()
    }

    private fun loadPreset() {
        val saved = prefs.getString(KEY_PRESET, "Plano") ?: "Plano"
        _currentPreset = saved
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        equalizer = null
    }

    private fun loadBands(eq: Equalizer) {
        for (i in 0 until eq.numberOfBands.toInt()) {
            val saved = prefs.getInt("band_$i", Int.MIN_VALUE)
            if (saved != Int.MIN_VALUE) {
                try {
                    eq.setBandLevel(i.toShort(), saved.toShort())
                } catch (_: Exception) {}
            }
        }
    }

    fun getBands(): List<Band> {
        val bands = mutableListOf<Band>()
        val range = getBandLevelRange()
        for (i in 0 until getNumberOfBands()) {
            bands.add(Band(
                index = i,
                centerFreq = getCenterFreq(i),
                level = getBandLevel(i),
                minLevel = range.first,
                maxLevel = range.second
            ))
        }
        return bands
    }

    data class Band(
        val index: Int,
        val centerFreq: Int,
        val level: Short,
        val minLevel: Short,
        val maxLevel: Short
    ) {
        fun freqLabel(): String {
            return when {
                centerFreq >= 1000000 -> "${centerFreq / 1000000} kHz"
                centerFreq >= 1000 -> "${centerFreq / 1000} Hz"
                else -> "$centerFreq Hz"
            }
        }

        fun levelPercent(): Float {
            val range = maxLevel - minLevel
            if (range.toInt() == 0) return 0.5f
            return (level - minLevel).toFloat() / range.toFloat()
        }

        fun levelFromPercent(percent: Float): Short {
            val range = maxLevel - minLevel
            return (minLevel + (range * percent).roundToInt()).toShort()
        }
    }
}
