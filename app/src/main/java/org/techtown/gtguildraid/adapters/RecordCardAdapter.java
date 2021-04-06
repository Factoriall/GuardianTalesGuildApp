package org.techtown.gtguildraid.adapters;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.models.Record;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecordCardAdapter extends RecyclerView.Adapter<RecordCardAdapter.ViewHolder> {
    private List<Record> recordList;

    @NonNull
    @Override
    public RecordCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_record, parent, false);

        return new RecordCardAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordCardAdapter.ViewHolder holder, int position) {
        Record record = recordList.get(position);
        holder.setItem(record);
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public void setItems(List<Record> recordList) {
        this.recordList = recordList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView damage;
        TextView level;
        ImageView bossImage;
        ImageView leaderImage;
        ImageView lastHit;
        TextView bossName;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            context = itemView.getContext();
            damage = itemView.findViewById(R.id.damage);
            level = itemView.findViewById(R.id.level);
            bossName = itemView.findViewById(R.id.bossName);
            bossImage = itemView.findViewById(R.id.bossImage);
            leaderImage = itemView.findViewById(R.id.leaderImage);
            lastHit = itemView.findViewById(R.id.lastHit);
        }

        public void setItem(Record record) {
            try {
                level.setText(getLevelFromRound(record.getRound()));
                String sBossName = record.getBoss().getName();
                if (sBossName.length() > 7)
                    sBossName = sBossName.substring(0, 7) + "..";
                bossName.setText(sBossName);
                bossImage.setImageResource(getIdentifierFromResource(
                        "boss_" + record.getBoss().getImgName()));

                damage.setText(NumberFormat.getNumberInstance(Locale.US).format(record.getDamage()));

                String leaderName = record.getLeader().getEnglishName();
                leaderImage.setImageResource(getIdentifierFromResource(
                        "character_" + leaderName));

                if (record.isLastHit()) {
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);
                    // make color filter
                    ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);
                    // set filter to ImageView
                    bossImage.setColorFilter(colorFilter);
                    bossImage.setImageAlpha(128);
                    lastHit.setVisibility(View.VISIBLE);
                }
                else {
                    bossImage.setColorFilter(null);
                    bossImage.setImageAlpha(255);
                    lastHit.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String getLevelFromRound(int round) {
            int[] levelPerRound = {50, 50, 55, 55, 60, 60};
            final int START_NUM = 65;
            final int START_IDX = 7;
            final int MAX_LEVEL = 80;

            int level = (round <= levelPerRound.length ? levelPerRound[round - 1] : START_NUM + (round - START_IDX));
            if (level > MAX_LEVEL) level = MAX_LEVEL;
            return "Lv." + level;
        }

        int getIdentifierFromResource(String name){
            return context.getResources().getIdentifier(
                    name, "drawable", context.getPackageName());
        }
    }
}
