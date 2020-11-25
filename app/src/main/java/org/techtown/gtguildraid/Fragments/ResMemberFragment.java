package org.techtown.gtguildraid.Fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Adapters.MemberAdapter;
import org.techtown.gtguildraid.Interfaces.MyButtonClickListener;
import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.MySwipeHelper;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.List;

public class ResMemberFragment extends Fragment {
    RecyclerView recyclerView;
    List<GuildMember> memberList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    RoomDB database;
    MemberAdapter adapter;
    final int MAX_MEMBER = 29;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_res_member, container, false);

        database = RoomDB.getInstance(getActivity());

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = view.findViewById(R.id.resignedRecyclerView);
        memberList = database.memberDao().getResignedMembers();
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MemberAdapter(getActivity(), memberList);
        recyclerView.setAdapter(adapter);

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
                            }
                        }));
                buffer.add(new MyButton(getActivity(),
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
                memberList.addAll(database.memberDao().getResignedMembers());

                adapter.notifyDataSetChanged();
            }
        });
        showToast("Update click");
    }

    private void recoverMember(int pos) {
        int memberNum = database.memberDao().getCurrentMembers().size();
        if(memberNum == MAX_MEMBER){
            showToast("인원이 가득찼습니다!");
            return;
        }

        GuildMember m = memberList.get(pos);
        database.memberDao().setIsResigned(m.getID(), false);

        memberList.remove(pos);

        adapter.notifyItemRemoved(pos);
        adapter.notifyItemRangeChanged(pos, memberList.size());
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
