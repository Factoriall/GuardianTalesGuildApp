package org.techtown.gtguildraid.Adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.techtown.gtguildraid.Fragments.StatisticMember3Fragment;

public class StatisticMember2PagerAdapter extends FragmentStatePagerAdapter {
    private static final int ITEM_SIZE = 5;
    private int memberId;
    private int raidId;
    private boolean isChecked;

    public void setData(int memberId, int raidId, Boolean isChecked){
        Log.d("setData", Boolean.toString(isChecked));
        this.memberId = memberId;
        this.raidId = raidId;
        this.isChecked = isChecked;
    }

    public StatisticMember2PagerAdapter(@NonNull FragmentManager fragmentManager) {
        super(fragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Log.d("getItem", Integer.toString(memberId));
        return StatisticMember3Fragment.newInstance(position, memberId, raidId, isChecked);
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
