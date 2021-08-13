package org.techtown.gtguildraid.etc;

import android.util.Log;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.techtown.gtguildraid.interfaces.CalculateFormatHelper;
import org.techtown.gtguildraid.interfaces.PoiHelper;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.IdDouble;
import org.techtown.gtguildraid.models.IdLong;
import org.techtown.gtguildraid.models.IdLongCnt;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.io.File;
import java.util.Calendar;
import java.util.List;

public class RankPoi extends PoiHelper {
    private final Raid raid;
    private final int raidId;
    private final RoomDB database;
    private final List<Boss> bosses;
    private final boolean isAdjusted;
    private final int startDay;
    private final int maxDay;
    private final double lastHitValue;

    public RankPoi(Raid raid, RoomDB db, boolean isAdjusted, boolean isDay1Contained, int maxDay, double lastHitValue) {
        super(raid.getName());
        this.raid = raid;
        raidId = raid.getRaidId();
        database = db;
        bosses = raid.getBossList();
        this.isAdjusted = isAdjusted;
        this.startDay = isDay1Contained ? 1 : 2;
        this.maxDay = maxDay;
        this.lastHitValue = lastHitValue;
    }

    HSSFColor subColor;
    HSSFColor sub2Color;
    CellStyle dataCellStyle;
    CellStyle subtitleStyle;
    CellStyle subtitle2Style;
    final int MAX_COLUMN = 11;
    CalculateFormatHelper calcHelper = new CalculateFormatHelper();


    @Override
    public void exportDataToExcel() {
        File file = new File(directory,raid.getName() + "_순위표.xls");
        subColor = wb.getCustomPalette().findSimilarColor(255, 255, 204);
        sub2Color = wb.getCustomPalette().findSimilarColor(204, 255, 204);

        //타이틀 설정
        CellStyle title = wb.createCellStyle();
        setCellStyle(title, 16, true);
        setColorInStyle(title, wb.getCustomPalette().findSimilarColor(220, 220, 220));

        //자주 쓰는 스타일 세팅
        subtitleStyle = wb.createCellStyle();
        subtitle2Style = wb.createCellStyle();
        dataCellStyle = wb.createCellStyle();

        setCellStyle(subtitleStyle, true);
        setColorInStyle(subtitleStyle, subColor);

        setCellStyle(subtitle2Style, true);
        setColorInStyle(subtitle2Style, sub2Color);

        setCellStyle(dataCellStyle, true);

        //열 길이 설정
        setColumnWidth(0, 50);
        for(int i=1; i<MAX_COLUMN; i++) {
            if(i % 2 == 0)
                setColumnWidth(i, 150);
            else
                setColumnWidth(i, 100);
        }

        int rowNum = 0;
        String dayText = maxDay == 14
                ? "최종" : maxDay + "일차";
        String titleName = raid.getName() + " - " + dayText + " 순위표 / "
                + "배율 " + (isAdjusted ? "ON" : "OFF") + ","
                + "1일차 " + (startDay == 1 ? "포함" : "미포함") + ","
                + "막타 배율 " + String.format("%.1f", lastHitValue) + "배";
        setCellValueAndStyle(0, 0, 0, MAX_COLUMN - 1, title, titleName);
        Row row = getOrCreateRow(rowNum);
        row.setHeightInPoints((short) 36);

        rowNum = addTotalRank(rowNum + 1);
        Log.d("rowNum", "" + rowNum);
        rowNum = addDetailRank(rowNum);

        writeFile(file);
    }

