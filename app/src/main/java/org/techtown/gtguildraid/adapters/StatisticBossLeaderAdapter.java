package org.techtown.gtguildraid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.models.daos.Hero;
import org.techtown.gtguildraid.models.LeaderInfo;
import org.techtown.gtguildraid.models.daos.Record;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticBossLeaderAdapter extends RecyclerView.Adapter<StatisticBossLeaderAdapter.ViewHolder> {
    private List<LeaderInfo> leaderList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_stat_boss_leader, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderInfo leader = leaderList.get(position);
        holder.setItem(leader);
    }

    public void setItems(List<LeaderInfo> records) {
        this.leaderList = records;
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
        TextView totalDamage;
        TextView averageDamage;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            leaderImage = itemView.findViewById(R.id.leaderImage);
            leaderElement = itemView.findViewById(R.id.leaderElement);
            leaderName = itemView.findViewById(R.id.leaderName);
            hitNum = itemView.findViewById(R.id.hitNum);
            totalDamage = itemView.findViewById(R.id.totalDamage);
            averageDamage = itemView.findViewById(R.id.averageDamage);
            context = itemView.getContext();
        }

        public void setItem(LeaderInfo info) {
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
            for(Record r : records){
                total += r.getDamage();
            }
            totalDamage.setText(getStandardNumberFormat(total));
            long average = total / records.size();
            averageDamage.setText(getStandardNumberFormat(average));
        }

        private String getStandardNumberFormat(long num){
            return NumberFormat.getNumberInstance(Locale.US).format(num);
        }
    }
}
