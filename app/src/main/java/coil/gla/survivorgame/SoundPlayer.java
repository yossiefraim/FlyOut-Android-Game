package coil.gla.survivorgame;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by GalAmitai-PC on 31-Dec-16.
 */

public class SoundPlayer {
    private static SoundPool soundPool;
    private static int hitSound;

    public SoundPlayer(Context context) {
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        hitSound = soundPool.load(context, R.raw.single_strike_of_match,1);
    }

    public void playHitSound() {
        soundPool.play(hitSound, 1.0f, 1.0f, 1, 0, 1.0f);
    }

}
