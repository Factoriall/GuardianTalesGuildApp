package org.techtown.gtguildraid.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kyleduo.switchbutton.SwitchButton;
import com.opencsv.CSVWriter;

import org.angmarch.views.NiceSpinner;
import org.apache.poi.hssf.usermodel.HSSFOptimiser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.GuildMember;
import org.techtown.gtguildraid.models.Hero;
import org.techtown.gtguildraid.models.LeaderInfo;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.models.Record;
import org.techtown.gtguildraid.utils.AppExecutor;
import org.techtown.gtguildraid.utils.RoomDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticMemberFragment extends Fragment {
    private static int raidId;
    private static boolean isDetailMode;
    private static List<GuildMember> membersInRaid = new ArrayList<>();
    private static int sMemberIdx = 0;

    RoomDB database;
    View view;
    NiceSpinner memberSpinner;
    HSSFWorkbook wb;
    Sheet sheet;

    HSSFColor titleColor;
    HSSFColor sub2Color;
    HSSFColor sub3Color;

    CellStyle dataCellStyle;
    CellStyle subtitleStyle;
    CellStyle subtitle2Style;
    CellStyle subtitle3Style;

    public static StatisticMemberFragment newInstance(int raidId) {
        StatisticMemberFragment fragment = new StatisticMemberFragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistic_member, container, false);
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        SwitchButton viewSwitch = view.findViewById(R.id.viewSwitch);
        Button csvButton = view.findViewById(R.id.csvButton);
        isDetailMode = viewSwitch.isChecked();
        sMemberIdx = 0;

        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
        }
        database = RoomDB.getInstance(getActivity());

        memberSpinner = view.findViewById(R.id.memberName);

        List<GuildMember> allMembers = database.memberDao().getAllMembers();

        membersInRaid.clear();
        for (GuildMember m : allMembers) {
            if (database.recordDao().get1MemberRecords(m.getID(), raidId).size() != 0)
                membersInRaid.add(m);
        }

        Collections.sort(membersInRaid, (guildMember, t1) -> guildMember.getName().compareTo(t1.getName()));

        List<String> memberNameList = new ArrayList<>();
        for (GuildMember m : membersInRaid) {
            memberNameList.add(m.getName());
        }
        memberSpinner.attachDataSource(memberNameList);
        if (memberNameList.size() == 1)
            memberSpinner.setText(memberNameList.get(0));

        memberSpinner.setOnSpinnerItemSelectedListener((parent, view1, position, id) -> {
            sMemberIdx = position;
            setView();
        });

        viewSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if(memberNameList.size() == 0) {
                Toast.makeText(getContext(), "데이터 없음", Toast.LENGTH_SHORT).show();
                return;
            }
            isDetailMode = b;
            setView();
        });

        if(memberNameList.size() != 0)
            setView();

        csvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(memberNameList.size() == 0) {
                    Toast.makeText(getContext(), "데이터 없음", Toast.LENGTH_SHORT).show();
                    return;
                }
                ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","CSV 파일 생성 중...", true);

                AppExecutor.getInstance().diskIO().execute(() -> {
                    //exportDataToCSV();
                    exportDataToExcel();
                    getActivity().runOnUiThread(() -> {
                        mProgressDialog.dismiss();
                        Toast.makeText(getContext(), "생성 완료", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });

        return view;
    }

    private void setView() {
        int memberId = membersInRaid.get(sMemberIdx).getID();
        StatisticMemberBasic1Fragment basicFragment = new StatisticMemberBasic1Fragment(raidId, memberId);
        StatisticMemberDetailFragment detailFragment = new StatisticMemberDetailFragment(raidId, memberId);
        if(isDetailMode){
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.container, detailFragment).commit();
        }
        else{
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.container, basicFragment).commit();
        }
    }

    private void exportDataToExcel() {
        Raid raid = database.raidDao().getRaidWithBosses(raidId);
        List<Boss> bosses = raid.getBossList();
        GuildMember member = membersInRaid.get(sMemberIdx);
        String name = member.getName();
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        final File file =  new File(directory,raid.getName() + "_" + name +  ".xls");

        wb = new HSSFWorkbook();
        sheet = wb.createSheet("new sheet");
        //색깔 만들어주기
        titleColor = wb.getCustomPalette().findSimilarColor(220, 220, 220);
        sub2Color = wb.getCustomPalette().findSimilarColor(255, 255, 204);
        sub3Color = wb.getCustomPalette().findSimilarColor(204, 255, 204);
        sheet.setColumnWidth(0, 60 * 256 / 9);

        //제목 스타일 만들기
        CellStyle title = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        title.setFont(font);
        title.setFillForegroundColor(titleColor.getIndex());
        title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        title.setAlignment(HorizontalAlignment.CENTER);
        title.setVerticalAlignment(VerticalAlignment.CENTER);

        //cellStyle 미리 만들기
        subtitleStyle = wb.createCellStyle();
        subtitle2Style = wb.createCellStyle();
        subtitle3Style = wb.createCellStyle();
        dataCellStyle = wb.createCellStyle();

        setSubtitleStyle(subtitleStyle);
        setVertTitleStyle(subtitle2Style, sub2Color.getIndex());
        setHorzTitleStyle(subtitle3Style, sub3Color.getIndex());
        setDataCellStyle(dataCellStyle);

        for(int i=1; i<=21; i++) {
            if(i % 3 == 0)
                sheet.setColumnWidth(i, 110 * 256 / 9);
            else
                sheet.setColumnWidth(i, 55 * 256 / 9);
        }

        int rowNum = 0;
        String titleName = name + " // " + raid.getName() + " 개인 딜 부검표";
        mergeCellWithBorder(0, 0, 0, 21, title, titleName);
        Row row = getOrCreateRow(rowNum);
        row.setHeightInPoints((short) 36);
        rowNum = addSummaryToExcel(rowNum + 1, member.getID(), raid);

        rowNum = addHistoryToExcel(rowNum, member.getID());
        rowNum = addBossInfoToExcel(rowNum, member.getID(), bosses);

        //설명 추가
        sheet.createRow(rowNum);
        sheet.createRow(rowNum + 1);
        sheet.createRow(rowNum + 2);
        sheet.createRow(rowNum + 3);
        String explanation1 = "* 모든 평균의 경우 막타를 제외하고 계산합니다.";
        mergeCell(rowNum, rowNum, 0, 21, explanation1);
        CellUtil.setAlignment(sheet.getRow(rowNum).getCell(0), HorizontalAlignment.CENTER);
        String explanation2 = "* 특수 상황의 '부족'은 각 보스/덱 별 자신의 전체 평균 대비 75% 이상 85% 미만의 딜량 기록, '전복'은 각 보스/덱 별 자신의 전체 평균 대비 75% 미만의 딜량 기록";
        mergeCell(rowNum + 1, rowNum+1, 0, 21, explanation2);
        CellUtil.setAlignment(sheet.getRow(rowNum + 1).getCell(0), HorizontalAlignment.CENTER);
        String explanation3 = "* '인분' 항목은 76렙 이후의 보스 및 공격 리더를 기준으로 길드원 평균 데미지에 비해 얼만큼 데미지를 가했는지 판정하며 3번 이상 막타를 제외하고 쳐야 통계에 계산됩나다.";
        mergeCell(rowNum + 2, rowNum + 2, 0, 21, explanation3);
        CellUtil.setAlignment(sheet.getRow(rowNum + 2).getCell(0), HorizontalAlignment.CENTER);
        String explanation4 = "\n 예를 들어 76렙 이후 에리나에 가람 리더로 평균 300만 데미지를 가하고 전체가 76렙 이상 에리나에 가람 리더로 평균 200만을 쳤다면 1.5인분으로 표시됩니다.";
        mergeCell(rowNum + 3, rowNum + 3, 0, 21, explanation4);
        CellUtil.setAlignment(sheet.getRow(rowNum + 3).getCell(0), HorizontalAlignment.CENTER);

        CellRangeAddress region = new CellRangeAddress(rowNum + 0, rowNum + 3, 0, 21);
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);

        try {
            FileOutputStream os = new FileOutputStream(file);
            wb.write(os);
        } catch (IOException e) { e.printStackTrace(); }

    }

    private int addSummaryToExcel(int rowNum, int memberId, Raid raid) {
        //row 크기 조정
        setWidthAndSideTitle(rowNum, 5, "요약");

        //길드 요약
        int colNum = 1;
        mergeCellWithBorder(rowNum, rowNum + 4, colNum,colNum, subtitle2Style, "길드");

        colNum += 1;
        mergeCellWithBorder(rowNum, rowNum, colNum, colNum + 1, subtitle3Style, "총 딜량&달성률");
        long totalDamageInGuild = database.recordDao().getTotalDamageInRaid(raidId);
        mergeCellWithBorder(rowNum+1, rowNum+1, colNum, colNum + 1,
                dataCellStyle, getNumberFormat(totalDamageInGuild));
        int maxRound = database.recordDao().getMaxRound(raidId);
        long maxRoundSum = database.recordDao().getMaxRoundRecordSum(raidId, maxRound);
        final int[] hpPerRound = {1080000, 1080000,
                1237500, 1237500,
                1500000, 1500000,
                2025000, 2640000, 3440000, 4500000, 5765625,
                7500000, 9750000, 12000000, 16650000, 24000000,
                35000000, 50000000, 72000000,
                100000000, 140000000, 200000000};
        long allBossHp = (maxRound >= hpPerRound.length) ? hpPerRound[21] * 4 : hpPerRound[maxRound-1] * 4;

        mergeCellWithBorder(rowNum+2, rowNum+2, colNum, colNum + 1,
                dataCellStyle, maxRound + "회차/" + getPercentage(maxRoundSum, allBossHp));
        mergeCellWithBorder(rowNum + 3, rowNum + 3, colNum, colNum + 1, subtitle3Style, "길드 순위");
        mergeCellWithBorder(rowNum + 4, rowNum + 4, colNum, colNum + 1, dataCellStyle, "100");

        //보스 요약
        colNum += 2;
        mergeCellWithBorder(rowNum, rowNum + 4, colNum, colNum, subtitle2Style, "보스 구성");

        colNum += 1;
        Row row = getOrCreateRow(rowNum);
        setSingleCellValueAndStyle(row.createCell(colNum), subtitle3Style, "속성");
        setSingleCellValueAndStyle(row.createCell(colNum+1), subtitle3Style, "이름");
        setSingleCellValueAndStyle(row.createCell(colNum+2), subtitle3Style, "배율");

        List<Boss> bosses = database.raidDao().getBossesList(raidId);
        for(int b = 1; b <= bosses.size(); b++){
            CellStyle elementStyle = wb.createCellStyle();
            Boss boss = bosses.get(b-1);
            int nRow = rowNum + b;
            row = sheet.getRow(nRow);
            int elementId = boss.getElementId();
            HSSFColor elementColor = getColorFromElement(elementId, (HSSFWorkbook) wb);
            setColorCellStyle(elementStyle, elementColor);
            setSingleCellValueAndStyle(row.createCell(colNum), elementStyle, getElementFromId(elementId));
            setSingleCellValueAndStyle(row.createCell(colNum+1), dataCellStyle, boss.getName());
            setSingleCellValueAndStyle(row.createCell(colNum+2), dataCellStyle, Double.toString(boss.getHardness()));
        }

        //개인 요약
        colNum += 3;
        mergeCellWithBorder(rowNum, rowNum + 4, colNum, colNum, subtitle2Style, "개인");

        colNum += 1;
        row = getOrCreateRow(rowNum);
        setSingleCellValueAndStyle(row.createCell(colNum), subtitle3Style, "배율");
        mergeCellWithBorder(rowNum, rowNum, colNum+1, colNum+2, subtitle3Style, "전시즌 순위");
        setSingleCellValueAndStyle(row.createCell(colNum + 3), subtitle3Style, "현시즌 순위");
        mergeCellWithBorder(rowNum, rowNum, colNum+4, colNum+6, subtitle3Style, "총 딜량/점유율");
        mergeCellWithBorder(rowNum, rowNum, colNum+7, colNum+8, subtitle3Style, "평균 딜량");
        setSingleCellValueAndStyle(row.createCell(colNum + 9), subtitle3Style, "배율 상승률");

        mergeCellWithBorder(rowNum+1, rowNum+2, colNum, colNum, subtitle3Style, "OFF");
        mergeCellWithBorder(rowNum+3, rowNum+4, colNum, colNum, subtitle3Style, "ON");

        colNum += 1;
        int aRow = 1;

        int pastRaidId;

        Raid pastRaid = database.raidDao().getPastRecentRaid(raid.getStartDay());
        if(pastRaid == null) pastRaidId = -1;
        else pastRaidId = pastRaid.getRaidId();

        //배율 OFF 데이터 넣기
        int pastRank;
        if(pastRaidId == -1) pastRank = -1;
        else pastRank = database.recordDao().getRankFromAllRecords(memberId, pastRaidId);
        int rank = database.recordDao().getRankFromAllRecords(memberId, raidId);
        List<Record> allRecords = database.recordDao().getAllRecordsWithExtra(raidId);
        List<Record> memberRecords = database.recordDao().get1MemberRecordsWithExtra(memberId, raidId);

        long allDamage = getDamageFromList(allRecords, false);
        long memberDamage = getDamageFromList(memberRecords, false);
        long averageDamage = getAverageFromList(memberRecords, false);

        String pRankText = pastRank != 0 ? Integer.toString(pastRank) : "X";
        mergeCellWithBorder(rowNum+aRow, rowNum+aRow+1, colNum, colNum+1, dataCellStyle, pRankText);
        mergeCellWithBorder(rowNum+aRow, rowNum+aRow+1, colNum+2, colNum+2, dataCellStyle, rank + " (" + getDiffOfRank(pastRank, rank) + ")");
        mergeCellWithBorder(rowNum+aRow, rowNum+aRow+1, colNum+3, colNum+5, dataCellStyle, getNumberFormat(memberDamage) + " / " + getPercentage(memberDamage, allDamage));
        mergeCellWithBorder(rowNum+aRow, rowNum+aRow+1, colNum+6, colNum+7, dataCellStyle, getNumberFormat(averageDamage));

        aRow += 2;
        //배율 ON 데이터 넣기
        int rankAdjust = database.recordDao().getRankFromAllAdjustRecords(memberId, raidId);
        int pastRankAdj = database.recordDao().getRankFromAllAdjustRecords(memberId, pastRaidId);

        long allDamageAdjust = getDamageFromList(allRecords, true);
        long memberDamageAdjust = getDamageFromList(memberRecords, true);
        long averageDamageAdjust = getAverageFromList(memberRecords, true);

        pRankText = pastRankAdj != 0 ? Integer.toString(pastRankAdj) : "X";
        mergeCellWithBorder(rowNum+aRow, rowNum+aRow+1, colNum, colNum+1, dataCellStyle, pRankText);
        mergeCellWithBorder(rowNum+aRow, rowNum+aRow+1, colNum+2, colNum+2, dataCellStyle, rankAdjust + " (" + getDiffOfRank(pastRankAdj, rankAdjust) + ")");
        mergeCellWithBorder(rowNum+aRow, rowNum+aRow+1, colNum+3, colNum+5, dataCellStyle, getNumberFormat(memberDamageAdjust) + " / " + getPercentage(memberDamageAdjust, allDamageAdjust));
        mergeCellWithBorder(rowNum+aRow, rowNum+aRow+1, colNum+6, colNum+7, dataCellStyle, getNumberFormat(averageDamageAdjust));

        aRow -= 2;
        mergeCellWithBorder(rowNum + aRow, rowNum+aRow+3, colNum+8, colNum+8, dataCellStyle, getPercentage(memberDamageAdjust - memberDamage, memberDamage));

        int noHitCount = 42 - memberRecords.size();

        colNum += 9;
        mergeCellWithBorder(rowNum, rowNum + 4, colNum, colNum, subtitle2Style, "특수 상황");
        colNum += 1;
        setSingleCellValueAndStyle(row.createCell(colNum), subtitle3Style, "상황");
        setSingleCellValueAndStyle(row.createCell(colNum + 1), subtitle3Style, "횟수");

        row = getOrCreateRow(rowNum + 1);
        colNum -= 1;
        CellStyle colorStyle = wb.createCellStyle();
        setColorCellStyle(colorStyle, wb.getCustomPalette().findSimilarColor(146, 208, 80));
        setSingleCellValueAndStyle(row.createCell(colNum + 1), colorStyle, "막타");

        row = getOrCreateRow(rowNum + 2);
        colorStyle = wb.createCellStyle();
        setColorCellStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 255, 50));
        setSingleCellValueAndStyle(row.createCell(colNum + 1), colorStyle, "부족");

        row = getOrCreateRow(rowNum + 3);
        colorStyle = wb.createCellStyle();
        setColorCellStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 60, 60));
        setSingleCellValueAndStyle(row.createCell(colNum + 1), colorStyle, "전복");

        row = getOrCreateRow(rowNum + 4);
        colorStyle = wb.createCellStyle();
        setColorCellStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 0, 0));
        setSingleCellValueAndStyle(row.createCell(colNum + 1), colorStyle, "소멸");

        setSingleCellValueAndStyle(row.createCell(colNum + 2), dataCellStyle, Integer.toString(noHitCount));

        //mergeCellWithBorder(rowNum + aRow, rowNum+aRow+3, colNum+9, colNum+9, dataCellStyle, Integer.toString(lastHitCount));
        return rowNum + 5;
    }

    private String getDiffOfRank(int pastRank, int rank) {
        if(pastRank == -1 || pastRank == 0) return "-";
        int diff = pastRank - rank;
        if(diff > 0) return diff + "▲";
        else if(diff < 0) return (-diff) + "▼";
        else return "동일";
    }

    private int addHistoryToExcel(int rowNum, int memberId) {
        setWidthAndSideTitle(rowNum, 10, "히스토리");

        //히스토리 설정
        CellStyle colorStyle;

        int colNum = 1;
        int lastCnt = 0;
        int lowCnt = 0;
        int boomCnt = 0;
        int outCnt = 0;
        List<List<Record>> dayRecords = getAllRecordsByDays(memberId);
        for(int r=0; r<=1; r++) {//2개 행으로 생성
            for (int d = 0; d < 7; d++) {//7개의 열로 생성
                int day = d + r * 7;
                int startCol = colNum + d * 3;
                colorStyle = wb.createCellStyle();
                setColorCellStyle(colorStyle, sub2Color);

                mergeCellWithBorder(rowNum, rowNum, startCol, startCol + 2, colorStyle, "Day " + (day + 1));

                long sum = 0;
                //Day 1 - 7 데이터 삽입
                for (int t = 1; t <= 3; t++) {//데이터 수만큼 돌리기
                    if (dayRecords.get(day).size() < t) {//데이터 없으면 border만 추가
                        outCnt++;
                        CellRangeAddress region = new CellRangeAddress(rowNum + t, rowNum + t, startCol, startCol + 2);
                        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
                        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
                        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
                        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
                        continue;
                    }

                    //데이터 추가 작업
                    Record record = dayRecords.get(day).get(t - 1);
                    Boss boss = record.getBoss();
                    Hero leader = record.getLeader();
                    long damage = record.getDamage();
                    long average = database.recordDao().getAverageOfMyLeader(raidId, memberId, boss.getBossId(), leader.getHeroId(), 0);

                    sum += damage;

                    int bossElementId = boss.getElementId();
                    int leaderElementId = leader.getElement();
                    HSSFColor bossElementColor = getColorFromElement(bossElementId, (HSSFWorkbook) wb);
                    HSSFColor leaderElementColor = getColorFromElement(leaderElementId, (HSSFWorkbook) wb);

                    colorStyle = wb.createCellStyle();
                    setColorCellStyle(colorStyle, bossElementColor);
                    setSingleCellValueAndStyle(sheet.getRow(rowNum + t).createCell(startCol), colorStyle, getShortWord(boss.getName()));

                    colorStyle = wb.createCellStyle();
                    setColorCellStyle(colorStyle, leaderElementColor);
                    setSingleCellValueAndStyle(sheet.getRow(rowNum + t).createCell(startCol + 1), colorStyle, getShortWord(leader.getKoreanName()));

                    colorStyle = wb.createCellStyle();
                    if(record.isLastHit()) {
                        lastCnt++;
                        setColorCellStyle(colorStyle, wb.getCustomPalette().findSimilarColor(146, 208, 80));
                    }
                    else if(betweenRange(record.getDamage(), average, 75, 85)) {
                        lowCnt++;
                        setColorCellStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 255, 50));
                    }
                    else if(betweenRange(record.getDamage(), average, 0, 75)) {
                        boomCnt++;
                        setColorCellStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 60, 60));
                    }
                    else
                        setDataCellStyle(colorStyle);
                    setSingleCellValueAndStyle(sheet.getRow(rowNum + t).createCell(startCol + 2), colorStyle, getNumberFormat(damage));
                }
                colorStyle = wb.createCellStyle();
                setColorCellStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 217, 102));
                mergeCellWithBorder(rowNum + 4, rowNum + 4, startCol, startCol + 2, colorStyle, getNumberFormat(sum));
                HSSFOptimiser.optimiseCellStyles(wb);//optimize 통해 계속 cellStyle 쓸 수 있게 처리리
            }
            rowNum += 5;
        }
        int cNum = 21;
        Row row = getOrCreateRow(2);
        setSingleCellValueAndStyle(row.createCell(cNum), dataCellStyle, Integer.toString(lastCnt));
        row = getOrCreateRow(3);
        setSingleCellValueAndStyle(row.createCell(cNum), dataCellStyle, Integer.toString(lowCnt));
        row = getOrCreateRow(4);
        setSingleCellValueAndStyle(row.createCell(cNum), dataCellStyle, Integer.toString(boomCnt));

        return rowNum;
    }

    private boolean betweenRange(long damage, long average, int min, int max) {
        float percent = (float) damage / average * 100f;
        if(percent >= min && percent < max)
            return true;
        return false;
    }

    private int addBossInfoToExcel(int rowNum, int memberId, List<Boss> bosses) {
        setWidthAndSideTitle(rowNum, 14, "보스별 상세 정보");

        int bossIdx = 0;
        for(int r = 0; r < 2; r++){
            setBossInfo(rowNum, memberId, bosses.get(bossIdx++), true);
            setBossInfo(rowNum, memberId, bosses.get(bossIdx++), false);

            rowNum += 7;
        }

        return rowNum;
    }

    private void setBossInfo(int rowNum, int memberId, Boss boss, boolean isLeft ) {
        int bossId = boss.getBossId();
        List<Record> allRecords = database.recordDao().get1BossRecordsWithExtra(raidId, bossId);
        List<Record> memberRecords = database.recordDao()
                .get1MemberRecordsWithExtra(memberId, raidId, bossId);

        long guildAverage = getAverageFromList(allRecords, false);
        long memberDamage = getDamageFromList(memberRecords, false);
        long allDamage = getDamageFromList(allRecords, false);
        long memberAverage = getAverageFromList(memberRecords, false);
        int rankTotal = database.recordDao().getTotalRankFromBossRecords(memberId, raidId, bossId);
        int rankAvg = memberAverage == 0 ? 0 : database.recordDao().getAvgRankFromBossRecords(memberId, raidId, bossId);
        String avg = memberAverage == 0 ? "-" : getNumberFormat(memberAverage);
        String rTotal = (rankTotal == 0) ? "-" : Integer.toString(rankTotal);
        String rAvg = (rankAvg == 0) ? "-" : Integer.toString(rankAvg);

        final int MIN_COUNT = 3;
        final int[] leftCols = {2, 1, 3, 2, 1, 2};
        final int leftWidth = 10;
        final int[] rightCols = {1, 2, 3, 1, 2, 1};
        final int rightWidth = 9;


        int[] usingCols;
        int width;
        int startCol;
        int colNum;

        if (isLeft) {
            usingCols = leftCols;
            width = leftWidth;
            startCol = 1;
        } else {
            usingCols = rightCols;
            width = rightWidth;
            startCol = 12;
        }

        CellStyle dayStyle = wb.createCellStyle();
        CellStyle totalStyle = wb.createCellStyle();
        setHorzTitleStyle(dayStyle, sub2Color.getIndex());
        setHorzTitleStyle(totalStyle, wb.getCustomPalette().findSimilarColor(255, 217, 102).getIndex());

        colNum = startCol;
        //길드 평균 삽입
        final String[] subtitles = {"길드 평균", "친 횟수", "총 딜량 / 점유율", "평균", "점유율 순위", "평균 순위"};
        final String[] briefValues = {getNumberFormat(guildAverage),
                Integer.toString(memberRecords.size()),
                getNumberFormat(memberDamage) + " / " + getPercentage(memberDamage, allDamage),
                avg,
                rTotal, rAvg};

        if(usingCols[0] == 1){
            setSingleCellValueAndStyle(getOrCreateRow(rowNum).createCell(colNum), dayStyle, subtitles[0]);
            setSingleCellValueAndStyle(getOrCreateRow(rowNum + 1).createCell(colNum), totalStyle, briefValues[0]);
        }
        else{
            mergeCellWithBorder(rowNum, rowNum, colNum, colNum + usingCols[0] - 1, dayStyle, subtitles[0]);
            mergeCellWithBorder(rowNum + 1, rowNum + 1, colNum, colNum + usingCols[0] - 1, totalStyle, briefValues[0]);
        }

        //brief 정보 삽입
        colNum += usingCols[0];
        for (int i = 1; i < 6; i++) {
            if (usingCols[i] == 1) {
                setSingleCellValueAndStyle(getOrCreateRow(rowNum).createCell(colNum), subtitle3Style, subtitles[i]);
                setSingleCellValueAndStyle(getOrCreateRow(rowNum + 1).createCell(colNum), dataCellStyle, briefValues[i]);
            } else {
                mergeCellWithBorder(rowNum, rowNum, colNum, colNum + usingCols[i] - 1, subtitle3Style, subtitles[i]);
                mergeCellWithBorder(rowNum + 1, rowNum + 1, colNum, colNum + usingCols[i] - 1, dataCellStyle, briefValues[i]);
            }
            colNum += usingCols[i];
        }

        //leader 정보 삽입
        colNum = startCol;
        rowNum += 2;

        final String[] leaderSub = {"리더", "총 딜량 / 지분", "횟수(막타)", "평균 딜량", "인분"};

        mergeCellWithBorder(rowNum, rowNum + 4, colNum, colNum + usingCols[0] - 1, subtitle2Style, boss.getName());
        mergeCellWithBorder(rowNum, rowNum, colNum + usingCols[0], colNum + width, subtitle3Style, "리더별 보스 상대 TOP3");
        colNum += usingCols[0];
        rowNum += 1;
        for(int i = 1; i < 6; i++){
            if (usingCols[i] == 1)
                setSingleCellValueAndStyle(getOrCreateRow(rowNum).createCell(colNum), subtitle3Style, leaderSub[i - 1]);
             else
                mergeCellWithBorder(rowNum, rowNum, colNum, colNum + usingCols[i] - 1, subtitle3Style, leaderSub[i - 1]);

            colNum += usingCols[i];
        }

        rowNum += 1;
        List<LeaderInfo> leaderList = getListOfLeaders(memberRecords);
        for(int l=0; l<3; l++){
            colNum = startCol + usingCols[0];
            if(leaderList.size() <= l) {
                CellRangeAddress region = new CellRangeAddress(rowNum, rowNum, colNum, colNum + width - usingCols[0]);
                RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
                RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
                RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
                RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
                rowNum += 1;
                continue;
            }
            LeaderInfo leaderInfo = leaderList.get(l);
            Hero leader = leaderInfo.getLeader();
            List<Record> records = leaderInfo.getRecordList();
            long averageAbove76 = 0;
            int cntNotLast = 0;
            for(Record r : records){
                if(!r.isLastHit())
                    cntNotLast++;
            }
            if(cntNotLast >= MIN_COUNT) {
                averageAbove76 = getAverageFromList(records, 18);
            }
            long guildAvg76 = database.recordDao().getAverageOfLeaderFromRecords(raidId, bossId, leader.getHeroId(), 18);

            String ACPL;
            if(averageAbove76 == 0) {
                ACPL = "-";
            }
            else {
                ACPL = String.format("%.3f", (float) averageAbove76 / guildAvg76);
            }

            long leaderDamage = getDamageFromList(records, false);
            final String[] leaderValues = {leader.getKoreanName(),
                    getNumberFormat(leaderDamage) + " / "
                    + getPercentage(leaderDamage, memberDamage),
                    records.size() + "(" + getLastHitNum(records) + ")",
                    getNumberFormat(getAverageFromList(records)),
                    ACPL};

            for(int i = 1; i < 6; i++){
                if (usingCols[i] == 1)
                    setSingleCellValueAndStyle(getOrCreateRow(rowNum).createCell(colNum), dataCellStyle, leaderValues[i - 1]);
                else
                    mergeCellWithBorder(rowNum, rowNum, colNum, colNum + usingCols[i] - 1, dataCellStyle, leaderValues[i - 1]);

                colNum += usingCols[i];
            }
            rowNum += 1;
        }
    }

    private int getLastHitNum(List<Record> records) {
        int count = 0;
        for(Record r : records){
            if(r.isLastHit())
                count++;
        }
        return count;
    }

    private List<LeaderInfo> getListOfLeaders(List<Record> records) {
        List<LeaderInfo> memberLeaderList = new ArrayList<>();
        for(Record r : records){
            boolean isMatched = false;
            for(LeaderInfo info : memberLeaderList){
                if(info.isMatched(r.getLeader())) {
                    info.addList(r);
                    isMatched = true;
                    break;
                }
            }
            if(!isMatched)
                memberLeaderList.add(new LeaderInfo(r.getLeader(), r));
        }
        Collections.sort(memberLeaderList);
        return memberLeaderList;
    }

    private void setWidthAndSideTitle(int sRow, int height, String title) {
        //너비 설정
        Row row;
        for(int i=0; i<height; i++){
            row = sheet.createRow(sRow + i);
            row.setHeightInPoints(17.4f);
        }
        //제목 설정
        int colNum = 0;
        mergeCellWithBorder(sRow, sRow + height - 1, colNum, colNum, subtitleStyle, title);
    }

    private void exportDataToCSV() {
        Raid raid = database.raidDao().getRaidWithBosses(raidId);
        List<Boss> bosses = raid.getBossList();
        GuildMember member = membersInRaid.get(sMemberIdx);
        String name = member.getName();
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        final File file =  new File(directory,raid.getName() + "_" + name +  ".csv");

        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(file));

            List<String[]> data = new ArrayList<String[]>();
            int memberId = member.getID();
            data.add(new String[]{"레이드 이름:" + raid.getName()});
            data.add(new String[]{"닉네임:" + name});

            addSummary(data, memberId);
            addHistory(data, memberId);
            addBossInfo(data, memberId, bosses);

            writer.writeAll(data); // data is adding to csv
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addBossInfo(List<String[]> data, int memberId, List<Boss> bosses) {
        addBlankLine(data);
        data.add(new String[]{"보스별 정보"});

        //보스 이름, 보스 난이도 설정
        //총 딜량, 기여도, 평균 딜량, 친 횟수
        //리더별 정보
        //이름, 총 데미지, 친 횟수, 평균 데미지(막타 제외)
        //
        for(int i=0; i<4; i++){
            addBlankLine(data);
            Boss boss = bosses.get(i);
            int bossId = boss.getBossId();
            //전체적인 보스 정보 저장
            data.add(new String[]{boss.getName() + " - 배율: " + boss.getHardness()});
            data.add(new String[]{"총 딜량", "딜량 점유율(%)", "평균(막타X)", "친 횟수"});
            List<Record> allRecords = database.recordDao().get1BossRecordsWithExtra(raidId, bossId);
            List<Record> memberRecords = database.recordDao()
                    .get1MemberRecordsWithExtra(memberId, raidId, bossId);

            long allDamage = getDamageFromList(allRecords, false);
            long memberDamage = getDamageFromList(memberRecords, false);
            long averageDamage = getAverageFromList(memberRecords);

            data.add(new String[]{
                    getNumberFormat(memberDamage),
                    getPercentage(memberDamage, allDamage),
                    getNumberFormat(averageDamage),
                    Integer.toString(memberRecords.size())
            });

            //리더별 보스 상대 정보 저장
            data.add(new String[]{"리더별 보스 상대 정보"});
            data.add(new String[]{"리더 이름", "총 데미지", "친 횟수", "평균(막타 제외)"});
            List<LeaderInfo> memberLeaderList = new ArrayList<>();
            for(Record r : memberRecords){
                boolean isMatched = false;
                for(LeaderInfo info : memberLeaderList){
                    if(info.isMatched(r.getLeader())) {
                        info.addList(r);
                        isMatched = true;
                        break;
                    }
                }
                if(!isMatched)
                    memberLeaderList.add(new LeaderInfo(r.getLeader(), r));
            }
            Collections.sort(memberLeaderList);

            for(LeaderInfo li : memberLeaderList){
                List<Record> records = li.getRecordList();
                long total = 0;
                for(Record r : records){
                    total += r.getDamage();
                }
                long average = records.size() == 0 ? 0 : total / records.size();

                data.add(new String[]{
                        li.getLeader().getKoreanName(),
                        getNumberFormat(total),
                        Integer.toString(records.size()),
                        getNumberFormat(average)
                });
            }
        }

    }

    private void addSummary(List<String[]> data, int memberId) {
        addBlankLine(data);
        data.add(new String[]{"요약"});
        data.add(new String[]{"순위", "전체 딜량", "딜량 점유율(%)", "평균(막타X)", "친 횟수", "막타 횟수"});

        int rank = database.recordDao().getRankFromAllRecords(memberId, raidId);
        List<Record> allRecords = database.recordDao().getAllRecordsWithExtra(raidId);
        List<Record> memberRecords = database.recordDao().get1MemberRecordsWithExtra(memberId, raidId);

        int lastHitCount = 0;
        for(Record r : memberRecords){
            if(r.isLastHit())
                lastHitCount++;
        }
        int hitCount = memberRecords.size();
        long allDamage = getDamageFromList(allRecords, false);
        long memberDamage = getDamageFromList(memberRecords, false);
        long averageDamage = getAverageFromList(memberRecords);

        data.add(new String[]{String.valueOf(rank),
                getNumberFormat(memberDamage),
                getPercentage(memberDamage, allDamage),
                getNumberFormat(averageDamage),
                String.valueOf(hitCount),
                String.valueOf(lastHitCount)
        });
    }

    private void addHistory(List<String[]> data, int memberId) {
        addBlankLine(data);
        data.add(new String[]{"히스토리(막타는 * 표시)"});
        List<List<Record>> dayRecords = getAllRecordsByDays(memberId);
        String[] dayLine = new String[21];
        for(int i=0; i<7; i++)
            dayLine[i * 3] = "Day" + (i+1);
        data.add(dayLine);
        for(int t=0; t<3; t++) {
            String[] recordLine = new String[21];
            for (int i = 0; i < 7; i++) {
                if(dayRecords.get(i).size() <= t)
                    continue;
                Record r = dayRecords.get(i).get(t);

                recordLine[i * 3] = r.getBoss().getName() + "/" + getLevelFromRound(r.getRound());
                recordLine[i * 3 + 1] = r.getLeader().getKoreanName();
                recordLine[i * 3 + 2] = getNumberFormat(r.getDamage()) + (r.isLastHit() ? "*" : "");
            }
            data.add(recordLine);
        }
        String[] resultLine = new String[21];
        for (int i = 0; i < 7; i++) {
            resultLine[i * 3 + 1] = "합:";
            List<Record> records = dayRecords.get(i);
            long sum = 0;
            for(Record r : records)
                sum += r.getDamage();
            resultLine[i * 3 + 2] = getNumberFormat(sum);
        }
        data.add(resultLine);

        dayLine = new String[21];
        for(int i=0; i<7; i++)
            dayLine[i * 3] = "Day" + (i+8);
        data.add(dayLine);
        for(int t=0; t<3; t++) {
            String[] recordLine = new String[21];
            for (int i = 0; i < 7; i++) {
                if(dayRecords.get(i + 7).size() <= t)
                    continue;
                Record r = dayRecords.get(i + 7).get(t);
                recordLine[i * 3] = r.getBoss().getName() + "/" + getLevelFromRound(r.getRound());
                recordLine[i * 3 + 1] = r.getLeader().getKoreanName();
                recordLine[i * 3 + 2] = getNumberFormat(r.getDamage()) + (r.isLastHit() ? "*" : "");
            }
            data.add(recordLine);
        }
        resultLine = new String[21];
        for (int i = 0; i < 7; i++) {
            resultLine[i * 3 + 1] = "합:";
            List<Record> records = dayRecords.get(i + 7);
            long sum = 0;
            for(Record r : records)
                sum += r.getDamage();
            resultLine[i * 3 + 2] = getNumberFormat(sum);
        }
        data.add(resultLine);
    }

    private String getLevelFromRound(int round) {
        int[] levelPerRound = {50, 50, 55, 55, 60, 60};
        final int START_NUM = 65;
        final int START_IDX = 7;
        final int MAX_LEVEL = 80;

        int level = (round <= levelPerRound.length ? levelPerRound[round - 1] : START_NUM + (round - START_IDX));
        if (level > MAX_LEVEL) level = MAX_LEVEL;
        return Integer.toString(level);
    }

    private List<List<Record>> getAllRecordsByDays(int memberId) {
        List<List<Record>> ret = new ArrayList<>();

        for(int i=1; i<=14; i++) {
            List<Record> list = database.recordDao().get1DayRecordsWithExtra(
                    memberId, raidId, i);
            ret.add(list);
        }

        return ret;
    }

    private long getDamageFromList(List<Record> records, boolean isAdjusted) {
        long damage = 0;
        for(Record r: records) {
            if(isAdjusted) {
                double hardness = r.getBoss().getHardness();
                damage += (r.getDamage()) * hardness;
            }
            else
                damage += r.getDamage();
        }

        return damage;
    }

    private long getAverageFromList(List<Record> records) {
        long damage = 0;
        int cnt = 0;
        for(Record r: records) {
            if(r.isLastHit())
                continue;
            damage += r.getDamage();
            cnt++;
        }

        return cnt == 0 ? 0 : damage / cnt;
    }

    private long getAverageFromList(List<Record> records, boolean isAdjust) {
        long damage = 0;
        int cnt = 0;
        for(Record r: records) {
            if(r.isLastHit())
                continue;
            if(isAdjust)
                damage += (r.getDamage() * r.getBoss().getHardness());
            else
                damage += r.getDamage();
            cnt++;
        }

        return cnt == 0 ? 0 : damage / cnt;
    }

    private long getAverageFromList(List<Record> records, int round) {
        long damage = 0;
        int cnt = 0;
        for(Record r: records) {
            if(r.isLastHit()) continue;
            if(r.getRound() < round) continue;

            damage += r.getDamage();
            cnt++;
        }

        return cnt == 0 ? 0 : damage / cnt;
    }

    private String getNumberFormat(long num) {
        return NumberFormat.getNumberInstance(Locale.US).format(num);
    }

    private String getPercentage(long memberDamage, long allDamage) {
        if(allDamage == 0)
            return "0.00";
        return String.format("%.2f", memberDamage/(double)allDamage * 100) + "%";
    }

    private void addBlankLine(List<String[]> data) {
        data.add(new String[]{});
    }

    private HSSFColor getColorFromElement(int elementId, HSSFWorkbook wb) {
        switch(elementId){
            case 1://화
                return wb.getCustomPalette().findSimilarColor(255, 153, 153);
            case 2://수
                return wb.getCustomPalette().findSimilarColor(204, 236, 255);
            case 3://지
                return wb.getCustomPalette().findSimilarColor(255, 204, 153);
            case 4://광
                return wb.getCustomPalette().findSimilarColor(255, 255, 153);
            case 5://암
                return wb.getCustomPalette().findSimilarColor(204, 204, 255);
            case 6://무
                return wb.getCustomPalette().findSimilarColor(180, 180, 180);
        }
        return wb.getCustomPalette().findSimilarColor(255, 255, 255);
    }

    private String getElementFromId(int elementId) {
        switch(elementId){
            case 1:
                return "화";
            case 2:
                return "수";
            case 3:
                return "지";
            case 4:
                return "광";
            case 5:
                return "암";
            case 6:
                return "무";
        }
        return "없음";
    }

    private void setSingleCellValueAndStyle(Cell cell, CellStyle style, String str) {
        setStyleWithBorder(style);
        cell.setCellValue(str);
        cell.setCellStyle(style);
    }

    private void setHorzTitleStyle(CellStyle style, short color) {
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
    }

    private void setVertTitleStyle(CellStyle style, short color) {
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setRotation((short) 0xff);
    }

    private void setSubtitleStyle(CellStyle style) {
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setRotation((short) 0xff);
    }

    private void setDataCellStyle(CellStyle style){
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
    }

    private void setColorCellStyle(CellStyle style, HSSFColor color){
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
    }


    private void mergeCell(int sr, int er, int sc, int ec, String str) {
        Row row = getOrCreateRow(sr);
        Cell cell = row.getCell(sc) != null ? row.getCell(sc) : row.createCell(sc);
        cell.setCellValue(str);
        sheet.addMergedRegion(new CellRangeAddress(sr, er, sc, ec));
    }


    private void mergeCellWithBorder(int sr, int er, int sc, int ec, CellStyle style, String str) {
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

    private Row getOrCreateRow(int rowNum) {
        return sheet.getRow(rowNum) == null ? sheet.createRow(rowNum) : sheet.getRow(rowNum);
    }

    private void setStyleWithBorder(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
    }

    private String getShortWord(String name) {
        if(name.length() > 2)
            return name.substring(0, 2);
        return name;
    }
}
