package coil.gla.survivorgame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {

    private ImageView imageView;
    public MediaPlayer intro;
    private boolean isSound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        // Set Screen to Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Turn off title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        Thread tMusic = new Thread(new Runnable() {
            @Override
            public void run() {
                intro = MediaPlayer.create(MainActivity.this, R.raw.intro);
                intro.start();
                isSound = true;
            }
        });
        tMusic.start();

        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.logo);



        Button play = (Button)findViewById(R.id.play);
        Button soundb = (Button) findViewById(R.id.musicb);
        Button aboutb = (Button) findViewById(R.id.about);


        soundb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSound) {
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

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Game.class);
                startActivity(intent);
            }
        });

        aboutb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, About.class);
                startActivity(intent);
            }
        });
    }
    @Override
     protected void onDestroy() {
        super.onPause();
        intro.release();
    }
}