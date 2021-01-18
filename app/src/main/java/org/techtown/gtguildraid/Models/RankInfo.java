package org.techtown.gtguildraid.Models;

public class RankInfo implements Comparable<RankInfo>{
    String memberName;
    int damage = 0;
    int hitNum;
    int finalDamage;

    public RankInfo(String name, int hitNum){
        memberName = name;
        this.hitNum = hitNum;
    }

    public void addDamage(int d){
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

    public int getDamage() {
        return damage;
    }

    public int getHitNum() {
        return hitNum;
    }

    public int getFinalDamage() {
        return finalDamage;
    }

    @Override
    public int compareTo(RankInfo rankInfo) {
        return rankInfo.finalDamage - finalDamage;
    }
}