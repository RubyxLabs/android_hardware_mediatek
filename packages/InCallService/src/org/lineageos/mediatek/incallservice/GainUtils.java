/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.mediatek.incallservice;

import android.media.AudioSystem;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.util.Log;

public class GainUtils {
    public static final String LOG_TAG = "MtkInCallService";
    public static final int volSteps = SystemProperties.getInt("ro.config.vc_call_vol_steps", 7);

    /**
     * Sets the gain level for a given audio device.
     * @param audioDevice The audio device to set the gain level for.
     * @param gainIndex The gain level to set.
     * @param streamType The stream type to set the gain level for.
     */
    public static void setGainLevel(int audioDevice, int gainIndex, int streamType) {
        String parameters = String.format("volumeDevice=%d;volumeIndex=%d;volumeStreamType=%d",
                                          audioDevice, Math.min(volSteps, gainIndex), streamType);
        Log.d(LOG_TAG, "Setting audio parameters to: " + parameters);
        AudioSystem.setParameters(parameters);
    }

    /**
     * Applies the current call volume to the selected communication device.
     */
    public static void setGainLevel(AudioManager audioManager) {
        AudioDeviceInfo callDevice = audioManager.getCommunicationDevice();
        if (callDevice == null) {
            return;
        }

        // The HAL expects the internal output-device mask, not AudioDeviceInfo.TYPE_*.
        setGainLevel(callDevice.getPort().type(),
                audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
                AudioSystem.STREAM_VOICE_CALL);
    }
}
