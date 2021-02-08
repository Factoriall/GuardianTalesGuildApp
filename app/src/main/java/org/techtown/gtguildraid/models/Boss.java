package org.techtown.gtguildraid.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "boss")
public class Boss implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int bossId;

    @ColumnInfo(name = "raidId")
    private int raidId;

    //Create name column
    @ColumnInfo(name = "name")
    private String name;

    //Create remark column
    @ColumnInfo(name = "hardness")
    private double hardness;

    @ColumnInfo(name = "imgName")
    private String imgName;

    public int getBossId() {
        return bossId;
    }

    public void setBossId(int bossId) {
        this.bossId = bossId;
    }

    public int getRaidId() {
        return raidId;
    }

    public void setRaidId(int raidId) {
        this.raidId = raidId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHardness() {
        return hardness;
    }

    public void setHardness(double hardness) {
        this.hardness = hardness;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }
}
