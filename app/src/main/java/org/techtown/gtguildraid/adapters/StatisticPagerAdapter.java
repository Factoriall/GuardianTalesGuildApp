package org.techtown.gtguildraid.adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.techtown.gtguildraid.fragments.StatisticBossFragment;
import org.techtown.gtguildraid.fragments.StatisticMemberFragment;
import org.techtown.gtguildraid.fragments.StatisticRankFragment;

public class StatisticPagerAdapter extends FragmentStateAdapter {
    private static final int ITEM_SIZE = 3;

    private static final int RANK = 0;
    private static final int MEMBER = 1;
    private static final int BOSS = 2;

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
        Log.d("statisticFragment", "" + position);
        switch(position) {
            case RANK:
                return StatisticRankFragment.newInstance(raidId);
            case MEMBER:
                return StatisticMemberFragment.newInstance(raidId);
            case BOSS:
                return StatisticBossFragment.newInstance(raidId);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return ITEM_SIZE;
    }
}
