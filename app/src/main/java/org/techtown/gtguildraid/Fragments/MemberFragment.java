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
    MemberCurrentFragment memberCurrentFragment;
    MemberResignedFragment memberResignedFragment;

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

        memberCurrentFragment = new MemberCurrentFragment();
        memberResignedFragment = new MemberResignedFragment();

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.childContainer, memberCurrentFragment).commit();

        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("현재 멤버"));
        tabs.addTab(tabs.newTab().setText("탈퇴 멤버"));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Fragment selected = null;
                if(position == 0)
                    selected = memberCurrentFragment;
                else if(position == 1)
                    selected = memberResignedFragment;

                getFragmentManager().beginTransaction().replace(R.id.childContainer, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        return view;
    }
}
