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
    RecyclerView rRecyclerView;

    List<GuildMember> cMemberList = new ArrayList<>();
    List<GuildMember> rMemberList = new ArrayList<>();
    LinearLayoutManager cLinearLayoutManager;
    LinearLayoutManager rLinearLayoutManager;
    RoomDB database;
    MemberAdapter cAdapter;
    MemberAdapter rAdapter;
    TextView currentCnt;

    final int MAX_MEMBER = 29;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String nickname = pref.getString("nickname", "");
        TextView myName = findViewById(R.id.myName);
        myName.setText(nickname);

        Button createButton = findViewById(R.id.createButton);
        currentCnt = findViewById(R.id.currentCnt);
        database = RoomDB.getInstance(this);
        cLinearLayoutManager = new LinearLayoutManager(this);

        cRecyclerView = findViewById(R.id.currentRecyclerView);
        cMemberList = database.memberDao().getCurrentMembers();
        currentCnt.setText((cMemberList.size() + 1) + "/30");

        cRecyclerView.setLayoutManager(cLinearLayoutManager);
        cAdapter = new MemberAdapter(MemberActivity.this, cMemberList);

        cRecyclerView.setAdapter(cAdapter);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cMemberList.size() == MAX_MEMBER){
                    showToast("멤버 인원이 가득찼습니다.");
                    return;
                }
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
                            member.setResigned(false);

                            database.memberDao().insert(member);

                            cMemberList.clear();
                            cMemberList.addAll(database.memberDao().getCurrentMembers());
                            currentCnt.setText((cMemberList.size() + 1) + "/30");

                            cAdapter.notifyDataSetChanged();
                        }
                        else{
                            showToast("이름을 입력하세요");
                        }
                    }
                });
            }
        });

        rRecyclerView = findViewById(R.id.resignedRecyclerView);
        rMemberList = database.memberDao().getResignedMembers();

        rRecyclerView.setLayoutManager(rLinearLayoutManager);
        rAdapter = new MemberAdapter(MemberActivity.this, rMemberList);

        rRecyclerView.setAdapter(rAdapter);
    }

    private void showToast(String msg){
        Toast.makeText(MemberActivity.this, msg, Toast.LENGTH_LONG).show();
    }
}
