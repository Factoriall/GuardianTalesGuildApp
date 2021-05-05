package org.techtown.gtguildraid.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.techtown.gtguildraid.fragments.StatisticMemberBasic2Fragment;

public class StatisticMemberBasicPagerAdapter extends FragmentStateAdapter {
    private static final int CARD_ITEM_SIZE = 15;
    private int raidId;
    private int memberId;

    public void setData(int raidId, int memberId){
        this.raidId = raidId;
        this.memberId = memberId;
    }

    public StatisticMemberBasicPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return StatisticMemberBasic2Fragment.newInstance(position, raidId, memberId);
    }

    @Override
    public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}
