package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import java.util.Date;
import java.util.List;

public class RecordFragment extends Fragment {
    final private String dateFormat = "yyyy-MM-dd";
    Raid raid;
    List<GuildMember> members;
    RoomDB database;
    TabLayout tabLayout;
    ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = (ViewGroup) inflater.inflate(R.layout.fragment_record, container, false);
        database = RoomDB.getInstance(getActivity());

        TextView raidName = view.findViewById(R.id.raidName);
        TextView raidTerm = view.findViewById(R.id.raidTerm);
        Spinner nSpinner = view.findViewById(R.id.nickname);

        raid = database.raidDao().getCurrentRaid(new Date());
        members = database.memberDao().getCurrentMembers();
        members.add(database.memberDao().getMe());

        raidName.setText(raid.getName());
        raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) +"~" +
                new SimpleDateFormat(dateFormat).format(raid.getEndDay())));

        ArrayAdapter<GuildMember> adapter = new ArrayAdapter<GuildMember>(
                getContext(), android.R.layout.simple_spinner_item, members);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        nSpinner.setAdapter(adapter);

        final int[] memberId = new int[1];
        nSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                memberId[0] = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tabs);
        viewPager.setAdapter(createCardAdapter());

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText("Day " + (position + 1) + "\n11/21")).attach();

        viewPager.setCurrentItem(3, true);

        return view;
    }

    private ViewPagerAdapter createCardAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity());
        return adapter;
    }
}
