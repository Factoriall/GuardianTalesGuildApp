package org.techtown.gtguildraid.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.RecordPagerAdapter;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RecordFragment extends Fragment {
    final int VIEWPAGER_NUM = 14;
    final int DAY_IN_SECONDS = 1000 * 3600 * 24;

    RoomDB database;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    RecordPagerAdapter vAdapter;

    Raid raid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        TextView raidName = view.findViewById(R.id.raidName);
        TextView raidTerm = view.findViewById(R.id.raidTerm);
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);

        database = RoomDB.getInstance(getActivity());
        raid = database.raidDao().getCurrentRaid(new Date());

        SharedPreferences pref = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        showToast("최근 기록: " + pref.getString("recentWrite" + raid.getRaidId(), "없음"));

        raidName.setText(raid.getName());
        String dateFormat = "yy/MM/dd";
        raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
                new SimpleDateFormat(dateFormat).format(adjustEndTime(raid.getEndDay()))));

        vAdapter = new RecordPagerAdapter(getChildFragmentManager(), getLifecycle());
        setViewPager(getIntegerFromToday());

        new TabLayoutMediator(tabLayout, viewPager, true, (tab, position) -> tab.setText("Day " + (position + 1) + "\n" + getRaidDate(position))).attach();

        viewPager.setUserInputEnabled(false);

        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        for(int i=getIntegerFromToday()+1; i<VIEWPAGER_NUM; i++){
            tabStrip.getChildAt(i).setBackgroundColor(Color.GRAY);
            tabStrip.getChildAt(i).setOnTouchListener((v, event) -> true);
        }

        return view;
    }

    private Date adjustEndTime(Date endDay) {
        Calendar end = Calendar.getInstance();
        end.setTime(endDay);
        end.add(Calendar.DATE, -2);

        return end.getTime();
    }

    private void setViewPager(int day) {
        vAdapter.setData(raid.getRaidId());
        viewPager.setAdapter(vAdapter);
        viewPager.setCurrentItem(day, false);
    }

    private int getIntegerFromToday() {
        Date today = new Date();
        Date startDate = raid.getStartDay();

        int differentDays = (int) ((today.getTime() - startDate.getTime()) / DAY_IN_SECONDS);

        return Math.max(differentDays, 0);
    }

    private String getRaidDate(int position) {
        Date startDate = raid.getStartDay();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.DATE, position);

        return new SimpleDateFormat("MM/dd").format(cal.getTime());
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
