package org.techtown.gtguildraid.record;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.common.CalculateFormatHelper;
import org.techtown.gtguildraid.models.entities.Boss;
import org.techtown.gtguildraid.models.entities.Raid;
import org.techtown.gtguildraid.models.entities.Record;
import org.techtown.gtguildraid.repository.RoomDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecordFragment extends Fragment {
    final int VIEWPAGER_NUM = 14;
    final int DAY_IN_SECONDS = 1000 * 3600 * 24;

    RoomDB database;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    RecordPagerAdapter vAdapter;
    private Toast myToast;

    Raid raid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        TextView raidName = view.findViewById(R.id.raidName);
        TextView raidTerm = view.findViewById(R.id.raidTerm);
        ImageView raidThumbnail = view.findViewById(R.id.raidThumbnail);
        FloatingActionButton checkFab = view.findViewById(R.id.fabCheck);
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);

        database = RoomDB.getInstance(getActivity());
        raid = database.raidDao().getCurrentRaid(new Date());

        CalculateFormatHelper cHelper = new CalculateFormatHelper();

        SharedPreferences pref = requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);

        raidName.setText(raid.getName());
        String dateFormat = "yy/MM/dd";
        raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
                new SimpleDateFormat(dateFormat).format(adjustEndTime(raid.getEndDay()))));
        raidThumbnail.setImageResource(getResources().getIdentifier(
                "character_" + raid.getThumbnail(),
                "drawable",
                requireContext().getPackageName()));


        vAdapter = new RecordPagerAdapter(getChildFragmentManager(), getLifecycle());
        setViewPager(getIntegerFromToday());

        new TabLayoutMediator(tabLayout, viewPager, true,
                (tab, position) -> tab.setText("Day " + (position + 1) + "\n"
                        + getRaidDate(position))).attach();

        viewPager.setUserInputEnabled(false);

        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        Log.d("recordFragment", "today: " + getIntegerFromToday());
        for(int i=getIntegerFromToday()+1; i<VIEWPAGER_NUM; i++){
            tabStrip.getChildAt(i).setBackgroundColor(Color.GRAY);
            tabStrip.getChildAt(i).setOnTouchListener((v, event) -> true);
        }

        final int[] hpPerRound = {1080000, 1080000,
                1237500, 1237500,
                1500000, 1500000,
                2025000, 2640000, 3440000, 4500000, 5765625,
                7500000, 9750000, 12000000, 16650000, 24000000,
                35000000, 50000000, 72000000,
                100000000, 140000000, 200000000};
        checkFab.setOnClickListener(view1 -> {
            int raidId = raid.getRaidId();
            int curRound = pref.getInt("currentRound", 0);
            List<Boss> bossList = database.raidDao().getBossesList(raidId);
            StringBuilder toastText = new StringBuilder((curRound + 1) + "회차 남은 데미지/막타 체크\n");
            for(int i = 0; i < bossList.size(); i++){
                int idx = curRound >= hpPerRound.length ? hpPerRound.length - 1 : curRound;
                long damage = database.recordDao().get1Boss1RoundSum(raidId, bossList.get(i).getBossId(), curRound + 1);
                long remain = hpPerRound[idx] - damage;
                Record lhr = database.recordDao().get1Boss1RoundLastHit(raidId, bossList.get(i).getBossId(), curRound + 1);
                toastText.append("[" + bossList.get(i).getName()).append("]\n");
                toastText.append(cHelper.getNumberFormat(remain));
                toastText.append(" / " + (lhr != null ?
                        database.memberDao().getMember(lhr.getMemberId()).getName() +"-" + database.heroDao().getHero(lhr.getLeaderId()).getKoreanName()
                        : "체크 X"));
                if(i != bossList.size() - 1) toastText.append("\n");
            }

            showToast(toastText.toString());
        });

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
        Date startDate = raid.getStartDay();

        int differentDays = (int) ((System.currentTimeMillis() - startDate.getTime()) / DAY_IN_SECONDS);
        Log.d("dateDifference", "" + differentDays);

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
        if(myToast != null) myToast.cancel();
        myToast = Toast.makeText(getActivity(), null, Toast.LENGTH_SHORT);
        myToast.setText(msg);
        myToast.show();
    }
}
