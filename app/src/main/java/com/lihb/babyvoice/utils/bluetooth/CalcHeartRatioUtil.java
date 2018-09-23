package com.lihb.babyvoice.utils.bluetooth;

import android.util.Log;

import com.cokus.wavelibrary.utils.BabyJni;
import com.lihb.babyvoice.DataManager;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

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
    private DisposableObserver<Long> pollingDisposableObserver;

    private HeartRatioCallback callback;

    public CalcHeartRatioUtil(RxFragmentActivity activity) {
        mActivity = activity;
        this.mCompositeDisposable = new CompositeDisposable();
    }

    public void setCallback(HeartRatioCallback callback) {
        this.callback = callback;
    }

    public void initRxBus() {
        RxBus.getDefault().registerOnActivity(BluetoothCommand.class, mActivity)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<BluetoothCommand, Boolean>() {
                    @Override
                    public Boolean call(BluetoothCommand bluetoothCommand) {
                        if (bluetoothCommand.getStatus() == BluetoothCommand.BlueToothStatus.DEV_UPLOAD_VOICE_DATA_SIGNAL) {
                            return true;
                        }
                        return false;
                    }
                })
                .map(new Func1<BluetoothCommand, double[]>() {
                    @Override
                    public double[] call(BluetoothCommand bluetoothCommand) {
                        final byte[] commandData = bluetoothCommand.getData();
                        int len = commandData.length;

                        double[] data = new double[len];
                        for (int i = 0; i < len; i++) {
                            data[i] = (commandData[i] & 0xff) * 1.0d;
                        }
                        return data;
                    }
                })
                .subscribe(new Action1<double[]>() {
                    @Override
                    public void call(double[] doubles) {
                        Log.i(TAG, "call :" + Arrays.toString(doubles));
                        if (DataManager.getInstance().isTransferDataStarted() == false) {
                            return;
                        }

                        int readSize = doubles.length;

                        if (beginTime == 0) {
                            startWork();
                        }
                        long pastTime = (System.currentTimeMillis() - beginTime) / 1000;
                        int sequence = (int) (pastTime * 1.0f / INTERVAL_COUNT);
                        int yu = (int) pastTime % INTERVAL_COUNT;
                        Log.i("[lihb ratio]", "pastTime = " + pastTime + ", sequence = " + sequence + ", yu = " + yu);

                        if (!dataMap.containsKey(yu)) {

                            dataMap.put(yu, doubles);
                            Log.i("[lihb ratio]", "if doubles size = " + doubles.length);

                        } else {
                            double[] saveData = dataMap.get(yu);
                            int currLen = saveData.length;

                            double[] tmpData = new double[readSize + currLen];
                            System.arraycopy(saveData, 0, tmpData, 0, currLen);
                            System.arraycopy(doubles, 0, tmpData, currLen, readSize);

                            Log.i("[lihb ratio]", "readSize = " + readSize + ", currLen = " + currLen + ", tmpData len = " + tmpData.length);
//                                Log.i("[lihb ratio]", "else saveData  = " +Arrays.toString(saveData) +
//                                        ", data= " + Arrays.toString(data)+",tmpData = " +Arrays.toString(tmpData));

                            dataMap.put(yu, tmpData);
                        }

                        setIndexList(yu, sequence);

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
        indexList.clear();
        dataMap.clear();

        Observable<Long> pollingObservable = Observable.interval(5000, 1000, TimeUnit.MILLISECONDS);
        pollingDisposableObserver = getPollingDisposableObserver();

        beginTime = System.currentTimeMillis();
        pollingObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe(pollingDisposableObserver);
        mCompositeDisposable.add(pollingDisposableObserver);
    }

    private DisposableObserver<Long> getPollingDisposableObserver() {

        return new DisposableObserver<Long>() {

            @Override
            public synchronized void onNext(Long aLong) {
                Log.i(TAG, "indexList =" + indexList);
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

                double[] replyArray = BabyJni.FHRCal(1, -0.993522329411315, 0.003238835294343, 0.003238835294343, sendDataArr);
//                Log.i("[lihb ratio]", "replyData " + Arrays.toString(replyArray));
                final int median = getMedian(replyArray);
                Log.i("[lihb ratio]", "replyData median =  " + median);
                if (callback != null) {
                    callback.updateHeartRatio(median);
                }

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


    /**
     * 得到每个数组索引
     *
     * @param yu
     * @param sequence
     */
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

    /**
     * 获取中位数
     *
     * @return
     */
    private int getMedian(double[] replyData) {
        int len = replyData.length;
        if (len == 0) {
            return 0;
        }
        if (len == 1) {
            return (int) replyData[0];
        }
        Arrays.sort(replyData);
        Log.i("[lihb ratio]", "replyData sort =  " + Arrays.toString(replyData));

        int median = (int) replyData[len / 2];
        if (len % 2 == 0) { // 数组长度为偶数
            median = (int) ((replyData[(len - 1) / 2] + replyData[(len + 1) / 2]) / 2);
        }
        return median;
    }

    public boolean release() {
        Log.i(TAG, "release()");
        if (pollingDisposableObserver != null && !pollingDisposableObserver.isDisposed()) {
            pollingDisposableObserver.dispose();
            pollingDisposableObserver = null;
        }
        mCompositeDisposable.clear();
        beginTime = 0;
        return true;
    }

    public interface HeartRatioCallback {
        void updateHeartRatio(int heartRatio);
    }
}
