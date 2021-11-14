package org.techtown.gtguildraid.fragments;

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

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.MemberCardAdapter;
import org.techtown.gtguildraid.models.entities.GuildMember;
import org.techtown.gtguildraid.utils.MySwipeHelper;
import org.techtown.gtguildraid.utils.RoomDB;

import java.util.ArrayList;
import java.util.List;

public class MemberResignedFragment extends Fragment {
    TextView resignCnt;
    RecyclerView recyclerView;
    List<GuildMember> memberList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    RoomDB database;
    MemberCardAdapter adapter;
    final int MAX_MEMBER = 29;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_member_resigned, container, false);

        database = RoomDB.getInstance(getActivity());

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = view.findViewById(R.id.resignedRecyclerView);
        memberList = database.memberDao().getResignedMembers();
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MemberCardAdapter(memberList);
        recyclerView.setAdapter(adapter);

        resignCnt = view.findViewById(R.id.resignCnt);
        resignCnt.setText(getString(R.string.member_numbers_resign, memberList.size()));

        //swipeHelper를 통해 swipe 구현
        new MySwipeHelper(getActivity(), recyclerView, 200) {
            //instaiateMyButton 구현을 통해 button 관련 정보를 삽입
            @Override
            public void instantiateMyButton(List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(getActivity(),
                        "삭제",
                        50,
                        0,
                        Color.parseColor("#FF0000"),
                        pos -> deleteMember(pos)));
                buffer.add(new MyButton(getActivity(),
                        "복귀",
                        50,
                        0,
                        Color.parseColor("#FFFF00"),
                        pos -> recoverMember(pos)));
                buffer.add(new MyButton(getActivity(),
                        "수정",
                        50,
                        0,
                        Color.parseColor("#90ee90"),
                        pos -> updateMember(pos)));
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

        dialog.setContentView(R.layout.dialog_member);

        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        final EditText name = dialog.findViewById(R.id.name);
        final EditText remark = dialog.findViewById(R.id.remark);
        Button updateButton = dialog.findViewById(R.id.updateButton);

        name.setText(sName);
        remark.setText(sRemark);

        updateButton.setOnClickListener(view -> {
            dialog.dismiss();
            String uName = name.getText().toString().trim();
            String uRemark = remark.getText().toString().trim();

            database.memberDao().update(sID, uName, uRemark);

            memberList.clear();
            memberList.addAll(database.memberDao().getResignedMembers());
            resignCnt.setText(getString(R.string.member_numbers_resign, memberList.size()));

            adapter.notifyDataSetChanged();
        });
    }

    private void recoverMember(int pos) {
        int memberNum = database.memberDao().getCurrentMembersWithoutMe().size();
        if(memberNum == MAX_MEMBER){
            showToast();
            return;
        }

        GuildMember m = memberList.get(pos);
        database.memberDao().setIsResigned(m.getID(), false);

        memberList.remove(pos);
        resignCnt.setText(getString(R.string.member_numbers_resign, memberList.size()));

        adapter.notifyItemRemoved(pos);
        adapter.notifyItemRangeChanged(pos, memberList.size());
    }

    private void deleteMember(int pos) {
        GuildMember m = memberList.get(pos);

        database.memberDao().delete(m);

        memberList.remove(pos);
        resignCnt.setText(getString(R.string.member_numbers_resign, memberList.size()));

        adapter.notifyItemRemoved(pos);
        adapter.notifyItemRangeChanged(pos, memberList.size());
    }

    private void showToast(){
        Toast.makeText(getActivity(), "인원이 가득찼습니다!", Toast.LENGTH_LONG).show();
    }
}
