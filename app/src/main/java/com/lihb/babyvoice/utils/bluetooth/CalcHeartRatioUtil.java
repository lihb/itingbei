package com.lihb.babyvoice.utils.bluetooth;

import android.util.Log;

import com.lihb.babyvoice.command.BluetoothCommand;
import com.lihb.babyvoice.utils.RxBus;
import com.trello.rxlifecycle.components.support.RxFragmentActivity;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
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
    private Map<Integer, double[]> dataMap = new TreeMap<>();
    private long beginTime;
    private CopyOnWriteArrayList<Integer> indexList = new CopyOnWriteArrayList<>();

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

                            if (beginTime == 0) {
                                startWork();
                            }
                            long pastTime = (System.currentTimeMillis() - beginTime) / 1000;
                            int sequence = (int) (pastTime * 1.0f / INTERVAL_COUNT);
                            int yu = (int) pastTime % INTERVAL_COUNT;
                            Log.i("[lihb ratio]", "pastTime = " + pastTime + ", sequence = " + sequence + ", yu = " + yu);

                            if (!dataMap.containsKey(yu)) {
                                double[] saveData = new double[readSize];

                                for (int i = 0; i < readSize; i++) {
                                    saveData[i] = (data[i] & 0xff) * 1.0d;
                                }
                                dataMap.put(yu, saveData);
                                Log.i("[lihb ratio]", "if saveData size = " + saveData.length);

                            } else {
                                double[] saveData = dataMap.get(yu);
                                int currLen = saveData.length;

                                double[] tmpData = new double[readSize + currLen];
                                System.arraycopy(saveData, 0, tmpData, 0, currLen);
                                Log.i("[lihb ratio]", "readSize = " + readSize + ", currLen = " + currLen);
//                                Log.i("[lihb ratio]", "else saveData  = " +Arrays.toString(saveData) +
//                                        ", data= " + Arrays.toString(data)+",tmpData = " +Arrays.toString(tmpData));

                                for (int i = 0; i < readSize; i++) {
                                    tmpData[currLen + i] = (data[i] & 0xff) * 1.0d;
                                }
                                dataMap.put(yu, tmpData);

                                Log.i("[lihb ratio]", "finally else tmpData size = " + tmpData.length);
                            }

                            setIndexList(yu, sequence);

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("[lihb ratio]", "error: " + throwable.getMessage());
                    }
                });

    }

    private void startWork() {
        Log.i(TAG, "startWork");

        Observable<Long> pollingObservable = Observable.interval(5000, 1000, TimeUnit.MILLISECONDS);
        DisposableObserver<Long> pollingDisposableObserver = getPollingDisposableObserver();

        beginTime = System.currentTimeMillis();
//        pollingObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe(pollingDisposableObserver);
        mCompositeDisposable.add(pollingDisposableObserver);
    }

    private DisposableObserver<Long> getPollingDisposableObserver() {

        return new DisposableObserver<Long>() {

            @Override
            public synchronized void onNext(Long aLong) {
                Log.i("[lihb ratio]", "indexList =" + indexList);
                double[] sendDataArr = null;
                for (Map.Entry<Integer, double[]> e : dataMap.entrySet()) {
                    double[] saveDataArr = e.getValue();
                    if (sendDataArr == null) {
                        sendDataArr = saveDataArr;
                        Log.i("[lihb ratio]", "if saveDataArr  size = " + saveDataArr.length);

                    } else {
                        double[] tmpDataArr = new double[sendDataArr.length + saveDataArr.length];
                        Log.i("[lihb ratio]", "else saveDataArr  size = " + saveDataArr.length + ", sendDataArr size = " + sendDataArr.length);
                        System.arraycopy(sendDataArr, 0, tmpDataArr, 0, sendDataArr.length);
                        System.arraycopy(saveDataArr, 0, tmpDataArr, sendDataArr.length, saveDataArr.length);
                        sendDataArr = tmpDataArr;
                    }
                }
                if (sendDataArr != null) {
                    Log.i("[lihb ratio]", "finally sendDataArr  size = " + sendDataArr.length);
                }

//                double[] replyArray = BabyJni.FHRCal(1, -0.993522329411315, 0.003238835294343, 0.003238835294343, sendDataArr);
//                Log.i("[lihb ratio]", "replyData " + Arrays.toString(replyArray));
            }

            @Override
            public void onError(Throwable throwable) {
                Log.i(TAG, "pollingDisposableObserver onError, reason=" + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                Log.i(TAG, "pollingDisposableObserver onComplete ");
            }
        };
    }


    private void setIndexList(int yu, int sequence) {
        if (indexList.size() < yu + 1) {
            indexList.add(yu, sequence);
        } else {
            Integer seq = indexList.get(yu);

            if (seq != null && seq != sequence) {
                dataMap.remove(yu);
            }
            indexList.set(yu, sequence);
        }
    }

    public void release() {
        mCompositeDisposable.clear();
        beginTime = 0;
    }
}
