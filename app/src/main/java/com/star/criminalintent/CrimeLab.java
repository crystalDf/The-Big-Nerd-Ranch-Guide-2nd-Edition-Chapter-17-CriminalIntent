package com.star.criminalintent;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.star.criminalintent.database.CrimeBaseHelper;
import com.star.criminalintent.database.CrimeCursorWrapper;
import com.star.criminalintent.database.CrimeDbSchema.CrimeTable;
import com.star.criminalintent.database.CrimeDbSchema.SuspectTable;
import com.star.criminalintent.database.SuspectCursorWrapper;
import com.star.criminalintent.model.Crime;
import com.star.criminalintent.model.Suspect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {

    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mSQLiteDatabase;

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mSQLiteDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    public static CrimeLab getInstance(Context context) {
        if (sCrimeLab == null) {
            synchronized (CrimeLab.class) {
                if (sCrimeLab == null) {
                    sCrimeLab = new CrimeLab(context);
                }
            }
        }
        return sCrimeLab;
    }

    public void addCrime(Crime crime) {
        ContentValues contentValues = getContentValues(crime);
        mSQLiteDatabase.insert(CrimeTable.TABLE_NAME, null, contentValues);
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues contentValues = getContentValues(crime);
        mSQLiteDatabase.update(CrimeTable.TABLE_NAME, contentValues,
                CrimeTable.Cols.UUID + " = ? ", new String[]{uuidString});
    }

    public CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mSQLiteDatabase.query(CrimeTable.TABLE_NAME, null, whereClause, whereArgs,
                null, null, null);
        return new CrimeCursorWrapper(cursor, mContext);
    }

    public void deleteCrime(Crime crime) {

        Suspect suspect = crime.getSuspect();

        if (suspect != null) {
            suspect.setCrimeCount(suspect.getCrimeCount() - 1);
            updateSuspect(suspect);
        }

        mSQLiteDatabase.delete(CrimeTable.TABLE_NAME, CrimeTable.Cols.UUID + " = ? ",
                new String[] { crime.getId().toString() });
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper crimeCursorWrapper = queryCrimes(null, null);

        try {
            while (crimeCursorWrapper.moveToNext()) {
                crimes.add(crimeCursorWrapper.getCrime());
            }
        } finally {
            crimeCursorWrapper.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper crimeCursorWrapper = queryCrimes(
                CrimeTable.Cols.UUID + " = ? ", new String[] { id.toString() });

        try {
            if (crimeCursorWrapper.getCount() == 0) {
                return null;
            }

            crimeCursorWrapper.moveToFirst();
            return crimeCursorWrapper.getCrime();
        } finally {
            crimeCursorWrapper.close();
        }
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CrimeTable.Cols.UUID, crime.getId().toString());
        contentValues.put(CrimeTable.Cols.TITLE, crime.getTitle());
        contentValues.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        contentValues.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        if (crime.getSuspect() != null) {
            contentValues.put(CrimeTable.Cols.SUSPECT, crime.getSuspect().getContactId());
        }

        return contentValues;
    }

    public void addSuspect(Suspect suspect) {
        ContentValues contentValues = getContentValues(suspect);
        mSQLiteDatabase.insert(SuspectTable.TABLE_NAME, null, contentValues);
    }

    public void updateSuspect(Suspect suspect) {
        String uuidString = suspect.getId().toString();
        ContentValues contentValues = getContentValues(suspect);
        mSQLiteDatabase.update(SuspectTable.TABLE_NAME, contentValues,
                SuspectTable.Cols.UUID + " = ? ", new String[]{uuidString});
        if (suspect.getCrimeCount() == 0) {
            deleteSuspect(suspect);
        }
    }

    public SuspectCursorWrapper querySuspects(String whereClause, String[] whereArgs) {
        Cursor cursor = mSQLiteDatabase.query(SuspectTable.TABLE_NAME, null, whereClause, whereArgs,
                null, null, null);
        return new SuspectCursorWrapper(cursor, mContext);
    }

    public void deleteSuspect(Suspect suspect) {
        mSQLiteDatabase.delete(SuspectTable.TABLE_NAME, SuspectTable.Cols.UUID + " = ? ",
                new String[] { suspect.getId().toString() });
    }

    public Suspect getSuspect(String contactId) {

        if (contactId == null) {
            return null;
        }

        SuspectCursorWrapper suspectCursorWrapper = querySuspects(
                SuspectTable.Cols.CONTACT_ID + " = ? ", new String[]{contactId});

        try {
            if (suspectCursorWrapper.getCount() == 0) {
                return null;
            }

            suspectCursorWrapper.moveToFirst();
            return suspectCursorWrapper.getSuspect();
        } finally {
            suspectCursorWrapper.close();
        }
    }

    private static ContentValues getContentValues(Suspect suspect) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SuspectTable.Cols.UUID, suspect.getId().toString());
        contentValues.put(SuspectTable.Cols.CONTACT_ID, suspect.getContactId());
        contentValues.put(SuspectTable.Cols.DISPLAY_NAME, suspect.getDisplayName());
        contentValues.put(SuspectTable.Cols.PHONE_NUMBER, suspect.getPhoneNumber());
        contentValues.put(SuspectTable.Cols.CRIME_COUNT, suspect.getCrimeCount());

        return contentValues;
    }

    public File getPhotoFile(Crime crime) {
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return (externalFilesDir != null)
                ? new File(externalFilesDir, crime.getPhotoFileName())
                : null;
    }
}
