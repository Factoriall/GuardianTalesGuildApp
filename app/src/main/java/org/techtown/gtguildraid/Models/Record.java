package org.techtown.gtguildraid.Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.techtown.gtguildraid.Utils.DateConverter;

import java.io.Serializable;

@Entity(tableName = "record")
@TypeConverters(DateConverter.class)
public class Record implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int recordId;

    private int memberId;
    private int raidId;
    private int day;

    private int bossId;
    private int level;
    private int damage;
    @Ignore
    public Boss boss;

    private int leaderId;
    @Ignore
    public Hero leader;

    public Record(int memberId, int raidId, int day) {
        this.memberId = memberId;
        this.raidId = raidId;
        this.day = day;
        this.damage = 0;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
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

    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = level; }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public Hero getLeader() {
        return leader;
    }

    public void setLeader(Hero leader) {
        this.leader = leader;
    }
}
