package com.star.criminalintent;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.star.criminalintent.model.Crime;
import com.star.criminalintent.model.Suspect;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_DETAIL_DISPLAY = "DialogDetailDisplay";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;

    private Crime mCrime;
    private File mPhotoFile;

    private EditText mTitleField;

    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;

    private Button mReportButton;
    private Button mSuspectButton;
    private Button mDialButton;

    private ImageView mPhotoView;
    private ImageButton mCameraButton;

    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment crimeFragment = new CrimeFragment();
        crimeFragment.setArguments(args);

        return crimeFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);

        mCrime = CrimeLab.getInstance(getContext()).getCrime(crimeId);
        mPhotoFile = CrimeLab.getInstance(getContext()).getPhotoFile(mCrime);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) view.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) view.findViewById(R.id.crime_date);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_LANDSCAPE) {
                    FragmentManager fragmentManager = getFragmentManager();
                    DatePickerFragment datePickerFragment =
                            DatePickerFragment.newInstance(mCrime.getDate());
                    datePickerFragment.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                    datePickerFragment.show(fragmentManager, DIALOG_DATE);
                } else if (getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_PORTRAIT) {
                    Intent intent = DatePickerActivity.newIntent(getActivity(), mCrime.getDate());
                    startActivityForResult(intent, REQUEST_DATE);
                }
            }
        });

        mTimeButton = (Button) view.findViewById(R.id.crime_time);
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_LANDSCAPE) {
                    FragmentManager fragmentManager = getFragmentManager();
                    TimePickerFragment timePickerFragment =
                            TimePickerFragment.newInstance(mCrime.getDate());
                    timePickerFragment.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                    timePickerFragment.show(fragmentManager, DIALOG_TIME);
                } else if (getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_PORTRAIT) {
                    Intent intent = TimePickerActivity.newIntent(getActivity(), mCrime.getDate());
                    startActivityForResult(intent, REQUEST_DATE);
                }

            }
        });

        mSolvedCheckBox = (CheckBox) view.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mReportButton = (Button) view.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareCompat.IntentBuilder
                        .from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(getString(R.string.send_report))
                        .startChooser();
            }
        });

        final Intent pickIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);

        mSuspectButton = (Button) view.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickIntent, REQUEST_CONTACT);
            }
        });

        PackageManager packageManager = getActivity().getPackageManager();

        boolean canChooseSuspect = (packageManager.resolveActivity(pickIntent,
                PackageManager.MATCH_DEFAULT_ONLY) != null);

        mSuspectButton.setEnabled(canChooseSuspect);

        mDialButton = (Button) view.findViewById(R.id.crime_dial);
        mDialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + mCrime.getSuspect().getPhoneNumber()));
                startActivity(intent);
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect().getDisplayName());
            mDialButton.setText(mCrime.getSuspect().getPhoneNumber());
        } else {
            mDialButton.setEnabled(false);
        }

        mPhotoView = (ImageView) view.findViewById(R.id.crime_photo);
        mPhotoView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePhotoView();
                mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                DetailDisplayFragment detailDisplayFragment =
                        DetailDisplayFragment.newInstance(mPhotoFile.getPath());
                detailDisplayFragment.show(fragmentManager, DIALOG_DETAIL_DISPLAY);
            }
        });

        final Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        mCameraButton = (ImageButton) view.findViewById(R.id.crime_camera);
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImageIntent, REQUEST_PHOTO);
            }
        });

        boolean canTakePhoto = (mPhotoFile != null) &&
                (captureImageIntent.resolveActivity(packageManager) != null);

        mCameraButton.setEnabled(canTakePhoto);

        if (canTakePhoto) {
            Uri targetUri = Uri.fromFile(mPhotoFile);
            captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
        }
        
        return  view;
    }

    @Override
    public void onResume() {
        super.onResume();
        
        updateUI();
    }

    private void updateUI() {
        mDateButton.setText(mCrime.getFormattedDate());
        mTimeButton.setText(mCrime.getFormattedTime());
    }

    @Override
    public void onPause() {
        super.onPause();

        updateCrime();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(PickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateUI();
        } else if (requestCode == REQUEST_CONTACT) {
            updateChooseSuspectButton(data);
            updateMakeAPhoneCallButton();

            CrimeLab.getInstance(getContext()).updateSuspect(mCrime.getSuspect());
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
        }

        updateCrime();
    }

    private void updateCrime() {
        CrimeLab.getInstance(getContext()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updateChooseSuspectButton(Intent data) {
        Uri contactUri = data.getData();

        String[] columns = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        Cursor cursor = getActivity().getContentResolver().query(contactUri, columns,
                null, null, null);

        if (cursor == null) {
            return;
        }

        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String contactId = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Suspect suspect = updateAndGetSuspect(contactId, displayName);

                mCrime.setSuspect(suspect);

                mSuspectButton.setText(mCrime.getSuspect().getDisplayName());
                mDialButton.setEnabled(true);
            }
        } finally {
            cursor.close();
        }

    }

    private Suspect updateAndGetSuspect(String contactId, String displayName) {
        Suspect oldSuspect = mCrime.getSuspect();

        if (oldSuspect != null) {
            oldSuspect.setCrimeCount(oldSuspect.getCrimeCount() - 1);
            CrimeLab.getInstance(getContext()).updateSuspect(oldSuspect);
        }

        Suspect newSuspect = CrimeLab.getInstance(getContext()).getSuspect(contactId);

        if (newSuspect == null) {
            newSuspect = new Suspect();
            newSuspect.setContactId(contactId);
            newSuspect.setDisplayName(displayName);
            CrimeLab.getInstance(getContext()).addSuspect(newSuspect);
        }

        newSuspect.setCrimeCount(newSuspect.getCrimeCount() + 1);

        return newSuspect;
    }

    private void updateMakeAPhoneCallButton() {
        if (mCrime.getSuspect() == null) {
            return;
        }

        Uri commonDataKindPhoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] columns = new String[] {
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        String whereClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ";
        String[] whereArgs = new String[] {mCrime.getSuspect().getContactId()};

        Cursor cursor = getActivity().getContentResolver().query(commonDataKindPhoneUri,
                columns, whereClause, whereArgs, null);

        if (cursor == null) {
            return;
        }

        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String phoneNumber = cursor.getString(
                        cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                mCrime.getSuspect().setPhoneNumber(phoneNumber);

                mDialButton.setText(phoneNumber);
            }
        } finally {
            cursor.close();
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_crime:
                if (mCrime != null) {
                    CrimeLab.getInstance(getContext()).deleteCrime(mCrime);
                    getActivity().finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getCrimeReport() {
        String solvedString = mCrime.isSolved()
                ? getString(R.string.crime_report_solved)
                : getString(R.string.crime_report_unsolved);

        String dateFormat = "EEE, MMM dd";
        String dateString = new SimpleDateFormat(dateFormat, Locale.US)
                .format(mCrime.getDate());

        String displayName = mCrime.getSuspect().getDisplayName();
        if (displayName == null) {
            displayName = getString(R.string.crime_report_no_suspect);
        } else {
            displayName = getString(R.string.crime_report_suspect, displayName);
        }

        return getString(R.string.crime_report, mCrime.getTitle(), dateString,
                solvedString, displayName);
    }

    private void updatePhotoView() {
        if ((mPhotoFile == null) || !mPhotoFile.exists()) {
            mPhotoView.setImageBitmap(null);
            mPhotoView.setClickable(false);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), mPhotoView.getWidth(), mPhotoView.getHeight());
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setClickable(true);
        }
    }

}
