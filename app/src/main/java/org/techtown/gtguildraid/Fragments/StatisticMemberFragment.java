package org.techtown.gtguildraid.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;
import org.techtown.gtguildraid.Adapters.StatisticMemberLeaderAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.Models.LeaderInfo;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.AppExecutor;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class StatisticMemberFragment extends Fragment {
    private static final int ALL = 0;
    private static final int BOSS_1 = 1;
    private static final int BOSS_2 = 2;
    private static final int BOSS_3 = 3;
    private static final int BOSS_4 = 4;

    private static int raidId;
    private static int bossPosition;
    private static boolean isAdjustMode;
    private static ArrayList<String> bossLabels = new ArrayList<>();
    private static List<GuildMember> membersInRaid = new ArrayList<>();

    RoomDB database;
    TextView damage;
    TextView contribution;
    TextView hitNum;
    TextView average;
    NiceSpinner memberSpinner;
    LinearLayout leaderCard;
    RecyclerView recyclerView;
    StatisticMemberLeaderAdapter adapter;

    private static int sMemberIdx = 0;


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
        View view = inflater.inflate(R.layout.fragment_statistic_member, container, false);
        damage = view.findViewById(R.id.damage);
        contribution = view.findViewById(R.id.contribution);
        hitNum = view.findViewById(R.id.hitNum);
        average = view.findViewById(R.id.average);
        leaderCard = view.findViewById(R.id.leaderCard);
        recyclerView = view.findViewById(R.id.recyclerView);

        Switch adjustSwitch = view.findViewById(R.id.adjustSwitch);
        isAdjustMode = adjustSwitch.isChecked();
        bossPosition = 0;
        sMemberIdx = 0;

        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }
        database = RoomDB.getInstance(getActivity());

        //member의 스피너 생성

        memberSpinner = view.findViewById(R.id.memberName);

        List<GuildMember> allMembers = database.memberDao().getAllMembers();
        List<Boss> bossesInRaid = database.raidDao().getBossesList(raidId);

        for (GuildMember m : allMembers) {
            if (database.recordDao().get1MemberRecords(m.getID(), raidId).size() != 0)
                membersInRaid.add(m);
        }

        Collections.sort(membersInRaid, new Comparator<GuildMember>() {
            @Override
            public int compare(GuildMember guildMember, GuildMember t1) {
                return guildMember.getName().compareTo(t1.getName());
            }
        });

        List<String> memberNameList = new ArrayList<>();
        for (GuildMember m : membersInRaid) {
            memberNameList.add(m.getName());
        }
        memberSpinner.attachDataSource(memberNameList);
        if (memberNameList.size() == 1)
            memberSpinner.setText(memberNameList.get(0));

        memberSpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position,
                                       long id) {
                sMemberIdx = position;
                setView();
            }
        });

        //보스 토글 스위치
        ToggleSwitch bossSwitch = view.findViewById(R.id.toggleSwitchBoss);

        if(bossLabels.isEmpty()) {
            bossLabels.add("보스 전체");
            for (Boss b : bossesInRaid) {
                String bossName = b.getName();
                if (bossName.length() > 5)
                    bossName = bossName.substring(0, 5) + "..";
                bossLabels.add(bossName);
            }
        }
        bossSwitch.setEntries(bossLabels);
        bossSwitch.setCheckedPosition(0);

        bossSwitch.setOnChangeListener(new ToggleSwitch.OnChangeListener() {
            @Override
            public void onToggleSwitchChanged(int i) {
                bossPosition = i;
                setView();
            }
        });

        adjustSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isAdjustMode = b;
                setView();
            }
        });
        setView();

        return view;
    }

    private void setView() {
        List<Boss> bosses = database.raidDao().getBossesList(raidId);
        switch(bossPosition){
            case ALL:
                setAllData();
                break;
            case BOSS_1:
                setBossData(bosses.get(0));
                break;
            case BOSS_2:
                setBossData(bosses.get(1));
                break;
            case BOSS_3:
                setBossData(bosses.get(2));
                break;
            case BOSS_4:
                setBossData(bosses.get(3));
                break;
        }
    }

    private void setAllData() {
        ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","데이터베이스 접속 중", true);

        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Record> allRecords = database.recordDao().getAllRecordsWithExtra(raidId);
                List<Record> memberRecords = database.recordDao()
                        .get1MemberRecordsWithExtra(membersInRaid.get(sMemberIdx).getID(), raidId);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mProgressDialog.dismiss();
                        leaderCard.setVisibility(View.GONE);
                        setData(allRecords, memberRecords);
                    }
                });
            }
        });
    }

    private void setBossData(Boss boss) {
        int bossId = boss.getBossId();
        ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","데이터베이스 접속 중", true);
        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Record> allRecords = database.recordDao().getAllRecordsWithExtra(raidId, bossId);
                List<Record> memberRecords = database.recordDao()
                        .get1MemberRecordsWithExtra(membersInRaid.get(sMemberIdx).getID(), raidId, bossId);

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mProgressDialog.dismiss();
                        leaderCard.setVisibility(View.VISIBLE);
                        setLeaderCard(memberRecords);
                        setData(allRecords, memberRecords);
                    }
                });
            }
        });
    }

    private void setData(List<Record> allRecords, List<Record> memberRecords) {
        long allDamage = getDamageFromList(allRecords, isAdjustMode);
        long memberDamage = getDamageFromList(memberRecords, isAdjustMode);

        damage.setText(NumberFormat.getNumberInstance(Locale.US).format(memberDamage));
        contribution.setText(getPercentage(memberDamage, allDamage));
        hitNum.setText(Integer.toString(memberRecords.size()));
        average.setText(memberRecords.size() == 0 ? Integer.toString(0) :
                NumberFormat.getNumberInstance(Locale.US).format(memberDamage / memberRecords.size()));
    }

    private void setLeaderCard(List<Record> records) {
        List<LeaderInfo> memberLeaderList = new ArrayList<>();
        for(Record r : records){
            boolean isMatched = false;
            for(LeaderInfo info : memberLeaderList){
                if(info.isMatched(r.getLeader())) {
                    info.addList(r);
                    isMatched = true;
                    break;
                }
            }
            if(!isMatched)
                memberLeaderList.add(new LeaderInfo(r.getLeader(), r));
        }
        Collections.sort(memberLeaderList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new StatisticMemberLeaderAdapter(memberLeaderList, isAdjustMode);

        recyclerView.setAdapter(adapter);
    }

    private String getPercentage(long memberDamage, long allDamage) {
        if(allDamage == 0)
            return "0.00";
        return String.format("%.2f", memberDamage/(double)allDamage * 100);
    }

    private long getDamageFromList(List<Record> records, boolean isAdjustMode) {
        long damage = 0;
        for(Record r: records){
            if(isAdjustMode) {
                Boss b = r.getBoss();
                damage += (long) (r.getDamage() * b.getHardness());
            }
            else
                damage += r.getDamage();
        }
        return damage;
    }
}
