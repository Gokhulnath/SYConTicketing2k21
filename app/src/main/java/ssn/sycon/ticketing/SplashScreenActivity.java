package ssn.sycon.ticketing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import ssn.sycon.ticketing.utils.SharedPref;

public class SplashScreenActivity extends AppCompatActivity {

    ImageView logoIV;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();

        logoIV = findViewById(R.id.logoIV);
        logoIV.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),
                R.anim.rotate
        ));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SharedPref.getInt(SplashScreenActivity.this, "login") != 1) {
                    Intent login = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(login);
                    finish();
                } else {
                    Intent main = new Intent(SplashScreenActivity.this, MenuActivity.class);
                    startActivity(main);
                    finish();
                }


            }
        }, 2000);
    }

}