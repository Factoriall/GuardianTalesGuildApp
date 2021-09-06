package org.techtown.gtguildraid.interfaces;

import org.techtown.gtguildraid.models.daos.Record;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CalculateFormatHelper {
    public String getLevelFromRound(int round) {
        int[] levelPerRound = {50, 50, 55, 55, 60, 60};
        final int START_NUM = 65;
        final int START_IDX = 7;
        final int MAX_LEVEL = 80;

        int level = (round <= levelPerRound.length ? levelPerRound[round - 1] : START_NUM + (round - START_IDX));
        if (level > MAX_LEVEL) level = MAX_LEVEL;
        return Integer.toString(level);
    }

    public long getDamageFromList(List<Record> records, boolean isAdjusted) {
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

    public String getElementFromId(int elementId) {
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

    public int getLastHitNum(List<Record> records) {
        int count = 0;
        for(Record r : records){
            if(r.isLastHit())
                count++;
        }
        return count;
    }

    public long getAverageFromList(List<Record> records) {
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

    public long getAverageFromList(List<Record> records, boolean isAdjust) {
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

    public long getAverageFromList(List<Record> records, int round) {
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

    public String getNumberFormat(long num) {
        return NumberFormat.getNumberInstance(Locale.US).format(num);
    }

    public String getPercentage(long memberDamage, long allDamage) {
        if(allDamage == 0)
            return "0.00";
        return String.format("%.2f", memberDamage/(double)allDamage * 100) + "%";
    }

    public String getPercentage(double div) {
        if(div == 0)
            return "0.00";
        return String.format("%.2f", div * 100.0) + "%";
    }

    public boolean betweenRange(long damage, long average, int min, int max) {
        float percent = (float) damage / average * 100f;
        if(percent >= min && percent < max)
            return true;
        return false;
    }

    private String getDiffOfRank(int pastRank, int rank) {
        if(pastRank == -1 || pastRank == 0) return "-";
        int diff = pastRank - rank;
        if(diff > 0) return diff + "▲";
        else if(diff < 0) return (-diff) + "▼";
        else return "동일";
    }

    public String getShortWord(String name) {
        if(name.length() > 2)
            return name.substring(0, 2);
        return name;
    }
}