    private int addTotalRank(int rowNum) {
        HSSFColor goldColor = wb.getCustomPalette().findSimilarColor(255, 192, 0);
        HSSFColor silverColor = wb.getCustomPalette().findSimilarColor(206, 206, 206);
        HSSFColor bronzeColor = wb.getCustomPalette().findSimilarColor(180, 90, 50);
        HSSFColor ironColor = wb.getCustomPalette().findSimilarColor(113, 113, 113);

        //제목 설정 - row: 1
        setCellValueAndStyle(rowNum, rowNum, 0, MAX_COLUMN - 1, subtitleStyle, "배율 적용 최종 순위");

        //순위 창
        rowNum += 1;
        int colNum = 0;
        setRowHeight(sheet.createRow(rowNum + 1), 70);
        setRowHeight(sheet.createRow(rowNum + 2), 35);

        int[] rankOrder = {4,2,1,3,5};
        List<IdLong> idLongs;
        if(isAdjusted)
            idLongs = database.recordDao().getRanksOfAdjustRecords(raidId, startDay, maxDay, lastHitValue);
        else
            idLongs = database.recordDao().getRanksOfNormalRecords(raidId, startDay, maxDay, lastHitValue);

        setCellValueAndStyle(getOrCreateRow(rowNum).createCell(colNum), subtitle2Style, "순위");
        setCellValueAndStyle(getOrCreateRow(rowNum + 1).createCell(colNum), subtitle2Style, "이름");
        setCellValueAndStyle(getOrCreateRow(rowNum + 2).createCell(colNum), subtitle2Style, "딜량");
        for(int i=0; i<5; i++) {
            final int first = i * 2 + 1;
            final int second = i * 2 + 2;
            CellStyle cellStyle = wb.createCellStyle();
            setCellStyle(cellStyle, true);
            int fontSize = 12;
            HSSFColor color = wb.getCustomPalette().findSimilarColor(0, 0, 0);

            int rank = rankOrder[i];
            switch (rank) {
                case 5:
                case 4:
                    color = ironColor;
                    break;
                case 3:
                    color = bronzeColor;
                    fontSize = 15;
                    break;
                case 2:
                    color = silverColor;
                    fontSize = 15;
                    break;
                case 1:
                    color = goldColor;
                    fontSize = 20;
            }
            setColorInStyle(cellStyle, color);
            setCellValueAndStyle(rowNum, rowNum, first, second, cellStyle, rankOrder[i] + "위");

            if (idLongs.size() >= rank) {
                IdLong idLong = idLongs.get(rank - 1);

                cellStyle = wb.createCellStyle();
                setCellStyle(cellStyle, true);
                setFontInStyle(cellStyle, fontSize, color);

                String name = database.memberDao().getMember(idLong.memberId).getName();
                setCellValueAndStyle(rowNum + 1, rowNum + 1, first, second, cellStyle, name);

                long damage = idLong.value;
                setCellValueAndStyle(rowNum + 2, rowNum + 2, first, second, cellStyle, calcHelper.getNumberFormat(damage));
            }
            else{
                addBorder(rowNum + 1, rowNum + 1, first, second);
                addBorder(rowNum + 2, rowNum + 2, first, second);
            }
        }

        rowNum += 3;
        colNum = 0;
        for(int i=1; i<=5; i++) {
            setCellValueAndStyle(getOrCreateRow(rowNum + i - 1).createCell(colNum), subtitle2Style,
                    (i*5 + 1) + "~" + ((i+1)*5) );
        }
        //남은 순위 border 추가
        addBorder(5, 9, 1, 10);

        for(int i=0; i<5; i++){
            colNum = 1;
            boolean isFinished = false;
            for(int j=0; j<5; j++){
                int rIdx = ((i+1) * 5)+j;
                if(rIdx >= idLongs.size()){
                    isFinished = true;
                    break;
                }
                setCellValueAndStyle(getOrCreateRow(rowNum + i).createCell(colNum), dataCellStyle,
                        database.memberDao().getMember(idLongs.get(rIdx).memberId).getName());
                setCellValueAndStyle(getOrCreateRow(rowNum + i).createCell(colNum + 1), dataCellStyle,
                        calcHelper.getNumberFormat(idLongs.get(((i+1)*5)+j).value));
                colNum += 2;
            }
            if(isFinished) break;
        }


        return rowNum + 5;
    }

