package org.techtown.gtguildraid.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import org.techtown.gtguildraid.adapters.StatisticBossLeaderAdapter;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.LeaderInfo;
import org.techtown.gtguildraid.models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.utils.AppExecutor;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StatisticBossFragment extends Fragment {
    static final int BOSS_1 = 0;
    static final int BOSS_2 = 1;
    static final int BOSS_3 = 2;
    static final int BOSS_4 = 3;

    RoomDB database;
    private static int raidId;
    private static int bossPosition;
    private static ArrayList<String> bossLabels = new ArrayList<>();

    View view;
    StatisticBossLeaderAdapter adapter;
    TextView averageDamage;
    TextView stDev;
    TextView hitNum;
    RecyclerView recyclerView;

    public static StatisticBossFragment newInstance(int raidId) {
        StatisticBossFragment fragment = new StatisticBossFragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistic_boss, container, false);

        database = RoomDB.getInstance(getActivity());
        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }

        //보스 토글 스위치
        ToggleSwitch bossSwitch = view.findViewById(R.id.toggleSwitchBoss);
        List<Boss> bossesInRaid = database.raidDao().getBossesList(raidId);

        bossLabels.clear();
        for (Boss b : bossesInRaid) {
            String bossName = b.getName();
            if (bossName.length() > 5)
                bossName = bossName.substring(0, 5) + "..";
            bossLabels.add(bossName);
        }

        bossSwitch.setEntries(bossLabels);
        bossSwitch.setCheckedPosition(0);

        bossSwitch.setOnChangeListener(i -> {
            bossPosition = i;
            setView();
        });
        setView();

        return view;
    }

    private void setView() {
        averageDamage = view.findViewById(R.id.averageDamage);
        stDev = view.findViewById(R.id.CV);
        hitNum = view.findViewById(R.id.hitNum);
        recyclerView = view.findViewById(R.id.recyclerView);
        ImageView help = view.findViewById(R.id.help);
        help.setOnClickListener(view -> {
            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.dialog_cvhelp);
            int width = WindowManager.LayoutParams.MATCH_PARENT;
            int height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
            dialog.show();

            Button button = dialog.findViewById(R.id.button);
            button.setOnClickListener(view1 -> dialog.dismiss());
        });

        List<Boss> bosses = database.raidDao().getBossesList(raidId);
        switch(bossPosition){
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

    private void setBossData(Boss boss) {
        int bossId = boss.getBossId();

        ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","데이터베이스 접속 중", true);
        AppExecutor.getInstance().diskIO().execute(() -> {
            List<Record> records = database.recordDao().getAllRecordsWithExtraOrdered(raidId, bossId);
            getActivity().runOnUiThread(() -> {
                int xAxisNum;
                if (records.size() != 0)
                    xAxisNum = records.get(records.size() - 1).getRound();
                else
                    xAxisNum = 1;

                long damage = getDamageFromList(records);
                long average = 0;
                if (records.size() != 0)
                    average = damage / records.size();
                hitNum.setText(Integer.toString(records.size()));
                averageDamage.setText(NumberFormat.getNumberInstance(Locale.US)
                        .format(average));
                stDev.setText(getCV(average, records));

                setLeaderCard(records, xAxisNum);
                mProgressDialog.dismiss();
            });
        });
    }

    private void setLeaderCard(List<Record> records, int xAxisNum) {
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
        adapter = new StatisticBossLeaderAdapter(memberLeaderList, xAxisNum);

        recyclerView.setAdapter(adapter);
    }

    private String getCV(long average, List<Record> records) {
        long devSquared = 0;
        for(Record r : records){
            devSquared += ((r.getDamage() - average) * (r.getDamage() - average));
        }
        double stDev = Math.sqrt(devSquared / (double) records.size());

        return String.format("%.2f", stDev / average * 100.0f);
    }

    private long getDamageFromList(List<Record> records) {
        long damage = 0;
        for(Record r: records)
            damage += r.getDamage();

        return damage;
    }
}
