package org.techtown.gtguildraid.Adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.techtown.gtguildraid.Fragments.RecordCardFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int CARD_ITEM_SIZE = 14;
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
        Log.d("createFragment", Integer.toString(memberId));
        return RecordCardFragment.newInstance(position, memberId, raidId, isChecked);
    }

    @Override
    public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}
