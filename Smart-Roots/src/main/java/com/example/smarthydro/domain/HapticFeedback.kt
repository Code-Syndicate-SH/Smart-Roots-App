package com.example.smarthydro.domain

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class HapticFeedback {

    operator fun invoke(ctx: Context): Boolean {
        var hasHaptics = false
        val vibrator = ctx.getSystemService(Vibrator::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {   // this is version 29, apis before that did not have good haptic feedback
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            hasHaptics = true
        }
        return hasHaptics
    }
}