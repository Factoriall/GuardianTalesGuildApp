package org.techtown.gtguildraid.models;

import androidx.room.ColumnInfo;

public class DamageInfo {
    @ColumnInfo(name = "memberId")
    public int memberId;
    @ColumnInfo(name = "total")
    public long total;
}
