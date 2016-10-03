package com.star.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.star.criminalintent.model.Crime;

import java.util.UUID;

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks {

    private static final int REQUEST_CRIME = 0;

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(
                    R.id.detail_fragment_container);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragment)
                        .commit();
            }
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivityForResult(intent, REQUEST_CRIME);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment crimeListFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        crimeListFragment.updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CRIME && resultCode == RESULT_OK
                && findViewById(R.id.detail_fragment_container) != null) {
            Crime crime = CrimeLab.getInstance(this).getCrime(
                    (UUID) data.getSerializableExtra(CrimePagerActivity.EXTRA_CRIME_ID));
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

}
