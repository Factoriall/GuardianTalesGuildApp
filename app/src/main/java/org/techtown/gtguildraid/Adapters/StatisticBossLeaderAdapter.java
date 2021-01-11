package org.techtown.gtguildraid.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.techtown.gtguildraid.Models.Hero;
import org.techtown.gtguildraid.Models.LeaderInformation;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticBossLeaderAdapter extends RecyclerView.Adapter<StatisticBossLeaderAdapter.ViewHolder> {
    private List<LeaderInformation> leaderList;

    public StatisticBossLeaderAdapter(List<LeaderInformation> records) {
        this.leaderList = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_stat_boss_leader, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderInformation leader = leaderList.get(position);
        holder.setItem(leader);
    }

    @Override
    public int getItemCount() {
        return leaderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        final String[] elementArray
                = new String[]{"normal", "fire", "water", "earth", "light", "dark", "basic"};

        ImageView leaderImage;
        ImageView leaderElement;
        TextView leaderName;
        TextView hitNum;
        TextView averageDamage;
        TextView CV;
        CombinedChartClass chart;
        Context context;

        private class CombinedChartClass{
            private CombinedChart chart;
            private int xAxisNum;
            private List<Record> records;

            public CombinedChartClass(CombinedChart chart) {
                this.chart = chart;
            }

            public void setxAxisNum(int xAxisNum) {
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
                chart.animateXY(2000, 2000);

                // draw bars behind lines
                chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                        CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
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
                rightAxis.setTextColor(context.getResources().getColor(R.color.line_chart_color));
                rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                YAxis leftAxis = chart.getAxisLeft();
                leftAxis.setGridColor(context.getResources().getColor(R.color.bar_chart_color));
                leftAxis.setTextColor(context.getResources().getColor(R.color.bar_chart_color));
                leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                XAxis xAxis = chart.getXAxis();
                xAxis.setGridColor(Color.WHITE);
                xAxis.setTextColor(Color.WHITE);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
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

                for(Record r : records)
                    numArray[r.getRound() - 1]++;

                for (int i = 0; i < xAxisNum; i++) {
                    entries.add(new Entry(i + 1f, numArray[i]));
                }

                LineDataSet set = new LineDataSet(entries, "친 횟수");

                int lineColor = context.getResources().getColor(R.color.line_chart_color);
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
                    BarEntry e = entries.get(r.getRound() - 1);
                    e.setY( e.getY() + r.getDamage() );
                }

                BarDataSet set = new BarDataSet(entries, "총 딜량");

                int barColor = context.getResources().getColor(R.color.bar_chart_color);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            leaderImage = itemView.findViewById(R.id.leaderImage);
            leaderElement = itemView.findViewById(R.id.leaderElement);
            leaderName = itemView.findViewById(R.id.leaderName);
            hitNum = itemView.findViewById(R.id.hitNum);
            averageDamage = itemView.findViewById(R.id.averageDamage);
            CV = itemView.findViewById(R.id.CV);
            chart = new CombinedChartClass(itemView.findViewById(R.id.chart));
            context = itemView.getContext();
        }

        public void setItem(LeaderInformation info) {
            Hero leader = info.getLeader();
            List<Record> records = info.getRecordList();
            if(records.size() != 0)
                chart.setxAxisNum(records.get(records.size() - 1).getRound());
            else
                chart.setxAxisNum(1);

            leaderName.setText(leader.getKoreanName());
            int leaderElementId = context.getResources().getIdentifier(
                    "element_" + elementArray[leader.getElement()], "drawable", context.getPackageName());
            leaderElement.setImageResource(leaderElementId);
            int leaderImageId = context.getResources().getIdentifier(
                    "character_" + leader.getEnglishName(), "drawable", context.getPackageName());
            leaderImage.setImageResource(leaderImageId);
            hitNum.setText(Integer.toString(records.size()));

            int total = 0;
            for(Record r : records){
                total += r.getDamage();
            }
            int average = total / records.size();
            averageDamage.setText(getStandardNumberFormat(average));
            CV.setText(getCV(average, records));

            chart.setRecords(records);
            chart.setCombinedChartUi();
        }

        private String getCV(int average, List<Record> records) {
            long devSquared = 0;
            for(Record r : records){
                devSquared += ((long)(r.getDamage() - average) * (long)(r.getDamage() - average));
            }
            double stDev = Math.sqrt(devSquared / (double) records.size());

            return String.format("%.2f", stDev / average * 100.0f);
        }

        private String getStandardNumberFormat(int num){
            return NumberFormat.getNumberInstance(Locale.US).format(num);
        }
    }
}
