package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.android.material.tabs.TabLayout;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import org.techtown.gtguildraid.Adapters.StatisticMember2PagerAdapter;
import org.techtown.gtguildraid.Adapters.StatisticRank2PagerAdapter;
import org.techtown.gtguildraid.Etc.CustomViewPager;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

public class StatisticRank2Fragment extends Fragment {
    RoomDB database;
    private int bossPosition;
    private int raidId;
    boolean isAverageMode;
    boolean isAdjustMode;

    public static StatisticRank2Fragment newInstance(int position, int raidId) {
        StatisticRank2Fragment fragment = new StatisticRank2Fragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);
        args.putInt("position", position);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_rank_2, container, false);
        database = RoomDB.getInstance(getActivity());
        ToggleSwitch toggleSwitch = view.findViewById(R.id.toggleSwitch);
        Switch adjustSwitch = view.findViewById(R.id.adjustSwitch);
        toggleSwitch.setCheckedPosition(0);
        isAverageMode = false;
        isAdjustMode = adjustSwitch.isChecked();

        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
            bossPosition = getArguments().getInt("position");
        }

        if(bossPosition == 0)
            adjustSwitch.setVisibility(View.VISIBLE);
        else
            adjustSwitch.setVisibility(View.GONE);

        String[] tabStringArray = new String[5];
        tabStringArray[0] = "전체";
        int level = 66;
        for(int i=1; i<4; i++){
            tabStringArray[i] = "Lv." + level + "↑";
            level += 5;
        }
        tabStringArray[4] =  "Lv.80";

        CustomViewPager viewPager = view.findViewById(R.id.viewPager);
        SlidingTabLayout tabLayout = view.findViewById(R.id.slidingTabLayout);

        StatisticRank2PagerAdapter adapter
                = new StatisticRank2PagerAdapter(getChildFragmentManager());

        adapter.setData(raidId, bossPosition, isAverageMode, isAdjustMode);
        viewPager.setPagingEnabled(false);
        viewPager.setAdapter(adapter);

        viewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setAlpha(0f);
                page.setVisibility(View.VISIBLE);

                // Start Animation for a short period of time
                page.animate()
                        .alpha(1f)
                        .setDuration(page.getResources().getInteger(android.R.integer.config_shortAnimTime));
            }
        });

        tabLayout.setViewPager(viewPager, tabStringArray);


        adjustSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int current = viewPager.getCurrentItem();
                isAdjustMode = b;
                adapter.setData(raidId, bossPosition, isAverageMode, isAdjustMode);
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(current, false);
            }
        });

        //toggleSwitch 설정
        toggleSwitch.setOnChangeListener(new ToggleSwitch.OnChangeListener() {
            @Override
            public void onToggleSwitchChanged(int i) {
                int current = viewPager.getCurrentItem();
                isAverageMode = i == 1;
                adapter.setData(raidId, bossPosition, isAverageMode, isAdjustMode);
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(current, false);
            }
        });


        return view;
    }
}
