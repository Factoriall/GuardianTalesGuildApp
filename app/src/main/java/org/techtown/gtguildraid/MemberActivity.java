package org.techtown.gtguildraid;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import org.techtown.gtguildraid.Helper.MyButtonClickListener;
import org.techtown.gtguildraid.Helper.MySwipeHelper;

import java.util.ArrayList;
import java.util.List;

public class MemberActivity extends AppCompatActivity {
    Toolbar toolbar;
    CurMemberFragment curMemberFragment;
    ResMemberFragment resMemberFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String nickname = pref.getString("nickname", "");
        TextView myName = findViewById(R.id.myName);
        myName.setText(nickname);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        curMemberFragment = new CurMemberFragment();
        resMemberFragment = new ResMemberFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, curMemberFragment).commit();

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("현재 멤버"));
        tabs.addTab(tabs.newTab().setText("탈퇴 멤버"));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Fragment selected = null;
                if(position == 0)
                    selected = curMemberFragment;
                else if(position == 1)
                    selected = resMemberFragment;

                getSupportFragmentManager().beginTransaction().replace(R.id.container, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
        /*
        getSupportFragmentManager().beginTransaction().replace(R.id.container, curMemberFragment).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.currentTab:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, curMemberFragment).commit();
                                return true;
                            case R.id.resignedTab:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, resMemberFragment).commit();
                                return true;
                        }
                        return false;
                    }
                }
        );*/

    }

    private void showToast(String msg){
        Toast.makeText(MemberActivity.this, msg, Toast.LENGTH_LONG).show();
    }
}
