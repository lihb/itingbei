package com.lihb.babyvoice.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lihb.babyvoice.BabyVoiceApp;
import com.lihb.babyvoice.db.DBHelper;
import com.lihb.babyvoice.db.IDBRxManager;
import com.lihb.babyvoice.model.PregnantDay;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by lhb on 2017/8/7.
 */

public class PregnantDateDataImpl implements IDBRxManager<PregnantDay> {
    private DBHelper dbHelper;

    private PregnantDateDataImpl() {
        dbHelper = new DBHelper(BabyVoiceApp.getInstance());
    }

    public static final PregnantDateDataImpl getInstance() {
        return PregnantDateDataImplSingletonHolder.instance;
    }

    @Override
    public Observable<Boolean> insertData(PregnantDay pregnantDay) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                queryData(pregnantDay)
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                if (!aBoolean) {
                                    ContentValues values = new ContentValues();
                                    values.put(DBHelper.PREGNANT_DATE_ENTRY.COLUMN_USERNAME, pregnantDay.username);
                                    values.put(DBHelper.PREGNANT_DATE_ENTRY.COLUMN_PREGNANT_DATE, pregnantDay.pregnantDay);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    if (db.insert(DBHelper.PREGNANT_DATE_ENTRY.TABLE_NAME, null, values) != -1) {
                                        subscriber.onNext(true);
                                        Logger.i("insert  data success!");
                                    } else {
                                        Logger.e("insert  data failed!");
                                        subscriber.onNext(false);
                                    }
                                    subscriber.onCompleted();
                                } else {
                                    updateData(pregnantDay).subscribe(aBoolean1 -> {
                                        if (aBoolean1) {
                                            subscriber.onNext(true);
                                            Logger.i("update  data success!");
                                        } else {
                                            Logger.e("update  data failed!");
                                            subscriber.onNext(false);
                                        }
                                    }, throwable -> {
                                        Logger.i("update  data error!");
                                    });
                                }
                            }
                        });

            }
        });
    }

    @Override
    public Observable<Void> batchInsertData(List<PregnantDay> dataList) {
        return null;
    }

    @Override
    public Observable<List<PregnantDay>> queryAllData() {
        return Observable.create(new Observable.OnSubscribe<List<PregnantDay>>() {
            @Override
            public void call(Subscriber<? super List<PregnantDay>> subscriber) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from " + DBHelper.PREGNANT_DATE_ENTRY.TABLE_NAME, null);
                List<PregnantDay> result = new ArrayList<PregnantDay>();

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        PregnantDay pregnantDay = new PregnantDay();
                        pregnantDay.username = cursor.getString(cursor.getColumnIndex(DBHelper.PREGNANT_DATE_ENTRY.COLUMN_USERNAME));
                        pregnantDay.pregnantDay = cursor.getString(cursor.getColumnIndex(DBHelper.PREGNANT_DATE_ENTRY.COLUMN_PREGNANT_DATE));
                        result.add(pregnantDay);
                    }
                    cursor.close();
                }
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public Observable<Boolean> queryData(PregnantDay pregnantDay) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from " + DBHelper.PREGNANT_DATE_ENTRY.TABLE_NAME + " where "
                        + DBHelper.PREGNANT_DATE_ENTRY.COLUMN_USERNAME + " = ? and "
                        + DBHelper.PREGNANT_DATE_ENTRY.COLUMN_PREGNANT_DATE + " = ?", new String[]{pregnantDay.username + "", pregnantDay.pregnantDay + ""});
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
    public Observable<Boolean> delData(PregnantDay pregnantDay) {
        return null;
    }

    @Override
    public Observable<Boolean> updateData(PregnantDay pregnantDay) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBHelper.PREGNANT_DATE_ENTRY.COLUMN_USERNAME, pregnantDay.username);
                values.put(DBHelper.PREGNANT_DATE_ENTRY.COLUMN_PREGNANT_DATE, pregnantDay.pregnantDay);

                int res = db.update(DBHelper.PREGNANT_DATE_ENTRY.TABLE_NAME, values,
                        DBHelper.PREGNANT_DATE_ENTRY.COLUMN_USERNAME + "=? ", new String[]{pregnantDay.username});
                if (res != 0) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onNext(false);
                }
                subscriber.onCompleted();
            }
        });
    }

    private static class PregnantDateDataImplSingletonHolder {
        private static final PregnantDateDataImpl instance = new PregnantDateDataImpl();
    }

}
