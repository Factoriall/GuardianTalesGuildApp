package org.techtown.gtguildraid.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.angmarch.views.NiceSpinner;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.StatisticPagerAdapter;
import org.techtown.gtguildraid.adapters.StatisticRaidSpinnerAdapter;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class StatisticFragment extends Fragment {
    private final String dateFormat = "yy/MM/dd";

    View view;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    StatisticPagerAdapter sAdapter;
    AlertDialog.Builder builder;
    Spinner spinner;
    //NiceSpinner spinner;

    RoomDB database;
    Boolean isSpinnerTouched;

    List<Raid> raids = new ArrayList<>();
    List<String> raidNameList = new LinkedList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistic, container, false);

        database = RoomDB.getInstance(getActivity());
        isSpinnerTouched = false;

        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.viewPager);
        //spinner = view.findViewById(R.id.raidName);
        spinner = view.findViewById(R.id.raidName);

        raids = database.raidDao().getAllRaids();
        if(raids.size() == 0){
            return view;
        }

        List<Integer> imageList = new ArrayList<>();
        List<String> raidInfoList = new ArrayList<>();
        for (Raid raid : raids) {
            raidInfoList.add(
            raid.getName()
            + "_" +
            (new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
                new SimpleDateFormat(dateFormat).format(getEndTime(raid.getStartDay()))));
            int imageId = getResources().getIdentifier("character_" + raid.getThumbnail(), "drawable", getContext().getPackageName());
            imageList.add(imageId);
        }

        StatisticRaidSpinnerAdapter adapter = new StatisticRaidSpinnerAdapter(getContext(), raidInfoList, imageList);
        spinner.setAdapter(adapter);

        /*
        raidNameList.clear();
        raidNameList.add("[레이드 선택]");
        for (Raid r : raids) {
            raidNameList.add(r.getName());
        }*/

        /*
        spinner.attachDataSource(raidNameList);
        spinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            if (position != 0) {
                viewPager.setVisibility(View.VISIBLE);
                setView(raids.get(position - 1));
            }
            else viewPager.setVisibility(View.GONE);
        });*/

        if(raids.size() != 0) {
            if (!database.raidDao().isStartedRaidExist(new Date())
                    && database.raidDao().isCurrentRaidExist(new Date())
                    && raids.size() >= 2) {//미리 만들었는데 실제 시작은 안했고 size가 2 이상이면
                spinner.setSelection(raids.size() - 2);
                setView(raids.get(raids.size() - 2));
            }
            else {
                spinner.setSelection(raids.size() - 1);
                setView(raids.get(raids.size() - 1));
            }
        }


        spinner.setOnTouchListener((v, event) -> {
            isSpinnerTouched = true;
            return false;
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(!isSpinnerTouched) return;
                viewPager.setVisibility(View.VISIBLE);
                setView(raids.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    private void setView(Raid raid) {
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);

        //TextView raidTerm = view.findViewById(R.id.raidTerm);

        //String dateFormat = "yy/MM/dd";
       // raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
        //        new SimpleDateFormat(dateFormat).format(getEndTime(raid.getStartDay()))));

        sAdapter = new StatisticPagerAdapter(getChildFragmentManager(), getLifecycle());
        sAdapter.setData(raid.getRaidId());
        viewPager.setAdapter(sAdapter);
        viewPager.setOffscreenPageLimit(2);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        builder = new AlertDialog.Builder(view.getContext());
        deleteButton.setOnClickListener(view1 -> {
            if(raid.getRaidId() == database.raidDao().getCurrentRaid(new Date()).getRaidId()){
                Toast.makeText(view.getContext(), "현재 진행 중인 레이드는 삭제할 수 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }
            builder.setMessage("삭제 진행 시 관련 데이터도 같이 삭제되며 복구가 불가능합니다. 그래도 삭제하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("네", (dialog, id) -> {
                        database.raidDao().delete(raid);
                        List<Integer> imageList = new ArrayList<>();
                        List<String> raidInfoList = new ArrayList<>();
                        for (Raid r : raids) {
                            raidInfoList.add(
                                    raid.getName()
                                            + "_" +
                                            (new SimpleDateFormat(dateFormat).format(r.getStartDay()) + "~" +
                                                    new SimpleDateFormat(dateFormat).format(getEndTime(r.getStartDay()))));
                            int imageId = getResources().getIdentifier("character_" + r.getThumbnail(), "drawable", getContext().getPackageName());
                            imageList.add(imageId);
                        }

                        StatisticRaidSpinnerAdapter adapter = new StatisticRaidSpinnerAdapter(getContext(), raidInfoList, imageList);
                        spinner.setAdapter(adapter);
                        dialog.dismiss();
                    })
                    .setNegativeButton("아니오", (dialog, id) -> {
                        dialog.dismiss();
                    });
            AlertDialog alert = builder.create();
            alert.setTitle("길드 레이드 삭제");
            alert.show();
        });

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
