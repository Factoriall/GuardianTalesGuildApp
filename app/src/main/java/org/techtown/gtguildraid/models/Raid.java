package org.techtown.gtguildraid.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity(tableName = "raid")
public class Raid implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int raidId;

    //Create name column
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "startDay")
    private Date startDay;

    @ColumnInfo(name = "endDay")
    private Date endDay;

    @Ignore
    public List<Boss> bossList;

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

    public Date getStartDay() {
        return startDay;
    }

    public void setStartDay(Date startDay) {
        this.startDay = startDay;
    }

    public Date getEndDay() {
        return endDay;
    }

    public void setEndDay(Date endDay) {
        this.endDay = endDay;
    }

    public List<Boss> getBossList() {
        return bossList;
    }

    public void setBossList(List<Boss> bossList) {
        this.bossList = bossList;
    }
}
