package org.techtown.gtguildraid.Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.techtown.gtguildraid.Utils.DateConverter;

import java.io.Serializable;
import java.util.List;

@Entity(tableName = "record")
@TypeConverters(DateConverter.class)
public class Record implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int recordID;

    private int memberId;
    private int raidId;
    private int day;

    private int bossId;
    private int damage;

    @Ignore
    public List<Integer> heroIds;

    @Ignore
    public Boss boss;

    public Record(int memberId, int raidId, int day) {
        this.memberId = memberId;
        this.raidId = raidId;
        this.day = day;
        this.bossId = -1;
        this.damage = 0;
    }

    public int getRecordID() {
        return recordID;
    }

    public void setRecordID(int recordID) {
        this.recordID = recordID;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getRaidId() {
        return raidId;
    }

    public void setRaidId(int raidId) {
        this.raidId = raidId;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public List<Integer> getHeroIds() {
        return heroIds;
    }

    public void setHeroIds(List<Integer> heroIds) {
        this.heroIds = heroIds;
    }

    public Boss getBoss() {
        return boss;
    }

    public void setBoss(Boss boss) {
        this.boss = boss;
    }

    public int getBossId() {
        return bossId;
    }

    public void setBossId(int bossId) {
        this.bossId = bossId;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
