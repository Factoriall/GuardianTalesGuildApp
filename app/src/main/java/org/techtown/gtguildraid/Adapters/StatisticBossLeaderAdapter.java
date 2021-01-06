package org.techtown.gtguildraid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.CombinedChart;

import org.techtown.gtguildraid.Models.Hero;
import org.techtown.gtguildraid.Models.LeaderInformation;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;

import java.text.NumberFormat;
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
        CombinedChart chart;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            leaderImage = itemView.findViewById(R.id.leaderImage);
            leaderElement = itemView.findViewById(R.id.leaderElement);
            leaderName = itemView.findViewById(R.id.leaderName);
            hitNum = itemView.findViewById(R.id.hitNum);
            averageDamage = itemView.findViewById(R.id.averageDamage);
            CV = itemView.findViewById(R.id.CV);
            chart = itemView.findViewById(R.id.chart);
            context = itemView.getContext();
        }

        public void setItem(LeaderInformation info) {
            Hero leader = info.getLeader();
            List<Record> records = info.getRecordList();

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
