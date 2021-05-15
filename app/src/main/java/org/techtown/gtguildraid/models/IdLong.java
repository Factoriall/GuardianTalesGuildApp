package org.techtown.gtguildraid.models;

import androidx.room.ColumnInfo;

public class IdLong {
    @ColumnInfo(name = "memberId")
    public int memberId;
    @ColumnInfo(name = "value")
    public long value;
}
