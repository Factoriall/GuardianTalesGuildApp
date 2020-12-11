package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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
        Spinner nSpinner = view.findViewById(R.id.nickname);

        raid = database.raidDao().getCurrentRaid(new Date());
        members = database.memberDao().getCurrentMembers();

        raidName.setText(raid.getName());
        raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) +"~" +
                new SimpleDateFormat(dateFormat).format(raid.getEndDay())));

        memberSpinner.clear();
        memberId.clear();
        for(GuildMember m : members){
            memberSpinner.add(m.getName());
            memberId.add(m.getID());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, memberSpinner);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tabs);
        adjustSwitch = view.findViewById(R.id.adjustSwitch);
        isAdjustMode = adjustSwitch.isChecked();

        nSpinner.setAdapter(adapter);

        vAdapter = new ViewPagerAdapter(getActivity());
        nSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(sMemberIdx != i) {
                    sMemberIdx = i;
                    setViewPager(isAdjustMode, getIntegerFromToday() + 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        setViewPager(isAdjustMode, getIntegerFromToday() + 1);

        new TabLayoutMediator(tabLayout, viewPager, true, true, (tab, position) -> {
            if(position != 0)
                tab.setText("Day " + position + "\n" + getRaidDate(position - 1));
            else
                tab.setText("전체 기록");
        }).attach();

        adjustSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isAdjustMode = isChecked;
                setViewPager(isChecked, tabLayout.getSelectedTabPosition());
            }
        });

        return view;
    }

    private void setViewPager(Boolean isChecked, int day){
        vAdapter.setData(memberId.get(sMemberIdx), raid.getRaidId(), isChecked);
        viewPager.setAdapter(vAdapter);
        viewPager.setCurrentItem(day, false);
    }

    private int getIntegerFromToday() {
        Date today = new Date();
        Date startDate = raid.getStartDay();

        int differentDays = (int)((today.getTime() - startDate.getTime()) / (1000 * 3600 * 24));

        if(differentDays < 0)
            return 0;
        else
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
