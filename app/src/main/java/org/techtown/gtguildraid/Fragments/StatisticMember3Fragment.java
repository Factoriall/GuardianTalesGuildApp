package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;

import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StatisticMember3Fragment extends Fragment {
    final int ALL = 0;
    final int BOSS_1 = 1;
    final int BOSS_2 = 2;
    final int BOSS_3 = 3;
    final int BOSS_4 = 4;

    RoomDB database;
    TextView damage;
    TextView contribution;
    TextView hitNum;
    TextView rank;
    LineChart dpsChart;

    int memberId;
    int raidId;
    int position;
    boolean isAdjustMode;

    private class MemberTotalDamage implements Comparable<MemberTotalDamage>{
        int memberId;
        int totalDamage;

        MemberTotalDamage(int memberId, int totalDamage){
            memberId = this.memberId;
            totalDamage = this.totalDamage;
        }

        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int memberId) {
            this.memberId = memberId;
        }

        public int getTotalDamage() {
            return totalDamage;
        }

        public void setTotalDamage(int totalDamage) {
            this.totalDamage = totalDamage;
        }

        @Override
        public int compareTo(MemberTotalDamage m){
            return m.getTotalDamage() - totalDamage;
        }
    }


    public static StatisticMember3Fragment newInstance(int position, int memberId, int raidId, boolean isChecked) {
        StatisticMember3Fragment fragment = new StatisticMember3Fragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("memberId", memberId);
        args.putInt("raidId", raidId);
        args.putBoolean("isChecked", isChecked);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_member_3, container, false);
        database = RoomDB.getInstance(getActivity());
        if (getArguments() != null) {
            memberId = getArguments().getInt("memberId");
            raidId = getArguments().getInt("raidId");
            isAdjustMode = getArguments().getBoolean("isChecked");
            position = getArguments().getInt("position");
        }
        Log.d("isAdjustMode", Boolean.toString(isAdjustMode));

        damage = view.findViewById(R.id.damage);
        contribution = view.findViewById(R.id.contribution);
        hitNum = view.findViewById(R.id.hitNum);
        rank = view.findViewById(R.id.rank);
        dpsChart = view.findViewById(R.id.dpsChart);

        setView(position);

        return view;
    }

    private void setView(int position) {
        List<Boss> bosses = database.raidDao().getBossesList(raidId);
        switch(position){
            case ALL:
                setAllData();
                break;
            case BOSS_1:
                setBossData(bosses.get(0));
                break;
            case BOSS_2:
                setBossData(bosses.get(1));
                break;
            case BOSS_3:
                setBossData(bosses.get(2));
                break;
            case BOSS_4:
                setBossData(bosses.get(3));
                break;
        }
    }

    private void setAllData() {
        List<Record> allRecords = database.recordDao().getAllRecordsWithBoss(raidId);
        List<Record> memberRecords = database.recordDao().getCertainMemberRecordsWithBoss(memberId, raidId);

        int allDamage = getDamageFromList(allRecords, isAdjustMode);
        int memberDamage = getDamageFromList(memberRecords, isAdjustMode);

        damage.setText(NumberFormat.getNumberInstance(Locale.US).format(memberDamage));
        contribution.setText(getPercentage(memberDamage, allDamage) + "%");
        hitNum.setText(Integer.toString(memberRecords.size()));

        rank.setText(getRank(allRecords, memberDamage, isAdjustMode));
    }

    private void setBossData(Boss boss) {
    }


    private String getRank(List<Record> allRecords, int memberDamage, boolean isAdjustMode) {
        if(memberDamage == 0)
            return "-";

        List<MemberTotalDamage> damageList = new ArrayList<>();
        int mId = -1;
        int damage = 0;
        for(Record r : allRecords){
            if(mId != r.getMemberId()){
                if(mId != -1)
                    damageList.add(new MemberTotalDamage(mId, damage));
                mId = r.getMemberId();
                damage = 0;
            }

            if(isAdjustMode) {
                Boss b = r.getBoss();
                damage += (int) (r.getDamage() * b.getHardness());
            }
            else
                damage += r.getDamage();
        }
        damageList.add(new MemberTotalDamage(mId, damage));

        Collections.sort(damageList);

        int rank = 1;
        for(MemberTotalDamage d : damageList){
            if(d.getMemberId() == memberId)
                break;
            rank++;
        }

        return Integer.toString(rank);
    }

    private String getPercentage(int memberDamage, int allDamage) {
        return String.format("%.2f", memberDamage/(double)allDamage * 100);
    }

    private int getDamageFromList(List<Record> records, boolean isAdjustMode) {
        int damage = 0;
        for(Record r: records){
            if(isAdjustMode) {
                Boss b = r.getBoss();
                damage += (int) (r.getDamage() * b.getHardness());
            }
            else
                damage += r.getDamage();
        }
        return damage;
    }

}
