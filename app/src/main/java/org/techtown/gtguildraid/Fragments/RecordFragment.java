package org.techtown.gtguildraid.Fragments;

import android.graphics.Color;
import android.os.Bundle;
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
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RecordFragment extends Fragment {
    final int VIEWPAGER_NUM = 14;
    final int MAX_RECORDS = 3;
    final int DAY_IN_SECONDS = 1000 * 3600 * 24;
    final private String dateFormat = "yyyy-MM-dd";

    RoomDB database;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter vAdapter;
    Switch adjustSwitch;
    NiceSpinner memberSpinner;

    Raid raid;
    List<GuildMember> members;

    private Boolean isAdjustMode = true;
    private int sMemberIdx = 0;

    private class MemberForSpinner implements Comparable<MemberForSpinner>{
        private String name;
        private int id;
        private int todayRemain;

        public MemberForSpinner(String name, int id, int todayRemain){
            this.name = name;
            this.id = id;
            this.todayRemain = todayRemain;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public int getTodayRemain() {
            return todayRemain;
        }

        public void setTodayRemain(int todayRemain) {
            this.todayRemain = todayRemain;
        }

        @Override
        public int compareTo(MemberForSpinner member) {
            return member.getTodayRemain() - todayRemain;
        }
    }


    List<MemberForSpinner> memberList = new ArrayList<>();

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
        memberSpinner = view.findViewById(R.id.nickname);
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);
        adjustSwitch = view.findViewById(R.id.adjustSwitch);
        isAdjustMode = adjustSwitch.isChecked();

        database = RoomDB.getInstance(getActivity());
        raid = database.raidDao().getCurrentRaid(new Date());
        members = database.memberDao().getCurrentMembers();

        raidName.setText(raid.getName());
        raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
                new SimpleDateFormat(dateFormat).format(raid.getEndDay())));

        setMemberSpinner();

        vAdapter = new ViewPagerAdapter(getChildFragmentManager(), getLifecycle());
        memberSpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                if (sMemberIdx != position) {
                    //refreshSpinnerItem(sMemberIdx);//spinner
                    sMemberIdx = position;
                    setViewPager(isAdjustMode, getIntegerFromToday());
                }
            }
        });

        setViewPager(isAdjustMode, getIntegerFromToday());

        new TabLayoutMediator(tabLayout, viewPager, true, (tab, position) -> {
            tab.setText("Day " + (position + 1) + "\n" + getRaidDate(position));
        }).attach();

        viewPager.setUserInputEnabled(false);

        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        for(int i=getIntegerFromToday()+1; i<VIEWPAGER_NUM; i++){
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
            }
        });

        return view;
    }

    private void setMemberSpinner() {
        memberList.clear();
        for (GuildMember m : members) {
            memberList.add(new MemberForSpinner(m.getName(), m.getID(),
                    getRemainedRecord(m.getID(), raid.getRaidId())));
        }

        attachDataToSpinner();
    }

    private void refreshSpinnerItem(int rIdx) {
        MemberForSpinner rMember = memberList.get(rIdx);
        rMember.setTodayRemain(getRemainedRecord(database.memberDao()
                .getMember(rMember.getId()).getID(), raid.getRaidId()));
        memberList.set(rIdx, rMember);

        attachDataToSpinner();
    }

    private void attachDataToSpinner(){
        Collections.sort(memberList);

        List<String> memberNameList = new ArrayList<>();
        for(MemberForSpinner m : memberList){
            memberNameList.add(m.getName() + " - " + m.getTodayRemain());
        }

        memberSpinner.attachDataSource(memberNameList);

        if(memberNameList.size() == 1){
            memberSpinner.setText(memberNameList.get(0));
        }
    }

    private void setViewPager(Boolean isChecked, int day) {
        if(sMemberIdx < memberList.size())
            vAdapter.setData(memberList.get(sMemberIdx).getId(), raid.getRaidId(), isChecked);
        else
            vAdapter.setData(memberList.get(0).getId(), raid.getRaidId(), isChecked);
        viewPager.setAdapter(vAdapter);
        viewPager.setCurrentItem(day, false);
    }

    private int getIntegerFromToday() {
        Date today = new Date();
        Date startDate = raid.getStartDay();

        int differentDays = (int) ((today.getTime() - startDate.getTime()) / DAY_IN_SECONDS);

        if (differentDays < 0)
            return -1;
        return differentDays;
    }

    private int getRemainedRecord(int id, int raidId) {
        List<Record> recordList = database.recordDao()
                .getCertainDayRecords(id, raidId, getIntegerFromToday() + 1);

        return MAX_RECORDS - recordList.size();
    }

    private String getRaidDate(int position) {
        Date startDate = raid.getStartDay();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.DATE, position);

        return new SimpleDateFormat("MM/dd").format(cal.getTime());
    }
}
