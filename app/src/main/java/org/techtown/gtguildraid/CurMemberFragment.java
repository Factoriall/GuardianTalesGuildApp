package org.techtown.gtguildraid;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Helper.MyButtonClickListener;
import org.techtown.gtguildraid.Helper.MySwipeHelper;

import java.util.ArrayList;
import java.util.List;

public class CurMemberFragment extends Fragment {
    RecyclerView recyclerView;
    List<GuildMember> memberList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    RoomDB database;
    MemberAdapter adapter;
    TextView currentCnt;
    final int MAX_MEMBER = 29;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_cur_member, container, false);

        Button createButton = view.findViewById(R.id.createButton);
        currentCnt = view.findViewById(R.id.currentCnt);

        database = RoomDB.getInstance(getActivity());

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = view.findViewById(R.id.currentRecyclerView);
        memberList = database.memberDao().getCurrentMembers();
        currentCnt.setText((memberList.size() + 1) + "/30");

        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MemberAdapter(getActivity(), memberList);
        recyclerView.setAdapter(adapter);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createMember();
            }
        });

        MySwipeHelper swipeHelper = new MySwipeHelper(getActivity(), recyclerView, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(getActivity(),
                        "삭제",
                        50,
                        0,
                        Color.parseColor("#FF0000"),
                        new MyButtonClickListener(){
                            @Override
                            public void onClick(int pos) {
                                deleteMember(pos);
                                currentCnt.setText((memberList.size() + 1) + "/30");
                            }
                        }));
                buffer.add(new MyButton(getActivity(),
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
                buffer.add(new MyButton(getActivity(),
                        "수정",
                        50,
                        0,
                        Color.parseColor("#90ee90"),
                        new MyButtonClickListener(){
                            @Override
                            public void onClick(int pos) {
                                updateMember(pos);
                            }
                        }));
            }
        };

        return view;
    }

    private void createMember() {
        if(memberList.size() == MAX_MEMBER){
            showToast("멤버 인원이 가득찼습니다.");
            return;
        }
        final Dialog dialog = new Dialog(getActivity());

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

                    memberList.clear();
                    memberList.addAll(database.memberDao().getCurrentMembers());
                    currentCnt.setText((memberList.size() + 1) + "/30");

                    adapter.notifyDataSetChanged();
                }
                else{
                    showToast("이름을 입력하세요");
                }
            }
        });
    }

    private void updateMember(int pos) {
        GuildMember m = memberList.get(pos);
        final int sID = m.getID();
        String sName = m.getName();
        String sRemark = m.getRemark();

        final Dialog dialog = new Dialog(getActivity());

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
        GuildMember m = memberList.get(pos);
        database.memberDao().setIsResigned(m.getID(), true);

        memberList.remove(pos);

        currentCnt.setText((memberList.size() + 1) + "/30");

        adapter.notifyItemRemoved(pos);
        adapter.notifyItemRangeChanged(pos, memberList.size());
        showToast("Resign click");
    }

    private void deleteMember(int pos) {
        GuildMember m = memberList.get(pos);

        database.memberDao().delete(m);

        memberList.remove(pos);

        adapter.notifyItemRemoved(pos);
        adapter.notifyItemRangeChanged(pos, memberList.size());
        showToast("Delete click");
    }

    private void showToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
