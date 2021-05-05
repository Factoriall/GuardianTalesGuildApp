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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class PoiHelper {
    protected HSSFWorkbook wb;
    protected Sheet sheet;
    protected File directory;

    protected PoiHelper(){
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("new sheet");
        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    }

    protected void writeFile(File file) {
        try {
            FileOutputStream os = new FileOutputStream(file);
            wb.write(os);
        } catch (IOException e) { e.printStackTrace(); }
    }

    protected void setColumnWidth(int col, int width) {
        sheet.setColumnWidth(col, width * 256 / 9);
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

    protected void setColorInStyle(CellStyle style, HSSFColor color){
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
}