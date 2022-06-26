package org.techtown.gtguildraid.statistics;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
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
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.models.entities.GuildMember;
import org.techtown.gtguildraid.repository.AppExecutor;
import org.techtown.gtguildraid.repository.RoomDB;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticMemberFragment extends Fragment {
    private static int raidId;
    private static boolean isDetailMode;
    private static final List<GuildMember> membersInRaid = new ArrayList<>();
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

        csvButton.setOnClickListener(view -> {
            if(memberNameList.size() == 0) {
                Toast.makeText(getContext(), "데이터 없음", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences pref = requireContext().getSharedPreferences("pref", Activity.MODE_PRIVATE);
            boolean isDay1Contained = pref.getBoolean("excelRankDay1Contained", false);
            double lhValue= 1f + 0.1 * pref.getInt("lastHitValue", 0);
            MemberPoi mp = new MemberPoi(database.raidDao().getRaidWithBosses(raidId), membersInRaid.get(sMemberIdx), database,
                    isDay1Contained, lhValue, getContext());
            ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","엑셀 파일 생성 중...", true);

            AppExecutor.getInstance().diskIO().execute(() -> {
                //exportDataToCSV();
                mp.exportDataToExcel();
                requireActivity().runOnUiThread(() -> {
                    mProgressDialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    String raidName = database.raidDao().getRaid(raidId).getName();
                    String dirName;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        dirName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                + "/가테_길레_" + raidName;
                    else
                        dirName = Environment.getExternalStorageDirectory() + "/가테_길레_" +raidName;

                    builder.setMessage("엑셀 파일을 보시겠습니까?" +
                            "\n저장 경로는 상단 i 버튼으로 확인이 가능합니다.")
                            .setCancelable(false)
                            .setPositiveButton("네", (dialog1, id) -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                try {
                                    File file = new File(dirName, raidName + "_개인_" + membersInRaid.get(sMemberIdx).getName() + ".xls");
                                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.ms-excel");
                                    //intent.setDataAndType(Uri.fromFile(file), DocumentsContract.Document.MIME_TYPE_DIR);
                                    startActivity(intent);
                                }catch(Exception e){
                                    Toast.makeText(getContext(), "엑셀이 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                                    Uri uri = Uri.parse(dirName);
                                    intent.setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR);
                                    startActivity(intent);
                                }
                                dialog1.dismiss();
                            })
                            .setNegativeButton("아니오", (dialog1, id) -> dialog1.dismiss());
                    AlertDialog alert = builder.create();
                    alert.setTitle("엑셀 생성 완료");
                    alert.show();
                    //Toast.makeText(getContext(), "생성 완료", Toast.LENGTH_SHORT).show();
                });
            });
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
