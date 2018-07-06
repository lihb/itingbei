package com.lihb.babyvoice.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.lihb.babyvoice.Constant;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.umeng.analytics.MobclickAgent;


public class WebViewFragment extends BaseFragment {

    private static final String TAG = "WebViewFragment";
    private ProgressBar mProgressBar = null;
    private WebView mWebView = null;
    private String mUrl = "about:blank";

    public static WebViewFragment create() {
        return new WebViewFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.loadUrl(mUrl);
        }
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.loadUrl("about:blank");
        }
        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_webview, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
    }

    private void initViews() {

        mProgressBar = (ProgressBar) getView().findViewById(R.id.showAdvProgressBar);
        mProgressBar.setProgress(0);

        mWebView = (WebView) getView().findViewById(R.id.showAdvWebView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                super.onProgressChanged(view, newProgress);
            }

//            @Override
//            public void onReceivedTitle(WebView view, String title) {
//                super.onReceivedTitle(view, title);
//                if (TextUtils.isEmpty(getIntent().getStringExtra("title"))) { // 如果没给标题，就从网页中获取标题
//                    mTitleTextView.setText(title);
//                }
//            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        mUrl = Constant.ITING_MUSIC;

        TitleBar titleBar = (TitleBar) getView().findViewById(R.id.title_bar);
        titleBar.setLeftVisible(false);
        titleBar.setTitle(R.string.tab_music);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ViewGroup viewGroup = (ViewGroup) getActivity().getWindow().getDecorView();
        if (viewGroup != null) {
            // Bug: Activity has leaked window android.widget.ZoomButtonsController that was originally added here android.view.WindowLeaked
            // 移除所有控件，防止关闭该activity时，内存泄漏的问题
            viewGroup.removeAllViews();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden == false) {
            showBottomTab();
        }
    }

    private void showBottomTab() {
        if (getActivity() == null) {
            return;
        }
        // 隐藏底部的导航栏和分割线
        (getActivity().findViewById(R.id.tab_layout)).setVisibility(View.VISIBLE);
        (getActivity().findViewById(R.id.main_divider_line)).setVisibility(View.VISIBLE);
    }
}
