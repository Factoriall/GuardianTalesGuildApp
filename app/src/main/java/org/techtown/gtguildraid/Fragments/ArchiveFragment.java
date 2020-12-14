package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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
import java.util.LinkedList;
import java.util.List;

public class ArchiveFragment extends Fragment {
    final private String dateFormat = "yyyy-MM-dd";

    View view;
    CardView switchCard;
    CardView membersCard;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter vAdapter;

    RoomDB database;

    List<Raid> pastRaids = new ArrayList<>();
    List<String> raidNameList = new LinkedList<>();
    List<Integer> raidIdList = new ArrayList<>();
    List<String> memberSpinner = new ArrayList<>();
    List<Integer> memberId = new ArrayList<>();

    private int sMemberIdx = 0;
    private boolean isAdjustMode = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_archive, container, false);

        database = RoomDB.getInstance(getActivity());

        switchCard = view.findViewById(R.id.switchCard);
        membersCard = view.findViewById(R.id.membersCard);
        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.viewpager);
        NiceSpinner spinner = view.findViewById(R.id.raidName);

        pastRaids = database.raidDao().getPastRaids(new Date());

        raidNameList.clear();
        raidIdList.clear();
        raidNameList.add("[과거 레이드 선택]");
        for (Raid r : pastRaids) {
            raidNameList.add(r.getName());
            raidIdList.add(r.getRaidId());
        }

        spinner.attachDataSource(raidNameList);
        Log.d("selectIndex", (String) spinner.getSelectedItem());

        spinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                Log.d("spinner position", Integer.toString(position));
                if(position != 0)
                    setView(raidIdList.get(position - 1));
            }
        });

        return view;
    }

    private void setView(int raidId) {
        switchCard.setVisibility(View.VISIBLE);
        membersCard.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);

        TextView raidTerm = view.findViewById(R.id.raidTerm);
        NiceSpinner nSpinner = view.findViewById(R.id.nickname);
        Switch adjustSwitch = view.findViewById(R.id.adjustSwitch);

        Raid raid = database.raidDao().getRaid(raidId);
        List<GuildMember> members = database.memberDao().getAllMembers();
        isAdjustMode = adjustSwitch.isChecked();

        raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
                new SimpleDateFormat(dateFormat).format(raid.getEndDay())));

        memberSpinner.clear();
        memberId.clear();
        for (GuildMember m : members) {
            if(database.recordDao().getCertainMemberRecords(m.getID(), raidId).size() != 0) {
                memberSpinner.add(m.getName());
                memberId.add(m.getID());
            }
        }

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
                    setViewPager(raid, isAdjustMode, 0);
                }
            }
        });

        setViewPager(raid, isAdjustMode, 0);

        new TabLayoutMediator(tabLayout, viewPager, true, true, (tab, position) -> {
            if (position != 0)
                tab.setText("Day " + position + "\n" + getRaidDate(raid, position - 1));
            else
                tab.setText("전체 기록");
        }).attach();

        adjustSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isAdjustMode = isChecked;
                setViewPager(raid, isChecked, tabLayout.getSelectedTabPosition());
                Log.d("setViewPager", "adjust");
            }
        });
    }

    private void setViewPager(Raid raid, Boolean isChecked, int day) {
        vAdapter.setData(memberId.get(sMemberIdx), raid.getRaidId(), isChecked);
        viewPager.setAdapter(vAdapter);
        viewPager.setCurrentItem(day, false);
    }

    private String getRaidDate(Raid raid, int position) {
        Date startDate = raid.getStartDay();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.DATE, position);

        return new SimpleDateFormat("MM/dd").format(cal.getTime());
    }
}
