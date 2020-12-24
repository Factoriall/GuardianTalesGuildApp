package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.util.Log;
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
import org.angmarch.views.OnSpinnerItemSelectedListener;
import org.techtown.gtguildraid.Adapters.StatisticPagerAdapter;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StatisticFragment extends Fragment {
    final private String dateFormat = "yyyy-MM-dd";

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
        Log.d("selectIndex", (String) spinner.getSelectedItem());

        spinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                Log.d("spinner position", Integer.toString(position));
                if (position != 0)
                    setView(raids.get(position - 1));
            }
        });

        return view;
    }

    private void setView(Raid raid) {
        Log.d("raidInfo", raid.getName());
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);

        TextView raidTerm = view.findViewById(R.id.raidTerm);

        raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
                new SimpleDateFormat(dateFormat).format(raid.getEndDay())));

        sAdapter = new StatisticPagerAdapter(getChildFragmentManager(), getLifecycle());

        setViewPager(raid);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if(position == 0)
                tab.setText("개인별 기록");
            else if(position == 1)
                tab.setText("보스별 기록");
            else
                tab.setText("순위표");
        }).attach();
        viewPager.setUserInputEnabled(false);
    }

    private void setViewPager(Raid raid) {
        sAdapter.setData(raid.getRaidId());
        viewPager.setAdapter(sAdapter);
    }
}
