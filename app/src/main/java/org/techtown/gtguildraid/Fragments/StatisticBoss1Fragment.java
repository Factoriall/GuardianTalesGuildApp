package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.SlidingTabLayout;

import org.techtown.gtguildraid.Adapters.StatisticBossPagerAdapter;
import org.techtown.gtguildraid.Etc.CustomViewPager;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.List;

public class StatisticBoss1Fragment extends Fragment {
    RoomDB database;
    int raidId;

    public StatisticBoss1Fragment() {
        // Required empty public constructor
    }

    public static StatisticBoss1Fragment newInstance(int raidId) {
        StatisticBoss1Fragment fragment = new StatisticBoss1Fragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_boss_1, container, false);
        database = RoomDB.getInstance(getActivity());
        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }

        //TabLayout 설정
        List<Boss> bossList = database.raidDao().getBossesList(raidId);
        String[] tabStringArray = new String[4];
        for(int i=0; i<4; i++){
            if(bossList.get(i).getName().length() > 5)
                tabStringArray[i] = bossList.get(i).getName().substring(0, 5) + "..";
            else
                tabStringArray[i] = bossList.get(i).getName();
        }

        CustomViewPager viewPager = view.findViewById(R.id.viewPager);
        SlidingTabLayout tabLayout = view.findViewById(R.id.slidingTabLayout);

        StatisticBossPagerAdapter adapter = new StatisticBossPagerAdapter(getChildFragmentManager());

        adapter.setData(raidId);

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

        return view;
    }
}
