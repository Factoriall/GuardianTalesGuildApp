package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordFragment extends Fragment {
    Raid raid;
    List<GuildMember> members;
    List<String> memberSpinner = new ArrayList<>();
    RoomDB database;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = (ViewGroup) inflater.inflate(R.layout.fragment_record, container, false);
        database = RoomDB.getInstance(getActivity());

        TextView raidName = view.findViewById(R.id.raidName);
        TextView raidTerm = view.findViewById(R.id.raidTerm);
        Spinner nSpinner = view.findViewById(R.id.nickname);

        raid = database.raidDao().getCurrentRaid(new Date());
        members = database.memberDao().getCurrentMembers();
        members.add(database.memberDao().getMe());

        for(GuildMember m : members){
            memberSpinner.add(m.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, memberSpinner);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        nSpinner.setAdapter(adapter);

        return view;
    }
}
