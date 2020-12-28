package org.techtown.gtguildraid.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.techtown.gtguildraid.Fragments.StatisticBoss2Fragment;


public class StatisticBossPagerAdapter extends FragmentStatePagerAdapter {
    private static final int ITEM_SIZE = 4;
    private int raidId;
    private boolean isChecked;

    public void setData(int raidId, boolean isChecked){
        this.raidId = raidId;
        this.isChecked = isChecked;
    }

    public StatisticBossPagerAdapter(@NonNull FragmentManager fragmentManager) {
        super(fragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return StatisticBoss2Fragment.newInstance(position, raidId, isChecked);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return ITEM_SIZE;
    }
}
