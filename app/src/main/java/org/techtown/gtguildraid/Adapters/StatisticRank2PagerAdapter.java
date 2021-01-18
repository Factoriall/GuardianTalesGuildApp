package org.techtown.gtguildraid.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.techtown.gtguildraid.Fragments.StatisticMember3Fragment;
import org.techtown.gtguildraid.Fragments.StatisticRank3Fragment;


public class StatisticRank2PagerAdapter extends FragmentStatePagerAdapter {
    private final int ITEM_NUM = 5;
    private int raidId;
    private int bossPosition;
    private boolean isAverageMode;

    public StatisticRank2PagerAdapter(@NonNull FragmentManager fragmentManager) {
        super(fragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    public void setData(int raidId, int bossPosition, boolean isAverageMode){
        this.raidId = raidId;
        this.bossPosition = bossPosition;
        this.isAverageMode = isAverageMode;
    }

    @NonNull
    @Override
    public Fragment getItem(int levelPosition) {
        return StatisticRank3Fragment.newInstance(raidId, bossPosition, levelPosition, isAverageMode);
    }

    @Override
    public int getCount() {
        return ITEM_NUM;
    }
}
