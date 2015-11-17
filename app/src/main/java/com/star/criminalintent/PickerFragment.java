package com.star.criminalintent;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;

public abstract class PickerFragment extends DialogFragment {

    protected static final String ARG_DATE = "date";

    public static final String EXTRA_DATE = "date";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Calendar calendar = getCalendar();

        View view = getView(inflater, container);
        int pickerId = getPickerId();

        setDate(calendar, view, pickerId);

        setPickerButtonOnClickListener(calendar, view);

        return view;
    }

    @NonNull
    protected Calendar getCalendar() {
        Date date = (Date) getArguments().getSerializable(ARG_DATE);

        Calendar calendar = Calendar.getInstance();

        if (date != null) {
            calendar.setTime(date);
        }

        return calendar;
    }

    protected abstract View getView(LayoutInflater inflater, ViewGroup container);

    protected abstract int getPickerId();

    protected abstract void setPickerButtonOnClickListener(final Calendar calendar, View view);

    protected abstract void setDate(Calendar calendar, View view, int pickerId);

    protected abstract Date getDate(Calendar calendar);

    protected void sendResult(int resultCode, Date date) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);

        if (getTargetFragment() == null) {
            getActivity().setResult(resultCode, intent);
            getActivity().finish();
        } else {
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
            dismiss();
        }
    }
}
