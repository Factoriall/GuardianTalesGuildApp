package org.techtown.gtguildraid.etc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFOptimiser;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;
import org.techtown.gtguildraid.interfaces.CalculateFormatHelper;
import org.techtown.gtguildraid.interfaces.PoiHelper;
import org.techtown.gtguildraid.models.daos.Boss;
import org.techtown.gtguildraid.models.daos.GuildMember;
import org.techtown.gtguildraid.models.daos.Hero;
import org.techtown.gtguildraid.models.LeaderInfo;
import org.techtown.gtguildraid.models.daos.Raid;
import org.techtown.gtguildraid.models.daos.Record;
import org.techtown.gtguildraid.utils.RoomDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemberPoi extends PoiHelper {
    private final GuildMember member;
    private final int memberId;
    private final Raid raid;
    private final int raidId;
    private final RoomDB database;
    private final List<Boss> bosses;
    private int startDay;
    private double lhValue;
    private final CalculateFormatHelper calcHelper = new CalculateFormatHelper();

    HSSFColor titleColor;
    HSSFColor sub2Color;
    HSSFColor sub3Color;

    CellStyle dataCellStyle;
    CellStyle subtitleStyle;
    CellStyle subtitle2Style;
    CellStyle subtitle3Style;

    public MemberPoi(Raid raid, GuildMember member, RoomDB db,
                     boolean isDay1Contained, double lhValue, Context context){
        super(raid.getName(), context);
        this.raid = raid;
        raidId = raid.getRaidId();
        this.member = member;
        memberId = member.getID();
        database = db;
        bosses = raid.getBossList();
        this.startDay = isDay1Contained ? 1 : 2;
        this.lhValue = lhValue;
    }

    @Override
    public void exportDataToExcel() {
        //File file = new File(directory,raid.getName() + "_개인_" + member.getName() +  ".xls");

        //색깔 만들어주기
        titleColor = wb.getCustomPalette().findSimilarColor(220, 220, 220);
        sub2Color = wb.getCustomPalette().findSimilarColor(255, 255, 204);
        sub3Color = wb.getCustomPalette().findSimilarColor(204, 255, 204);
        setColumnWidth(0, 60);

        //제목 스타일 만들기
        CellStyle title = wb.createCellStyle();
        setCellStyle(title, 16, true);
        setColorInStyle(title, titleColor);

        //cellStyle 미리 만들기
        subtitleStyle = wb.createCellStyle();
        subtitle2Style = wb.createCellStyle();
        subtitle3Style = wb.createCellStyle();
        dataCellStyle = wb.createCellStyle();

        setCellStyle(subtitleStyle, 12, false);

        setCellStyle(subtitle2Style, false);
        setColorInStyle(subtitle2Style, sub2Color);

        setCellStyle(subtitle3Style, true);
        setColorInStyle(subtitle3Style, sub3Color);

        setCellStyle(dataCellStyle, true);

        for(int i=1; i<=21; i++) {
            if(i % 3 == 0)
                setColumnWidth(i, 110);
            else
                setColumnWidth(i, 55);
        }

        int rowNum = 0;
        String titleName = member.getName() + " // " + raid.getName() + " 개인 딜 부검표";
        setCellValueAndStyle(0, 0, 0, 21, title, titleName);
        Row row = getOrCreateRow(rowNum);
        row.setHeightInPoints((short) 36);
        rowNum = addSummaryToExcel(rowNum + 1);
        rowNum = addHistoryToExcel(rowNum);
        rowNum = addBossInfoToExcel(rowNum);
        addExplanationToExcel(rowNum);

        writeFile(raid.getName() + "_개인_" + member.getName() +  ".xls");
    }

    private int addSummaryToExcel(int rowNum) {
        //row 크기 조정
        setWidthAndSideTitle(rowNum, 5, "요약");

        //길드 요약
        int colNum = 1;
        setCellValueAndStyle(rowNum, rowNum + 4, colNum,colNum, subtitle2Style, "길드");

        colNum += 1;
        setCellValueAndStyle(rowNum, rowNum, colNum, colNum + 1, subtitle3Style, "총 딜량&달성률");
        long totalDamageInGuild = database.recordDao().getTotalDamageInRaid(raidId);
        setCellValueAndStyle(rowNum+1, rowNum+2, colNum, colNum + 1,
                dataCellStyle, calcHelper.getNumberFormat(totalDamageInGuild));
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

        setCellValueAndStyle(rowNum + 3, rowNum + 4, colNum, colNum + 1, dataCellStyle, maxRound + "회차/" + calcHelper.getPercentage(maxRoundSum, allBossHp));

        //보스 요약
        colNum += 2;
        setCellValueAndStyle(rowNum, rowNum + 4, colNum, colNum, subtitle2Style, "보스 구성");

        colNum += 1;
        Row row = getOrCreateRow(rowNum);
        setCellValueAndStyle(row.createCell(colNum), subtitle3Style, "속성");
        setCellValueAndStyle(row.createCell(colNum+1), subtitle3Style, "이름");
        setCellValueAndStyle(row.createCell(colNum+2), subtitle3Style, "배율");

        List<Boss> bosses = database.raidDao().getBossesList(raidId);
        for(int b = 1; b <= bosses.size(); b++){
            CellStyle elementStyle = wb.createCellStyle();
            setCellStyle(elementStyle, true);
            Boss boss = bosses.get(b-1);
            int nRow = rowNum + b;
            row = sheet.getRow(nRow);
            int elementId = boss.getElementId();
            HSSFColor elementColor = getColorFromElement(elementId);
            setColorInStyle(elementStyle, elementColor);
            setCellValueAndStyle(row.createCell(colNum), elementStyle, calcHelper.getElementFromId(elementId));
            setCellValueAndStyle(row.createCell(colNum+1), dataCellStyle, boss.getName());
            setCellValueAndStyle(row.createCell(colNum+2), dataCellStyle, Double.toString(boss.getHardness()));
        }

        //개인 요약
        colNum += 3;
        setCellValueAndStyle(rowNum, rowNum + 4, colNum, colNum, subtitle2Style, "개인");

        colNum += 1;
        row = getOrCreateRow(rowNum);
        setCellValueAndStyle(row.createCell(colNum), subtitle3Style, "배율");
        setCellValueAndStyle(row.createCell(colNum + 1),  subtitle3Style, "시즌");
        setCellValueAndStyle(row.createCell(colNum + 2), subtitle3Style, "순위");
        setCellValueAndStyle(rowNum, rowNum, colNum+3, colNum+5, subtitle3Style, "총 딜량/점유율");
        setCellValueAndStyle(row.createCell(colNum + 6), subtitle3Style, "평균 딜량");
        setCellValueAndStyle(rowNum, rowNum, colNum+7, colNum+8, subtitle3Style, "성장률");
        setCellValueAndStyle(row.createCell(colNum + 9), subtitle3Style, "배율 상승률");

        setCellValueAndStyle(rowNum+1, rowNum+2, colNum, colNum, subtitle3Style, "OFF");
        setCellValueAndStyle(rowNum+3, rowNum+4, colNum, colNum, subtitle3Style, "ON");

        colNum += 1;
        row = getOrCreateRow(rowNum + 1);
        setCellValueAndStyle(row.createCell(colNum), dataCellStyle, "전");

        CellStyle colorStyle = wb.createCellStyle();
        setCellStyle(colorStyle, true);
        setColorInStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 217, 102));

        row = getOrCreateRow(rowNum + 2);
        setCellValueAndStyle(row.createCell(colNum), colorStyle, "현");

        row = getOrCreateRow(rowNum + 3);
        setCellValueAndStyle(row.createCell(colNum), dataCellStyle, "전");

        row = getOrCreateRow(rowNum + 4);
        setCellValueAndStyle(row.createCell(colNum), colorStyle, "현");
        int aRow = 1;
        colNum += 1;

        int pastRaidId;

        Raid pastRaid = database.raidDao().getPastRecentRaid(raid.getStartDay());
        if(pastRaid == null) pastRaidId = -1;
        else pastRaidId = pastRaid.getRaidId();

        //배율 OFF 데이터 넣기
        int pastRank;
        if(pastRaidId == -1) pastRank = -1;
        else pastRank = database.recordDao().getRankFromAllRecords(memberId, pastRaidId, startDay, lhValue);

        int rank = database.recordDao().getRankFromAllRecords(memberId, raidId, startDay, lhValue);

        List<Record> beforeAll = new ArrayList<>();
        List<Record> beforeMember = new ArrayList<>();
        if(pastRaidId != -1){
            beforeAll = database.recordDao().getAllRecordsWithExtra(pastRaidId);
            beforeMember = database.recordDao().get1MemberRecordsWithExtra(memberId, pastRaidId);
        }
        List<Record> allRecords = database.recordDao().getAllRecordsWithExtra(raidId);
        List<Record> memberRecords = database.recordDao().get1MemberRecordsWithExtra(memberId, raidId);

        long allDamage = calcHelper.getDamageFromList(allRecords, false);
        long memberDamage = calcHelper.getDamageFromList(memberRecords, false);
        long averageDamage = calcHelper.getAverageFromList(memberRecords, false);

        long allBeforeDamage = calcHelper.getDamageFromList(beforeAll, false);
        long memberBeforeDamage = calcHelper.getDamageFromList(beforeMember, false);
        long averageBeforeDamage = calcHelper.getAverageFromList(beforeMember, false);

        String pRankText = pastRaidId != -1 ? Integer.toString(pastRank) : "-";
        String pDamageText = pastRaidId != -1 ? calcHelper.getNumberFormat(memberBeforeDamage) : "-";
        String pPercentText = pastRaidId != -1 ? calcHelper.getPercentage(memberBeforeDamage, allBeforeDamage) : "-";
        String pAverageText = pastRaidId != -1 ? calcHelper.getNumberFormat(averageBeforeDamage) : "-";
        //전 시즌 라인
        setCellValueAndStyle(getOrCreateRow(rowNum+aRow).createCell(colNum), dataCellStyle, pRankText);
        setCellValueAndStyle(rowNum+aRow, rowNum+aRow, colNum+1, colNum+3, dataCellStyle, pDamageText + " / " + pPercentText);
        setCellValueAndStyle(getOrCreateRow(rowNum+aRow).createCell(colNum+4), dataCellStyle, pAverageText);
        //현 시즌
        setCellValueAndStyle(getOrCreateRow(rowNum+aRow+1).createCell(colNum), colorStyle, Integer.toString(rank));
        setCellValueAndStyle(rowNum+aRow+1, rowNum+aRow+1, colNum+1, colNum+3, colorStyle, calcHelper.getNumberFormat(memberDamage) +
                " / " + calcHelper.getPercentage(memberDamage, allDamage));
        setCellValueAndStyle(getOrCreateRow(rowNum+aRow+1).createCell(colNum+4), colorStyle, calcHelper.getNumberFormat(averageDamage));

        aRow += 2;
        //배율 ON 데이터 넣기
        int rankAdjust = database.recordDao().getRankFromAllAdjustRecords(memberId, raidId, startDay, lhValue);
        int pastRankAdj = database.recordDao().getRankFromAllAdjustRecords(memberId, pastRaidId, startDay, lhValue);

        long allDamageAdjust = calcHelper.getDamageFromList(allRecords, true);
        long memberDamageAdjust = calcHelper.getDamageFromList(memberRecords, true);
        long averageDamageAdjust = calcHelper.getAverageFromList(memberRecords, true);

        long allBeforeDamageAdj = calcHelper.getDamageFromList(beforeAll, true);
        long memberBeforeDamageAdj = calcHelper.getDamageFromList(beforeMember, true);
        long averageBeforeDamageAdj = calcHelper.getAverageFromList(beforeMember, true);

        pRankText = pastRaidId != -1 ? Integer.toString(pastRankAdj) : "-";
        pDamageText = pastRaidId != -1 ? calcHelper.getNumberFormat(memberBeforeDamageAdj) : "-";
        pPercentText = pastRaidId != -1 ? calcHelper.getPercentage(memberBeforeDamageAdj, allBeforeDamageAdj) : "-";
        pAverageText = pastRaidId != -1 ? calcHelper.getNumberFormat(averageBeforeDamageAdj) : "-";
        //전 시즌 라인
        setCellValueAndStyle(getOrCreateRow(rowNum+aRow).createCell(colNum), dataCellStyle, pRankText);
        setCellValueAndStyle(rowNum+aRow, rowNum+aRow, colNum+1, colNum+3, dataCellStyle, pDamageText + " / " + pPercentText);
        setCellValueAndStyle(getOrCreateRow(rowNum+aRow).createCell(colNum+4), dataCellStyle, pAverageText);
        //현 시즌
        setCellValueAndStyle(getOrCreateRow(rowNum+aRow+1).createCell(colNum), colorStyle, Integer.toString(rankAdjust));
        setCellValueAndStyle(rowNum+aRow+1, rowNum+aRow+1, colNum+1, colNum+3, colorStyle, calcHelper.getNumberFormat(memberDamageAdjust)
                + " / " + calcHelper.getPercentage(memberDamageAdjust, allDamageAdjust));
        setCellValueAndStyle(getOrCreateRow(rowNum+aRow+1).createCell(colNum+4), colorStyle, calcHelper.getNumberFormat(averageDamageAdjust));

        //성장률
        aRow -= 2;
        colNum += 5;
        String growthText = pastRaidId != -1 ? calcHelper.getPercentage(averageDamage - averageBeforeDamage, averageBeforeDamage): "-";
        String growthTextAdj = pastRaidId != -1 ? calcHelper.getPercentage(averageDamageAdjust - averageBeforeDamageAdj, averageBeforeDamageAdj): "-";
        setCellValueAndStyle(rowNum + aRow, rowNum+aRow+1, colNum, colNum+1, dataCellStyle, growthText);
        setCellValueAndStyle(rowNum + aRow+2, rowNum+aRow+3, colNum, colNum+1, dataCellStyle, growthTextAdj);

        setCellValueAndStyle(rowNum + aRow, rowNum+aRow+3, colNum+2, colNum+2, dataCellStyle, calcHelper.getPercentage(memberDamageAdjust - memberDamage, memberDamage));

        colNum += 3;

        setCellValueAndStyle(rowNum, rowNum + 4, colNum, colNum, subtitle2Style, "특수 상황");
        colNum += 1;
        row = getOrCreateRow(rowNum);
        setCellValueAndStyle(row.createCell(colNum), subtitle3Style, "상황");
        setCellValueAndStyle(row.createCell(colNum + 1), subtitle3Style, "횟수");

        row = getOrCreateRow(rowNum + 1);
        colNum -= 1;

        colorStyle = wb.createCellStyle();
        setCellStyle(colorStyle, true);
        setColorInStyle(colorStyle, wb.getCustomPalette().findSimilarColor(146, 208, 80));
        setCellValueAndStyle(row.createCell(colNum + 1), colorStyle, "막타");

        row = getOrCreateRow(rowNum + 2);
        colorStyle = wb.createCellStyle();
        setCellStyle(colorStyle, true);
        setColorInStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 255, 50));
        setCellValueAndStyle(row.createCell(colNum + 1), colorStyle, "부족");

        row = getOrCreateRow(rowNum + 3);
        colorStyle = wb.createCellStyle();
        setCellStyle(colorStyle, true);
        setColorInStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 60, 60));
        setCellValueAndStyle(row.createCell(colNum + 1), colorStyle, "전복");

        row = getOrCreateRow(rowNum + 4);
        colorStyle = wb.createCellStyle();
        setCellStyle(colorStyle, true);
        setColorInStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 0, 0));
        setCellValueAndStyle(row.createCell(colNum + 1), colorStyle, "소멸");

        int noHitCount = 42 - memberRecords.size();
        setCellValueAndStyle(row.createCell(colNum + 2), dataCellStyle, Integer.toString(noHitCount));

        //mergeCellWithBorder(rowNum + aRow, rowNum+aRow+3, colNum+9, colNum+9, dataCellStyle, Integer.toString(lastHitCount));
        return rowNum + 5;
    }

    private int addHistoryToExcel(int rowNum) {
        setWidthAndSideTitle(rowNum, 10, "히스토리");

        //히스토리 설정
        CellStyle colorStyle;

        int colNum = 1;
        int lastCnt = 0;
        int lowCnt = 0;
        int boomCnt = 0;
        List<List<Record>> dayRecords = getAllRecordsByDays(memberId);
        for(int r=0; r<=1; r++) {//2개 행으로 생성
            for (int d = 0; d < 7; d++) {//7개의 열로 생성

                int day = d + r * 7;
                Log.d("excel", "day:" + day);
                int startCol = colNum + d * 3;
                colorStyle = wb.createCellStyle();
                setCellStyle(colorStyle, true);
                setColorInStyle(colorStyle, sub2Color);

                setCellValueAndStyle(rowNum, rowNum, startCol, startCol + 2, colorStyle, "Day " + (day + 1));

                long sum = 0;
                //Day 1 - 7 데이터 삽입
                for (int t = 1; t <= 3; t++) {//데이터 수만큼 돌리기
                    if (dayRecords.get(day).size() < t) {//데이터 없으면 border만 추가
                        addBorder(rowNum + t, rowNum + t, startCol, startCol + 2);
                        continue;
                        /*
                        CellRangeAddress region = new CellRangeAddress(rowNum + t, rowNum + t, startCol, startCol + 2);
                        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
                        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
                        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
                        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
                        continue;*/
                    }

                    //데이터 추가 작업
                    Record record = dayRecords.get(day).get(t - 1);
                    Boss boss = record.getBoss();
                    Hero leader = record.getLeader();
                    long damage = record.getDamage();
                    long average = database.recordDao().getAverageOfMyLeader(raidId, memberId, boss.getBossId(), leader.getHeroId(), 0);
                    Log.d("excel", "damage:" + damage);

                    sum += damage;

                    int bossElementId = boss.getElementId();
                    int leaderElementId = leader.getElement();
                    HSSFColor bossElementColor = getColorFromElement(bossElementId);
                    HSSFColor leaderElementColor = getColorFromElement(leaderElementId);

                    colorStyle = wb.createCellStyle();
                    setCellStyle(colorStyle, true);
                    setColorInStyle(colorStyle, bossElementColor);
                    setCellValueAndStyle(sheet.getRow(rowNum + t).createCell(startCol), colorStyle, calcHelper.getShortWord(boss.getName()));

                    colorStyle = wb.createCellStyle();
                    setCellStyle(colorStyle, true);
                    setColorInStyle(colorStyle, leaderElementColor);
                    setCellValueAndStyle(sheet.getRow(rowNum + t).createCell(startCol + 1), colorStyle, calcHelper.getShortWord(leader.getKoreanName()));

                    colorStyle = wb.createCellStyle();
                    setCellStyle(colorStyle, true);
                    if(record.isLastHit()) {
                        lastCnt++;
                        setColorInStyle(colorStyle, wb.getCustomPalette().findSimilarColor(146, 208, 80));
                    }
                    else if(calcHelper.betweenRange(record.getDamage(), average, 75, 85)) {
                        lowCnt++;
                        setColorInStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 255, 50));
                    }
                    else if(calcHelper.betweenRange(record.getDamage(), average, 0, 75)) {
                        boomCnt++;
                        setColorInStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 60, 60));
                    }

                    setCellValueAndStyle(sheet.getRow(rowNum + t).createCell(startCol + 2), colorStyle, calcHelper.getNumberFormat(damage));
                }
                colorStyle = wb.createCellStyle();
                setCellStyle(colorStyle, true);
                setColorInStyle(colorStyle, wb.getCustomPalette().findSimilarColor(255, 217, 102));
                setCellValueAndStyle(rowNum + 4, rowNum + 4, startCol, startCol + 2, colorStyle, calcHelper.getNumberFormat(sum));
                HSSFOptimiser.optimiseCellStyles(wb);//optimize 통해 계속 cellStyle 쓸 수 있게 처리리
            }
            rowNum += 5;
        }
        int cNum = 21;
        Row row = getOrCreateRow(2);
        setCellValueAndStyle(row.createCell(cNum), dataCellStyle, Integer.toString(lastCnt));
        row = getOrCreateRow(3);
        setCellValueAndStyle(row.createCell(cNum), dataCellStyle, Integer.toString(lowCnt));
        row = getOrCreateRow(4);
        setCellValueAndStyle(row.createCell(cNum), dataCellStyle, Integer.toString(boomCnt));

        return rowNum;
    }

    private int addBossInfoToExcel(int rowNum) {
        setWidthAndSideTitle(rowNum, 14, "보스별 상세 정보");

        int bossIdx = 0;
        for(int r = 0; r < 2; r++){
            setBossInfo(rowNum, memberId, bosses.get(bossIdx++), true);
            setBossInfo(rowNum, memberId, bosses.get(bossIdx++), false);

            rowNum += 7;
        }

        return rowNum;
    }

    @SuppressLint("DefaultLocale")
    private void setBossInfo(int rowNum, int memberId, Boss boss, boolean isLeft ) {
        int bossId = boss.getBossId();
        List<Record> allRecords = database.recordDao().get1BossRecordsWithExtra(raidId, bossId);
        List<Record> memberRecords = database.recordDao()
                .get1MemberRecordsWithExtra(memberId, raidId, bossId);

        long guildAverage = calcHelper.getAverageFromList(allRecords, false);
        long memberDamage = calcHelper.getDamageFromList(memberRecords, false);
        long allDamage = calcHelper.getDamageFromList(allRecords, false);
        long memberAverage = calcHelper.getAverageFromList(memberRecords, false);
        int rankTotal = database.recordDao().getTotalRankFromBossRecords(memberId, raidId, bossId);
        int rankAvg = memberAverage == 0 ? 0 : database.recordDao().getAvgRankFromBossRecords(memberId, raidId, bossId);
        String avg = memberAverage == 0 ? "-" : calcHelper.getNumberFormat(memberAverage);
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
        setCellStyle(dayStyle, true);
        setColorInStyle(dayStyle, sub2Color);

        CellStyle totalStyle = wb.createCellStyle();
        setCellStyle(totalStyle, true);
        setColorInStyle(totalStyle, wb.getCustomPalette().findSimilarColor(255, 217, 102));

        colNum = startCol;
        //길드 평균 삽입
        final String[] subtitles = {"길드 평균", "친 횟수", "총 딜량 / 점유율", "평균", "점유율 순위", "평균 순위"};
        final String[] briefValues = {calcHelper.getNumberFormat(guildAverage),
                Integer.toString(memberRecords.size()),
                calcHelper.getNumberFormat(memberDamage) + " / " + calcHelper.getPercentage(memberDamage, allDamage),
                avg,
                rTotal, rAvg};

        if(usingCols[0] == 1){
            setCellValueAndStyle(getOrCreateRow(rowNum).createCell(colNum), dayStyle, subtitles[0]);
            setCellValueAndStyle(getOrCreateRow(rowNum + 1).createCell(colNum), totalStyle, briefValues[0]);
        }
        else{
            setCellValueAndStyle(rowNum, rowNum, colNum, colNum + usingCols[0] - 1, dayStyle, subtitles[0]);
            setCellValueAndStyle(rowNum + 1, rowNum + 1, colNum, colNum + usingCols[0] - 1, totalStyle, briefValues[0]);
        }

        //brief 정보 삽입
        colNum += usingCols[0];
        for (int i = 1; i < 6; i++) {
            if (usingCols[i] == 1) {
                setCellValueAndStyle(getOrCreateRow(rowNum).createCell(colNum), subtitle3Style, subtitles[i]);
                setCellValueAndStyle(getOrCreateRow(rowNum + 1).createCell(colNum), dataCellStyle, briefValues[i]);
            } else {
                setCellValueAndStyle(rowNum, rowNum, colNum, colNum + usingCols[i] - 1, subtitle3Style, subtitles[i]);
                setCellValueAndStyle(rowNum + 1, rowNum + 1, colNum, colNum + usingCols[i] - 1, dataCellStyle, briefValues[i]);
            }
            colNum += usingCols[i];
        }

        //leader 정보 삽입
        colNum = startCol;
        rowNum += 2;

        final String[] leaderSub = {"리더", "총 딜량 / 지분", "횟수(막타)", "평균 딜량", "인분"};

        setCellValueAndStyle(rowNum, rowNum + 4, colNum, colNum + usingCols[0] - 1, subtitle2Style, boss.getName());
        setCellValueAndStyle(rowNum, rowNum, colNum + usingCols[0], colNum + width, subtitle3Style, "리더별 보스 상대 TOP3");
        colNum += usingCols[0];
        rowNum += 1;
        for(int i = 1; i < 6; i++){
            if (usingCols[i] == 1)
                setCellValueAndStyle(getOrCreateRow(rowNum).createCell(colNum), subtitle3Style, leaderSub[i - 1]);
            else
                setCellValueAndStyle(rowNum, rowNum, colNum, colNum + usingCols[i] - 1, subtitle3Style, leaderSub[i - 1]);

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
                averageAbove76 = calcHelper.getAverageFromList(records, 18);
            }
            long guildAvg76 = database.recordDao().getAverageOfLeaderFromRecords(raidId, bossId, leader.getHeroId(), 18);

            String ACPL = "-";
            if(averageAbove76 != 0)
                ACPL = String.format("%.3f", (float) averageAbove76 / guildAvg76);

            long leaderDamage = calcHelper.getDamageFromList(records, false);
            final String[] leaderValues = {leader.getKoreanName(),
                    calcHelper.getNumberFormat(leaderDamage) + " / "
                            + calcHelper.getPercentage(leaderDamage, memberDamage),
                    records.size() + "(" + calcHelper.getLastHitNum(records) + ")",
                    calcHelper.getNumberFormat(calcHelper.getAverageFromList(records)),
                    ACPL};

            for(int i = 1; i < 6; i++){
                if (usingCols[i] == 1)
                    setCellValueAndStyle(getOrCreateRow(rowNum).createCell(colNum), dataCellStyle, leaderValues[i - 1]);
                else
                    setCellValueAndStyle(rowNum, rowNum, colNum, colNum + usingCols[i] - 1, dataCellStyle, leaderValues[i - 1]);

                colNum += usingCols[i];
            }
            rowNum += 1;
        }
    }

    private void addExplanationToExcel(int rowNum) {
        sheet.createRow(rowNum);
        sheet.createRow(rowNum + 1);
        sheet.createRow(rowNum + 2);
        sheet.createRow(rowNum + 3);
        String explanation1 = "* 모든 평균의 경우 막타를 제외하고 계산합니다. * 상승률은 전 시즌 대비 얼마나 평균이 올랐나, 배율 상승률은 현 시즌 배율OFF 대비 ON했을 시 얼마나 올랐나를 계산합니다.";
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

        CellRangeAddress region = new CellRangeAddress(rowNum, rowNum + 3, 0, 21);
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
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
        setCellValueAndStyle(sRow, sRow + height - 1, colNum, colNum, subtitleStyle, title);
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

    private List<List<Record>> getAllRecordsByDays(int memberId) {
        List<List<Record>> ret = new ArrayList<>();
        for(int i=1; i<=14; i++) {
            List<Record> list = database.recordDao().get1DayRecordsWithExtra(
                    memberId, raidId, i);
            ret.add(list);
        }

        return ret;
    }
}
