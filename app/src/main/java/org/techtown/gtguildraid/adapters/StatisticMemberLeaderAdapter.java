package org.techtown.gtguildraid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.models.Hero;
import org.techtown.gtguildraid.models.LeaderInfo;
import org.techtown.gtguildraid.models.Record;
import org.techtown.gtguildraid.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StatisticMemberLeaderAdapter extends RecyclerView.Adapter<StatisticMemberLeaderAdapter.ViewHolder>{
    private List<LeaderInfo> leaderList;
    private boolean isAdjustMode;

    public StatisticMemberLeaderAdapter(List<LeaderInfo> records, boolean isAdjustMode) {
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
        LeaderInfo leader = leaderList.get(position);
        holder.setItem(leader, isAdjustMode);
    }

    @Override
    public int getItemCount() {
        return leaderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private static final long MAX_NUM = Long.MAX_VALUE;
        final String[] elementArray
                = new String[]{"normal", "fire", "water", "earth", "light", "dark", "basic"};

        ImageView leaderImage;
        ImageView leaderElement;
        TextView leaderName;
        TextView totalDamage;
        TextView hitNum;
        TextView averageDamage;
        TextView minDamage;
        TextView maxDamage;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            leaderImage = itemView.findViewById(R.id.leaderImage);
            leaderElement = itemView.findViewById(R.id.leaderElement);
            leaderName = itemView.findViewById(R.id.leaderName);
            hitNum = itemView.findViewById(R.id.hitNum);
            totalDamage = itemView.findViewById(R.id.totalDamage);
            averageDamage = itemView.findViewById(R.id.averageDamage);
            minDamage = itemView.findViewById(R.id.minDamage);
            maxDamage = itemView.findViewById(R.id.maxDamage);
            context = itemView.getContext();
        }

        public void setItem(LeaderInfo info, boolean isAdjustMode) {
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

            long total = 0;
            long min = MAX_NUM;
            long max = 0;
            for(Record r : records){
                long adjustDamage = getAdjustDamage(r, isAdjustMode);
                total += adjustDamage;
                min = Math.min(min, adjustDamage);
                max = Math.max(max, adjustDamage);
            }
            long average = total / records.size();

            totalDamage.setText(getStandardNumberFormat(total));
            averageDamage.setText(getStandardNumberFormat(average));
            minDamage.setText(getStandardNumberFormat(min));
            maxDamage.setText(getStandardNumberFormat(max));
        }

        private long getAdjustDamage(Record record, boolean isAdjustMode) {
            if(isAdjustMode)
                return (long) (record.getDamage() * record.getBoss().getHardness());

            return record.getDamage();
        }

        private String getStandardNumberFormat(long num){
            return NumberFormat.getNumberInstance(Locale.US).format(num);
        }
    }
}
