package org.techtown.gtguildraid.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RaidAdapter extends RecyclerView.Adapter<RaidAdapter.ViewHolder> {
    private List<Raid> raidList;
    private Activity context;

    public RaidAdapter(Activity context, List<Raid> raidList) {
        this.raidList = raidList;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_raid, parent, false);

        return new RaidAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Raid raid = raidList.get(position);
        holder.setItem(raid);
    }

    @Override
    public int getItemCount() {
        return raidList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView raidName;
        TextView raidTerm;
        TextView[] bossNameList;
        TextView[] bossHardnessList;
        ProgressBar[] bossBarList;
        Context context;
        RoomDB database;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            raidName = itemView.findViewById(R.id.raidName);
            raidTerm = itemView.findViewById(R.id.raidTerm);
            database = RoomDB.getInstance(itemView.getContext());

            bossNameList = new TextView[4];
            bossHardnessList = new TextView[4];
            bossBarList = new ProgressBar[4];
            context = itemView.getContext();

            for(int i=1; i<=4; i++){
                Resources res = context.getResources();
                int nameId = res.getIdentifier("boss" + i, "id", context.getPackageName());
                int barId = res.getIdentifier("progressBar" + i, "id", context.getPackageName());
                int hardnessId = res.getIdentifier("hardness" + i, "id", context.getPackageName());

                bossNameList[i-1] = itemView.findViewById(nameId);
                bossHardnessList[i-1] = itemView.findViewById(hardnessId);
                bossBarList[i-1] = itemView.findViewById(barId);
            }
        }

        public void setItem(Raid raid){
            raidName.setText(raid.getName());
            Date aEnd = adjustEndTime(raid.getEndDay());
            raidTerm.setText(new SimpleDateFormat("yyyy-MM-dd").format(raid.getStartDay()) + "~"
                    + new SimpleDateFormat("yyyy-MM-dd").format(aEnd));

            for(int i=0; i<4; i++) {
                Boss boss = database.raidDao().getBossesList(raid.getRaidId()).get(i);
                bossNameList[i].setText(boss.getName());
                bossHardnessList[i].setText("배율: " + String.format("%.1f", boss.getHardness()));
                bossBarList[i].setProgress((int)(boss.getHardness() * 10));
            }
        }
    }

    private static Date adjustEndTime(Date eDate) {
        Calendar end = Calendar.getInstance();
        end.setTime(eDate);
        end.add(Calendar.DATE, -1);

        return end.getTime();
    }
}
