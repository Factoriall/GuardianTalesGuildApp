package org.techtown.gtguildraid.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.StatisticRankCardAdapter;
import org.techtown.gtguildraid.etc.RankPoi;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.GuildMember;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.models.RankInfo;
import org.techtown.gtguildraid.models.Record;
import org.techtown.gtguildraid.utils.AppExecutor;
import org.techtown.gtguildraid.utils.RoomDB;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatisticRankFragment extends Fragment {
    static int raidId;
    static View view;
    static int bossPosition;
    static int levelPosition;
    static boolean isAverageMode;
    static boolean isAdjustMode;
    static boolean isDay1Contained;
    static ArrayList<String> bossLabels = new ArrayList<>();
    static ArrayList<String> levelLabels = new ArrayList<>();
    RoomDB database;
    StatisticRankCardAdapter adapter;
    RecyclerView recyclerView;
    Button excelButton;

    public static Fragment newInstance(int raidId) {
        StatisticRankFragment fragment = new StatisticRankFragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistic_rank, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new StatisticRankCardAdapter();
        recyclerView.setAdapter(adapter);
        isDay1Contained = true;

        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }

        database = RoomDB.getInstance(getActivity());

        ToggleSwitch bossSwitch = view.findViewById(R.id.toggleSwitchBoss);
        List<Boss> bossesInRaid = database.raidDao().getBossesList(raidId);

        bossLabels.clear();
        bossLabels.add("보스 전체");
        for (Boss b : bossesInRaid) {
            String bossName = b.getName();
            if (bossName.length() > 5)
                bossName = bossName.substring(0, 5) + "..";
            bossLabels.add(bossName);
        }

        bossSwitch.setEntries(bossLabels);
        bossSwitch.setCheckedPosition(0);
        bossPosition = 0;
        bossSwitch.setOnChangeListener(i -> {
            bossPosition = i;
            setRankView();
        });

        ImageButton settingButton = view.findViewById(R.id.setting);
        settingButton.setOnClickListener(view -> {
            setSettingDialog();
        });


        setRankView();

        return view;
    }

    private void setSettingDialog() {
        final Dialog dialog = new Dialog(getActivity());

        dialog.setContentView(R.layout.dialog_rank_setting);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        if(levelLabels.isEmpty()) {
            levelLabels.add("Lv.50↑");
            levelLabels.add("Lv.66↑");
            levelLabels.add("Lv.71↑");
            levelLabels.add("Lv.76↑");
            levelLabels.add("Lv.80");
        }

        ToggleSwitch levelSwitch = dialog.findViewById(R.id.toggleSwitchLevel);
        levelSwitch.setEntries(levelLabels);
        levelSwitch.setCheckedPosition(levelPosition);

        levelSwitch.setOnChangeListener(i -> levelPosition = i);

        ToggleSwitch avgSwitch = dialog.findViewById(R.id.toggleSwitchAvg);
        avgSwitch.setCheckedPosition(isAverageMode ? 1 : 0);
        avgSwitch.setOnChangeListener(i -> isAverageMode = i == 1);

        Switch adjustSwitch = dialog.findViewById(R.id.adjustSwitch);
        adjustSwitch.setChecked(isAdjustMode);
        adjustSwitch.setOnCheckedChangeListener((compoundButton, b) -> isAdjustMode = b);

        Switch day1AdjustSwitch = dialog.findViewById(R.id.day1AdjustSwitch);
        day1AdjustSwitch.setChecked(isDay1Contained);
        day1AdjustSwitch.setOnCheckedChangeListener((compoundButton, b) -> isDay1Contained = b);

        Button completeButton = dialog.findViewById(R.id.completeButton);
        completeButton.setOnClickListener(view -> {
            setRankView();
            dialog.dismiss();
        });

        Raid raid = database.raidDao().getRaid(raidId);
        int maxDay = getMaxDay(raid.getStartDay());
        Switch excelAdjustSwitch = dialog.findViewById(R.id.excelAdjustSwitch);
        Switch excelDay1Switch = dialog.findViewById(R.id.excelDay1Switch);
        TextView maxDayValue = dialog.findViewById(R.id.maxDayValue);
        SeekBar maxDayBar = dialog.findViewById(R.id.maxDayBar);

        maxDayBar.setMax(maxDay);
        maxDayBar.setProgress(maxDay);
        maxDayValue.setText("~ Day " + maxDay);
        maxDayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if(progress < 1) {
                    maxDayBar.setProgress(1);
                    maxDayValue.setText("~ Day 1");
                }
                else
                    maxDayValue.setText("~ Day " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        SeekBar lastHitBar = dialog.findViewById(R.id.lastHitBar);
        TextView lastHitValue = dialog.findViewById(R.id.lastHitValue);

        lastHitBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                double value = 1f + i * 0.1;
                lastHitValue.setText("x " + String.format("%.1f", value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        excelButton = dialog.findViewById(R.id.excelButton);
        excelButton.setOnClickListener(view -> {
            if(database.recordDao().getAllRecords(raidId).size() == 0) {
                Toast.makeText(getContext(), "데이터 없음", Toast.LENGTH_SHORT).show();
                return;
            }
            else if(excelDay1Switch.isChecked() == true && maxDay == 1){
                Toast.makeText(getContext(), "1일차 적용 여부 스위치를 끄거나 범위를 늘려주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","CSV 파일 생성 중...", true);

            AppExecutor.getInstance().diskIO().execute(() -> {
                RankPoi rp = new RankPoi(database.raidDao().getRaidWithBosses(raidId),
                        database,
                        excelAdjustSwitch.isChecked(),
                        excelDay1Switch.isChecked(),
                        maxDayBar.getProgress(),
                        1 + lastHitBar.getProgress() * 0.1);
                rp.exportDataToExcel();
                requireActivity().runOnUiThread(() -> {
                    mProgressDialog.dismiss();
                    Toast.makeText(getContext(), "생성 완료", Toast.LENGTH_SHORT).show();
                });
            });
        });
    }

    private int getMaxDay(Date start) {
        Calendar end = Calendar.getInstance();
        end.setTime(start);
        end.add(Calendar.DATE, 14);
        Date today = new Date();

        if(today.compareTo(end.getTime()) >= 0) return 14;
        else return Math.max((int)((today.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)), 0) + 1;
    }

    private void setRankView() {
        int[] rounds = {1, 7, 12, 17, 22};
        ProgressDialog mProgressDialog = null;
        List<RankInfo> rankInfos = new ArrayList<>();
        if(bossPosition == 0) {
            mProgressDialog
                    = ProgressDialog.show(getContext(), "잠시 대기", "보스 - 전체 데이터 저장", true);
        }
        ProgressDialog finalMProgressDialog = mProgressDialog;
        AppExecutor.getInstance().diskIO().execute(() -> {
            List<GuildMember> members = database.memberDao().getAllMembers();
            for(GuildMember m : members) {
                if(database.recordDao().get1MemberRecords(m.getID(), raidId).size() == 0)
                    continue;

                List<Record> recordList;
                int day = 1;
                if(!isDay1Contained) day = 2;
                if(bossPosition != 0) {
                    recordList = database.recordDao().get1MemberRoundRecordsWithExtra(
                            m.getID(),
                            raidId,
                            database.raidDao().getBossesList(raidId).get(bossPosition-1).getBossId(),
                            rounds[levelPosition],
                            day);
                }
                else{
                    recordList = database.recordDao().get1MemberRoundRecordsWithExtra(
                            m.getID(),
                            raidId,
                            rounds[levelPosition],
                            day);
                }

                RankInfo ri = new RankInfo(m.getName(), recordList.size());

                for(Record r : recordList) {
                    if(isAverageMode && r.isLastHit()) {
                        ri.setHitNum(ri.getHitNum() - 1);
                        continue;
                    }
                    if(isAdjustMode) {
                        ri.addDamage((long) (r.getDamage() * r.getBoss().getHardness()));
                    }
                    else
                        ri.addDamage(r.getDamage());
                }

                ri.setFinalDamage(isAverageMode);
                rankInfos.add(ri);
            }
            Collections.sort(rankInfos);
            adapter.setItems(rankInfos);

            requireActivity().runOnUiThread(() -> {
                if(bossPosition == 0) finalMProgressDialog.dismiss();
                TextView conditionText = view.findViewById(R.id.conditionText);
                String levelInfo = "";
                switch(levelPosition){
                    case 0:
                        levelInfo = "전체";
                        break;
                    case 1:
                        levelInfo = "Lv.66 이상";
                        break;
                    case 2:
                        levelInfo = "Lv.71 이상";
                        break;
                    case 3:
                        levelInfo = "Lv.76 이상";
                        break;
                    case 4:
                        levelInfo = "Lv.80";
                }
                conditionText.setText("회차 범위: " + levelInfo + " / 기준: "
                        + (isAverageMode ? "평균" : "총합")
                        + "\n배율: " + (isAdjustMode ? "ON" : "OFF")
                        + " / 1일차 적용: " + (isDay1Contained ? "ON" : "OFF"));
                adapter.setItems(rankInfos);
                adapter.notifyDataSetChanged();
            });
        });
    }
}
