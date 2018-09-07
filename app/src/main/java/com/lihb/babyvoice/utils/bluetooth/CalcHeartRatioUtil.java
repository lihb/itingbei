package com.lihb.babyvoice.utils.bluetooth;

import android.util.Log;

import com.cokus.wavelibrary.utils.BabyJni;
import com.lihb.babyvoice.command.BluetoothCommand;
import com.lihb.babyvoice.utils.RxBus;
import com.trello.rxlifecycle.components.support.RxFragmentActivity;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import rx.functions.Action1;

/**
 * Created by lihb on 2018/9/6.
 * 计算胎心率
 * 每次计算5s数据
 * 每秒钟拿前5秒的数据计算
 */

public class CalcHeartRatioUtil {

    private static final int INTERVAL_COUNT = 5;
    private static final String TAG = CalcHeartRatioUtil.class.getSimpleName();
    private final RxFragmentActivity mActivity;

    private CompositeDisposable mCompositeDisposable;
    private Map<Integer, CopyOnWriteArrayList<Double>> dataMap = new TreeMap<>();
    private long beginTime;
    private CopyOnWriteArrayList<Integer> indexList = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<Double> sendDataList = new CopyOnWriteArrayList<>();

    public CalcHeartRatioUtil(RxFragmentActivity activity) {
        mActivity = activity;
        this.mCompositeDisposable = new CompositeDisposable();
    }

    public void initRxBus() {
        RxBus.getDefault().registerOnActivity(BluetoothCommand.class, mActivity)
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new Action1<BluetoothCommand>() {
                    @Override
                    public void call(BluetoothCommand command) {
                        if (command.getmStatus() == BluetoothCommand.BlueToothStatus.DEV_UPLOAD_VOICE_DATA_SIGNAL) {

                            final byte[] data = command.getData();
                            int readSize = data.length;

//                            double[] doubleData = new double[readSize];

                            if (beginTime == 0) {
                                startWork();
                            }
                            long pastTime = (System.currentTimeMillis() - beginTime) / 1000;
                            int sequence = (int) (pastTime * 1.0f / INTERVAL_COUNT);
                            int yu = (int) pastTime % INTERVAL_COUNT;
                            Log.d(TAG, "dataDisposableObserver onNext, pastTime = " + pastTime + ", sequence = " + sequence + ", yu = " + yu);

                            if (!dataMap.containsKey(yu)) {
                                CopyOnWriteArrayList list = new CopyOnWriteArrayList<Double>();
                                dataMap.put(yu, list);
                            }
                            CopyOnWriteArrayList list = dataMap.get(yu);

                            setIndexList(yu, list, sequence);
                            for (int i = 0; i < readSize; i++) {
//                                doubleData[i] = (data[i] & 0xff);
                                list.add((data[i] & 0xff) * 1.0d);
                            }

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("[lihb command]", "error: " + throwable.getMessage());
                    }
                });

    }

    private void startWork() {
        Log.d(TAG, "startWork");

        Observable<Long> pollingObservable = Observable.interval(5000, 1000, TimeUnit.MILLISECONDS);
        DisposableObserver<Long> pollingDisposableObserver = getPollingDisposableObserver();

        beginTime = System.currentTimeMillis();
        pollingObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe(pollingDisposableObserver);
        mCompositeDisposable.add(pollingDisposableObserver);
    }

    private DisposableObserver<Long> getPollingDisposableObserver() {

        return new DisposableObserver<Long>() {

            @Override
            public synchronized void onNext(Long aLong) {
//                Log.d(TAG, "pollingDisposableObserver onNext,  aLong =" + aLong);
//                Log.d(TAG, "pollingDisposableObserver onNext, indexList =" + indexList);
                sendDataList.clear();
                for (Map.Entry<Integer, CopyOnWriteArrayList<Double>> e : dataMap.entrySet()) {
                    CopyOnWriteArrayList<Double> list = e.getValue();
                    sendDataList.addAll(list);
                }
                final int size = sendDataList.size();
                Log.d(TAG, "pollingDisposableObserver onNext, size = " + size + ", sendDataList =" + sendDataList);
                double[] sendDataArr = new double[size];
                for (int i = 0; i < size; i++) {
                    sendDataArr[i] = sendDataList.get(i);
                }
                double[] replyArray = BabyJni.FHRCal(1, -0.993522329411315, 0.003238835294343, 0.003238835294343, sendDataArr);
                Log.d("[lihb command]", "replyData " + Arrays.toString(replyArray));
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG, "pollingDisposableObserver onError, reason=" + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "pollingDisposableObserver onComplete ");
            }
        };
    }


    private void setIndexList(int yu, CopyOnWriteArrayList list, int sequence) {
        if (indexList.size() < yu + 1) {
            indexList.add(yu, sequence);
        } else {
            Integer seq = indexList.get(yu);

            if (seq != null && seq != sequence) {
                list.clear();
            }
            indexList.set(yu, sequence);
        }
    }

    public void release() {
        mCompositeDisposable.clear();
        beginTime = 0;
    }
}
