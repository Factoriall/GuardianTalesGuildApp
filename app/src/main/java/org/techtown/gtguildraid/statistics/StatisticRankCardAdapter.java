package org.techtown.gtguildraid.statistics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.models.RankInfo;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticRankCardAdapter extends RecyclerView.Adapter<StatisticRankCardAdapter.ViewHolder> {
    private List<RankInfo> rankList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_rank, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticRankCardAdapter.ViewHolder holder, int position) {
        RankInfo ri = rankList.get(position);
        holder.setItem(ri, position);
    }

    @Override
    public int getItemCount() {
        return rankList.size();
    }

    public void setItems(List<RankInfo> rankList) {
        this.rankList = rankList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankNum;
        TextView memberName;
        TextView hitNum;
        TextView damage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            rankNum = itemView.findViewById(R.id.rankNum);
            memberName = itemView.findViewById(R.id.memberName);
            hitNum = itemView.findViewById(R.id.hitNum);
            damage = itemView.findViewById(R.id.damage);
        }

        public void setItem(RankInfo ri, int position) {
            rankNum.setText(Integer.toString(position + 1));
            memberName.setText(ri.getMemberName());
            hitNum.setText(Integer.toString(ri.getHitNum()));
            damage.setText(NumberFormat.getNumberInstance(Locale.US).format(ri.getFinalDamage()));
        }
    }
}
