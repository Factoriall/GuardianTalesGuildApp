package org.techtown.gtguildraid.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.StatisticBossLeaderAdapter;
import org.techtown.gtguildraid.models.daos.Boss;
import org.techtown.gtguildraid.models.LeaderInfo;
import org.techtown.gtguildraid.models.daos.Record;
import org.techtown.gtguildraid.utils.AppExecutor;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static androidx.core.content.ContextCompat.getColor;

public class

StatisticBossFragment extends Fragment {
    static final int ALL = 0;
    static final int BOSS_1 = 1;
    static final int BOSS_2 = 2;
    static final int BOSS_3 = 3;
    static final int BOSS_4 = 4;

    RoomDB database;
    private static int raidId;
    private static int bossPosition;
    private static final ArrayList<String> bossLabels = new ArrayList<>();

    View view;
    StatisticBossLeaderAdapter adapter;
    TextView averageDamage;
    TextView hitNum;
    HoriBarChart leaderNumChart;
    HoriBarChart leaderDamageChart;
    RecyclerView recyclerView;

    private class HoriBarChart {
        private final HorizontalBarChart chart;
        private List<Record> records;

        public HoriBarChart(HorizontalBarChart chart) {
            this.chart = chart;
        }

        public void setRecords(List<Record> records) {
            this.records = records;
        }

        class HeroWithValue implements Comparable<HeroWithValue>{
            String heroName;
            long value;

            public HeroWithValue(String key, long value) {
                heroName = key;
                this.value = value;
            }

            @Override
            public int compareTo(HeroWithValue heroWithCount) {
                if(heroWithCount.value > value)
                    return 1;
                else if (heroWithCount.value < value)
                    return -1;
                return 0;
            }
        }

        public void setChartUi(boolean isDamage) { //CombinedChart의 ui 설정
            chart.getDescription().setEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setDrawBarShadow(false);
            chart.setHighlightFullBarEnabled(false);
            chart.setExtraOffsets(0, 0, 0, 10);
            chart.animateY(2000);
            chart.setPinchZoom(false);
            chart.setScaleEnabled(false);
            chart.setTouchEnabled(false);
            chart.setExtraLeftOffset(30f);
            if(isDamage)
                chart.setExtraRightOffset(80f);

            Legend l = chart.getLegend();
            l.setEnabled(false);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setEnabled(false);

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setGridColor(getResources().getColor(R.color.bar_chart_color));
            leftAxis.setTextColor(getResources().getColor(R.color.bar_chart_color));
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
            leftAxis.setGranularity(1f);
            leftAxis.setValueFormatter(new LargeValueFormatter());

            XAxis xAxis = chart.getXAxis();
            xAxis.setEnabled(false);


            setChartData(isDamage);
        }

        private void setChartData(boolean isDamage) {
            ArrayList<BarEntry> entries = new ArrayList<>();

            HashMap<String, Integer> count = new HashMap<>();
            HashMap<String, Long> total = new HashMap<>();
            for(Record r : records){
                String name = r.getLeader().getEnglishName();
                if (!count.containsKey(name))
                    count.put(name, 1);
                else
                    count.put(name, count.get(name) + 1);

                if(isDamage){
                    long damage = r.getDamage();

                    if (!total.containsKey(name))
                        total.put(name, damage);
                    else
                        total.put(name, total.get(name) + damage);
                }
            }

            List<HeroWithValue> hwc = new ArrayList<>();
            for ( Map.Entry<String, Integer> entry : count.entrySet()) {
                final double MIN_PER = 0.03;
                final int MIN_COUNT = (int)(records.size() * MIN_PER);//min 카운트를 record 개수의 5%로 설정
                String key = entry.getKey();
                long value = entry.getValue();
                if(isDamage) {//평균값 삽입
                    if(value < MIN_COUNT)//최소값보다 작으면 카운트 안함
                        continue;
                    value = value == 0 ? 0 : (total.get(key) / value);
                }
                hwc.add(new HeroWithValue(key, value));
            }
            Collections.sort(hwc);

            int MAX_NUM = 5;
            int axisNum = Math.min(hwc.size(), MAX_NUM);
            for(int i=1; i<=axisNum; i++) {
                HeroWithValue hero = hwc.get(axisNum - i);
                Drawable dr = getResources().getDrawable(getResources().getIdentifier(
                        "character_" + hero.heroName,
                        "drawable",
                        requireActivity().getPackageName()));
                Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                int adjustSize = Math.min(90, 360 / axisNum);
                Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, adjustSize, adjustSize, true));
                entries.add(new BarEntry(i, hero.value, d));
            }

            BarDataSet set1;
            if (chart.getData() != null &&
                    chart.getData().getDataSetCount() > 0) {
                set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
                set1.setValues(entries);
                chart.getData().notifyDataChanged();
                chart.notifyDataSetChanged();
            } else {
                set1 = new BarDataSet(entries, "");
                set1.setDrawIcons(true);
                set1.setIconsOffset(new MPPointF(-25, 0));

                Context ctx = requireContext();
                set1.setColors(getColor(ctx, android.R.color.holo_orange_dark),
                        getColor(ctx, android.R.color.holo_blue_dark),
                        getColor(ctx, android.R.color.holo_green_dark),
                        getColor(ctx, android.R.color.holo_red_dark),
                        getColor(ctx, android.R.color.holo_purple));

                ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1);

                BarData data = new BarData(dataSets);
                data.setValueTextSize(14f);
                data.setBarWidth(0.7f);
                if(!isDamage)
                    data.setValueFormatter(new LargeValueFormatter());
                chart.setData(data);
            }
        }
    }

    public static StatisticBossFragment newInstance(int raidId) {
        StatisticBossFragment fragment = new StatisticBossFragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistic_boss, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new StatisticBossLeaderAdapter();
        recyclerView.setAdapter(adapter);

        database = RoomDB.getInstance(getActivity());
        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }

        //보스 토글 스위치
        ToggleSwitch bossSwitch = view.findViewById(R.id.toggleSwitchBoss);
        List<Boss> bossesInRaid = database.raidDao().getBossesList(raidId);

        bossLabels.clear();
        bossLabels.add("보스 전체");
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
        hitNum = view.findViewById(R.id.hitNum);
        leaderNumChart = new HoriBarChart(view.findViewById(R.id.leaderNumChart));
        leaderDamageChart = new HoriBarChart(view.findViewById(R.id.leaderDamageChart));
        
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
        ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","보스 - 전체 데이터 저장", true);
        AppExecutor.getInstance().diskIO().execute(() -> {
            List<Record> records = database.recordDao().getAllRecordsWithExtra(raidId);
            requireActivity().runOnUiThread(() -> {
                mProgressDialog.dismiss();
                setData(records);
            });
        });
    }

    private void setBossData(Boss boss) {
        int bossId = boss.getBossId();
        AppExecutor.getInstance().diskIO().execute(() -> {
            List<Record> records = database.recordDao().get1BossRecordsWithExtra(raidId, bossId);
            requireActivity().runOnUiThread(() -> setData(records));
        });
    }

    private void setData(List<Record> records) {
        long damage = getDamageFromList(records);
        long average = 0;
        if (records.size() != 0)
            average = damage / records.size();
        hitNum.setText(Integer.toString(records.size()));
        averageDamage.setText(NumberFormat.getNumberInstance(Locale.US).format(average));

        leaderNumChart.setRecords(records);
        leaderNumChart.setChartUi(false);

        leaderDamageChart.setRecords(records);
        leaderDamageChart.setChartUi(true);
        setLeaderCard(records);
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

        adapter.setItems(memberLeaderList);
        adapter.notifyDataSetChanged();
    }

    private long getDamageFromList(List<Record> records) {
        long damage = 0;
        for(Record r: records)
            damage += r.getDamage();

        return damage;
    }
}
