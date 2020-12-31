package org.techtown.gtguildraid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Models.Hero;
import org.techtown.gtguildraid.Models.LeaderInformation;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StatisticMemberLeaderAdapter extends RecyclerView.Adapter<StatisticMemberLeaderAdapter.ViewHolder>{
    private List<LeaderInformation> leaderList;
    private boolean isAdjustMode;

    public StatisticMemberLeaderAdapter(List<LeaderInformation> records, boolean isAdjustMode){
        this.leaderList = records;
        this.isAdjustMode = isAdjustMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_stat_member_leader, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderInformation leader = leaderList.get(position);
        holder.setItem(leader, isAdjustMode);
    }

    @Override
    public int getItemCount() {
        return leaderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private static final int MAX_NUM = 1234567890;
        ImageView leaderImage;
        TextView leaderName;
        TextView totalDamage;
        TextView hitNum;
        TextView averageDamage;
        TextView minDamage;
        TextView maxDamage;
        TextView stDev;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            leaderImage = itemView.findViewById(R.id.leaderImage);
            leaderName = itemView.findViewById(R.id.leaderName);
            hitNum = itemView.findViewById(R.id.hitNum);
            totalDamage = itemView.findViewById(R.id.totalDamage);
            averageDamage = itemView.findViewById(R.id.averageDamage);
            minDamage = itemView.findViewById(R.id.minDamage);
            maxDamage = itemView.findViewById(R.id.maxDamage);
            stDev = itemView.findViewById(R.id.stDev);
            context = itemView.getContext();
        }

        public void setItem(LeaderInformation info, boolean isAdjustMode){
            Hero leader = info.getLeader();
            List<Record> records = info.getRecordList();

            leaderName.setText(leader.getKoreanName());
            int imageId = context.getResources().getIdentifier(
                    "character_" + leader.getEnglishName(), "drawable", context.getPackageName());
            leaderImage.setImageResource(imageId);
            hitNum.setText(Integer.toString(records.size()));

            int total = 0;
            int min = MAX_NUM;
            int max = 0;
            for(Record r : records){
                int adjustDamage = getAdjustDamage(r, isAdjustMode);
                total += adjustDamage;
                min = Math.min(min, adjustDamage);
                max = Math.max(max, adjustDamage);
            }
            int average = total / records.size();
            double stdev = getStandardDeviation(average, records, isAdjustMode);

            totalDamage.setText(getStandardNumberFormat(total));
            averageDamage.setText(getStandardNumberFormat(average));
            minDamage.setText(getStandardNumberFormat(min));
            maxDamage.setText(getStandardNumberFormat(max));
            stDev.setText(String.format("%.2f", stdev));
        }

        private double getStandardDeviation(int average, List<Record> records, boolean isAdjustMode) {
            int devSquared = 0;
            for(Record r : records){
                devSquared += (getAdjustDamage(r, isAdjustMode) - average)
                        * (getAdjustDamage(r, isAdjustMode) - average);
            }

            return Math.sqrt(devSquared / (float) records.size());
        }

        private int getAdjustDamage(Record record, boolean isAdjustMode) {
            if(isAdjustMode)
                return (int) (record.getDamage() * record.getBoss().getHardness());

            return record.getDamage();
        }

        private String getStandardNumberFormat(int num){
            return NumberFormat.getNumberInstance(Locale.US).format(num);
        }
    }
}
