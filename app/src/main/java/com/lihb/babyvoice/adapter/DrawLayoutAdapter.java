package com.lihb.babyvoice.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.activity.BluetoothActivityNew;
import com.lihb.babyvoice.activity.WebViewActivity;
import com.lihb.babyvoice.customview.CircularImageView;
import com.lihb.babyvoice.customview.DividerLine;
import com.lihb.babyvoice.customview.IconFontTextView;
import com.lihb.babyvoice.model.DrawLayoutEntity;
import com.lihb.babyvoice.utils.CommonToast;
import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/9/23.
 */

public class DrawLayoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private List<DrawLayoutEntity> mData;

    public DrawLayoutAdapter(Context mContext, List<DrawLayoutEntity> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.common_item, parent, false);
        return new DrawLayoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DrawLayoutViewHolder) {
            DrawLayoutViewHolder viewHolder = (DrawLayoutViewHolder) holder;
            DrawLayoutEntity entity = mData.get(position);
            viewHolder.bindData(entity);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class DrawLayoutViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_icon_left)
        IconFontTextView itemIconLeft;
        @BindView(R.id.item_name)
        TextView itemName;
        @BindView(R.id.item_icon_right)
        ImageView itemIconRight;
        @BindView(R.id.item_value)
        TextView itemValue;
        @BindView(R.id.item_divider)
        DividerLine itemDivider;
        @BindView(R.id.common_item_group)
        RelativeLayout commonItemGroup;
        @BindView(R.id.user_avatar)
        CircularImageView userAvatar;

        private DrawLayoutEntity mEntity;

        public DrawLayoutViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                if (mEntity != null && !TextUtils.isEmpty(mEntity.itemUrl)) {
                    WebViewActivity.navigate(itemView.getContext(), mEntity.itemUrl, null);
                } else if (mEntity != null && mEntity.title.equals("蓝牙")) {
                    BluetoothActivityNew.navigate(itemView.getContext());
                } else {
                    Logger.e("url 为空！！");
                    CommonToast.showShortToast(mEntity.title + " was clicked");
                }
            });
        }

        public void bindData(DrawLayoutEntity entity) {
            mEntity = entity;
            itemDivider.setVisibility(View.VISIBLE);
            itemIconRight.setVisibility(View.INVISIBLE);
            itemName.setText(entity.title);
            userAvatar.setVisibility(View.GONE);
//            itemName.setTextSize(DimensionUtil.spToPx(mContext, 5.5f));
            itemValue.setText(entity.detail);
            itemIconLeft.setText(entity.icon);
        }
    }
}
