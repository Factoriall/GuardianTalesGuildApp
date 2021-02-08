package org.techtown.gtguildraid.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.techtown.gtguildraid.models.GuildMember;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.utils.RoomDB;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText nickname = findViewById(R.id.myName);
        final EditText guildName = findViewById(R.id.guildName);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            String cNickname = nickname.getText().toString();
            String cGuildName = guildName.getText().toString();
            if(cNickname.matches("") || cGuildName.matches("")){
                showToast("닉네임과 길드 이름을 적어주세요.");
                return;
            }

            SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

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
}
