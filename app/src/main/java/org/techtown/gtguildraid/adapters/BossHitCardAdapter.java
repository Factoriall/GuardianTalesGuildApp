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
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.utils.RoomDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BossHitCardAdapter extends RecyclerView.Adapter<BossHitCardAdapter.ViewHolder> {
    HashMap<Integer, Integer> bossMap;
    List<BossCountList> list = new ArrayList<>();

    public class BossCountList{
        int bossId;
        int count;

        BossCountList(int i, int c){
            bossId = i;
            count = c;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_boss_hit, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BossHitCardAdapter.ViewHolder holder, int position) {
        BossCountList record = list.get(position);
        holder.setItem(record);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setItems(HashMap<Integer, Integer> bossCount) {
        this.bossMap = bossCount;
        for ( Map.Entry<Integer, Integer> entry : bossMap.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            list.add(new BossCountList(key, value));
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bossName;
        ImageView bossImage;
        TextView bossCount;
        RoomDB database;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bossName = itemView.findViewById(R.id.bossName);
            bossImage = itemView.findViewById(R.id.bossImage);
            bossCount = itemView.findViewById(R.id.bossHitNum);
            context = itemView.getContext();
            database = RoomDB.getInstance(context);
        }

        public void setItem(BossCountList record) {
            Boss boss = database.recordDao().getBoss(record.bossId);
            bossName.setText(boss.getName());
            bossCount.setText(record.count + "회");
            bossImage.setImageResource(getIdentifierFromResource(
                    "boss_" + boss.getImgName()));
        }

        int getIdentifierFromResource(String name){
            return context.getResources().getIdentifier(
                    name, "drawable", context.getPackageName());
        }
    }
}
