package org.techtown.gtguildraid.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.techtown.gtguildraid.Utils.DateConverter;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "record")
@TypeConverters(DateConverter.class)
public class Record implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int recordID;

    private int memberId;
    private Date day;

    private int hero1Id;
    private int hero2Id;
    private int hero3Id;
    private int hero4Id;
    private int bossId;
    private int damage;

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

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public int getHero1Id() {
        return hero1Id;
    }

    public void setHero1Id(int hero1Id) {
        this.hero1Id = hero1Id;
    }

    public int getHero2Id() {
        return hero2Id;
    }

    public void setHero2Id(int hero2Id) {
        this.hero2Id = hero2Id;
    }

    public int getHero3Id() {
        return hero3Id;
    }

    public void setHero3Id(int hero3Id) {
        this.hero3Id = hero3Id;
    }

    public int getHero4Id() {
        return hero4Id;
    }

    public void setHero4Id(int hero4Id) {
        this.hero4Id = hero4Id;
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
