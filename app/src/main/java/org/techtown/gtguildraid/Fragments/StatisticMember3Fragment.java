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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.techtown.gtguildraid.Models.Boss;
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
    TextView rank;
    LineChartClass dpsChart;

    int memberId;
    int raidId;
    int position;
    boolean isAdjustMode;

    private class MemberTotalDamage implements Comparable<MemberTotalDamage>{
        int memberId;
        int totalDamage;

        MemberTotalDamage(int memberId, int totalDamage){
            this.memberId = memberId;
            this.totalDamage = totalDamage;
        }

        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int memberId) {
            this.memberId = memberId;
        }

        public int getTotalDamage() {
            return totalDamage;
        }

        public void setTotalDamage(int totalDamage) {
            this.totalDamage = totalDamage;
        }

        @Override
        public int compareTo(MemberTotalDamage m){
            return m.getTotalDamage() - totalDamage;
        }
    }

    class LineChartClass {
        LineChart chart;
        int xAxisNum;

        LineChartClass(View viewById, int xAxisNum) {
            this.chart = (LineChart) viewById;
            this.xAxisNum = xAxisNum;
        }

        public void setLineChartUi(List<Record> records, boolean isAdjustMode) {
            // background color
            chart.setBackgroundColor(Color.WHITE);

            // disable description text
            chart.getDescription().setEnabled(false);

            // enable touch gestures
            chart.setTouchEnabled(true);

            // set listeners
            chart.setDrawGridBackground(false);

            // enable scaling and dragging
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);

            XAxis xAxis;
            {   // // X-Axis Style // //
                xAxis = chart.getXAxis();

                xAxis.setLabelCount(xAxisNum, true);

                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setDrawGridLines(false);
            }

            YAxis yAxis;
            {   // // Y-Axis Style // //
                yAxis = chart.getAxisLeft();

                // disable dual axis (only use LEFT axis)
                chart.getAxisRight().setEnabled(false);
                yAxis.setAxisMinimum(0.0f);
            }

            // get the legend (only possible after setting data)
            Legend l = chart.getLegend();
            l.setEnabled(false);

            setLineChartData(records, isAdjustMode);
        }

        private void setLineChartData(List<Record> records, boolean isAdjustMode) {
            ArrayList<Entry> values = new ArrayList<>();
            for(int i=0; i<xAxisNum; i++){
                values.add(new Entry(i, 0));
            }

            for(Record r : records) {
                Log.d("recordInfo", Integer.toString(r.getDamage()));

                Entry e = values.get(r.getDay() - 1);
                int rawDamage = r.getDamage();
                if(isAdjustMode) {
                    Boss b = r.getBoss();
                    e.setY( e.getY() + (int) (rawDamage * b.getHardness()) );
                }
                else
                    e.setY( e.getY() + rawDamage );
            }

            XAxis xAxis = chart.getXAxis();
            xAxis.setDrawLabels(true);
            final ArrayList<String> xAxisLabel = new ArrayList<>();
            for(int i=1; i<=xAxisNum; i++){
                xAxisLabel.add("Day " + i);
            }
            xAxis.setValueFormatter(new ValueFormatter(){
                @Override
                public String getFormattedValue(float value) {
                    Log.d("formattedValue", Float.toString(value));
                    return xAxisLabel.get((int) value);
                }
            });

            LineDataSet set1;

            if (chart.getData() != null &&
                    chart.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
                set1.setValues(values);
                set1.notifyDataSetChanged();
                chart.getData().notifyDataChanged();
                chart.notifyDataSetChanged();
            } else {
                // create a dataset and give it a type
                set1 = new LineDataSet(values, "DataSet 1");

                set1.setDrawIcons(false);

                // black lines and points
                set1.setColor(Color.BLACK);
                set1.setCircleColor(Color.BLACK);

                // line thickness and point size
                set1.setLineWidth(1f);
                set1.setCircleRadius(3f);

                // draw points as solid circles
                set1.setDrawCircleHole(false);

                // customize legend entry
                set1.setFormLineWidth(1f);
                set1.setFormSize(15.f);

                // text size of values
                set1.setValueTextSize(9f);

                set1.setDrawValues(false);
                // set the filled area
                set1.setDrawFilled(true);
                set1.setFillFormatter(new IFillFormatter() {
                    @Override
                    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                        return chart.getAxisLeft().getAxisMinimum();
                    }
                });

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1); // add the data sets

                // create a data object with the data sets
                LineData data = new LineData(dataSets);

                // set data
                chart.setData(data);
            }
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
        contribution = view.findViewById(R.id.contribution);
        hitNum = view.findViewById(R.id.hitNum);
        rank = view.findViewById(R.id.rank);
        dpsChart = new LineChartClass(view.findViewById(R.id.dpsChart), getIntegerFromToday());

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
        List<Record> allRecords = database.recordDao().getAllRecordsWithBoss(raidId);
        List<Record> memberRecords = database.recordDao().getCertainMemberRecordsWithBoss(memberId, raidId);

        setData(allRecords, memberRecords);
    }

    private void setBossData(Boss boss) {
        int bossId = boss.getBossId();

        List<Record> allRecords = database.recordDao().getAllRecordsWithOneBoss(raidId, bossId);
        List<Record> memberRecords = database.recordDao().getMemberRecordsWithOneBoss(memberId, raidId, bossId);

        setData(allRecords, memberRecords);
    }


    private void setData(List<Record> allRecords, List<Record> memberRecords) {
        int allDamage = getDamageFromList(allRecords, isAdjustMode);
        int memberDamage = getDamageFromList(memberRecords, isAdjustMode);

        damage.setText(NumberFormat.getNumberInstance(Locale.US).format(memberDamage));
        contribution.setText(getPercentage(memberDamage, allDamage) + "%");
        hitNum.setText(Integer.toString(memberRecords.size()));

        rank.setText(getRank(allRecords, memberDamage, isAdjustMode));
        dpsChart.setLineChartUi(memberRecords, isAdjustMode);
    }

    private String getRank(List<Record> allRecords, int memberDamage, boolean isAdjustMode) {
        if(memberDamage == 0)
            return "-";

        List<MemberTotalDamage> damageList = new ArrayList<>();
        int mId = -1;
        int damage = 0;
        for(Record r : allRecords){
            if(mId != r.getMemberId()){
                if(mId != -1)
                    damageList.add(new MemberTotalDamage(mId, damage));
                mId = r.getMemberId();
                damage = 0;
            }

            if(isAdjustMode) {
                Boss b = r.getBoss();
                damage += (int) (r.getDamage() * b.getHardness());
            }
            else
                damage += r.getDamage();
        }
        damageList.add(new MemberTotalDamage(mId, damage));

        Collections.sort(damageList);

        int rank = 1;
        for(MemberTotalDamage d : damageList){
            if(d.getMemberId() == memberId)
                break;
            rank++;
        }

        return Integer.toString(rank);
    }

    private String getPercentage(int memberDamage, int allDamage) {
        if(allDamage == 0)
            return "0.00";
        return String.format("%.2f", memberDamage/(double)allDamage * 100);
    }

    private int getDamageFromList(List<Record> records, boolean isAdjustMode) {
        int damage = 0;
        for(Record r: records){
            if(isAdjustMode) {
                Boss b = r.getBoss();
                damage += (int) (r.getDamage() * b.getHardness());
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
            return -1;
        else if(differentDays > MAX_DURATION)
            return MAX_DURATION;
        return differentDays;
    }
}
