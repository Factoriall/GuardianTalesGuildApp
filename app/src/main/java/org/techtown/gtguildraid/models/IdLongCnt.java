package org.techtown.gtguildraid.models;

import androidx.room.ColumnInfo;

public class IdLongCnt {
    @ColumnInfo(name = "memberId")
    public int memberId;
    @ColumnInfo(name = "value")
    public long value;
    @ColumnInfo(name = "count")
    public int count;
}
