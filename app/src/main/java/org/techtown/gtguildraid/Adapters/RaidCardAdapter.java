package org.techtown.gtguildraid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class RaidCardAdapter extends RecyclerView.Adapter<RaidCardAdapter.ViewHolder> {
    private List<Raid> raidList;

    public RaidCardAdapter(List<Raid> raidList) {
        this.raidList = raidList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_raid, parent, false);

        return new RaidCardAdapter.ViewHolder(view);
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
        final private String dateFormat = "yy/MM/dd";
        TextView raidName;
        TextView raidTerm;
        TextView[] bossNameList;
        TextView[] bossHardnessList;
        ProgressBar[] bossBarList;
        ImageView[] bossImageList;
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
            bossImageList = new ImageView[4];
            context = itemView.getContext();

            for(int i=1; i<=4; i++){
                int nameId = getIdentifierFromResource("boss" + i, "id");
                int barId = getIdentifierFromResource("progressBar" + i, "id");
                int hardnessId = getIdentifierFromResource("hardness" + i, "id");
                int imageId = getIdentifierFromResource("boss" + i + "Image", "id");
                bossNameList[i-1] = itemView.findViewById(nameId);
                bossHardnessList[i-1] = itemView.findViewById(hardnessId);
                bossBarList[i-1] = itemView.findViewById(barId);
                bossImageList[i-1] = itemView.findViewById(imageId);
            }

            ImageView arrow = itemView.findViewById(R.id.currentArrow);
            LinearLayout bossInfo = itemView.findViewById(R.id.bossInfo);
            arrow.setOnClickListener(view -> {
                if(bossInfo.getVisibility() == View.VISIBLE){
                    bossInfo.setVisibility(View.GONE);
                    arrow.setImageResource(R.drawable.icon_arrow_down);
                }
                else{
                    bossInfo.setVisibility(View.VISIBLE);
                    arrow.setImageResource(R.drawable.icon_arrow_up);
                }
            });
        }

        public void setItem(Raid raid){
            raidName.setText(raid.getName());
            Date aEnd = adjustEndTime(raid.getEndDay());
            raidTerm.setText(new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~"
                    + new SimpleDateFormat(dateFormat).format(aEnd));

            for(int i=0; i<4; i++) {
                Boss boss = database.raidDao().getBossesList(raid.getRaidId()).get(i);
                bossNameList[i].setText(boss.getName());
                bossHardnessList[i].setText("배율: " + String.format("%.1f", boss.getHardness()));
                bossBarList[i].setProgress((int)(boss.getHardness() * 10));
                bossImageList[i].setImageResource(
                        getIdentifierFromResource("boss_" + boss.getImgName(), "drawable"));
            }
        }

        int getIdentifierFromResource(String name, String defType){
            return context.getResources().getIdentifier(
                    name, defType, context.getPackageName());
        }
    }

    private static Date adjustEndTime(Date eDate) {
        Calendar end = Calendar.getInstance();
        end.setTime(eDate);
        end.add(Calendar.DATE, -1);

        return end.getTime();
    }
}
