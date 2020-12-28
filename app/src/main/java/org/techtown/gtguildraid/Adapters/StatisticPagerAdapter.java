package org.techtown.gtguildraid.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.techtown.gtguildraid.Fragments.StatisticBoss1Fragment;
import org.techtown.gtguildraid.Fragments.StatisticMember1Fragment;
import org.techtown.gtguildraid.Fragments.StatisticRankFragment;

public class StatisticPagerAdapter extends FragmentStateAdapter {
    private static final int ITEM_SIZE = 3;

    private static final int MEMBER = 0;
    private static final int BOSS = 1;
    private static final int RANK = 2;
    private int raidId;

    public void setData(int raidId){
        this.raidId = raidId;
    }

    public StatisticPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case MEMBER:
                return StatisticMember1Fragment.newInstance(raidId);
            case BOSS:
                return StatisticBoss1Fragment.newInstance(raidId);
            case RANK:
                return StatisticRankFragment.newInstance(raidId);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return ITEM_SIZE;
    }
}