package org.techtown.gtguildraid.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.techtown.gtguildraid.Fragments.BossFragment;
import org.techtown.gtguildraid.Fragments.MemberFragment;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Fragments.RecordFragment;
import org.techtown.gtguildraid.Fragments.StatisticFragment;

public class MainActivity extends AppCompatActivity {

    RecordFragment recordFragment;
    MemberFragment memberFragment;
    BossFragment bossFragment;
    StatisticFragment statisticFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        Boolean isRegistered = pref.getBoolean("isRegistered", false);
        if(!isRegistered){
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_down );
        }

        recordFragment = new RecordFragment();
        memberFragment = new MemberFragment();
        bossFragment = new BossFragment();
        statisticFragment = new StatisticFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, memberFragment).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.recordTab:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, recordFragment).commit();
                                return true;
                            case R.id.statisticTab:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, statisticFragment).commit();
                                return true;
                            case R.id.memberTab:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, memberFragment).commit();
                                return true;
                            case R.id.bossTab:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, bossFragment).commit();
                                return true;
                        }
                        return false;
                    }
                }
        );
        /*
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
        });*/
    }
}