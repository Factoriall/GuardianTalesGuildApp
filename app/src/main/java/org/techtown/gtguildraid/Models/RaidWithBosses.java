package org.techtown.gtguildraid.Models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class RaidWithBosses {
    @Embedded public Raid raid;
    @Relation(
            parentColumn = "raidId",
            entityColumn = "bossId"
    )

    public List<Boss> bosses;
}
