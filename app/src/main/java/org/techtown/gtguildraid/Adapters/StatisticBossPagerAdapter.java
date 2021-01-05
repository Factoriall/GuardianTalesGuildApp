package org.techtown.gtguildraid.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.techtown.gtguildraid.Fragments.StatisticBoss2Fragment;


public class StatisticBossPagerAdapter extends FragmentStatePagerAdapter {
    private static final int ITEM_SIZE = 4;
    private int raidId;

    public void setData(int raidId){
        this.raidId = raidId;
    }

    public StatisticBossPagerAdapter(@NonNull FragmentManager fragmentManager) {
        super(fragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return StatisticBoss2Fragment.newInstance(position, raidId);
    }

    @Override
    public int getCount() {
        return ITEM_SIZE;
    }
}
