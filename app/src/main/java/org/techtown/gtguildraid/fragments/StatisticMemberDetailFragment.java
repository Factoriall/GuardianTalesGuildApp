package org.techtown.gtguildraid.fragments;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import org.angmarch.views.NiceSpinner;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.StatisticMemberLeaderAdapter;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.GuildMember;
import org.techtown.gtguildraid.models.LeaderInfo;
import org.techtown.gtguildraid.models.Record;
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

public class StatisticMemberDetailFragment extends Fragment {
    static int raidId;
    static int memberId;

    private static final int ALL = 0;
    private static final int BOSS_1 = 1;
    private static final int BOSS_2 = 2;
    private static final int BOSS_3 = 3;
    private static final int BOSS_4 = 4;

    private static int bossPosition;
    private static boolean isAdjustMode;
    private static ArrayList<String> bossLabels = new ArrayList<>();
    GuildMember member;

    RoomDB database;
    View view;
    TextView damage;
    TextView contribution;
    TextView hitNum;
    TextView average;
    LinearLayout leaderCard;
    RecyclerView recyclerView;
    StatisticMemberLeaderAdapter adapter;
    HoriBarChart leaderNumChart;
    HoriBarChart leaderDamageChart;

    private class HoriBarChart {
        private HorizontalBarChart chart;
        private final int MAX_NUM = 5;
        private List<Record> records;

        public HoriBarChart(HorizontalBarChart chart) {
            this.chart = chart;
        }

        public void setRecords(List<Record> records) {
            this.records = records;
        }

        class HeroWithValue implements Comparable<HoriBarChart.HeroWithValue>{
            String heroName;
            long value;

            public HeroWithValue(String key, long value) {
                heroName = key;
                this.value = value;
            }

            @Override
            public int compareTo(HoriBarChart.HeroWithValue heroWithCount) {
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
            chart.setExtraLeftOffset(20f);

            if(isDamage)
                chart.setExtraRightOffset(80f);
            else
                chart.setExtraRightOffset(30f);

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
                    if(isAdjustMode)
                        damage *= r.getBoss().getHardness();

                    if (!total.containsKey(name))
                        total.put(name, damage);
                    else
                        total.put(name, total.get(name) + damage);
                }
            }

            List<HoriBarChart.HeroWithValue> hwc = new ArrayList<>();
            for ( Map.Entry<String, Integer> entry : count.entrySet()) {
                String key = entry.getKey();
                long value = entry.getValue();
                if(isDamage)
                    value = value == 0 ? 0 : (total.get(key) / value);
                hwc.add(new HoriBarChart.HeroWithValue(key, value));
            }
            Collections.sort(hwc);

            int axisNum = Math.min(hwc.size(), MAX_NUM);
            for(int i=1; i<=axisNum; i++) {
                HoriBarChart.HeroWithValue hero = hwc.get(axisNum - i);
                Drawable dr = getResources().getDrawable(getResources().getIdentifier(
                        "character_" + hero.heroName,
                        "drawable",
                        getActivity().getPackageName()));
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

                set1.setColors(getColor(getContext(), android.R.color.holo_orange_dark),
                        getColor(getContext(), android.R.color.holo_blue_dark),
                        getColor(getContext(), android.R.color.holo_green_dark),
                        getColor(getContext(), android.R.color.holo_red_dark),
                        getColor(getContext(), android.R.color.holo_purple));

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
    StatisticMemberDetailFragment(int raidId, int memberId){
        this.raidId = raidId;
        this.memberId = memberId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistic_member_detail, container, false);
        damage = view.findViewById(R.id.damage);
        contribution = view.findViewById(R.id.contribution);
        hitNum = view.findViewById(R.id.hitNum);
        average = view.findViewById(R.id.average);
        leaderCard = view.findViewById(R.id.leaderCard);
        recyclerView = view.findViewById(R.id.recyclerView);
        leaderNumChart = new HoriBarChart(view.findViewById(R.id.leaderNumChart));
        leaderDamageChart = new HoriBarChart(view.findViewById(R.id.leaderDamageChart));


        Switch adjustSwitch = view.findViewById(R.id.adjustSwitch);
        isAdjustMode = adjustSwitch.isChecked();
        bossPosition = 0;
        database = RoomDB.getInstance(getActivity());

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new StatisticMemberLeaderAdapter();
        recyclerView.setAdapter(adapter);

        member = database.memberDao().getMember(memberId);

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

        adjustSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            isAdjustMode = b;
            setView();
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

        AppExecutor.getInstance().diskIO().execute(() -> {
            List<Record> allRecords = database.recordDao().getAllRecordsWithExtra(raidId);
            List<Record> memberRecords = database.recordDao()
                    .get1MemberRecordsWithExtra(memberId, raidId);
            getActivity().runOnUiThread(() -> {
                mProgressDialog.dismiss();
                leaderCard.setVisibility(View.GONE);
                setData(allRecords, memberRecords);
            });
        });
    }

    private void setBossData(Boss boss) {
        int bossId = boss.getBossId();
        ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","데이터베이스 접속 중", true);
        AppExecutor.getInstance().diskIO().execute(() -> {
            List<Record> allRecords = database.recordDao().get1BossRecordsWithExtra(raidId, bossId);
            List<Record> memberRecords = database.recordDao()
                    .get1MemberRecordsWithExtra(memberId, raidId, bossId);

            getActivity().runOnUiThread(() -> {
                mProgressDialog.dismiss();
                leaderCard.setVisibility(View.VISIBLE);
                setLeaderCard(memberRecords);
                setData(allRecords, memberRecords);
            });
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

        leaderNumChart.setRecords(memberRecords);
        leaderNumChart.setChartUi(false);

        leaderDamageChart.setRecords(memberRecords);
        leaderDamageChart.setChartUi(true);
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

        adapter.setItems(memberLeaderList, isAdjustMode);
        adapter.notifyDataSetChanged();
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