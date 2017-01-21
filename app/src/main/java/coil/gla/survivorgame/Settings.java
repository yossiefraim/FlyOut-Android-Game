package coil.gla.survivorgame;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import static coil.gla.survivorgame.MainActivity.intro;
import static coil.gla.survivorgame.MainActivity.isSound;

public class Settings extends Activity {

    private ImageView imageView;
    private Button back;
    private Switch musicb;
    private SeekBar volume = null;
    private AudioManager audioManager;
    int vol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set Screen to Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Turn off title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);

        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.logo);
        back = (Button) findViewById(R.id.back);
        musicb = (Switch) findViewById(R.id.switch1);
        volume = (SeekBar) findViewById(R.id.seekBar);
        volume.setProgress(50);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        musicb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b) {
                    intro.pause();
                    isSound = false;
                    Toast.makeText(getApplicationContext(), "Music is now OFF!", Toast.LENGTH_SHORT).show();
                }
                else {
                    intro.start();
                    isSound = true;
                    Toast.makeText(getApplicationContext(), "Music is now ON!", Toast.LENGTH_SHORT).show();
                }
           }
        });

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume.setMax(maxVolume);
        volume.setProgress(curVolume);

        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekb, int progress, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                vol = progress;
            }

            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
            }
        });
    }
}
