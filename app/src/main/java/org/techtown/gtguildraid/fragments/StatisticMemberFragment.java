package org.techtown.gtguildraid.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kyleduo.switchbutton.SwitchButton;

import org.angmarch.views.NiceSpinner;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.etc.MemberPoi;
import org.techtown.gtguildraid.interfaces.CalculateFormatHelper;
import org.techtown.gtguildraid.models.GuildMember;
import org.techtown.gtguildraid.utils.AppExecutor;
import org.techtown.gtguildraid.utils.RoomDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticMemberFragment extends Fragment {
    private static int raidId;
    private static boolean isDetailMode;
    private static List<GuildMember> membersInRaid = new ArrayList<>();
    private static int sMemberIdx = 0;

    RoomDB database;
    View view;
    NiceSpinner memberSpinner;

    public static StatisticMemberFragment newInstance(int raidId) {
        StatisticMemberFragment fragment = new StatisticMemberFragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistic_member, container, false);
        //System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        //System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        //System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        SwitchButton viewSwitch = view.findViewById(R.id.viewSwitch);
        Button csvButton = view.findViewById(R.id.csvButton);
        isDetailMode = viewSwitch.isChecked();
        sMemberIdx = 0;

        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }
        database = RoomDB.getInstance(getActivity());

        memberSpinner = view.findViewById(R.id.memberName);

        List<GuildMember> allMembers = database.memberDao().getAllMembers();

        membersInRaid.clear();
        for (GuildMember m : allMembers) {
            if (database.recordDao().get1MemberRecords(m.getID(), raidId).size() != 0)
                membersInRaid.add(m);
        }

        Collections.sort(membersInRaid, (guildMember, t1) -> guildMember.getName().compareTo(t1.getName()));

        List<String> memberNameList = new ArrayList<>();
        for (GuildMember m : membersInRaid) {
            memberNameList.add(m.getName());
        }
        memberSpinner.attachDataSource(memberNameList);
        if (memberNameList.size() == 1)
            memberSpinner.setText(memberNameList.get(0));

        memberSpinner.setOnSpinnerItemSelectedListener((parent, view1, position, id) -> {
            sMemberIdx = position;
            setView();
        });

        viewSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if(memberNameList.size() == 0) {
                Toast.makeText(getContext(), "데이터 없음", Toast.LENGTH_SHORT).show();
                return;
            }
            isDetailMode = b;
            setView();
        });

        if(memberNameList.size() != 0)
            setView();

        csvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(memberNameList.size() == 0) {
                    Toast.makeText(getContext(), "데이터 없음", Toast.LENGTH_SHORT).show();
                    return;
                }
                MemberPoi mp = new MemberPoi(database.raidDao().getRaidWithBosses(raidId), membersInRaid.get(sMemberIdx), database);
                ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","엑셀 파일 생성 중...", true);

                AppExecutor.getInstance().diskIO().execute(() -> {
                    //exportDataToCSV();
                    mp.exportDataToExcel();
                    getActivity().runOnUiThread(() -> {
                        mProgressDialog.dismiss();
                        Toast.makeText(getContext(), "생성 완료", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });

        return view;
    }

    private void setView() {
        int memberId = membersInRaid.get(sMemberIdx).getID();
        StatisticMemberBasic1Fragment basicFragment = new StatisticMemberBasic1Fragment(raidId, memberId);
        StatisticMemberDetailFragment detailFragment = new StatisticMemberDetailFragment(raidId, memberId);
        if(isDetailMode){
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.container, detailFragment).commit();
        }
        else{
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.container, basicFragment).commit();
        }
    }
}
