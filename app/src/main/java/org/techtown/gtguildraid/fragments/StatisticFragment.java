package org.techtown.gtguildraid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.angmarch.views.NiceSpinner;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.StatisticPagerAdapter;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class StatisticFragment extends Fragment {
    View view;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    StatisticPagerAdapter sAdapter;

    RoomDB database;

    List<Raid> raids = new ArrayList<>();
    List<String> raidNameList = new LinkedList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistic, container, false);

        database = RoomDB.getInstance(getActivity());

        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.viewPager);
        NiceSpinner spinner = view.findViewById(R.id.raidName);

        raids = database.raidDao().getAllRaids();

        raidNameList.clear();
        raidNameList.add("[레이드 선택]");
        for (Raid r : raids) {
            raidNameList.add(r.getName());
        }

        spinner.attachDataSource(raidNameList);
        spinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            if (position != 0)
                setView(raids.get(position - 1));
        });

        if(database.raidDao().isStartedRaidExist(new Date())) {
            spinner.setSelectedIndex(raidNameList.size() - 1);
            setView(raids.get(raids.size() - 1));
        }
        else if(raids.size() >= 2){
            spinner.setSelectedIndex(raidNameList.size() - 2);
            setView(raids.get(raids.size() - 2));
        }

        return view;
    }

    private void setView(Raid raid) {
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);

        TextView raidTerm = view.findViewById(R.id.raidTerm);

        String dateFormat = "yy/MM/dd";
        raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
                new SimpleDateFormat(dateFormat).format(getEndTime(raid.getStartDay()))));

        sAdapter = new StatisticPagerAdapter(getChildFragmentManager(), getLifecycle());
        sAdapter.setData(raid.getRaidId());
        viewPager.setAdapter(sAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if(position == 0)
                tab.setText("순위표");
            else if(position == 1)
                tab.setText("개인별 기록");
            else if(position == 2)
                tab.setText("보스별 기록");
        }).attach();
        viewPager.setUserInputEnabled(false);
    }

    private Date getEndTime(Date day) {
        Calendar end = Calendar.getInstance();
        end.setTime(day);
        end.add(Calendar.DATE, 13);

        return end.getTime();
    }
}
