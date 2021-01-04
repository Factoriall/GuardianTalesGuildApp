package org.techtown.gtguildraid.Fragments;

import android.graphics.Color;
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

import org.techtown.gtguildraid.Adapters.StatisticBossLeaderAdapter;
import org.techtown.gtguildraid.Models.Boss;
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
    CombinedChartClass chart;
    RecyclerView recyclerView;

    private class CombinedChartClass{
        private CombinedChart chart;
        private int xAxisNum;
        private List<Record> records;

        public CombinedChartClass(CombinedChart chart, int xAxisNum) {
            this.chart = chart;
            this.xAxisNum = xAxisNum;
        }

        public void setRecords(List<Record> records){
            this.records = records;
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
                xAxisLabel.add("Level " + i);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return xAxisLabel.get((int) value);
                }
            });

            CombinedData data = new CombinedData();

            data.setData(generateLineData());
            data.setData(generateBarData());

            xAxis.setAxisMaximum(data.getXMax() + 0.8f);

            chart.setData(data);
            chart.invalidate();
        }

        private LineData generateLineData(){ // 횟수 그래프
            LineData d = new LineData();

            ArrayList<Entry> entries = new ArrayList<>();

            int[] numArray = new int[xAxisNum];

            for(Record r : records) {
                int rawDamage = r.getDamage();
                if(isAdjustMode) {
                    Boss b = r.getBoss();
                    numArray[r.getDay() - 1] += (int) (rawDamage * b.getHardness());
                }
                else
                    numArray[r.getDay() - 1] += (rawDamage);
            }

            LineDataSet set = new LineDataSet(entries, "평균 데미지");
            set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format("%d", value);
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
        }

        private BarData generateBarData(){ // 평균 딜량 그래프
            ArrayList<BarEntry> entries = new ArrayList<>();

            for(int i=0; i<xAxisNum; i++){
                entries.add(new BarEntry(i + 1f, 0));
            }

            for(Record r : records) {
                BarEntry e = entries.get(r.getDay() - 1);
                int rawDamage = r.getDamage();
                if(isAdjustMode) {
                    Boss b = r.getBoss();
                    e.setY( e.getY() + (int) (rawDamage * b.getHardness()) );
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
        chart = new CombinedChartClass(view.findViewById(R.id.chart), 10);
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

        //setChartData(records);
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
