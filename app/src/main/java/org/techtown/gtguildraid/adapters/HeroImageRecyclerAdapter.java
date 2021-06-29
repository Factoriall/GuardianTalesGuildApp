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
import org.techtown.gtguildraid.models.Hero;

import java.util.List;

public class HeroImageRecyclerAdapter
        extends RecyclerView.Adapter<HeroImageRecyclerAdapter.ViewHolder> {
    List<Hero> heroes;
    private final BottomSheetListener mListener;

    public interface BottomSheetListener{
        void onImageClicked(Hero hero);
    }

    public HeroImageRecyclerAdapter(List<Hero> heroList, Fragment fragment) {
        heroes = heroList;
        this.mListener =  (BottomSheetListener) fragment;
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

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hero hero = heroes.get(position);
        holder.setItem(hero, mListener);
    }

    @Override
    public int getItemCount() {
        return heroes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profileImage);
            context = itemView.getContext();
        }

        public void setItem(Hero hero, BottomSheetListener mListener) {
            imageView.setImageResource(
                    context.getResources().getIdentifier
                            ("character_" + hero.getEnglishName(), "drawable", context.getPackageName()));
            imageView.setOnClickListener(view -> {
                mListener.onImageClicked(hero);
            });
        }
    }
}
