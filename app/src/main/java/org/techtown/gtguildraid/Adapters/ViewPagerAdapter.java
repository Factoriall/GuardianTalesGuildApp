package org.techtown.gtguildraid.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.techtown.gtguildraid.Fragments.CardFragment;
import org.techtown.gtguildraid.Fragments.RecordOverallFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int CARD_ITEM_SIZE = 15;
    private int memberId;
    private int raidId;
    private boolean isChecked;

    public void setData(int memberId, int raidId, Boolean isChecked){
        this.memberId = memberId;
        this.raidId = raidId;
        this.isChecked = isChecked;
    }

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position != 0)
            return CardFragment.newInstance(position, memberId, raidId, isChecked);
        else
            return RecordOverallFragment.newInstance(memberId, raidId);
    }

    @Override
    public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}
