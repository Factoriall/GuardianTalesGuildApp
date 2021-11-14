package org.techtown.gtguildraid.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import org.techtown.gtguildraid.models.entities.Boss;
import org.techtown.gtguildraid.models.entities.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MemberFragment memberFragment;
    RaidRenewalFragment raidFragment;
    RecordFragment recordFragment;
    StatisticRenewalFragment statisticRenewalFragment;
    private Toast myToast;

    //RegisterActivity 이후로 fragment를 리셋시키는 용도로 넣음
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

        //필요한 fragment 초기화
        memberFragment = new MemberFragment();
        raidFragment = new RaidRenewalFragment();
        recordFragment = new RecordFragment();
        statisticRenewalFragment = new StatisticRenewalFragment();
        myToast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_LONG);

        //sharedPreference를 통해 처음 register됐는지 확인, mode_private는 자신 앱에서만 사용하겠다는 의미
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        boolean isRegistered = pref.getBoolean("isRegistered", false);
        if(!isRegistered){
            startActivityForResult(new Intent(MainActivity.this, RegisterActivity.class), 101);
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_down );
            //RegisterActivity 시작 및 끝날 시의 애니메이션 삽입
        }

        RoomDB database = RoomDB.getInstance(this);

        /* BottomNavigationView
        getSupportFragmentManager().beginTransaction().replace를 통해 tab 내용을 바꿀 수 있음
        bottom_navigation에 menu 정보를 넣어 여기에 Tab 정보를 삽입할 수 있음
         */
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

        //처음에 보스 정보가 다 정해져있으면 raidTab을, 아니면 recordTab을 사용
        boolean isElementAllSelected = false;
        if(database.raidDao().isCurrentRaidExist(new Date())) {
            Raid raid = database.raidDao().getCurrentRaidWithBosses(new Date());
            List<Boss> bosses = raid.getBossList();
            boolean isElementAllExist = true;
            for(Boss boss : bosses){
                if(boss.getElementId() == 0){
                    isElementAllExist = false;
                    break;
                }
            }
            if(isElementAllExist) isElementAllSelected = true;
        }
        boolean isStarted = database.raidDao().isCurrentRaidExist(new Date())
                &&
                (database.raidDao().getCurrentRaid(new Date()).getStartDay().compareTo(new Date()) <= 0);
        if(isElementAllSelected && isStarted){
            getSupportFragmentManager().beginTransaction().replace(R.id.container, recordFragment).commit();
            bottomNavigationView.setSelectedItemId(R.id.recordTab);
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, raidFragment).commit();
            bottomNavigationView.setSelectedItemId(R.id.raidTab);
        }

        //권한을 부여하는 library 사용.
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() { }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                showToast("권한 거부\n" + deniedPermissions.toString());
            }
        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("권한 거부 시 레이드 정보를 종합해주는 엑셀 파일 출력이 안될 수 있습니다.\n\n[설정] > [권한] 에서 수동으로 권한 부여를 해주시길 바랍니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    public void showToast(String msg){
        if(myToast != null) myToast.cancel();
        myToast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);
        myToast.setText(msg);
        myToast.show();
    }

    //backPress 관련
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