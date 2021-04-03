package org.techtown.gtguildraid.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.GuildMember;
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

        Workbook wb = new HSSFWorkbook(); //or new XSSFWorkbook();
        Sheet sheet = wb.createSheet("new sheet");
        CellStyle title = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 14);
        title.setFont(font);
        CellStyle subtitle = wb.createCellStyle();
        font.setFontHeightInPoints((short) 12);
        subtitle.setFont(font);

        int rowNum = 0;
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(raid.getName() + " - " + name);
        cell.setCellStyle(title);

        rowNum = setSubtitle(sheet, rowNum, "요약", subtitle);
        rowNum = addSummaryToExcel(wb, sheet, rowNum + 1, member.getID());

        /*
        for(int i=0; i<10; i++)
            sheet.setColumnWidth(i, 3000);*/

        try {
            FileOutputStream os = new FileOutputStream(file);
            wb.write(os);
        } catch (IOException e) { e.printStackTrace(); }

    }

    private int setSubtitle(Sheet sheet, int rowNum, String s, CellStyle subtitle) {
        rowNum += 2;
        Row row = sheet.createRow(rowNum);
        setCellValueAndStyle(row, 0, s, subtitle);
        return rowNum;
    }

    private int addSummaryToExcel(Workbook wb, Sheet sheet, int rowNum, int memberId) {
        CellStyle borderStyle = wb.createCellStyle();
        borderStyle.setBorderTop(BorderStyle.MEDIUM);
        borderStyle.setBorderBottom(BorderStyle.MEDIUM);
        borderStyle.setBorderLeft(BorderStyle.MEDIUM);
        borderStyle.setBorderRight(BorderStyle.MEDIUM);

        Row row = sheet.createRow(rowNum);
        setCellValueAndStyle(row, 1, "순위", borderStyle);
        setCellValueAndStyle(row, 2, "전체 딜량", borderStyle);
        setCellValueAndStyle(row, 3, "딜량 점유율(%)", borderStyle);
        setCellValueAndStyle(row, 4, "평균(막타 X)", borderStyle);
        setCellValueAndStyle(row, 5, "친 횟수", borderStyle);
        setCellValueAndStyle(row, 6, "막타 횟수", borderStyle);

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
        long averageDamage = getAverageFromList(memberRecords, false);

        rowNum++;

        row = sheet.createRow(rowNum);
        setCellValueAndStyle(row, 0, "배율OFF", borderStyle);
        setCellValueAndStyle(row, 1, String.valueOf(rank), borderStyle);
        setCellValueAndStyle(row, 2, getNumberFormat(memberDamage), borderStyle);
        setCellValueAndStyle(row, 3, getPercentage(memberDamage, allDamage), borderStyle);
        setCellValueAndStyle(row, 4, getNumberFormat(averageDamage), borderStyle);
        setCellValueAndStyle(row, 5, String.valueOf(hitCount), borderStyle);
        setCellValueAndStyle(row, 6, String.valueOf(lastHitCount), borderStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum+1, 5,5));
        sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum+1, 6,6));



        rowNum++;
        row = sheet.createRow(rowNum);
        int rankAdjust = database.recordDao().getRankFromAllAdjustRecords(memberId, raidId);
        long allDamageAdjust = getDamageFromList(allRecords, true);
        long memberDamageAdjust = getDamageFromList(memberRecords, true);
        long averageDamageAdjust = getAverageFromList(memberRecords, true);

        setCellValueAndStyle(row, 0, "배율ON", borderStyle);
        setCellValueAndStyle(row, 1, String.valueOf(rankAdjust), borderStyle);
        setCellValueAndStyle(row, 2, getNumberFormat(memberDamageAdjust), borderStyle);
        setCellValueAndStyle(row, 3, getPercentage(memberDamageAdjust, allDamageAdjust), borderStyle);
        setCellValueAndStyle(row, 4, getNumberFormat(averageDamageAdjust), borderStyle);
        setCellValueAndStyle(row, 5, "", borderStyle);
        setCellValueAndStyle(row, 6, "", borderStyle);

        return rowNum + 1;
    }

    private void setCellValueAndStyle(Row row, int i, String str, CellStyle style) {
        Cell cell = row.createCell(i);
        cell.setCellValue(str);
        cell.setCellStyle(style);
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

    private String getNumberFormat(long num) {
        return NumberFormat.getNumberInstance(Locale.US).format(num);
    }

    private String getPercentage(long memberDamage, long allDamage) {
        if(allDamage == 0)
            return "0.00";
        return String.format("%.2f", memberDamage/(double)allDamage * 100);
    }

    private void addBlankLine(List<String[]> data) {
        data.add(new String[]{});
    }
}
