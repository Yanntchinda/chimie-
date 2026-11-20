package com.yanntchinda.centrenotifications;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

/** État de l'appareil, utile pour décider si le centre doit s'afficher sur l'écran verrouillé. */
final class DeviceState {

    private DeviceState() {
    }

    /** Vrai quand l'écran est éteint ou que le téléphone est verrouillé. */
    static boolean lockedOrOff(Context context) {
        if (context == null) return false;
        PowerManager power = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        KeyguardManager keyguard = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean off = power != null && !power.isInteractive();
        boolean locked = keyguard != null && keyguard.isKeyguardLocked();
        return off || locked;
    }
}
