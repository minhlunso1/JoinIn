package com.bluebirdaward.joinin.vc.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.R;
import com.rey.material.widget.Spinner;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.apptik.widget.MultiSlider;

public class SearchFilterFragment extends DialogFragment {

    @Bind(R.id.sliderDistance)
    MultiSlider sliderDistance;
    @Bind(R.id.sliderAge)
    MultiSlider sliderAge;
    @Bind(R.id.spinnerGender)
    Spinner spinnerGender;
    @Bind(R.id.tvAgeRangeNumber)
    TextView tvAgeNumber;
    @Bind(R.id.tvDistanceNumber)
    TextView tvDistanceNumber;
    @Bind(R.id.btnDone)
    Button btnDone;
    @Bind(R.id.btnCancel)
    Button btnCancel;

    private int maxDistance, maxAge, minAge, gender;

    public interface SearchFilterFragmentListener {
        void onFilter();
    }

    public SearchFilterFragment() {
        // Required empty public constructor
    }

    public static SearchFilterFragment newInstance() {
        SearchFilterFragment fragment = new SearchFilterFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_filter, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupValue();
        //Distance
        setDistanceNumber(tvDistanceNumber, maxDistance);
        sliderDistance.getThumb(0).setValue(maxDistance);
        sliderDistance.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int thumbIndex, int value) {
                maxDistance = value;
                setDistanceNumber(tvDistanceNumber, value);
            }
        });

        //Age
        sliderAge.getThumb(0).setValue(minAge);
        sliderAge.getThumb(1).setValue(maxAge);
        setAgeRangeNumber(tvAgeNumber, minAge, maxAge);
        sliderAge.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int thumbIndex, int value) {
                if (thumbIndex == 0) {
                    minAge = value;
                } else {
                    maxAge = value;
                }
                setAgeRangeNumber(tvAgeNumber, minAge, maxAge);
            }
        });

        //Gender
        String[] items = new String[]{getResources().getString(R.string.only_men), getResources().getString(R.string.only_women), getResources().getString(R.string.men_and_women)};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, items);
        spinnerGender.setAdapter(spinnerAdapter);
        spinnerGender.setSelection(gender);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JoininApplication.filter.maxDistance = maxDistance;
                JoininApplication.filter.maxAge = maxAge;
                JoininApplication.filter.minAge = minAge;
                JoininApplication.filter.gender = spinnerGender.getSelectedItemPosition();
                SearchFilterFragmentListener listener = (SearchFilterFragmentListener) getActivity();
                listener.onFilter();
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void setupValue() {
        minAge = JoininApplication.filter.minAge;
        maxAge = JoininApplication.filter.maxAge;
        maxDistance = JoininApplication.filter.maxDistance;
        gender = JoininApplication.filter.gender;
    }

    @Override
    public void onResume() {
        makeWrapContent();
        super.onResume();
    }

    private void setDistanceNumber(TextView tv, int value) {
        if (value < 1000)
            tv.setText(String.valueOf(value) + " m");
        else if (value == 2000)
            tv.setText(String.valueOf((Double.valueOf(value) / 1000)) + "+ km");
        else
            tv.setText(String.valueOf((Double.valueOf(value) / 1000)) + " km");
    }

    private void setAgeRangeNumber(TextView tv, int valueMin, int valueMax) {
        if (valueMax == 80)
            tv.setText(String.valueOf(valueMin) + " - " + String.valueOf(valueMax) + "+");
        else
            tv.setText(String.valueOf(valueMin) + " - " + String.valueOf(valueMax));
    }

    private void makeWrapContent() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }
}