package com.lihb.babyvoice.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lihb.babyvoice.BabyVoiceApp;
import com.lihb.babyvoice.db.DBHelper;
import com.lihb.babyvoice.db.IDBRxManager;
import com.lihb.babyvoice.model.PregnantRemindInfo;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by Administrator on 2017/3/20.
 */

public class PregnantRemindDataImpl implements IDBRxManager<PregnantRemindInfo> {

    private DBHelper dbHelper;

    private PregnantRemindDataImpl() {
        dbHelper = new DBHelper(BabyVoiceApp.getInstance());
    }

    private static PregnantRemindDataImpl instance = new PregnantRemindDataImpl();

    public static PregnantRemindDataImpl getInstance() {
        return instance;
    }

    @Override
    public Observable<Boolean> insertData(final PregnantRemindInfo pregnantRemindInfo) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                queryData(pregnantRemindInfo)
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                if (!aBoolean) {
                                    ContentValues values = new ContentValues();
                                    values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_EVENT, pregnantRemindInfo.eventName);
                                    values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_EVENT_EN, pregnantRemindInfo.eventNameEn);
                                    values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_REMIND_DATE, pregnantRemindInfo.eventDate);
                                    values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_HAS_READ, pregnantRemindInfo.hasRead);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    if (db.insert(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.TABLE_NAME, null, values) != -1) {
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
    public Observable<Void> batchInsertData(final List<PregnantRemindInfo> dataList) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                final SQLiteDatabase db = dbHelper.getWritableDatabase();
                for (final PregnantRemindInfo pregnantRemindInfo : dataList) {
                    queryData(pregnantRemindInfo)
                            .subscribe(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean aBoolean) {
                                    if (!aBoolean) {
                                        ContentValues values = new ContentValues();
                                        values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_EVENT, pregnantRemindInfo.eventName);
                                        values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_EVENT_EN, pregnantRemindInfo.eventNameEn);
                                        values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_REMIND_DATE, pregnantRemindInfo.eventDate);
                                        values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_HAS_READ, pregnantRemindInfo.hasRead);
                                        if (db.insert(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.TABLE_NAME, null, values) == -1) {
                                            Log.e("PregnantRemindDataImpl", pregnantRemindInfo.toString() + "插入失败");
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
    public Observable<List<PregnantRemindInfo>> queryAllData() {
        return Observable.create(new Observable.OnSubscribe<List<PregnantRemindInfo>>() {
            @Override
            public void call(Subscriber<? super List<PregnantRemindInfo>> subscriber) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from " + DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.TABLE_NAME, null);
                List<PregnantRemindInfo> result = new ArrayList<PregnantRemindInfo>();

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        PregnantRemindInfo pregnantRemindInfo = new PregnantRemindInfo();
                        pregnantRemindInfo.eventName = cursor.getString(cursor.getColumnIndex(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_EVENT));
                        pregnantRemindInfo.eventNameEn = cursor.getString(cursor.getColumnIndex(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_EVENT_EN));
                        pregnantRemindInfo.eventDate = cursor.getInt(cursor.getColumnIndex(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_REMIND_DATE));
                        pregnantRemindInfo.hasRead = cursor.getInt(cursor.getColumnIndex(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_HAS_READ));
                        result.add(pregnantRemindInfo);
                    }
                    cursor.close();
                }
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public Observable<Boolean> queryData(final PregnantRemindInfo pregnantRemindInfo) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from " + DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.TABLE_NAME + " where "
                        + DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_EVENT + " = ? and "
                        + DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_REMIND_DATE + " = ?", new String[]{pregnantRemindInfo.eventName + "", pregnantRemindInfo.eventDate + ""});
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
    public Observable<Boolean> delData(final PregnantRemindInfo pregnantRemindInfo) {
        return null;
    }

    @Override
    public Observable<Boolean> updateData(final PregnantRemindInfo pregnantRemindInfo) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_EVENT, pregnantRemindInfo.eventName);
                values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_EVENT_EN, pregnantRemindInfo.eventNameEn);
                values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_REMIND_DATE, pregnantRemindInfo.eventDate);
                values.put(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_HAS_READ, pregnantRemindInfo.hasRead);
                int res = db.update(DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.TABLE_NAME, values,
                        DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_EVENT + "=? and "
                                + DBHelper.PREGNANT_EXAMINE_REMIND_ENTRY.COLUMN_REMIND_DATE + "=?", new String[]{pregnantRemindInfo.eventName, pregnantRemindInfo.eventDate + ""});
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