    private int addDetailRank(int rowNum) {
        //제목 설정 - row: 1
        int initial = rowNum;
        setCellValueAndStyle(rowNum, rowNum, 0, MAX_COLUMN - 3, subtitleStyle, "보스 별 딜링 및 평균");
        setCellValueAndStyle(rowNum, rowNum+1, MAX_COLUMN - 2, MAX_COLUMN - 1, subtitleStyle, "헌신도");

        //자잘한 제목 붙이기
        rowNum += 1;
        setCellValueAndStyle(getOrCreateRow(rowNum).createCell(0), subtitleStyle, "보스");
        setCellValueAndStyle(getOrCreateRow(rowNum + 1).createCell(0), subtitleStyle, "순위");
        setCellValueAndStyle(rowNum + 1, rowNum + 1, 1, MAX_COLUMN - 3, subtitle2Style, "지분");
        setCellValueAndStyle(rowNum + 1, rowNum + 1, MAX_COLUMN - 2, MAX_COLUMN - 1, subtitle2Style, "막타 횟수");

        rowNum += 2;
        for(int i=0; i<5; i++)
            setCellValueAndStyle(getOrCreateRow(rowNum + i).createCell(0), subtitle2Style, (i+1) +"위");

        rowNum += 5;
        setCellValueAndStyle(getOrCreateRow(rowNum).createCell(0), subtitleStyle, "순위");
        setCellValueAndStyle(rowNum, rowNum, 1, MAX_COLUMN - 3, subtitle2Style, "평균(횟수)");
        setCellValueAndStyle(rowNum, rowNum, MAX_COLUMN - 2, MAX_COLUMN - 1, subtitle2Style, "배율 상승률");

        rowNum += 1;
        for(int i=0; i<5; i++)
            setCellValueAndStyle(getOrCreateRow(rowNum + i).createCell(0), subtitle2Style, (i+1) +"위");

        final int BOSS = initial + 1;
        final int FIRST = initial + 3;
        final int SECOND = initial + 9;
        for(int i=0; i<bosses.size(); i++){
            Boss boss = bosses.get(i);
            HSSFColor color = getColorFromElement(boss.getElementId());
            int firstCol = i * 2 + 1;
            int secondCol = i * 2 + 2;


            CellStyle cellStyle = wb.createCellStyle();
            setCellStyle(cellStyle, true);
            setColorInStyle(cellStyle, color);
            setCellValueAndStyle(BOSS, BOSS, firstCol, secondCol, cellStyle, boss.getName() + " - " + boss.getHardness() + "배율");
            List<IdLong> totalRank = database.recordDao().getRanksOfBossTotal(raidId, boss.getBossId(), startDay, maxDay);

            int minCount = (maxDay - startDay + 1) / 2;
            List<IdLongCnt> avgRank = database.recordDao().getRanksOfBossAverage(raidId, boss.getBossId(), minCount, startDay, maxDay);

            for(int r=0; r<5; r++) {
                if (totalRank.size() > r) {
                    setCellValueAndStyle(getOrCreateRow(FIRST + r).createCell(firstCol), dataCellStyle, database.memberDao().getMember(totalRank.get(r).memberId).getName());
                    setCellValueAndStyle(getOrCreateRow(FIRST + r).createCell(secondCol), dataCellStyle, calcHelper.getPercentage(totalRank.get(r).value,
                            database.recordDao().getTotalOfBoss(raidId, boss.getBossId(), startDay, maxDay)));

                    setCellValueAndStyle(getOrCreateRow(SECOND + r).createCell(firstCol), dataCellStyle, database.memberDao().getMember(avgRank.get(r).memberId).getName());
                    setCellValueAndStyle(getOrCreateRow(SECOND + r).createCell(secondCol), dataCellStyle, calcHelper.getNumberFormat(avgRank.get(r).value) + " (" + avgRank.get(r).count + ")");
                }
                else{
                    addBorder(FIRST + r, FIRST + r, firstCol, secondCol);
                    addBorder(SECOND + r, SECOND + r, firstCol, secondCol);
                }
            }
        }

        List<IdLong> lastRank = database.recordDao().getRanksOfLastHit(raidId, startDay, maxDay);
        List<IdDouble> growthRank = database.recordDao().getRanksOfAdjustGrowth(raidId, startDay, maxDay);
        for(int i=0; i<5; i++) {
            if (lastRank.size() > i) {
                setCellValueAndStyle(getOrCreateRow(FIRST + i).createCell(9), dataCellStyle, database.memberDao().getMember(lastRank.get(i).memberId).getName());
                setCellValueAndStyle(getOrCreateRow(FIRST + i).createCell(10), dataCellStyle, lastRank.get(i).value + "회");
            }
            else{
                addBorder(FIRST + i, FIRST + i, 9, 10);

            }

            if (growthRank.size() > i) {
                setCellValueAndStyle(getOrCreateRow(SECOND + i).createCell(9), dataCellStyle, database.memberDao().getMember(growthRank.get(i).memberId).getName());
                setCellValueAndStyle(getOrCreateRow(SECOND + i).createCell(10), dataCellStyle, calcHelper.getPercentage(growthRank.get(i).value));
            }
            else{
                addBorder(SECOND + i, SECOND + i, 9, 10);
            }
        }

        return 0;
    }
}
