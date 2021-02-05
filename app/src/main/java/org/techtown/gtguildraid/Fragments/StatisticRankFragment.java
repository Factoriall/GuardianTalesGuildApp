package org.techtown.gtguildraid.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import org.techtown.gtguildraid.Adapters.StatisticRankCardAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.Models.RankInfo;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.AppExecutor;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticRankFragment extends Fragment {
    static int raidId;
    static View view;
    static int bossPosition;
    static int levelPosition;
    static boolean isAverageMode;
    static boolean isAdjustMode;
    static ArrayList<String> bossLabels = new ArrayList<>();
    static ArrayList<String> levelLabels = new ArrayList<>();
    RoomDB database;

    public static Fragment newInstance(int raidId) {
        StatisticRankFragment fragment = new StatisticRankFragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistic_rank, container, false);

        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }

        database = RoomDB.getInstance(getActivity());

        ToggleSwitch bossSwitch = view.findViewById(R.id.toggleSwitchBoss);

        ImageButton arrow = view.findViewById(R.id.arrow);
        ConstraintLayout cl = view.findViewById(R.id.conditionLayout);
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cl.getVisibility() == View.VISIBLE){
                    cl.setVisibility(View.GONE);
                    arrow.setImageResource(R.drawable.icon_arrow_down);
                }
                else{
                    cl.setVisibility(View.VISIBLE);
                    arrow.setImageResource(R.drawable.icon_arrow_up);
                }
            }
        });


        List<Boss> bossesInRaid = database.raidDao().getBossesList(raidId);

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
        bossPosition = 0;
        bossSwitch.setOnChangeListener(new ToggleSwitch.OnChangeListener() {
            @Override
            public void onToggleSwitchChanged(int i) {
                bossPosition = i;
                setRankView();
            }
        });

        ToggleSwitch levelSwitch = view.findViewById(R.id.toggleSwitchLevel);

        if(levelLabels.isEmpty()) {
            levelLabels.add("Lv.50↑");
            levelLabels.add("Lv.66↑");
            levelLabels.add("Lv.71↑");
            levelLabels.add("Lv.76↑");
            levelLabels.add("Lv.80");
        }
        levelSwitch.setEntries(levelLabels);
        levelSwitch.setCheckedPosition(0);
        levelPosition = 0;
        levelSwitch.setOnChangeListener(new ToggleSwitch.OnChangeListener() {
            @Override
            public void onToggleSwitchChanged(int i) {
                levelPosition = i;
                setRankView();
            }
        });

        ToggleSwitch avgSwitch = view.findViewById(R.id.toggleSwitchAvg);
        avgSwitch.setCheckedPosition(0);
        isAverageMode = false;
        avgSwitch.setOnChangeListener(new ToggleSwitch.OnChangeListener() {
            @Override
            public void onToggleSwitchChanged(int i) {
                isAverageMode = i == 1;
                setRankView();
            }
        });

        Switch adjustSwitch = view.findViewById(R.id.adjustSwitch);
        isAdjustMode = adjustSwitch.isChecked();
        adjustSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isAdjustMode = b;
                setRankView();
            }
        });

        setRankView();

        return view;
    }

    private void setRankView() {
        int[] rounds = {1, 7, 12, 17, 22};

        List<RankInfo> rankInfos = new ArrayList<>();
        ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","데이터베이스 접속 중", true);
        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                for(GuildMember m : database.memberDao().getAllMembers()) {
                    if(database.recordDao().getCertainMemberRecords(m.getID(), raidId).size() == 0)
                        continue;

                    List<Record> recordList;
                    if(bossPosition != 0) {
                        recordList = database.recordDao().getMemberRoundRecordsWithOneBossAndLeader(
                                m.getID(),
                                raidId,
                                database.raidDao().getBossesList(raidId).get(bossPosition-1).getBossId(),
                                rounds[levelPosition]);
                    }
                    else{
                        recordList = database.recordDao().getCertainMemberRoundRecordsWithOneBossAndLeader(
                                m.getID(),
                                raidId,
                                rounds[levelPosition]);
                    }

                    RankInfo ri = new RankInfo(m.getName(), recordList.size());

                    for(Record r : recordList) {
                        if(isAdjustMode) {
                            ri.addDamage((long) (r.getDamage() * r.getBoss().getHardness()));
                        }
                        else
                            ri.addDamage(r.getDamage());
                    }

                    ri.setFinalDamage(isAverageMode);
                    rankInfos.add(ri);
                }
                Collections.sort(rankInfos);

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mProgressDialog.dismiss();
                        TextView conditionText = view.findViewById(R.id.conditionText);
                        conditionText.setText(bossLabels.get(bossPosition)+" / "+levelLabels.get(levelPosition)+
                                " / "+ (isAverageMode ? "평균" : "총합") + " / 배율 " + (isAdjustMode ? "ON" : "OFF"));
                        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

                        StatisticRankCardAdapter adapter = new StatisticRankCardAdapter();
                        adapter.setItems(rankInfos);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });
    }
}
