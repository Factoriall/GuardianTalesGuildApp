package org.techtown.gtguildraid.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.techtown.gtguildraid.Adapters.StatisticMemberLeaderAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.LeaderInfo;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticMember3Fragment extends Fragment {
    final int ALL = 0;
    final int BOSS_1 = 1;
    final int BOSS_2 = 2;
    final int BOSS_3 = 3;
    final int BOSS_4 = 4;
    final int DAY_IN_SECONDS = 1000 * 3600 * 24;
    final int MAX_DURATION = 14;

    RoomDB database;
    TextView damage;
    TextView contribution;
    TextView hitNum;
    TextView average;
    LinearLayout leaderCard;
    CombinedChartClass dpsChart;
    RecyclerView recyclerView;
    StatisticMemberLeaderAdapter adapter;

    int memberId;
    int raidId;
    int position;
    boolean isAdjustMode;

    private class MemberTotalDamage implements Comparable<MemberTotalDamage>{
        int memberId;
        long totalDamage;

        MemberTotalDamage(int memberId, long totalDamage){
            this.memberId = memberId;
            this.totalDamage = totalDamage;
        }

        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int memberId) {
            this.memberId = memberId;
        }

        public long getTotalDamage() {
            return totalDamage;
        }

        @Override
        public int compareTo(MemberTotalDamage m){
            if(totalDamage - m.getTotalDamage() > 0)
                return -1;
            else if(totalDamage - m.getTotalDamage() < 0)
                return 1;
            return 0;
        }
    }

    private class CombinedChartClass{
        private CombinedChart chart;
        private int xAxisNum;
        private List<Record> memberRecords;

        public CombinedChartClass(CombinedChart chart, int xAxisNum) {
            this.chart = chart;
            this.xAxisNum = xAxisNum;
        }

        public void setRecords(List<Record> memberRecords){
            this.memberRecords = memberRecords;
        }

        public void setCombinedChartUi(){ //CombinedChart의 ui 설정
            chart.getDescription().setEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setDrawBarShadow(false);
            chart.setHighlightFullBarEnabled(false);
            chart.setExtraOffsets(0, 0, 0, 10);

            // draw bars behind lines
            chart.setDrawOrder(new DrawOrder[]{
                    DrawOrder.BAR, DrawOrder.LINE
            });

            Legend l = chart.getLegend();
            l.setWordWrapEnabled(true);
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setTextColor(Color.WHITE);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setDrawGridLines(false);
            rightAxis.setTextColor(getResources().getColor(R.color.line_chart_color));
            rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setGridColor(getResources().getColor(R.color.bar_chart_color));
            leftAxis.setTextColor(getResources().getColor(R.color.bar_chart_color));
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            XAxis xAxis = chart.getXAxis();
            xAxis.setGridColor(Color.WHITE);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setPosition(XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(0f);
            xAxis.setGranularity(1f);

            final ArrayList<String> xAxisLabel = new ArrayList<>();
            xAxisLabel.add("");
            for(int i=1; i<=xAxisNum; i++)
                xAxisLabel.add("Day " + i);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return xAxisLabel.get((int) value);
                }
            });

            CombinedData data = new CombinedData();

            //data.setData(generateLineData());
            data.setData(generateBarData());

            xAxis.setAxisMaximum(data.getXMax() + 0.8f);

            chart.setData(data);
            chart.animateXY(2000, 2000);
            chart.invalidate();
        }

        /*
        private LineData generateLineData(){ // 기여도 그래프
            LineData d = new LineData();

            ArrayList<Entry> entries = new ArrayList<>();

            long[] allArray = new long[xAxisNum];
            long[] memberArray = new long[xAxisNum];

            for(Record r : memberRecords) {
                long rawDamage = r.getDamage();
                if(isAdjustMode) {
                    Boss b = r.getBoss();
                    memberArray[r.getDay() - 1] += (long) (rawDamage * b.getHardness());
                }
                else
                    memberArray[r.getDay() - 1] += (rawDamage);
            }

            for(Record r : allRecords) {
                long rawDamage = r.getDamage();
                if(isAdjustMode) {
                    Boss b = r.getBoss();
                    allArray[r.getDay() - 1] += (long) (rawDamage * b.getHardness());
                }
                else
                    allArray[r.getDay() - 1] += (rawDamage);
            }

            for (int i = 0; i < xAxisNum; i++) {
                if(allArray[i] == 0)
                    entries.add(new Entry(i + 1f, 0f));
                else
                    entries.add(new Entry(i + 1f, memberArray[i] / (float) allArray[i] * 100));
            }

            LineDataSet set = new LineDataSet(entries, "기여도(%)");
            set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format("%.1f", value);
                }
            });
            int lineColor = getResources().getColor(R.color.line_chart_color);
            set.setColor(lineColor);
            set.setLineWidth(2.5f);
            set.setCircleColor(lineColor);
            set.setCircleRadius(5f);
            set.setFillColor(lineColor);
            set.setDrawValues(true);
            set.setValueTextSize(10f);
            set.setValueTextColor(lineColor);

            set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            d.addDataSet(set);

            return d;
        }*/

        private BarData generateBarData(){ // 최종 딜량 그래프
            ArrayList<BarEntry> entries = new ArrayList<>();

            for(int i=0; i<xAxisNum; i++){
                entries.add(new BarEntry(i + 1f, 0));
            }

            for(Record r : memberRecords) {
                BarEntry e = entries.get(r.getDay() - 1);
                long rawDamage = r.getDamage();
                if(isAdjustMode) {
                    Boss b = r.getBoss();
                    e.setY( e.getY() + (long) (rawDamage * b.getHardness()) );
                }
                else
                    e.setY( e.getY() + rawDamage );
            }

            BarDataSet set = new BarDataSet(entries, "총 딜량");

            int barColor = getResources().getColor(R.color.bar_chart_color);
            set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "" + (int) value;
                }
            });
            set.setColor(barColor);
            set.setValueTextColor(barColor);
            set.setValueTextSize(10f);

            set.setAxisDependency(YAxis.AxisDependency.LEFT);

            BarData data = new BarData(set);
            data.setBarWidth(0.8f);
            return data;
        }
    }

    public static StatisticMember3Fragment newInstance(int position, int memberId, int raidId, boolean isChecked) {
        StatisticMember3Fragment fragment = new StatisticMember3Fragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("memberId", memberId);
        args.putInt("raidId", raidId);
        args.putBoolean("isChecked", isChecked);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_member_3, container, false);

        database = RoomDB.getInstance(getActivity());
        if (getArguments() != null) {
            memberId = getArguments().getInt("memberId");
            raidId = getArguments().getInt("raidId");
            isAdjustMode = getArguments().getBoolean("isChecked");
            position = getArguments().getInt("position");
        }

        damage = view.findViewById(R.id.damage);
        //contribution = view.findViewById(R.id.contribution);
        hitNum = view.findViewById(R.id.hitNum);
        average = view.findViewById(R.id.average);
        leaderCard = view.findViewById(R.id.leaderCard);
        recyclerView = view.findViewById(R.id.recyclerView);

        //처음 빈 공간을 위해 +1 처리
        dpsChart = new CombinedChartClass(view.findViewById(R.id.dpsChart), getIntegerFromToday() + 1);

        setView(position);

        return view;
    }

    private void setView(int position) {
        List<Boss> bosses = database.raidDao().getBossesList(raidId);
        switch(position){
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
        List<Record> allRecords = database.recordDao().getAllRecordsWithBossAndLeader(raidId);
        List<Record> memberRecords = database.recordDao().getCertainMemberRecordsWithBossAndLeader(memberId, raidId);
        leaderCard.setVisibility(View.GONE);

        setData(memberRecords);
    }

    private void setBossData(Boss boss) {
        int bossId = boss.getBossId();
        List<Record> memberRecords = database.recordDao().getMemberRecordsWithOneBossAndLeader(memberId, raidId, bossId);

        setLeaderCard(memberRecords);
        setData(memberRecords);
    }

    private void setData(List<Record> memberRecords) {
        //long allDamage = getDamageFromList(allRecords, isAdjustMode);
        long memberDamage = getDamageFromList(memberRecords, isAdjustMode);

        damage.setText(NumberFormat.getNumberInstance(Locale.US).format(memberDamage));
        //contribution.setText(getPercentage(memberDamage, allDamage));
        hitNum.setText(Integer.toString(memberRecords.size()));
        average.setText(memberRecords.size() == 0 ? Integer.toString(0) :
                NumberFormat.getNumberInstance(Locale.US).format(memberDamage / memberRecords.size()));

        dpsChart.setRecords(memberRecords);
        dpsChart.setCombinedChartUi();
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


    private int getIntegerFromToday() {
        Date today = new Date();
        Date startDate = database.raidDao().getRaid(raidId).getStartDay();

        int differentDays = (int) ((today.getTime() - startDate.getTime()) / DAY_IN_SECONDS);

        if (differentDays < 0)
            return 0;
        else if(differentDays > MAX_DURATION)
            return MAX_DURATION;
        return differentDays;
    }
}
