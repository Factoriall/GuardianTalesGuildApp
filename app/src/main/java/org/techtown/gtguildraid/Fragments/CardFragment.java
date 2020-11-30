package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Adapters.MemberAdapter;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.List;

public class CardFragment extends Fragment {
    RecyclerView recyclerView;
    List<Record> recordList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    RoomDB database;
    MemberAdapter adapter;


    private static final String ARG_COUNT = "param1";
    private Integer counter;

    public CardFragment() {
        // Required empty public constructor
    }

    public static CardFragment newInstance(Integer counter) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COUNT, counter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            counter = getArguments().getInt(ARG_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record_recycler, container, false);

        database = RoomDB.getInstance(getActivity());
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = view.findViewById(R.id.recordRecyclerView);
        //recordList = database.recordDao().getCertainRecord();

        recyclerView.setLayoutManager(linearLayoutManager);
        //adapter = new RecordAdapter(getActivity(), recordList);
        //recyclerView.setAdapter(adapter);
        Toast.makeText(getActivity(), "counter: " + counter, Toast.LENGTH_LONG).show();

        return view;
    }
}
