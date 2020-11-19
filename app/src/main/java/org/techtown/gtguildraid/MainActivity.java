package org.techtown.gtguildraid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        Boolean isRegistered = pref.getBoolean("isRegistered", false);
        String nickname = pref.getString("nickname", "");
        String guildName = pref.getString("guildName", "");

        TextView nNameText = findViewById(R.id.myName);
        TextView gNameText = findViewById(R.id.guildName);

        if(!isRegistered){
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_down );
        }

        nNameText.setText("닉네임: " + nickname);
        gNameText.setText("길드 이름: " + guildName);

        Button memberButton = findViewById(R.id.memberButton);
        memberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MemberActivity.class);
                startActivity(intent);
            }
        });

        Button bossButton = findViewById(R.id.bossButton);
        bossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BossActivity.class);
                startActivity(intent);
            }
        });

        Button recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
            }
        });

        Button statisticButton = findViewById(R.id.statisticButton);
        statisticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, StatisticActivity.class);
                startActivity(intent);
            }
        });
    }
}