package com.lihb.babyvoice.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.adapter.BabyInfoAdapter;
import com.lihb.babyvoice.customview.RefreshLayout;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.customview.base.BaseRecyclerView;
import com.lihb.babyvoice.model.BabyBirthDay;
import com.lihb.babyvoice.utils.CommonToast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lihb on 2017/3/5.
 */

public class BabyInfoFragment extends BaseFragment {

    private RefreshLayout mRefreshLayout;
    private BaseRecyclerView mRecyclerView;
    private BabyInfoAdapter mBabyInfoAdapter;
    private List<BabyBirthDay> mData = new ArrayList<>();
    private boolean hasMoreData = false;
    private View emptyView;
    private ImageView mAddBabyInfoImg;

    private static final int COUNT = 10;

    private int mSelYear, mSelMonth, mSelDay;

    private TitleBar mTitleBar;
    private DateSelectFragment mDateSelectFragment;

    public static BabyInfoFragment create() {
        return new BabyInfoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_baby_info, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {

        mTitleBar = (TitleBar) getView().findViewById(R.id.title_bar);
        mTitleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        mAddBabyInfoImg = (ImageView) getView().findViewById(R.id.add_grow_up_record_img);

        mAddBabyInfoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonToast.showShortToast("Add Baby Info ");
                gotoDateSelectFragment(MeFragment.ITEM_SET_BABY_BIRTHDAY);
            }
        });

        mRefreshLayout = (RefreshLayout) getView().findViewById(R.id.grow_up_swipe_refresh_widget);
        mRecyclerView = (BaseRecyclerView) getView().findViewById(R.id.grow_up_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.setEmptyView(emptyView);

        mBabyInfoAdapter = new BabyInfoAdapter(getContext(), mData);
        mRecyclerView.setAdapter(mBabyInfoAdapter);

        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.setRefreshing(true);
                getData(true);
            }
        });
//        mRefreshLayout.registerLoadMoreListenerForChildView(mRecyclerView, new RefreshLayout.OnLoadListener() {
//            @Override
//            public void onLoad() {
//                if (hasMoreData) {
//                    getData(false);
//                    return;
//                } else {
//                    CommonToast.showShortToast("加载完毕");
//                }
//                mRefreshLayout.setLoading(false);
//            }
//        });

    }


    @Override
    public void onResume() {
        super.onResume();
        getData(true);
    }

    private void getData(final boolean refresh) {
        //测试数据
        for (int i = 0; i < 3; i++) {
            BabyBirthDay birthday = new BabyBirthDay();
            birthday.username = "昵称 " + i;
            birthday.birthday = "2017/10/" + i;
            birthday.isSelected = (i % 2 != 0);
            mData.add(birthday);
        }
        mBabyInfoAdapter.updateData(mData);

    }


    private void gotoDateSelectFragment(int type) {
        if (null == mDateSelectFragment) {
            mDateSelectFragment = DateSelectFragment.create();
        }
        Bundle bundle = new Bundle();
        bundle.putInt("itemType", type);
        mDateSelectFragment.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(this);
        transaction.add(R.id.main_layout, mDateSelectFragment, "DateSelectFragment")
                .show(mDateSelectFragment)
                .addToBackStack(null)
                .commit();

    }
}
