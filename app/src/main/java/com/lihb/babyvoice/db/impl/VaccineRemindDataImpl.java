package com.lihb.babyvoice.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lihb.babyvoice.BabyVoiceApp;
import com.lihb.babyvoice.db.DBHelper;
import com.lihb.babyvoice.db.IDBRxManager;
import com.lihb.babyvoice.model.VaccineRemindInfo;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by Administrator on 2017/3/20.
 */

public class VaccineRemindDataImpl implements IDBRxManager<VaccineRemindInfo> {

    private DBHelper dbHelper;

    private VaccineRemindDataImpl() {
        dbHelper = new DBHelper(BabyVoiceApp.getInstance());
    }

    private static VaccineRemindDataImpl instance = new VaccineRemindDataImpl();

    public static VaccineRemindDataImpl getInstance() {
        return instance;
    }

    @Override
    public Observable<Boolean> insertData(final VaccineRemindInfo vaccineRemindInfo) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                queryData(vaccineRemindInfo)
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                if (!aBoolean) {
                                    ContentValues values = new ContentValues();
                                    values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_NAME, vaccineRemindInfo.vaccineName);
                                    values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_NAME_EN, vaccineRemindInfo.vaccineNameEn);
                                    values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_REMIND_DATE, vaccineRemindInfo.ageToInject);
                                    values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_HAS_READ, vaccineRemindInfo.hasRead);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    if (db.insert(DBHelper.VACCINE_REMIND_ENTRY.TABLE_NAME, null, values) != -1) {
                                        subscriber.onNext(true);
                                        Logger.i("insert  data success!");
                                    } else {
                                        Logger.e("insert  data failed!");
                                        subscriber.onNext(false);
                                    }
                                    subscriber.onCompleted();
                                }
                            }
                        });

            }
        });
    }

    @Override
    public Observable<Void> batchInsertData(final List<VaccineRemindInfo> dataList) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                final SQLiteDatabase db = dbHelper.getWritableDatabase();
                for (final VaccineRemindInfo vaccineRemindInfo : dataList) {
                    queryData(vaccineRemindInfo)
                            .subscribe(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean aBoolean) {
                                    if (!aBoolean) {
                                        ContentValues values = new ContentValues();
                                        values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_NAME, vaccineRemindInfo.vaccineName);
                                        values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_NAME_EN, vaccineRemindInfo.vaccineNameEn);
                                        values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_REMIND_DATE, vaccineRemindInfo.ageToInject);
                                        values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_HAS_READ, vaccineRemindInfo.hasRead);
                                        if (db.insert(DBHelper.VACCINE_REMIND_ENTRY.TABLE_NAME, null, values) == -1) {
                                            Log.e("VaccineRemindDataImpl", vaccineRemindInfo.toString() + "插入失败");
                                        }
                                    }

                                }
                            });

                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public Observable<List<VaccineRemindInfo>> queryAllData() {
        return Observable.create(new Observable.OnSubscribe<List<VaccineRemindInfo>>() {
            @Override
            public void call(Subscriber<? super List<VaccineRemindInfo>> subscriber) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from " + DBHelper.VACCINE_REMIND_ENTRY.TABLE_NAME, null);
                List<VaccineRemindInfo> result = new ArrayList<VaccineRemindInfo>();

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        VaccineRemindInfo vaccineRemindInfo = new VaccineRemindInfo();
                        vaccineRemindInfo.vaccineName = cursor.getString(cursor.getColumnIndex(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_NAME));
                        vaccineRemindInfo.vaccineNameEn = cursor.getString(cursor.getColumnIndex(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_NAME_EN));
                        vaccineRemindInfo.ageToInject = cursor.getInt(cursor.getColumnIndex(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_REMIND_DATE));
                        vaccineRemindInfo.hasRead = cursor.getInt(cursor.getColumnIndex(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_HAS_READ));
                        result.add(vaccineRemindInfo);
                    }
                    cursor.close();
                }
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public Observable<Boolean> queryData(final VaccineRemindInfo vaccineRemindInfo) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from " + DBHelper.VACCINE_REMIND_ENTRY.TABLE_NAME + " where "
                        + DBHelper.VACCINE_REMIND_ENTRY.COLUMN_NAME + " = ? and "
                        + DBHelper.VACCINE_REMIND_ENTRY.COLUMN_REMIND_DATE + " = ?", new String[]{vaccineRemindInfo.vaccineName + "", vaccineRemindInfo.ageToInject + ""});
                if (cursor != null && cursor.getCount() > 0) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onNext(false);
                }
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public Observable<Boolean> delData(final VaccineRemindInfo vaccineRemindInfo) {
        return null;
    }

    @Override
    public Observable<Boolean> updateData(final VaccineRemindInfo vaccineRemindInfo) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_NAME, vaccineRemindInfo.vaccineName);
                values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_NAME_EN, vaccineRemindInfo.vaccineNameEn);
                values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_REMIND_DATE, vaccineRemindInfo.ageToInject);
                values.put(DBHelper.VACCINE_REMIND_ENTRY.COLUMN_HAS_READ, vaccineRemindInfo.hasRead);
                int res = db.update(DBHelper.VACCINE_REMIND_ENTRY.TABLE_NAME, values,
                        DBHelper.VACCINE_REMIND_ENTRY.COLUMN_NAME + "=? and "
                                + DBHelper.VACCINE_REMIND_ENTRY.COLUMN_REMIND_DATE + "=?", new String[]{vaccineRemindInfo.vaccineName, vaccineRemindInfo.ageToInject + ""});
                if (res != 0) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onNext(false);
                }
                subscriber.onCompleted();
            }
        });
    }
}
