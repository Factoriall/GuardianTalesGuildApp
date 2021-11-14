package org.techtown.gtguildraid.etc;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.HeroImageRecyclerAdapter;
import org.techtown.gtguildraid.models.entities.Hero;
import org.techtown.gtguildraid.utils.RoomDB;

import java.util.List;

public class HeroBottomSheetDialog
        extends BottomSheetDialogFragment
        implements HeroImageRecyclerAdapter.BottomSheetListener {
    private final BottomSheetListener mListener;

    //콜백 함수
    public interface BottomSheetListener{
        void onImageClicked(Hero hero);
    }

    //이는 registerActivity 및 memberFragment에 사용하므로 둘다 선언
    public HeroBottomSheetDialog(Activity activity){
        this.mListener = (BottomSheetListener) activity;
    }

    public HeroBottomSheetDialog(Fragment fragment){
        this.mListener = (BottomSheetListener) fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_gallery, container, false);
        RoomDB database = RoomDB.getInstance(getActivity());
        List<Hero> heroList = database.heroDao().getAllHeroes();
        //recyclerView를 활용해 이미지 선택
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 3));

        HeroImageRecyclerAdapter adapter = new HeroImageRecyclerAdapter(heroList, this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    //recyclerAdapter에서 클릭하면 mListener 통해 fragment 및 activity에 hero 정보를 전해줌
    @Override
    public void onImageClicked(Hero hero) {
        mListener.onImageClicked(hero);
        dismiss();
    }
}
