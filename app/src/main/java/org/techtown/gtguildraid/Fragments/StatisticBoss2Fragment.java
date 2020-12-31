package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.CombinedChart;

import org.techtown.gtguildraid.Adapters.StatisticBossLeaderAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StatisticBoss2Fragment extends Fragment {
    final int BOSS_1 = 0;
    final int BOSS_2 = 1;
    final int BOSS_3 = 2;
    final int BOSS_4 = 3;
    final int DAY_IN_SECONDS = 1000 * 3600 * 24;
    final int MAX_DURATION = 14;

    RoomDB database;
    int raidId;
    int position;
    boolean isAdjustMode;

    StatisticBossLeaderAdapter adapter;
    TextView averageDamage;
    TextView stDev;
    TextView hitNum;
    CombinedChart chart;
    RecyclerView recyclerView;

    public static StatisticBoss2Fragment newInstance(int position, int raidId, boolean isChecked) {
        StatisticBoss2Fragment fragment = new StatisticBoss2Fragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("raidId", raidId);
        args.putBoolean("isChecked", isChecked);

        fragment.setArguments(args);
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_boss_2, container, false);
        database = RoomDB.getInstance(getActivity());
        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
            isAdjustMode = getArguments().getBoolean("isChecked");
            position = getArguments().getInt("position");
        }
        averageDamage = view.findViewById(R.id.averageDamage);
        stDev = view.findViewById(R.id.stDev);
        hitNum = view.findViewById(R.id.hitNum);
        chart = view.findViewById(R.id.chart);
        recyclerView = view.findViewById(R.id.recyclerView);

        setView(position);
        
        return view;
    }

    private void setView(int position) {
        List<Boss> bosses = database.raidDao().getBossesList(raidId);
        switch(position){
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

        List<Record> records = database.recordDao().getAllRecordsWithOneBossAndLeader(raidId, bossId);

        int damage = getDamageFromList(records, isAdjustMode);
        int average = 0;
        if(records.size() != 0)
            average = damage / records.size();
        hitNum.setText(Integer.toString(records.size()));
        averageDamage.setText(NumberFormat.getNumberInstance(Locale.US)
                .format(average));
        stDev.setText(String.format("%.2f", getStandardDeviation(average, records, isAdjustMode)));

        //setChartData()
        //recyclerView

    }

    private double getStandardDeviation(int average, List<Record> records, boolean isAdjustMode) {
        int devSquared = 0;
        for(Record r : records){
            devSquared += (getAdjustDamage(r, isAdjustMode) - average)
                    * (getAdjustDamage(r, isAdjustMode) - average);
        }

        return Math.sqrt(devSquared / (float) records.size());
    }

    private int getDamageFromList(List<Record> records, boolean isAdjustMode) {
        int damage = 0;
        for(Record r: records)
            damage += getAdjustDamage(r, isAdjustMode);

        return damage;
    }

    private int getAdjustDamage(Record record, boolean isAdjustMode) {
        if(isAdjustMode)
            return (int) (record.getDamage() * record.getBoss().getHardness());

        return record.getDamage();
    }
}
