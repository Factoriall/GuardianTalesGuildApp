package org.techtown.gtguildraid.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.StatisticRankCardAdapter;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.GuildMember;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.models.RankInfo;
import org.techtown.gtguildraid.models.Record;
import org.techtown.gtguildraid.utils.AppExecutor;
import org.techtown.gtguildraid.utils.RoomDB;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticRankFragment extends Fragment {
    static int raidId;
    static View view;
    static int bossPosition;
    static int levelPosition;
    static boolean isAverageMode;
    static boolean isAdjustMode;
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

        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }

        database = RoomDB.getInstance(getActivity());

        ImageButton arrow = view.findViewById(R.id.arrow);
        ConstraintLayout cl = view.findViewById(R.id.conditionLayout);
        arrow.setOnClickListener(view -> {
            if(cl.getVisibility() == View.VISIBLE){
                cl.setVisibility(View.GONE);
                arrow.setImageResource(R.drawable.icon_arrow_down);
            }
            else{
                cl.setVisibility(View.VISIBLE);
                arrow.setImageResource(R.drawable.icon_arrow_up);
            }
        });

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

        ToggleSwitch levelSwitch = view.findViewById(R.id.toggleSwitchLevel);

        if(levelLabels.isEmpty()) {
            levelLabels.add("Lv.50↑");
            levelLabels.add("Lv.66↑");
            levelLabels.add("Lv.71↑");
            levelLabels.add("Lv.76↑");
            levelLabels.add("Lv.80");
        }

        levelSwitch.setEntries(levelLabels);
        levelSwitch.setCheckedPosition(0);
        levelPosition = 0;
        levelSwitch.setOnChangeListener(i -> {
            levelPosition = i;
            setRankView();
        });

        ToggleSwitch avgSwitch = view.findViewById(R.id.toggleSwitchAvg);
        avgSwitch.setCheckedPosition(0);
        isAverageMode = false;
        avgSwitch.setOnChangeListener(i -> {
            isAverageMode = i == 1;
            setRankView();
        });

        Switch adjustSwitch = view.findViewById(R.id.adjustSwitch);
        isAdjustMode = adjustSwitch.isChecked();
        adjustSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            isAdjustMode = b;
            setRankView();
        });

        excelButton = view.findViewById(R.id.excelButton);
        excelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(database.recordDao().getAllRecords(raidId).size() == 0) {
                    Toast.makeText(getContext(), "데이터 없음", Toast.LENGTH_SHORT).show();
                    return;
                }
                ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","CSV 파일 생성 중...", true);

                AppExecutor.getInstance().diskIO().execute(() -> {
                    exportDataToExcel();
                    getActivity().runOnUiThread(() -> {
                        mProgressDialog.dismiss();
                        Toast.makeText(getContext(), "생성 완료", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });

        setRankView();

        return view;
    }

    private void exportDataToExcel() {
        Raid raid = database.raidDao().getRaidWithBosses(raidId);
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        final File file =  new File(directory,raid.getName() + " 순위표.xls");

        //색깔 만들어주기
        ExcelHelper excelHelper = new ExcelHelper(new HSSFWorkbook());

        excelHelper.addTotalRank();
        excelHelper.addDetailRank();


    }

    private static class ExcelHelper{
        HSSFWorkbook wb;
        Sheet sheet;

        HSSFColor goldColor = wb.getCustomPalette().findSimilarColor(255, 255, 0);
        HSSFColor silverColor = wb.getCustomPalette().findSimilarColor(206, 206, 206);
        HSSFColor bronzeColor = wb.getCustomPalette().findSimilarColor(191, 143, 0);
        HSSFColor ironColor = wb.getCustomPalette().findSimilarColor(113, 113, 113);
        HSSFColor sub1Color = wb.getCustomPalette().findSimilarColor(204, 255, 204);

        public ExcelHelper(HSSFWorkbook wb) {
            this.wb = wb;
            sheet = wb.createSheet("new sheet");
        }

        public void addTotalRank() {
        }

        public void addDetailRank() {
        }

        private void setCellValueAndStyle(int sr, int er, int sc, int ec, CellStyle style, String str) {
            setStyleWithBorder(style);
            for(int i = sr; i <= er; i++){
                Row row = getOrCreateRow(i);
                for(int j = sc; j <= ec; j++){
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(style);
                }
            }
            mergeCell(sr, er, sc, ec, str);
        }

        private void setCellValueAndStyle(Cell cell, CellStyle style, String str) {
            setStyleWithBorder(style);
            cell.setCellValue(str);
            cell.setCellStyle(style);
        }

        private void mergeCell(int sr, int er, int sc, int ec, String str) {
            Row row = getOrCreateRow(sr);
            Cell cell = row.getCell(sc) != null ? row.getCell(sc) : row.createCell(sc);
            cell.setCellValue(str);
            sheet.addMergedRegion(new CellRangeAddress(sr, er, sc, ec));
        }

        private void setStyleWithBorder(CellStyle style) {
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
        }

        private Row getOrCreateRow(int rowNum) {
            return sheet.getRow(rowNum) == null ? sheet.createRow(rowNum) : sheet.getRow(rowNum);
        }


    }



    private void setRankView() {
        int[] rounds = {1, 7, 12, 17, 22};

        List<RankInfo> rankInfos = new ArrayList<>();
        ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","데이터베이스 접속 중", true);
        AppExecutor.getInstance().diskIO().execute(() -> {
            List<GuildMember> members = database.memberDao().getAllMembers();
            for(GuildMember m : members) {
                if(database.recordDao().get1MemberRecords(m.getID(), raidId).size() == 0)
                    continue;

                List<Record> recordList;
                if(bossPosition != 0) {
                    recordList = database.recordDao().get1MemberRoundRecordsWithExtra(
                            m.getID(),
                            raidId,
                            database.raidDao().getBossesList(raidId).get(bossPosition-1).getBossId(),
                            rounds[levelPosition]);
                }
                else{
                    recordList = database.recordDao().get1MemberRoundRecordsWithExtra(
                            m.getID(),
                            raidId,
                            rounds[levelPosition]);
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

            getActivity().runOnUiThread(() -> {
                mProgressDialog.dismiss();
                TextView conditionText = view.findViewById(R.id.conditionText);
                conditionText.setText(bossLabels.get(bossPosition) + " / " + levelLabels.get(levelPosition) +
                        " / " + (isAverageMode ? "평균" : "총합") + " / 배율 " + (isAdjustMode ? "ON" : "OFF"));
                adapter.setItems(rankInfos);
                adapter.notifyDataSetChanged();
            });
        });
    }
}
