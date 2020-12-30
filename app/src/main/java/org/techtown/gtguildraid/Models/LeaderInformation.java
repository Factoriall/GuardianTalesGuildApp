package org.techtown.gtguildraid.Models;

import java.util.ArrayList;
import java.util.List;

public class LeaderInformation {
    private Hero leader;
    private List<Record> recordList = new ArrayList<>();

    public LeaderInformation(Hero leader, Record r) {
        this.leader = leader;
        recordList.add(r);
    }

    public Hero getLeader() {
        return leader;
    }

    public List<Record> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<Record> recordList) {
        this.recordList = recordList;
    }

    public void addList(Record r) {
        recordList.add(r);
    }

    public boolean isMatched(Hero leader) {
        return this.leader.getHeroId() == leader.getHeroId();
    }
}
