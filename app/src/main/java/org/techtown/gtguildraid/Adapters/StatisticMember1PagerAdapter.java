package org.techtown.gtguildraid.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.techtown.gtguildraid.Fragments.StatisticMember2Fragment;

public class StatisticMember1PagerAdapter extends FragmentStateAdapter {
    private int memberNumber;
    private int memberId;
    private int raidId;

    public void setData(int memberId, int raidId){
        this.memberId = memberId;
        this.raidId = raidId;
    }

    public void setMemberNumber(int number){this.memberNumber = number;}

    public StatisticMember1PagerAdapter(@NonNull FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return StatisticMember2Fragment.newInstance(memberId, raidId);
    }

    @Override
    public int getItemCount() {
        return memberNumber;
    }
}
