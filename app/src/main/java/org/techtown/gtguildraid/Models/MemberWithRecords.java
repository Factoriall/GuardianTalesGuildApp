package org.techtown.gtguildraid.Models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.lang.reflect.Member;
import java.util.List;

public class MemberWithRecords {
    @Embedded
    public GuildMember member;
    @Relation(
            parentColumn = "ID",
            entityColumn = "recordId"
    )

    public List<Record> records;
}
