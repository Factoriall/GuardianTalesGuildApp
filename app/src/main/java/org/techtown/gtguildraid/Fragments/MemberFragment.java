package org.techtown.gtguildraid.Fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

import org.techtown.gtguildraid.R;

public class MemberFragment extends Fragment {
    ViewGroup view;
    CurMemberFragment curMemberFragment;
    ResMemberFragment resMemberFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_member, container, false);

        SharedPreferences pref = getActivity().getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String nickname = pref.getString("nickname", "");
        String guildName = pref.getString("guildName", "");

        TextView myName = view.findViewById(R.id.myName);
        TextView myGuildName = view.findViewById(R.id.guildName);

        myName.setText(nickname);
        myGuildName.setText(guildName);

        curMemberFragment = new CurMemberFragment();
        resMemberFragment = new ResMemberFragment();

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.childContainer, curMemberFragment).commit();

        TabLayout tabs = view.findViewById(R.id.tabs);
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

                getFragmentManager().beginTransaction().replace(R.id.childContainer, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        return view;
    }

    /*
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_member);

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
        );

    }

    private void showToast(String msg){
        Toast.makeText(MemberFragment.this, msg, Toast.LENGTH_LONG).show();
    }*/
}
