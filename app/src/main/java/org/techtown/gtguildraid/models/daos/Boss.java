package org.techtown.gtguildraid.models.daos;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import static androidx.room.ForeignKey.CASCADE;


@Entity(tableName = "boss", foreignKeys={
        @ForeignKey(onDelete = CASCADE, entity = Raid.class,
                parentColumns = "raidId",childColumns = "raidId")},
        indices = {
                @Index("raidId")
        })
public class Boss implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int bossId;

    @ColumnInfo(name = "raidId")
    private int raidId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "hardness")
    private double hardness;

    @ColumnInfo(name = "imgName")
    private String imgName;

    @ColumnInfo(name = "elementId")
    private int elementId;

    @ColumnInfo(name = "isFurious")
    private boolean isFurious;

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

    public int getElementId() {
        return elementId;
    }

    public void setElementId(int elementId) {
        this.elementId = elementId;
    }

    public boolean isFurious() {
        return isFurious;
    }

    public void setFurious(boolean furious) {
        isFurious = furious;
    }
}
