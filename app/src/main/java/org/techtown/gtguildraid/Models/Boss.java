package org.techtown.gtguildraid.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "boss")
public class Boss implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int ID;

    //Create name column
    @ColumnInfo(name = "name")
    private String name;

    //Create remark column
    @ColumnInfo(name = "hardness")
    private double hardness;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
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
}
