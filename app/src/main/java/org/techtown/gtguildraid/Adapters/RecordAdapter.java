package org.techtown.gtguildraid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private List<Record> recordList;

    @NonNull
    @Override
    public RecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_record, parent, false);

        return new RecordAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordAdapter.ViewHolder holder, int position) {
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
        TextView round;
        ImageView bossImage;
        ImageView leaderImage;
        ImageView lastHit;
        TextView bossName;
        RoomDB database;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            context = itemView.getContext();
            database = RoomDB.getInstance(itemView.getContext());

            //heroes = new ImageView[4];
            damage = itemView.findViewById(R.id.damage);
            round = itemView.findViewById(R.id.round);
            level = itemView.findViewById(R.id.level);
            bossName = itemView.findViewById(R.id.bossName);
            bossImage = itemView.findViewById(R.id.bossImage);
            leaderImage = itemView.findViewById(R.id.leaderImage);
            lastHit = itemView.findViewById(R.id.lastHit);
        }

        public void setItem(Record record) {
            round.setText(record.getRound() + "회차");
            level.setText(getLevelFromRound(record.getRound()));
            String sBossName = record.getBoss().getName();
            if(sBossName.length() > 7)
                sBossName = sBossName.substring(0,7) + "..";
            bossName.setText(sBossName);
            bossImage.setImageResource(getIdentifierFromResource(
                    "boss_" + record.getBoss().getImgName(), "drawable"));

            damage.setText(NumberFormat.getNumberInstance(Locale.US).format(record.getDamage()));

            String leaderName = record.getLeader().getEnglishName();
            leaderImage.setImageResource(getIdentifierFromResource(
                    "character_" + leaderName, "drawable"));

            if(record.isLastHit())
                lastHit.setVisibility(View.VISIBLE);
            else
                lastHit.setVisibility(View.GONE);
        }

        private String getLevelFromRound(int round) {
            int[] levelPerRound = {50, 50, 55, 55, 60, 60};
            final int START_NUM = 65;
            final int START_IDX = 7;

            return Integer.toString(round <= levelPerRound.length ? levelPerRound[round - 1] : START_NUM + (round - START_IDX));
        }

        int getIdentifierFromResource(String name, String defType){
            return context.getResources().getIdentifier(
                    name, defType, context.getPackageName());
        }
    }
}
