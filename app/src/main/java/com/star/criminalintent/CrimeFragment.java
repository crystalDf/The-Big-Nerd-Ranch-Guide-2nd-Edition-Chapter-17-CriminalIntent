package com.star.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
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

    public static final String EXTRA_DATE = "date";

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

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment crimeFragment = new CrimeFragment();
        crimeFragment.setArguments(args);

        return crimeFragment;
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
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) view.findViewById(R.id.crime_date);
        updateDate();
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
                    Intent intent = new Intent(CrimeFragment.this.getActivity(),
                            DatePickerActivity.class);
                    intent.putExtra(EXTRA_DATE, mCrime.getDate());
                    startActivityForResult(intent, REQUEST_DATE);
                }

            }
        });

        mTimeButton = (Button) view.findViewById(R.id.crime_time);
        updateTime();
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
                    Intent intent = new Intent(CrimeFragment.this.getActivity(),
                            TimePickerActivity.class);
                    intent.putExtra(EXTRA_DATE, mCrime.getDate());
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
        if (packageManager.resolveActivity(pickIntent,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

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
        mPhotoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePhotoView();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
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

        mCameraButton = (ImageButton) view.findViewById(R.id.crime_camera);

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = (mPhotoFile != null) &&
                (captureImage.resolveActivity(packageManager) != null);
        mCameraButton.setEnabled(canTakePhoto);

        if (canTakePhoto) {
            Uri targetUri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
        }

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        return  view;
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.getInstance(getContext()).updateCrime(mCrime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(PickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
            updateTime();
        } else if (requestCode == REQUEST_CONTACT) {
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

                    Suspect oldSuspect = mCrime.getSuspect();

                    if (oldSuspect != null) {
                        oldSuspect.setCrimeCount(oldSuspect.getCrimeCount() - 1);
                        CrimeLab.getInstance(getContext()).updateSuspect(oldSuspect);
                    }

                    Suspect suspect = CrimeLab.getInstance(getContext()).getSuspect(contactId);
                    if (suspect == null) {
                        suspect = new Suspect();
                        suspect.setContactId(contactId);
                        suspect.setDisplayName(displayName);
                        CrimeLab.getInstance(getContext()).addSuspect(suspect);
                    }
                    suspect.setCrimeCount(suspect.getCrimeCount() + 1);
                    CrimeLab.getInstance(getContext()).updateSuspect(suspect);

                    mCrime.setSuspect(suspect);

                    mSuspectButton.setText(mCrime.getSuspect().getDisplayName());
                    mDialButton.setEnabled(true);
                }
            } finally {
                cursor.close();
            }

            if (mCrime.getSuspect() == null) {
                return;
            }

            Uri commonDataKindPhoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            columns = new String[] {
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            String whereClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ";
            String[] whereArgs = new String[] {mCrime.getSuspect().getContactId()};

            cursor = getActivity().getContentResolver().query(commonDataKindPhoneUri,
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
                    CrimeLab.getInstance(getContext()).updateSuspect(mCrime.getSuspect());

                    mDialButton.setText(phoneNumber);
                }
            } finally {
                cursor.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getFormattedDate());
    }

    private void updateTime() {
        mTimeButton.setText(mCrime.getFormattedTime());
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
