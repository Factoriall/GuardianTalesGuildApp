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

import org.techtown.gtguildraid.Adapters.StatisticMember2PagerAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.List;

public class StatisticMember2Fragment extends Fragment {
    RoomDB database;
    int raidId;
    int memberId;
    boolean isAdjustMode;

    public StatisticMember2Fragment() {
        // Required empty public constructor
    }

    public static StatisticMember2Fragment newInstance(int memberId, int raidId) {
        StatisticMember2Fragment fragment = new StatisticMember2Fragment();
        Bundle args = new Bundle();
        args.putInt("memberId", memberId);
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_member_2, container, false);
        database = RoomDB.getInstance(getActivity());
        Switch adjustSwitch = view.findViewById(R.id.adjustSwitch);
        isAdjustMode = adjustSwitch.isChecked();
        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
            memberId = getArguments().getInt("memberId");
        }

        //TabLayout 설정
        List<Boss> bossList = database.raidDao().getBossesList(raidId);
        String[] tabStringArray = new String[5];
        tabStringArray[0] = "전체";
        for(int i=1; i<5; i++){
            if(bossList.get(i-1).getName().length() > 2)
                tabStringArray[i] = bossList.get(i-1).getName().substring(0,2);
            else
                tabStringArray[i] = bossList.get(i-1).getName();
        }

        ArrayList<Fragment> fragments = new ArrayList<>();
        for(int i=0; i<5; i++){
            fragments.add(new StatisticMember3Fragment());
        }

        ViewPager viewPager = view.findViewById(R.id.viewPager);
        SlidingTabLayout tabLayout = view.findViewById(R.id.slidingTabLayout);

        StatisticMember2PagerAdapter adapter = new StatisticMember2PagerAdapter(getChildFragmentManager());

        adapter.setData(memberId, raidId, isAdjustMode);
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

        //adjustSwitch 설정
        adjustSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isAdjustMode != isChecked) {
                    isAdjustMode = isChecked;
                    adapter.setData(memberId, raidId, isAdjustMode);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        tabLayout.setViewPager(viewPager, tabStringArray);

        return view;
    }
}
