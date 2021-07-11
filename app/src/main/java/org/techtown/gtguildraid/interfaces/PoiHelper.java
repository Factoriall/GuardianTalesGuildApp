package org.techtown.gtguildraid.interfaces;

import android.os.Environment;

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
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class PoiHelper {
    protected HSSFWorkbook wb;
    protected Sheet sheet;
    protected File directory;

    protected PoiHelper(String raidName){
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("new sheet");
        directory = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "가테_길레_" + raidName);
        if(!directory.exists()) directory.mkdirs();
    }

    protected abstract void exportDataToExcel();

    protected void writeFile(File file) {
        try {
            FileOutputStream os = new FileOutputStream(file);
            wb.write(os);
        } catch (IOException e) { e.printStackTrace(); }
    }

    protected void setColumnWidth(int col, int width) {
        sheet.setColumnWidth(col, width * 256 / 9);
    }

    protected void setRowHeight(Row row, int height){
        row.setHeightInPoints((short) height);
    }

    protected void setCellValueAndStyle(int sr, int er, int sc, int ec, CellStyle style, String str) {
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

    protected void setCellValueAndStyle(Cell cell, CellStyle style, String str) {
        setStyleWithBorder(style);
        cell.setCellValue(str);
        cell.setCellStyle(style);
    }

    protected void mergeCell(int sr, int er, int sc, int ec, String str) {
        Row row = getOrCreateRow(sr);
        Cell cell = row.getCell(sc) != null ? row.getCell(sc) : row.createCell(sc);
        cell.setCellValue(str);
        sheet.addMergedRegion(new CellRangeAddress(sr, er, sc, ec));
    }

    protected void addBorder(int sRow, int eRow, int sCol, int eCol){
        CellRangeAddress region = new CellRangeAddress(sRow, eRow, sCol, eCol);
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
    }

    protected void setStyleWithBorder(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
    }

    protected Row getOrCreateRow(int rowNum) {
        return sheet.getRow(rowNum) == null ? sheet.createRow(rowNum) : sheet.getRow(rowNum);
    }

    protected void setCellStyle(CellStyle style, boolean isHorizontal){
        if(isHorizontal)
            setHorzCenterStyle(style);
        else
            setVertCenterStyle(style);
    }

    protected void setCellStyle(CellStyle style, int fontSize, boolean isHorizontal){
        setFontInStyle(style, fontSize);
        if(isHorizontal)
            setHorzCenterStyle(style);
        else
            setVertCenterStyle(style);
    }

    protected void setHorzCenterStyle(CellStyle style) {
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
    }

    protected void setVertCenterStyle(CellStyle style) {
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setRotation((short) 0xff);
    }

    protected void setFontInStyle(CellStyle style, int fontSize){
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) fontSize);
        style.setFont(font);
    }

    protected void setFontInStyle(CellStyle style, int fontSize, HSSFColor color){
        Font font = wb.createFont();
        font.setColor(color.getIndex());
        font.setFontHeightInPoints((short) fontSize);
        style.setFont(font);
    }

    protected void setColorInStyle(CellStyle style, HSSFColor color){
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    protected HSSFColor getColorFromElement(int elementId) {
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
}