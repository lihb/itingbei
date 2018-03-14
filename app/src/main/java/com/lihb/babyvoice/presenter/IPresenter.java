package com.lihb.babyvoice.presenter;

import com.lihb.babyvoice.view.IView;

/**
 * Created by Administrator on 2017/9/16.
 */

public interface IPresenter<V extends IView> {

    void attachView(V mvpView);

    void detachView();

    boolean isViewAttached();

    V getMvpView();
}
