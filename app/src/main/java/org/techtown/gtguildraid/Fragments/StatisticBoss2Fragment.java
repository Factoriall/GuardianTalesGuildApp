package org.techtown.gtguildraid.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.techtown.gtguildraid.Adapters.StatisticBossLeaderAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.LeaderInfo;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticBoss2Fragment extends Fragment {
    final int BOSS_1 = 0;
    final int BOSS_2 = 1;
    final int BOSS_3 = 2;
    final int BOSS_4 = 3;

    RoomDB database;
    int raidId;
    int position;

    StatisticBossLeaderAdapter adapter;
    TextView averageDamage;
    TextView stDev;
    TextView hitNum;
    BarChartClass overallChart;
    RecyclerView recyclerView;

    private class BarChartClass{
        private BarChart chart;
        private int xAxisNum;
        private List<Record> records;

        public BarChartClass(BarChart chart) {
            this.chart = chart;
        }

        public void setxAxisNum(int xAxisNum) {
            this.xAxisNum = xAxisNum;
        }

        public void setRecords(List<Record> records){
            this.records = records;
        }

        public void setBarChartUi(){ //BarChart의 ui 설정
            chart.getDescription().setEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setDrawBarShadow(false);
            chart.setHighlightFullBarEnabled(false);
            chart.setExtraOffsets(0, 0, 0, 10);
            chart.animateXY(2000, 2000);

            Legend l = chart.getLegend();
            l.setWordWrapEnabled(true);
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setTextColor(Color.WHITE);

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setGridColor(getResources().getColor(R.color.bar_chart_color));
            leftAxis.setTextColor(getResources().getColor(R.color.bar_chart_color));
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setEnabled(false);

            XAxis xAxis = chart.getXAxis();
            xAxis.setGridColor(Color.WHITE);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setPosition(XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(0f);
            xAxis.setGranularity(1f);

            final ArrayList<String> xAxisLabel = new ArrayList<>();
            xAxisLabel.add("");
            for(int i=1; i<=xAxisNum; i++)
                xAxisLabel.add(i + "회차");
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return xAxisLabel.get((int) value);
                }
            });

            BarData data = generateBarData();

            xAxis.setAxisMaximum(data.getXMax() + 0.8f);

            chart.setData(data);
            chart.invalidate();
        }


        private BarData generateBarData(){ // 평균 딜량 그래프
            ArrayList<BarEntry> entries = new ArrayList<>();

            for(int i=0; i<xAxisNum; i++) {
                entries.add(new BarEntry(i + 1f, 0));
            }

            int[] damageArray = new int[xAxisNum];
            int[] numArray = new int[xAxisNum];
            Log.d("getround", "xaxisnum:" + xAxisNum);
            Log.d("getround", "recordNum:" + records.size());
            for(Record r : records) {
                Log.d("getround", "" + r.getRound());
                damageArray[r.getRound() - 1] += r.getDamage();
                numArray[r.getRound() - 1]++;
            }

            int idx = 0;
            for(BarEntry e : entries){
                if(numArray[idx] != 0)
                    e.setY(damageArray[idx] / numArray[idx]);
                else
                    e.setY(0);
                idx++;
            }

            BarDataSet set = new BarDataSet(entries, "평균 딜량");

            int barColor = getResources().getColor(R.color.bar_chart_color);
            set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "" + (int) value;
                }
            });
            set.setColor(barColor);
            set.setDrawValues(false);

            set.setAxisDependency(YAxis.AxisDependency.LEFT);

            BarData data = new BarData(set);
            data.setBarWidth(0.8f);
            return data;
        }
    }

    public static StatisticBoss2Fragment newInstance(int position, int raidId) {
        StatisticBoss2Fragment fragment = new StatisticBoss2Fragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("raidId", raidId);

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
            position = getArguments().getInt("position");
        }
        averageDamage = view.findViewById(R.id.averageDamage);
        stDev = view.findViewById(R.id.CV);
        hitNum = view.findViewById(R.id.hitNum);
        recyclerView = view.findViewById(R.id.recyclerView);
        overallChart = new BarChartClass(view.findViewById(R.id.chart));

        setView(position);
        Log.d("bossPosition", "pos: " + position);
        
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
        Log.d("bossName", boss.getName());

        List<Record> records = database.recordDao().getAllRecordsWith1BossLeaderOrdered(raidId, bossId);
        int xAxisNum;
        if(records.size() != 0)
            xAxisNum = records.get(records.size() - 1).getRound();
        else
            xAxisNum = 1;
        overallChart.setxAxisNum(xAxisNum);

        int damage = getDamageFromList(records);
        int average = 0;
        Log.d("damage", "" + damage);
        if(records.size() != 0)
            average = damage / records.size();
        Log.d("average", "" + average);
        hitNum.setText(Integer.toString(records.size()));
        averageDamage.setText(NumberFormat.getNumberInstance(Locale.US)
                .format(average));
        Log.d("setBoss", "rec num: " + records.size());
        stDev.setText(getCV(average, records));

        overallChart.setRecords(records);
        overallChart.setBarChartUi();
        setLeaderCard(records, xAxisNum);
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new StatisticBossLeaderAdapter(memberLeaderList, xAxisNum);

        recyclerView.setAdapter(adapter);
    }

    private String getCV(int average, List<Record> records) {
        long devSquared = 0;
        for(Record r : records){
            devSquared += ((long)(r.getDamage() - average) * (long)(r.getDamage() - average));
        }
        double stDev = Math.sqrt(devSquared / (double) records.size());

        return String.format("%.2f", stDev / average * 100.0f);
    }

    private int getDamageFromList(List<Record> records) {
        int damage = 0;
        for(Record r: records)
            damage += r.getDamage();

        return damage;
    }
}
