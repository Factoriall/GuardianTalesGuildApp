package org.techtown.gtguildraid.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.angmarch.views.NiceSpinner;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.RecordPagerAdapter;
import org.techtown.gtguildraid.adapters.StatisticMemberBasicPagerAdapter;
import org.techtown.gtguildraid.adapters.StatisticMemberLeaderAdapter;
import org.techtown.gtguildraid.models.GuildMember;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.models.Record;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.core.content.ContextCompat.getColor;

public class StatisticMemberBasicFragment extends Fragment {
    static final int VIEWPAGER_NUM = 15;
    static final int DAY_IN_SECONDS = 1000 * 3600 * 24;
    static int raidId;
    static int memberId;

    ViewPager2 viewPager;
    TabLayout tabLayout;
    RoomDB database;
    Raid raid;
    StatisticMemberBasicPagerAdapter vAdapter;

    StatisticMemberBasicFragment(int raidId, int memberId){
        this.raidId = raidId;
        this.memberId = memberId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_member_basic, container, false);
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);

        database = RoomDB.getInstance(getActivity());
        raid = database.raidDao().getRaid(raidId);

        vAdapter = new StatisticMemberBasicPagerAdapter(getChildFragmentManager(), getLifecycle());
        setViewPager(getIntegerFromToday());

        new TabLayoutMediator(tabLayout, viewPager, true,
                (tab, position) -> {
                    if(position != 0)
                        tab.setText("Day " + position + "\n" + getRaidDate(position));
                    else
                        tab.setText("전체 기록");
                }
        ).attach();

        viewPager.setUserInputEnabled(false);

        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        for(int i=getIntegerFromToday() + 2; i<VIEWPAGER_NUM; i++){
            tabStrip.getChildAt(i).setBackgroundColor(Color.GRAY);
            tabStrip.getChildAt(i).setOnTouchListener((v, event) -> true);
        }

        return view;
    }

    private void setViewPager(int day) {
        vAdapter.setData(raidId, memberId);
        viewPager.setAdapter(vAdapter);
        viewPager.setCurrentItem(0, false);
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
}
