package org.techtown.gtguildraid.Models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "member")
public class GuildMember implements Serializable {
    //Create id column
    @PrimaryKey(autoGenerate = true)
    private int ID;

    //Create name column
    @ColumnInfo(name = "name")
    private String name;

    //Create remark column
    @ColumnInfo(name = "remark")
    private String remark;

    @ColumnInfo(name = "isMe")
    private Boolean isMe;

    @ColumnInfo(name = "isResigned")
    private Boolean isResigned;

    //getter and setter
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getMe() {
        return isMe;
    }

    public void setMe(Boolean me) {
        isMe = me;
    }

    public Boolean getResigned() {
        return isResigned;
    }

    public void setResigned(Boolean resigned) {
        isResigned = resigned;
    }

    @NonNull
    @Override
    public String toString() {
        // A value you want to be displayed in the spinner item.
        return name;
    }
}
