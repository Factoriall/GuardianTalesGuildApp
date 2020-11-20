package org.techtown.gtguildraid;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import org.techtown.gtguildraid.Helper.MyButtonClickListener;
import org.techtown.gtguildraid.Helper.MySwipeHelper;

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
    TextView resignedCnt;

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
        resignedCnt = findViewById(R.id.resignedCnt);
        database = RoomDB.getInstance(this);

        cLinearLayoutManager = new LinearLayoutManager(this);
        cRecyclerView = findViewById(R.id.currentRecyclerView);
        cMemberList = database.memberDao().getCurrentMembers();
        currentCnt.setText((cMemberList.size() + 1) + "/30");

        cRecyclerView.setLayoutManager(cLinearLayoutManager);
        cAdapter = new MemberAdapter(MemberActivity.this, cMemberList);
        cRecyclerView.setAdapter(cAdapter);

        rLinearLayoutManager = new LinearLayoutManager(this);
        rRecyclerView = findViewById(R.id.resignedRecyclerView);
        rMemberList = database.memberDao().getResignedMembers();
        resignedCnt.setText(rMemberList.size() + "명");

        rRecyclerView.setLayoutManager(rLinearLayoutManager);
        rAdapter = new MemberAdapter(MemberActivity.this, rMemberList);
        rRecyclerView.setAdapter(rAdapter);

        MySwipeHelper swipeHelper1 = new MySwipeHelper(this, cRecyclerView, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(MemberActivity.this,
                        "삭제",
                        50,
                        0,
                        Color.parseColor("#FF0000"),
                        new MyButtonClickListener(){
                            @Override
                            public void onClick(int pos) {
                                deleteMember(cMemberList, cAdapter, pos);
                                currentCnt.setText((cMemberList.size() + 1) + "/30");
                            }
                        }));
                buffer.add(new MyButton(MemberActivity.this,
                        "탈퇴",
                        50,
                        0,
                        Color.parseColor("#FFFF00"),
                        new MyButtonClickListener(){
                            @Override
                            public void onClick(int pos) {
                                resignMember(pos);
                            }
                        }));
                buffer.add(new MyButton(MemberActivity.this,
                        "수정",
                        50,
                        0,
                        Color.parseColor("#90ee90"),
                        new MyButtonClickListener(){
                            @Override
                            public void onClick(int pos) {
                                updateMember(cMemberList, cAdapter, pos);
                            }
                        }));
            }
        };

        MySwipeHelper swipeHelper2 = new MySwipeHelper(this, rRecyclerView, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(MemberActivity.this,
                        "삭제",
                        50,
                        0,
                        Color.parseColor("#FF0000"),
                        new MyButtonClickListener(){
                            @Override
                            public void onClick(int pos) {
                                deleteMember(rMemberList, rAdapter, pos);
                                resignedCnt.setText((rMemberList.size()) + "명");
                            }
                        }));
                buffer.add(new MyButton(MemberActivity.this,
                        "복귀",
                        50,
                        0,
                        Color.parseColor("#FFFF00"),
                        new MyButtonClickListener(){
                            @Override
                            public void onClick(int pos) {
                                recoverMember(pos);
                            }
                        }));
                buffer.add(new MyButton(MemberActivity.this,
                        "수정",
                        50,
                        0,
                        Color.parseColor("#90ee90"),
                        new MyButtonClickListener(){
                            @Override
                            public void onClick(int pos) {
                                updateMember(rMemberList, rAdapter, pos);
                            }
                        }));
            }
        };


        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createMember();
            }
        });
    }

    private void recoverMember(int pos) {
        if(cMemberList.size() == MAX_MEMBER){
            showToast("인원이 가득찼습니다!");
            return;
        }
        GuildMember m = rMemberList.get(pos);
        database.memberDao().setIsResigned(m.getID(), false);

        rMemberList.remove(pos);

        cMemberList.clear();
        cMemberList.addAll(database.memberDao().getCurrentMembers());
        cAdapter.notifyDataSetChanged();

        resignedCnt.setText(rMemberList.size() + "명");
        currentCnt.setText((cMemberList.size() + 1) + "/30");

        rAdapter.notifyItemRemoved(pos);
        rAdapter.notifyItemRangeChanged(pos, rMemberList.size());
    }

    private void updateMember(final List<GuildMember> memberList, final MemberAdapter adapter, int pos) {
        GuildMember m = memberList.get(pos);
        final int sID = m.getID();
        String sName = m.getName();
        String sRemark = m.getRemark();

        final Dialog dialog = new Dialog(MemberActivity.this);

        dialog.setContentView(R.layout.dialog_update);

        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        final EditText name = dialog.findViewById(R.id.name);
        final EditText remark = dialog.findViewById(R.id.remark);
        Button updateButton = dialog.findViewById(R.id.updateButton);

        name.setText(sName);
        remark.setText(sRemark);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                String uName = name.getText().toString().trim();
                String uRemark = remark.getText().toString().trim();

                database.memberDao().update(sID, uName, uRemark);

                memberList.clear();
                memberList.addAll(database.memberDao().getCurrentMembers());

                adapter.notifyDataSetChanged();
            }
        });
        showToast("Update click");
    }

    private void resignMember(int pos) {
        GuildMember m = cMemberList.get(pos);
        database.memberDao().setIsResigned(m.getID(), true);

        cMemberList.remove(pos);

        rMemberList.clear();
        rMemberList.addAll(database.memberDao().getResignedMembers());

        resignedCnt.setText(rMemberList.size() + "명");
        currentCnt.setText((cMemberList.size() + 1) + "/30");

        cAdapter.notifyItemRemoved(pos);
        cAdapter.notifyItemRangeChanged(pos, cMemberList.size());
        rAdapter.notifyDataSetChanged();
        showToast("Resign click");
    }

    private void deleteMember(List<GuildMember> memberList, MemberAdapter adapter, int pos) {
        GuildMember m = memberList.get(pos);

        database.memberDao().delete(m);

        memberList.remove(pos);

        adapter.notifyItemRemoved(pos);
        adapter.notifyItemRangeChanged(pos, memberList.size());
        showToast("Delete click");
    }

    private void createMember() {
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
                    member.setMe(false);

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

    private void showToast(String msg){
        Toast.makeText(MemberActivity.this, msg, Toast.LENGTH_LONG).show();
    }
}
