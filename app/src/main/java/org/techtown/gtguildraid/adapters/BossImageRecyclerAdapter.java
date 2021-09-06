package org.techtown.gtguildraid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.models.BossImage;

import java.util.List;

public class BossImageRecyclerAdapter
        extends RecyclerView.Adapter<BossImageRecyclerAdapter.ViewHolder> {
    List<BossImage> bosses;
    private final BottomSheetListener mListener;

    public interface BottomSheetListener{
        void onImageClicked(BossImage boss);
    }

    public BossImageRecyclerAdapter(List<BossImage> bossList, Fragment fragment) {
        bosses = bossList;
        this.mListener = (BossImageRecyclerAdapter.BottomSheetListener) fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_image_selector, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = parent.getWidth() / 3;
        layoutParams.height = parent.getWidth() / 3;
        view.setLayoutParams(layoutParams);

        return new BossImageRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BossImage boss = bosses.get(position);
        holder.setItem(boss, mListener);
    }

    @Override
    public int getItemCount() {
        return bosses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profileImage);
            context = itemView.getContext();
        }

        public void setItem(BossImage boss, BottomSheetListener mListener) {
            imageView.setImageResource(
                    context.getResources().getIdentifier
                            ("boss_" + boss.getImgName(), "drawable", context.getPackageName()));
            imageView.setOnClickListener(view -> {
                mListener.onImageClicked(boss);
            });
        }
    }
}
