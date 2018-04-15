package com.lihb.babyvoice.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.command.UpdateUserInfoItemCommand;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.utils.RxBus;
import com.lihb.babyvoice.utils.SoftInputUtil;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by lhb on 2017/2/8.
 */

public class PersonalInfoChangeFragment extends BaseFragment {

    private static final String TAG = "PersonalInfoChangeFragment";
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.personal_info_change_content_edit)
    EditText personalInfoChangeContentEdit;
    @BindView(R.id.personal_info_change_clear_input_img)
    ImageView personalInfoChangeClearInputImg;
    @BindView(R.id.personal_info_change_content_rl)
    RelativeLayout personalInfoChangeContentRl;
    Unbinder unbinder;

    private String content;
    private int currChangeItem;

    public static PersonalInfoChangeFragment create() {
        return new PersonalInfoChangeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle b = getArguments();
        if (null != b) {
            content = b.getString("content");
            currChangeItem = b.getInt("itemIndex");
        }
        View view = inflater.inflate(R.layout.fragment_personal_change, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        personalInfoChangeClearInputImg.setVisibility(content.length() > 0 ? View.VISIBLE : View.GONE);
        personalInfoChangeContentEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                personalInfoChangeClearInputImg.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });

        titleBar.setRightOnClickListener(v -> {
            SoftInputUtil.hideSoftInput(getActivity());
            content = personalInfoChangeContentEdit.getText().toString();
            RxBus.getDefault().post(new UpdateUserInfoItemCommand(content, currChangeItem));
            getActivity().onBackPressed();
        });

        titleBar.setLeftOnClickListener(v -> {
            SoftInputUtil.hideSoftInput(getActivity());
            getActivity().onBackPressed();
        });
    }


    public void onResume() {
        super.onResume();
        personalInfoChangeContentEdit.setText(content);
        MobclickAgent.onPageStart(TAG);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.personal_info_change_clear_input_img)
    public void onViewClicked() {
        personalInfoChangeContentEdit.setText("");
    }

}
