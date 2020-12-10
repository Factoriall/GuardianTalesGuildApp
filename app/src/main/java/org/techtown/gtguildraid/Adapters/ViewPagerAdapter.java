package org.techtown.gtguildraid.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.techtown.gtguildraid.Fragments.CardFragment;
import org.techtown.gtguildraid.Fragments.RecordOverallFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int CARD_ITEM_SIZE = 15;
    private int memberId;
    private int raidId;

    public void setData(int memberId, int raidId){
        this.memberId = memberId;
        this.raidId = raidId;
    }

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position != 0)
            return CardFragment.newInstance(position, memberId, raidId);
        else
            return RecordOverallFragment.newInstance(memberId, raidId);
    }

    @Override
    public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}
