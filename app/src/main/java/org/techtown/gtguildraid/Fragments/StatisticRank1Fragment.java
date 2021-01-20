package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.techtown.gtguildraid.Adapters.StatisticRank1PagerAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.List;

public class StatisticRank1Fragment extends Fragment {
    int raidId;
    RoomDB database;
    ViewPager2 viewPager;
    TabLayout tabLayout;

    public static Fragment newInstance(int raidId) {
        StatisticRank1Fragment fragment = new StatisticRank1Fragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_rank, container, false);

        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }

        database = RoomDB.getInstance(getActivity());
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);

        List<Boss> bossesInRaid = new ArrayList<>();

        for (Boss b : database.raidDao().getBossesList(raidId)) {
            bossesInRaid.add(b);
        }

        StatisticRank1PagerAdapter adapter =
                new StatisticRank1PagerAdapter(getChildFragmentManager(), getLifecycle());

        adapter.setData(raidId);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, false, false, (tab, position) -> {
            if (position == 0)
                tab.setText("전체");
            else
                tab.setText(bossesInRaid.get(position - 1).getName());
        }).attach();

        viewPager.setUserInputEnabled(false);

        return view;
    }
}
