package org.techtown.gtguildraid.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.techtown.gtguildraid.Fragments.MemberFragment;
import org.techtown.gtguildraid.Fragments.RaidFragment;
import org.techtown.gtguildraid.Fragments.RecordFragment;
import org.techtown.gtguildraid.Fragments.StatisticFragment;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    RecordFragment recordFragment;
    MemberFragment memberFragment;
    RaidFragment raidFragment;
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
        raidFragment = new RaidFragment();
        statisticFragment = new StatisticFragment();

        RoomDB database = RoomDB.getInstance(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, memberFragment).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.recordTab:
                                Boolean isExist = database.raidDao().isCurrentRaidExist(new Date());
                                if(isExist)
                                    getSupportFragmentManager().beginTransaction().replace(R.id.container, recordFragment).commit();
                                else
                                    Toast.makeText(MainActivity.this, "레이드 정보를 입력하세요!", Toast.LENGTH_LONG).show();
                                return true;
                            case R.id.statisticTab:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, statisticFragment).commit();
                                return true;
                            case R.id.memberTab:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, memberFragment).commit();
                                return true;
                            case R.id.raidTab:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, raidFragment).commit();
                                return true;
                        }
                        return false;
                    }
                }
        );
    }
}