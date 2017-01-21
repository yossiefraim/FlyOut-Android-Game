package coil.gla.survivorgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class Splash_Screen extends Activity {

    private static int SPLASH_TIMEOUT = 2500;
    private ImageView splashbg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        // Set Screen to Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Turn off title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash__screen);
        splashbg = (ImageView)findViewById(R.id.imageView2);
        splashbg.setImageResource(R.drawable.splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent home = new Intent(Splash_Screen.this, MainActivity.class);
                startActivity(home);
                finish();
            }
        },SPLASH_TIMEOUT);
    }
}
