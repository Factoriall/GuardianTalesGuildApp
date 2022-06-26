package org.techtown.gtguildraid.statistics;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.models.entities.Raid;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticRaidCardAdapter extends RecyclerView.Adapter<StatisticRaidCardAdapter.ViewHolder> {
    List<Raid> raids;
    private final RecyclerViewListener rListener;

    public StatisticRaidCardAdapter(Fragment fragment, List<Raid> pastRaids) {
        raids = pastRaids;
        rListener = (RecyclerViewListener) fragment;
    }

    public interface RecyclerViewListener{
        void onDeleteClicked(int position);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_statistic_raid, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Raid raid = raids.get(position);
        holder.setItem(raid, position);
        holder.deleteButton = holder.itemView.findViewById(R.id.deleteButton);

        holder.deleteButton.setOnClickListener(view -> {
            rListener.onDeleteClicked(position);
        });

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), StatisticRaidActivity.class);
            intent.putExtra("raidId", raid.getRaidId());
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return raids.size();
    }



    public void removeRaid(int position){
        raids.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        final String dateFormat = "yy/MM/dd";
        ImageView raidThumbnail;
        TextView raidName;
        TextView raidTerm;
        ImageButton deleteButton;
        Context context;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            raidThumbnail = itemView.findViewById(R.id.raidThumbnail);
            raidName = itemView.findViewById(R.id.raidName);
            raidTerm = itemView.findViewById(R.id.raidTerm);
            context = itemView.getContext();
        }

        public void setItem(Raid raid, int position) {
            raidThumbnail.setImageResource(context.getResources().getIdentifier(
                    "character_"+raid.getThumbnail(), "drawable", context.getPackageName()
            ));
            raidName.setText(raid.getName());
            raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
                    new SimpleDateFormat(dateFormat).format(getEndTime(raid.getStartDay()))));
        }

        private Date getEndTime(Date day) {
            Calendar end = Calendar.getInstance();
            end.setTime(day);
            end.add(Calendar.DATE, 13);

            return end.getTime();
        }
    }
}