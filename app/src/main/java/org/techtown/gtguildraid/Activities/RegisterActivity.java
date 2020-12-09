package org.techtown.gtguildraid.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText nickname = findViewById(R.id.raidName);
        final EditText guildName = findViewById(R.id.guildName);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                member.setRemark(cGuildName);
                member.setResigned(false);
                member.setMe(true);
                database.memberDao().insert(member);

                editor.putString("nickname", cNickname);
                editor.putString("guildName", cGuildName);
                editor.putBoolean("isRegistered", true);
                editor.commit();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
