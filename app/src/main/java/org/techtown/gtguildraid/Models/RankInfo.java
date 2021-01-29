package org.techtown.gtguildraid.Models;

public class RankInfo implements Comparable<RankInfo>{
    String memberName;
    long damage = 0;
    int hitNum;
    long finalDamage;

    public RankInfo(String name, int hitNum){
        memberName = name;
        this.hitNum = hitNum;
    }

    public void addDamage(long d){
        damage += d;
    }

    public void setFinalDamage(boolean isAverageMode){
        if(isAverageMode) {
            if(hitNum == 0)
                finalDamage = 0;
            else
                finalDamage = damage / hitNum;
        }
        else
            finalDamage = damage;
    }

    public String getMemberName() {
        return memberName;
    }

    public long getDamage() {
        return damage;
    }

    public int getHitNum() {
        return hitNum;
    }

    public long getFinalDamage() {
        return finalDamage;
    }

    @Override
    public int compareTo(RankInfo rankInfo) {
        if(finalDamage - rankInfo.finalDamage > 0)
            return -1;
        else if(finalDamage - rankInfo.finalDamage < 0)
            return 1;
        return 0;
    }
}