package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.angmarch.views.NiceSpinner;
import org.techtown.gtguildraid.Adapters.StatisticMember1PagerAdapter;
import org.techtown.gtguildraid.Adapters.StatisticMember2PagerAdapter;
import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.List;

public class StatisticMember1Fragment extends Fragment {
    RoomDB database;
    NiceSpinner memberSpinner;
    StatisticMember2PagerAdapter adapter;
    ViewPager2 viewPager;
    TabLayout tabLayout;

    int memberId;
    int raidId;
    boolean isAdjustMode;
    int sMemberIdx;

    List<GuildMember> members = new ArrayList<>();

    public StatisticMember1Fragment() {
        // Required empty public constructor
    }

    public static StatisticMember1Fragment newInstance(int raidId) {
        StatisticMember1Fragment fragment = new StatisticMember1Fragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_member, container, false);

        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }


        database = RoomDB.getInstance(getActivity());
        Log.d("raidInfo2", database.raidDao().getRaid(raidId).getName());

        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);

        List<GuildMember> membersInRaid = new ArrayList<>();
        for(GuildMember m : database.memberDao().getAllMembers()){
            if(database.recordDao().getCertainMemberRecords(m.getID(), raidId).size() != 0)
                membersInRaid.add(m);
        }

        if(membersInRaid.isEmpty()) {
            return view;
        }

        StatisticMember1PagerAdapter adapter
                = new StatisticMember1PagerAdapter(getChildFragmentManager(), getLifecycle());

        adapter.setMemberNumber(membersInRaid.size());
        sMemberIdx = 0;
        adapter.setData(membersInRaid.get(sMemberIdx).getID(), raidId);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, false, false, (tab, position) -> {
            tab.setText(membersInRaid.get(position).getName());
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(sMemberIdx != tabLayout.getSelectedTabPosition()) {
                    sMemberIdx = tabLayout.getSelectedTabPosition();
                    adapter.setData(membersInRaid.get(sMemberIdx).getID(), raidId);
                    viewPager.setAdapter(adapter);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.setUserInputEnabled(false);


        return view;
    }
}
