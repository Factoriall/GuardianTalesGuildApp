package org.techtown.gtguildraid.models;

import java.util.ArrayList;
import java.util.List;

public class LeaderInfo implements Comparable<LeaderInfo>{
    private Hero leader;
    private List<Record> recordList = new ArrayList<>();

    public LeaderInfo(Hero leader, Record r) {
        this.leader = leader;
        recordList.add(r);
    }

    public Hero getLeader() {
        return leader;
    }

    public List<Record> getRecordList() {
        return recordList;
    }

    public void addList(Record r) {
        recordList.add(r);
    }

    public boolean isMatched(Hero leader) {
        return this.leader.getHeroId() == leader.getHeroId();
    }

    @Override
    public int compareTo(LeaderInfo leaderInfo) {
        return leaderInfo.recordList.size() - recordList.size();
    }
}
