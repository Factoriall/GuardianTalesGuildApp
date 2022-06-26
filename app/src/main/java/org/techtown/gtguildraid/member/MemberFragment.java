package org.techtown.gtguildraid.member;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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
import org.techtown.gtguildraid.common.HeroBottomSheetDialog;
import org.techtown.gtguildraid.models.entities.GuildMember;
import org.techtown.gtguildraid.models.entities.Hero;
import org.techtown.gtguildraid.repository.RoomDB;

import static android.app.Activity.RESULT_OK;

public class MemberFragment extends Fragment implements HeroBottomSheetDialog.BottomSheetListener {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    RoomDB database;
    ViewGroup view;
    MemberCurrentFragment memberCurrentFragment;
    MemberResignedFragment memberResignedFragment;
    TextView myName;
    TextView myGuildName;
    ImageView profileImage;
    ImageView profileImageInDialog;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101){
            if(resultCode == RESULT_OK){
                myName.setText(database.memberDao().getMe().getName());
                myGuildName.setText(pref.getString("guildName", ""));
                profileImage.setImageResource(
                        getResources().getIdentifier("character_" + pref.getString("profileImage", "knight"), "drawable", requireContext().getPackageName()));
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_member, container, false);
        database = RoomDB.getInstance(getActivity());

        pref = requireActivity().getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        GuildMember me = database.memberDao().getMe();
        String nickname = me.getName();
        String guildName = pref.getString("guildName", "");
        String profileImageName = pref.getString("profileImage", "knight");

        myName = view.findViewById(R.id.nickname);
        myGuildName = view.findViewById(R.id.guildName);
        profileImage = view.findViewById(R.id.profileImage);

        myName.setText(nickname);
        myGuildName.setText(guildName);
        profileImage.setImageResource(
                getResources().getIdentifier("character_" + profileImageName, "drawable", requireContext().getPackageName()));

        Button editButton = view.findViewById(R.id.editButton);
        editButton.setOnClickListener(view -> setDialogView());

        //fragment 생성
        memberCurrentFragment = new MemberCurrentFragment();
        memberResignedFragment = new MemberResignedFragment();

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.childContainer, memberCurrentFragment).commit();

        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("현재 멤버"));
        tabs.addTab(tabs.newTab().setText("탈퇴 멤버"));
        //tab 선택 통해 fragment 선택
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

                assert selected != null;
                getChildFragmentManager().beginTransaction().replace(R.id.childContainer, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        return view;
    }

    //profile 변경 dialog
    private void setDialogView() {
        final Dialog dialog = new Dialog(getActivity());

        //dialog 설정
        dialog.setContentView(R.layout.dialog_profile_edit);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        EditText dialogGuildName = dialog.findViewById(R.id.guildName);
        EditText dialogNickname = dialog.findViewById(R.id.nickname);
        profileImageInDialog = dialog.findViewById(R.id.profileImage);

        GuildMember me1 = database.memberDao().getMe();
        dialogNickname.setText(me1.getName());
        dialogGuildName.setText(pref.getString("guildName", ""));
        profileImageInDialog.setImageResource(
                getResources().getIdentifier("character_" +
                        pref.getString("profileImage", "knight"), "drawable", requireContext().getPackageName()));
        profileImageInDialog.setOnClickListener(view1 -> {
            HeroBottomSheetDialog bottomDialog = new HeroBottomSheetDialog(this);
            bottomDialog.show(requireActivity().getSupportFragmentManager(), "bottomSheetDialog");
        });


        Button button = dialog.findViewById(R.id.updateButton);
        button.setOnClickListener(view -> {
            String sGuildName = dialogGuildName.getText().toString().trim();
            String sNickname = dialogNickname.getText().toString().trim();
            if(!sGuildName.equals("") && !sNickname.equals("")) {
                database.memberDao().update(me1.getID(), sNickname, me1.getRemark());
                myName.setText(sNickname);

                editor.putString("guildName", sGuildName);
                editor.apply();
                myGuildName.setText(sGuildName);

                String newProfileString = pref.getString("profileImage", "knight");
                Log.d("dialog", newProfileString);

                profileImage.setImageResource(
                        getResources().getIdentifier("character_" +
                                newProfileString, "drawable", getContext().getPackageName()));

                dialog.dismiss();
            }
            else{
                Toast.makeText(getActivity(), "길드 이름 및 닉네임을 입력하세요", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onImageClicked(Hero hero) {
        editor.putString("profileImage", hero.getEnglishName());
        profileImageInDialog.setImageResource(
                getResources().getIdentifier("character_" + hero.getEnglishName() , "drawable", requireContext().getPackageName()));
    }
}
