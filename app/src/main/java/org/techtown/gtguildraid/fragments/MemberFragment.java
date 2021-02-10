package org.techtown.gtguildraid.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.models.GuildMember;
import org.techtown.gtguildraid.utils.RoomDB;

public class MemberFragment extends Fragment {
    RoomDB database;
    ViewGroup view;
    MemberCurrentFragment memberCurrentFragment;
    MemberResignedFragment memberResignedFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = (ViewGroup) inflater.inflate(R.layout.fragment_member, container, false);
        database = RoomDB.getInstance(getActivity());

        SharedPreferences pref = getActivity().getSharedPreferences("pref", Activity.MODE_PRIVATE);

        GuildMember me = database.memberDao().getMe();
        String nickname = me.getName();
        String guildName = pref.getString("guildName", "");

        TextView myName = view.findViewById(R.id.myName);
        TextView myGuildName = view.findViewById(R.id.guildName);

        myName.setText(nickname);
        myGuildName.setText(guildName);

        ImageView guildNameEditButton = view.findViewById(R.id.guildNameEditButton);
        guildNameEditButton.setOnClickListener(view -> {
            final Dialog dialog = new Dialog(getActivity());

            dialog.setContentView(R.layout.dialog_guildname);
            int width = WindowManager.LayoutParams.MATCH_PARENT;
            int height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
            dialog.show();

            EditText dialogGuildName = dialog.findViewById(R.id.guildName);
            dialogGuildName.setText(pref.getString("guildName", ""));

            Button button = dialog.findViewById(R.id.updateButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String sName = dialogGuildName.getText().toString().trim();
                    if(!sName.equals("")) {
                        pref.edit().putString("guildName", sName).apply();
                        myGuildName.setText(sName);
                        dialog.dismiss();
                    }
                    else{
                        showToast("길드명을 입력하세요");
                    }
                }
            });
        });
        ImageView myNameEditButton = view.findViewById(R.id.myNameEditButton);
        myNameEditButton.setOnClickListener(view -> {
            final Dialog dialog = new Dialog(getActivity());

            dialog.setContentView(R.layout.dialog_myname);
            int width = WindowManager.LayoutParams.MATCH_PARENT;
            int height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
            dialog.show();

            EditText dialogMyName = dialog.findViewById(R.id.myName);
            GuildMember me1 = database.memberDao().getMe();
            dialogMyName.setText(me1.getName());

            Button button = dialog.findViewById(R.id.updateButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String sName = dialogMyName.getText().toString().trim();
                    if(!sName.equals("")) {
                        database.memberDao().update(me1.getID(), sName, me1.getRemark());
                        myName.setText(sName);
                        dialog.dismiss();
                    }
                    else{
                        showToast("닉네임을 입력하세요");
                    }
                }
            });
        });

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
                Log.d("tabSelected", "" + position);
                Fragment selected = null;
                if(position == 0)
                    selected = memberCurrentFragment;
                else if(position == 1)
                    selected = memberResignedFragment;

                getChildFragmentManager().beginTransaction().replace(R.id.childContainer, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        return view;
    }



    private void showToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
