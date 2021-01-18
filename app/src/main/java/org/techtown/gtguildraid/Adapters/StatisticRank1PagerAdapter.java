package org.techtown.gtguildraid.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.techtown.gtguildraid.Fragments.StatisticRank2Fragment;

public class StatisticRank1PagerAdapter extends FragmentStateAdapter {
    private final int ITEM_NUM = 5;
    private int raidId;

    public StatisticRank1PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public void setData(int raidId){
        this.raidId = raidId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return StatisticRank2Fragment.newInstance(position, raidId);
    }

    @Override
    public int getItemCount() {
        return ITEM_NUM;
    }
}
