package org.techtown.gtguildraid.raid;

import android.content.Context;
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
import org.techtown.gtguildraid.models.entities.Boss;

import java.util.List;

public class RaidBossRecyclerAdapter extends RecyclerView.Adapter<RaidBossRecyclerAdapter.ViewHolder> {
    List<Boss> bosses;
    private final RecyclerViewListener rListener;
    public RaidBossRecyclerAdapter(List<Boss> bosses, Fragment fragment) {
        this.bosses = bosses;
        rListener = (RecyclerViewListener) fragment;
    }

    public void updateItem(Boss updated, int position) {
        bosses.set(position, updated);
        notifyItemChanged(position);
    }

    public interface RecyclerViewListener{
        void onEditButtonClicked(int bossId, int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_raid_boss, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Boss boss = bosses.get(position);
        holder.setItem(boss, position, rListener);
    }

    @Override
    public int getItemCount() {
        return bosses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bossImage;
        TextView bossName;
        ImageView elementImage;
        TextView hardness;
        ImageButton editButton;
        ImageView furious;
        Context context;
        private String[] elementEnglish = {"선택", "fire", "water", "earth", "light", "dark", "basic"};

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bossImage = itemView.findViewById(R.id.bossImage);
            bossName = itemView.findViewById(R.id.bossName);
            elementImage = itemView.findViewById(R.id.elementImage);
            hardness = itemView.findViewById(R.id.hardness);
            editButton = itemView.findViewById(R.id.editButton);
            furious = itemView.findViewById(R.id.furious);
            context = itemView.getContext();
        }

        public void setItem(Boss boss, int position, RecyclerViewListener rListener) {
            bossName.setText(boss.getName());
            hardness.setText("x " + boss.getHardness());
            elementImage.setImageResource(context.getResources().getIdentifier(
                    "element_" + boss.getElementId(), "drawable", context.getPackageName()));
            bossImage.setImageResource(context.getResources()
                    .getIdentifier("boss_" + boss.getImgName(), "drawable", context.getPackageName()));
            elementImage.setImageResource(context.getResources()
                    .getIdentifier("element_" + elementEnglish[boss.getElementId()], "drawable", context.getPackageName()));
            if(boss.isFurious()) furious.setVisibility(View.VISIBLE);
            else furious.setVisibility(View.GONE);
            editButton.setOnClickListener(view -> {
                rListener.onEditButtonClicked(boss.getBossId(), position);
            });

        }
    }
}