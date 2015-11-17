package com.star.criminalintent;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.star.criminalintent.model.Crime;

import java.util.List;


public class CrimeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mCrimeAdapter;

    private LinearLayout mEmptyViewLinearLayout;
    private Button mNewCrimeButton;

    private boolean mSubtitleVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mCrimeRecyclerView.setLayoutManager(linearLayoutManager);

        mEmptyViewLinearLayout = (LinearLayout) view.findViewById(R.id.empty_view);
        mNewCrimeButton = (Button) view.findViewById(R.id.new_crime_button);
        mNewCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newCrime();
            }
        });

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.getInstance(getContext());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mCrimeAdapter == null) {
            mCrimeAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mCrimeAdapter);
        } else {
            mCrimeAdapter.setCrimes(crimes);
            mCrimeAdapter.notifyDataSetChanged();
        }

        updateSubtitle();

        int crimeCount = CrimeLab.getInstance(getContext()).getCrimes().size();
        mEmptyViewLinearLayout.setVisibility(crimeCount == 0 ? View.VISIBLE : View.GONE);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder {

        private Crime mCrime;

        private TextView mTitleTextView;
        private CheckBox mSolvedCheckBox;
        private TextView mDateTextView;

        public CrimeHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = CrimePagerActivity.newIntent(getContext(), mCrime.getId());
                    startActivity(intent);
                }
            });

            mTitleTextView = (TextView)
                    itemView.findViewById(R.id.list_item_crime_title_text_view);
            mSolvedCheckBox = (CheckBox)
                    itemView.findViewById(R.id.list_item_crime_solved_check_box);
            mDateTextView = (TextView)
                    itemView.findViewById(R.id.list_item_crime_date_text_view);
        }

        public void bindCrime(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mSolvedCheckBox.setChecked(mCrime.isSolved());
            mDateTextView.setText(mCrime.getFormattedDate());
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());

            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);

            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bindCrime(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_show_subtitle);

        if (mSubtitleVisible) {
            menuItem.setTitle(R.string.hide_subtitle);
        } else {
            menuItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_crime:
                newCrime();
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().supportInvalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.getInstance(getContext());
        int crimeCount = crimeLab.getCrimes().size();

        String subtitle;

        if (crimeCount == 1) {
            subtitle = getString(R.string.subtitle_singular_format);
        } else {
            subtitle = getString(R.string.subtitle_plural_format, crimeCount);
        }

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(subtitle);

    }

    private void newCrime() {
        Crime crime = new Crime();
        CrimeLab.getInstance(getContext()).addCrime(crime);
        Intent intent = CrimePagerActivity.newIntent(getContext(), crime.getId());
        startActivity(intent);
    }
}
