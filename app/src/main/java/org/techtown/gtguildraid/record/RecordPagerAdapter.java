package org.techtown.gtguildraid.record;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.techtown.gtguildraid.record.RecordMemberFragment;

public class RecordPagerAdapter extends FragmentStateAdapter {
    private static final int CARD_ITEM_SIZE = 14;
    private int raidId;

    public void setData(int raidId){
        this.raidId = raidId;
    }

    public RecordPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return RecordMemberFragment.newInstance(position, raidId);
    }

    @Override
    public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}
