package com.lihb.babyvoice.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.action.ApiManager;
import com.lihb.babyvoice.action.ServiceGenerator;
import com.lihb.babyvoice.adapter.MessageAdapter;
import com.lihb.babyvoice.customview.RefreshLayout;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.customview.base.BaseRecyclerView;
import com.lihb.babyvoice.model.Message;
import com.lihb.babyvoice.utils.CommonToast;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by lhb on 2017/2/8.
 */

public class MessageFragment extends BaseFragment {

    private static final String TAG = "MessageFragment";
    private RefreshLayout mRefreshLayout;
    private BaseRecyclerView mRecyclerView;
    private MessageAdapter mMessageAdapter;
    private List<Message> mData = new ArrayList<>();
    private boolean hasMoreData = false;
    private View emptyView;

    private TitleBar mTitleBar;

    private static final int COUNT = 10;

    public static MessageFragment create() {
        return new MessageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        hideBottomTab();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden == false) {
            hideBottomTab();
        }
    }

    private void initView() {
        emptyView = getView().findViewById(R.id.empty_root_view);

        emptyView.findViewById(R.id.empty_image).setVisibility(View.INVISIBLE);
        ((TextView) emptyView.findViewById(R.id.empty_txt)).setText("暂无消息~");

        mTitleBar = (TitleBar) getView().findViewById(R.id.title_bar);
        mTitleBar.setLeftOnClickListener(v -> getActivity().onBackPressed());

        mRefreshLayout = (RefreshLayout) getView().findViewById(R.id.message_refreshlayout);
        mRecyclerView = (BaseRecyclerView) getView().findViewById(R.id.message_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.setEmptyView(emptyView);

        mMessageAdapter = new MessageAdapter(getContext(), mData);
        mRecyclerView.setAdapter(mMessageAdapter);

        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.setRefreshing(true);
                getData(true);
            }
        });
        mRefreshLayout.registerLoadMoreListenerForChildView(mRecyclerView, new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                if (hasMoreData) {
                    getData(false);
                    return;
                } else {
                    CommonToast.showShortToast("加载完毕");
                }
                mRefreshLayout.setLoading(false);
            }
        });
        getData(true);

    }

    private void hideBottomTab() {
        if (getActivity() == null) {
            return;
        }
        // 隐藏底部的导航栏和分割线
        (getActivity().findViewById(R.id.tab_layout)).setVisibility(View.GONE);
        (getActivity().findViewById(R.id.main_divider_line)).setVisibility(View.GONE);
    }

    private void getData(final boolean refresh) {
        int start = 0;
        if (refresh) {
            start = 0;
        } else {
            start = mData.size();
        }
        ServiceGenerator.createService(ApiManager.class)
                .getMessage(0, 1200, 0, 1, 10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(httpResListHttpResponse -> {
                    List<Message> messageList = httpResListHttpResponse.list;
                    if (!messageList.isEmpty()) {
                        if (refresh) {
                            mData.clear();
                        }

                        hasMoreData = COUNT <= messageList.size();

                        mData.addAll(messageList);
                        mMessageAdapter.notifyDataSetChanged();
                    }
                    onLoadedData(refresh);
                }, throwable -> {
                    CommonToast.showShortToast("获取消息数据失败");
                    Logger.e(throwable.toString());
                });
    }


    private void onLoadedData(final boolean refresh) {
        if (refresh) {
            mRefreshLayout.setRefreshing(false);
        } else {
            mRefreshLayout.setLoading(false);
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

}
