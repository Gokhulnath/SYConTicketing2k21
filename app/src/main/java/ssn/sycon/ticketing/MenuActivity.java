package ssn.sycon.ticketing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {
    Button overallBT;
    Button personalBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        overallBT = findViewById(R.id.overallBT);
        personalBT = findViewById(R.id.personalBT);

        overallBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent list = new Intent(MenuActivity.this, MainActivity.class);
                startActivity(list);
            }
        });

        personalBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent list = new Intent(MenuActivity.this, PersonalActivity.class);
                startActivity(list);
            }
        });
    }
}