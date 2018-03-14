package com.lihb.babyvoice.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lihb.babyvoice.BabyVoiceApp;
import com.lihb.babyvoice.db.DBHelper;
import com.lihb.babyvoice.db.IDBRxManager;
import com.lihb.babyvoice.model.BabyBirthDay;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by lhb on 2017/8/7.
 */

public class BirthdayDataImpl implements IDBRxManager<BabyBirthDay> {
    private DBHelper dbHelper;

    private BirthdayDataImpl() {
        dbHelper = new DBHelper(BabyVoiceApp.getInstance());
    }

    public static final BirthdayDataImpl getInstance() {
        return BirthdayDataImplSingletonHolder.instance;
    }

    private static class BirthdayDataImplSingletonHolder {
        private static final BirthdayDataImpl instance = new BirthdayDataImpl();
    }

    @Override
    public Observable<Boolean> insertData(BabyBirthDay babyBirthDay) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                queryData(babyBirthDay)
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                if (!aBoolean) {
                                    ContentValues values = new ContentValues();
                                    values.put(DBHelper.BABY_BIRTH_ENTRY.COLUMN_USERNAME, babyBirthDay.username);
                                    values.put(DBHelper.BABY_BIRTH_ENTRY.COLUMN_BIRTH_DATE, babyBirthDay.birthday);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    if (db.insert(DBHelper.BABY_BIRTH_ENTRY.TABLE_NAME, null, values) != -1) {
                                        subscriber.onNext(true);
                                        Logger.i("insert  data success!");
                                    } else {
                                        Logger.e("insert  data failed!");
                                        subscriber.onNext(false);
                                    }
                                    subscriber.onCompleted();
                                } else {
                                    updateData(babyBirthDay).subscribe(aBoolean1 -> {
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
    public Observable<Void> batchInsertData(List<BabyBirthDay> dataList) {
        return null;
    }

    @Override
    public Observable<List<BabyBirthDay>> queryAllData() {
        return Observable.create(new Observable.OnSubscribe<List<BabyBirthDay>>() {
            @Override
            public void call(Subscriber<? super List<BabyBirthDay>> subscriber) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from " + DBHelper.BABY_BIRTH_ENTRY.TABLE_NAME, null);
                List<BabyBirthDay> result = new ArrayList<BabyBirthDay>();

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        BabyBirthDay babyBirthDay = new BabyBirthDay();
                        babyBirthDay.username = cursor.getString(cursor.getColumnIndex(DBHelper.BABY_BIRTH_ENTRY.COLUMN_USERNAME));
                        babyBirthDay.birthday = cursor.getString(cursor.getColumnIndex(DBHelper.BABY_BIRTH_ENTRY.COLUMN_BIRTH_DATE));
                        result.add(babyBirthDay);
                    }
                    cursor.close();
                }
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public Observable<Boolean> queryData(BabyBirthDay babyBirthDay) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from " + DBHelper.BABY_BIRTH_ENTRY.TABLE_NAME + " where "
                        + DBHelper.BABY_BIRTH_ENTRY.COLUMN_USERNAME + " = ?", new String[]{babyBirthDay.username});
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
    public Observable<Boolean> delData(BabyBirthDay babyBirthDay) {
        return null;
    }

    @Override
    public Observable<Boolean> updateData(BabyBirthDay babyBirthDay) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBHelper.BABY_BIRTH_ENTRY.COLUMN_USERNAME, babyBirthDay.username);
                values.put(DBHelper.BABY_BIRTH_ENTRY.COLUMN_BIRTH_DATE, babyBirthDay.birthday);

                int res = db.update(DBHelper.BABY_BIRTH_ENTRY.TABLE_NAME, values,
                        DBHelper.BABY_BIRTH_ENTRY.COLUMN_USERNAME + "=? ", new String[]{babyBirthDay.username});
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
