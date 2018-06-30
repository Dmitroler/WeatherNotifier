package com.finalProject.dmitroLer.weathernotifier;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class SplashFragment extends Fragment {


    public static final int TIME_OUT = 7 * 1000;
    private OnFragmentTimeOutListener mListener;
    private Timer timer;
    private View loadingIcon;

    public SplashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.finalProject.dmitroLer.weathernotifier.R.layout.fragment_splash, container, false);
        loadingIcon = view.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.loading_icon);

        timer = new Timer();
        startTimeOutTimer(view.getContext());

        return view;
    }

    public void startTimeOutTimer(Context context) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mListener.onFragmentTimeOut();
            }
        }, TIME_OUT);
        startLoadingAnimation(context);
    }

    private void startLoadingAnimation(Context context) {
        Animation animation = AnimationUtils.loadAnimation(context, com.finalProject.dmitroLer.weathernotifier.R.anim.rotate_around_center_point);
        animation.setInterpolator(new LinearInterpolator());
        loadingIcon.startAnimation(animation);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentTimeOutListener) {
            mListener = (OnFragmentTimeOutListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentTimeOutListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void cancelTimeOutTimer() {
        timer.cancel();
    }

    public void stopLoadingAnimation() {
        loadingIcon.clearAnimation();
    }

    interface OnFragmentTimeOutListener {
        void onFragmentTimeOut();
    }
}
