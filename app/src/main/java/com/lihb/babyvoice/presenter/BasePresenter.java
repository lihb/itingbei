package com.lihb.babyvoice.presenter;

import com.lihb.babyvoice.view.IView;
import com.orhanobut.logger.Logger;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Administrator on 2017/9/16.
 */

public class BasePresenter<V extends IView> implements IPresenter<V> {

    private static final String TAG = "BasePresenter";

    private V mMvpView;
    private boolean mIsAttachViewMethodCalled = false;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public void attachView(V mvpView) {
        mIsAttachViewMethodCalled = true;
        mMvpView = mvpView;
    }

    @Override
    public void detachView() {
        mMvpView = null;
        unSubscribe();
    }

    @Override
    public boolean isViewAttached() {
        return mMvpView != null;
    }

    @Override
    public V getMvpView() {
        if (!mIsAttachViewMethodCalled) {
            throw new RuntimeException("please call attachView first");
        }
        return mMvpView;
    }

    protected void addSubscribe(Subscription subscription) {
        if (mCompositeSubscription == null) {
            mCompositeSubscription = new CompositeSubscription();
        }
        Logger.i(TAG, "addSubscribe");
        mCompositeSubscription.add(subscription);
    }

    protected void unSubscribe() {
        if (mCompositeSubscription != null) {
            Logger.i(TAG, "unSubscribe");
            mCompositeSubscription.unsubscribe();
        }
    }
}
