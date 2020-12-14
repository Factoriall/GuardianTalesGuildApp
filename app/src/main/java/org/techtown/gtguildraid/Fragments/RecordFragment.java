package org.techtown.gtguildraid.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;
import org.techtown.gtguildraid.Adapters.ViewPagerAdapter;
import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecordFragment extends Fragment {
    final int VIEWPAGER_NUM = 15;
    final private String dateFormat = "yyyy-MM-dd";

    RoomDB database;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter vAdapter;
    Switch adjustSwitch;

    Raid raid;
    List<GuildMember> members;
    List<String> memberSpinner = new ArrayList<>();
    List<Integer> memberId = new ArrayList<>();

    private Boolean isAdjustMode = true;
    private int sMemberIdx = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        database = RoomDB.getInstance(getActivity());

        TextView raidName = view.findViewById(R.id.raidName);
        TextView raidTerm = view.findViewById(R.id.raidTerm);
        NiceSpinner nSpinner = view.findViewById(R.id.nickname);

        raid = database.raidDao().getCurrentRaid(new Date());
        members = database.memberDao().getCurrentMembers();

        raidName.setText(raid.getName());
        raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
                new SimpleDateFormat(dateFormat).format(raid.getEndDay())));

        memberSpinner.clear();
        memberId.clear();
        for (GuildMember m : members) {
            memberSpinner.add(m.getName());
            memberId.add(m.getID());
        }

        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);
        adjustSwitch = view.findViewById(R.id.adjustSwitch);
        isAdjustMode = adjustSwitch.isChecked();

        nSpinner.attachDataSource(memberSpinner);
        if(memberSpinner.size() == 1){
            nSpinner.setText(memberSpinner.get(0));
        }

        vAdapter = new ViewPagerAdapter(getChildFragmentManager(), getLifecycle());
        nSpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                if (sMemberIdx != position) {
                    Log.d("setViewPager", "nSpinner");
                    sMemberIdx = position;
                    setViewPager(isAdjustMode, getIntegerFromToday() + 1);
                }
            }
        });

        setViewPager(isAdjustMode, getIntegerFromToday() + 1);

        new TabLayoutMediator(tabLayout, viewPager, true, (tab, position) -> {
            if (position != 0)
                tab.setText("Day " + position + "\n" + getRaidDate(position - 1));
            else
                tab.setText("전체 기록");
        }).attach();

        viewPager.setUserInputEnabled(false);

        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        for(int i=getIntegerFromToday()+2; i<VIEWPAGER_NUM; i++){
            tabStrip.getChildAt(i).setBackgroundColor(Color.GRAY);
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

        adjustSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isAdjustMode = isChecked;
                setViewPager(isChecked, tabLayout.getSelectedTabPosition());
                Log.d("setViewPager", "adjust");
            }
        });

        return view;
    }

    private void setViewPager(Boolean isChecked, int day) {
        vAdapter.setData(memberId.get(sMemberIdx), raid.getRaidId(), isChecked);
        viewPager.setAdapter(vAdapter);
        viewPager.setCurrentItem(day, false);
    }

    private int getIntegerFromToday() {
        Date today = new Date();
        Date startDate = raid.getStartDay();

        int differentDays = (int) ((today.getTime() - startDate.getTime()) / (1000 * 3600 * 24));

        if (differentDays < 0)
            return -1;
        return differentDays;
    }

    private String getRaidDate(int position) {
        Date startDate = raid.getStartDay();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.DATE, position);

        return new SimpleDateFormat("MM/dd").format(cal.getTime());
    }
}
