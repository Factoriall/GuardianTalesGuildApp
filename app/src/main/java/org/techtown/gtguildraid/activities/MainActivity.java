package org.techtown.gtguildraid.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.fragments.MemberFragment;
import org.techtown.gtguildraid.fragments.RaidFragment;
import org.techtown.gtguildraid.fragments.RecordFragment;
import org.techtown.gtguildraid.fragments.StatisticFragment;
import org.techtown.gtguildraid.utils.RoomDB;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    MemberFragment memberFragment;
    RaidFragment raidFragment;
    RecordFragment recordFragment;
    StatisticFragment statisticFragment;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101){
            if(resultCode == RESULT_OK){
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                assert fragment != null;
                ft.detach(fragment);
                ft.attach(fragment);
                ft.commit();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memberFragment = new MemberFragment();
        raidFragment = new RaidFragment();
        recordFragment = new RecordFragment();
        statisticFragment = new StatisticFragment();

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        boolean isRegistered = pref.getBoolean("isRegistered", false);
        if(!isRegistered){
            startActivityForResult(new Intent(MainActivity.this, RegisterActivity.class), 101);
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_down );
        }

        RoomDB database = RoomDB.getInstance(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch(item.getItemId()) {
                        case R.id.memberTab:
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, memberFragment).commit();
                            return true;
                        case R.id.raidTab:
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, raidFragment).commit();
                            return true;
                        case R.id.recordTab:
                            Boolean isRaidExist = database.raidDao().isCurrentRaidExist(new Date());
                            if(!isRaidExist){
                                Toast.makeText(MainActivity.this, "레이드 정보를 입력하세요!", Toast.LENGTH_LONG).show();
                                return false;
                            }
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, recordFragment).commit();
                            return true;
                        case R.id.statisticTab:
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, statisticFragment).commit();
                            return true;
                    }
                    return false;
                }
        );

        boolean isAccessible = database.raidDao().isCurrentRaidExist(new Date())
                && database.raidDao().getCurrentRaid(new Date()).getStartDay().compareTo(new Date()) <= 0;
        if(isAccessible) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, recordFragment).commit();
            bottomNavigationView.setSelectedItemId(R.id.recordTab);
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, raidFragment).commit();
            bottomNavigationView.setSelectedItemId(R.id.raidTab);
        }
    }
}