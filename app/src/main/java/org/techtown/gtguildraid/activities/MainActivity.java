package org.techtown.gtguildraid.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.fragments.MemberFragment;
import org.techtown.gtguildraid.fragments.RaidRenewalFragment;
import org.techtown.gtguildraid.fragments.RecordFragment;
import org.techtown.gtguildraid.fragments.StatisticRenewalFragment;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MemberFragment memberFragment;
    RaidRenewalFragment raidFragment;
    RecordFragment recordFragment;
    StatisticRenewalFragment statisticRenewalFragment;
    private Toast myToast;

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
        getSupportActionBar().hide();

        memberFragment = new MemberFragment();
        raidFragment = new RaidRenewalFragment();
        recordFragment = new RecordFragment();
        statisticRenewalFragment = new StatisticRenewalFragment();
        myToast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_LONG);

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
                            if(!database.raidDao().isCurrentRaidExist(new Date())){
                                Toast.makeText(MainActivity.this, "레이드 정보를 입력하세요!", Toast.LENGTH_LONG).show();
                                return false;
                            }
                            Raid raid = database.raidDao().getCurrentRaidWithBosses(new Date());
                            List<Boss> bosses = raid.getBossList();
                            boolean isElementAllExist = true;
                            for(Boss boss : bosses){
                                if(boss.getElementId() == 0){
                                    isElementAllExist = false;
                                    break;
                                }
                            }
                            if(!isElementAllExist){
                                showToast("보스 속성 정보를 모두 입력하세요!");
                                return false;
                            }
                            showToast("최근 기록: " + pref.getString("recentWrite", "없음"));

                            getSupportFragmentManager().beginTransaction().replace(R.id.container, recordFragment).commit();
                            return true;
                        case R.id.statisticTab:
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, statisticRenewalFragment).commit();
                            return true;
                    }
                    return false;
                }
        );

        boolean isAccessible = database.raidDao().isCurrentRaidExist(new Date())
                &&
                (database.raidDao().getCurrentRaid(new Date()).getStartDay().compareTo(new Date()) <= 0);
        if(isAccessible) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, recordFragment).commit();
            bottomNavigationView.setSelectedItemId(R.id.recordTab);
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, raidFragment).commit();
            bottomNavigationView.setSelectedItemId(R.id.raidTab);
        }
    }

    public void showToast(String msg){
        if(myToast != null) myToast.cancel();
        myToast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);
        myToast.setText(msg);
        myToast.show();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("종료하시겠습니까?")
                .setPositiveButton("네", (dialog, id) -> {
                    dialog.dismiss();
                    finish();
                })
                .setNegativeButton("아니오", (dialog, id) -> {
                    dialog.dismiss();
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}