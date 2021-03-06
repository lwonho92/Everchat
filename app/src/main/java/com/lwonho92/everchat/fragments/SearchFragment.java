package com.lwonho92.everchat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gigamole.infinitecycleviewpager.VerticalInfiniteCycleViewPager;
import com.lwonho92.everchat.R;
import com.lwonho92.everchat.adapters.HorizontalCardAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by MY on 2017-02-14.
 */

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";
    SelectCountryListener mListener;

    @BindView(R.id.hicvp_card) VerticalInfiniteCycleViewPager infiniteCycleViewPager;

    public SearchFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mListener = (SelectCountryListener) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(e.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        infiniteCycleViewPager.setAdapter(new HorizontalCardAdapter(getContext(), mListener));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    public static boolean isFirst = true;
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public interface SelectCountryListener {
        public void setSelectedCountry(String str);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(infiniteCycleViewPager != null) {
            if (isVisibleToUser) {
                infiniteCycleViewPager.startAutoScroll(false);
            } else {
                infiniteCycleViewPager.stopAutoScroll();
            }
        }
    }
}
