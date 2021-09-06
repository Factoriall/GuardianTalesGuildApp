package org.techtown.gtguildraid.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.etc.HeroBottomSheetDialog;
import org.techtown.gtguildraid.models.daos.GuildMember;
import org.techtown.gtguildraid.models.daos.Hero;
import org.techtown.gtguildraid.utils.RoomDB;

public class RegisterActivity extends AppCompatActivity
        implements HeroBottomSheetDialog.BottomSheetListener{
    SharedPreferences.Editor editor;
    ImageView profileImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("내 프로필 등록");

        editor = getSharedPreferences("pref", Activity.MODE_PRIVATE).edit();

        final EditText nickname = findViewById(R.id.nickname);
        final EditText guildName = findViewById(R.id.guildName);
        profileImage = findViewById(R.id.profileImage);
        profileImage.setImageResource(
                getResources().getIdentifier("character_knight" , "drawable", getPackageName()));
        profileImage.setOnClickListener(view -> {
            HeroBottomSheetDialog dialog = new HeroBottomSheetDialog(this);
            dialog.show(getSupportFragmentManager(), "bottomSheetDialog");
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            String cNickname = nickname.getText().toString();
            String cGuildName = guildName.getText().toString();
            if(cNickname.matches("") || cGuildName.matches("")){
                showToast("닉네임과 길드 이름을 적어주세요.");
                return;
            }

            RoomDB database = RoomDB.getInstance(RegisterActivity.this);
            GuildMember member = new GuildMember();
            member.setName(cNickname);
            member.setRemark("");
            member.setResigned(false);
            member.setMe(true);
            database.memberDao().insert(member);

            editor.putString("guildName", cGuildName);
            editor.putBoolean("isRegistered", true);
            editor.apply();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);

            finish();
        });
    }

    @Override
    public void onBackPressed() {
        showToast("닉네임 및 길드 이름 삽입 후 시작 버튼을 눌러주세요");
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onImageClicked(Hero hero) {
        editor.putString("profileImage", hero.getEnglishName());
        profileImage.setImageResource(
                getResources().getIdentifier("character_" + hero.getEnglishName() , "drawable", getPackageName()));
    }
}
