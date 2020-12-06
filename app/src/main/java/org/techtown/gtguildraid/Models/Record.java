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
    private int recordID;

    private int memberId;
    private int raidId;
    private int day;

    private int bossId;
    private int damage;

    private int hero1Id;
    private int hero2Id;
    private int hero3Id;
    private int hero4Id;

    @Ignore
    public Boss boss;

    @Ignore
    public Hero hero1;

    @Ignore
    public Hero hero2;

    @Ignore
    public Hero hero3;

    @Ignore
    public Hero hero4;


    public Record(int memberId, int raidId, int day) {
        this.memberId = memberId;
        this.raidId = raidId;
        this.day = day;
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

    public Hero getHero1() {
        return hero1;
    }

    public void setHero1(Hero hero1) {
        this.hero1 = hero1;
    }

    public Hero getHero2() {
        return hero2;
    }

    public void setHero2(Hero hero2) {
        this.hero2 = hero2;
    }

    public Hero getHero3() {
        return hero3;
    }

    public void setHero3(Hero hero3) {
        this.hero3 = hero3;
    }

    public Hero getHero4() {
        return hero4;
    }

    public void setHero4(Hero hero4) {
        this.hero4 = hero4;
    }
}
