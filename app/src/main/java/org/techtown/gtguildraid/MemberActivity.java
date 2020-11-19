package org.techtown.gtguildraid;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemberActivity extends AppCompatActivity {
    RecyclerView cRecyclerView;

    List<GuildMember> memberList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    RoomDB database;
    MemberAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String nickname = pref.getString("nickname", "");
        TextView myName = findViewById(R.id.myName);
        myName.setText(nickname);

        Button createButton = findViewById(R.id.createButton);
        cRecyclerView = findViewById(R.id.currentRecyclerView);

        database = RoomDB.getInstance(this);

        memberList = database.memberDao().getAll();

        linearLayoutManager = new LinearLayoutManager(this);
        cRecyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MemberAdapter(MemberActivity.this, memberList);

        cRecyclerView.setAdapter(adapter);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(MemberActivity.this);

                dialog.setContentView(R.layout.dialog_update);
                int width = WindowManager.LayoutParams.MATCH_PARENT;
                int height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setLayout(width, height);
                dialog.show();

                final EditText name = dialog.findViewById(R.id.name);
                final EditText remark = dialog.findViewById(R.id.remark);
                Button updateButton = dialog.findViewById(R.id.updateButton);

                updateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String sName = name.getText().toString().trim();
                        String sRemark = remark.getText().toString().trim();
                        if(!sName.equals("")){
                            dialog.dismiss();
                            GuildMember member = new GuildMember();
                            member.setName(sName);
                            member.setRemark(sRemark);

                            database.memberDao().insert(member);

                            memberList.clear();
                            memberList.addAll(database.memberDao().getAll());
                            adapter.notifyDataSetChanged();
                        }
                        else{
                            Toast.makeText(MemberActivity.this, "이름을 입력하세요", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }
}
