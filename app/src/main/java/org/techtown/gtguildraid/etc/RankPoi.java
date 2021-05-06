package org.techtown.gtguildraid.etc;

import android.util.Log;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.techtown.gtguildraid.interfaces.CalculateFormatHelper;
import org.techtown.gtguildraid.interfaces.PoiHelper;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.DamageInfo;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.io.File;
import java.util.List;

public class RankPoi extends PoiHelper {
    private final Raid raid;
    private final int raidId;
    private final RoomDB database;
    private final List<Boss> bosses;

    public RankPoi(Raid raid, RoomDB db) {
        super();
        this.raid = raid;
        raidId = raid.getRaidId();
        database = db;
        bosses = raid.getBossList();
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
        String titleName = raid.getName() + " - 순위표";
        setCellValueAndStyle(0, 0, 0, MAX_COLUMN - 1, title, titleName);
        Row row = getOrCreateRow(rowNum);
        row.setHeightInPoints((short) 36);

        rowNum = addTotalRank(rowNum + 1);
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
        List<DamageInfo> damageInfos = database.recordDao().getRanksOfAdjustRecords(raidId);
        for(DamageInfo info : damageInfos)
            Log.d("info", "name:" + database.memberDao().getMember(info.memberId).getName());

        setCellValueAndStyle(getOrCreateRow(rowNum).createCell(colNum), subtitle2Style, "순위");
        setCellValueAndStyle(getOrCreateRow(rowNum + 1).createCell(colNum), subtitle2Style, "이름");
        setCellValueAndStyle(getOrCreateRow(rowNum + 2).createCell(colNum), subtitle2Style, "딜량");
        for(int i=0; i<5; i++){
            final int first = i*2+1;
            final int second = i*2+2;
            CellStyle cellStyle = wb.createCellStyle();
            setCellStyle(cellStyle, true);
            int fontSize = 12;
            HSSFColor color = wb.getCustomPalette().findSimilarColor(0, 0,0);

            int rank = rankOrder[i];
            switch(rank){
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

            DamageInfo damageInfo = damageInfos.get(rank - 1);

            cellStyle = wb.createCellStyle();
            setCellStyle(cellStyle, true);
            setFontInStyle(cellStyle, fontSize, color);

            String name = database.memberDao().getMember(damageInfo.memberId).getName();
            setCellValueAndStyle(rowNum+1, rowNum+1, first, second, cellStyle, name);

            long damage = damageInfo.total;
            setCellValueAndStyle(rowNum+2, rowNum+2, first, second, cellStyle, calcHelper.getNumberFormat(damage));
        }

        return rowNum + 3;
    }


    private int addDetailRank(int i) {
        return 0;
    }
}
