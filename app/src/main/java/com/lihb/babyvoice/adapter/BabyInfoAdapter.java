package com.lihb.babyvoice.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.model.BabyBirthDay;

import java.util.List;

/**
 * Created by lhb on 2017/2/9.
 */
public class BabyInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private List<BabyBirthDay> mData;

    public BabyInfoAdapter(Context mContext, List<BabyBirthDay> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.baby_list_item, parent, false);
        return new BabyInfoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BabyBirthDay babyBirthDay = mData.get(position);
        if (holder instanceof BabyInfoViewHolder) {
            BabyInfoViewHolder viewHolder = (BabyInfoViewHolder) holder;
            viewHolder.bindData(babyBirthDay);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void updateData(List<BabyBirthDay> data) {
        mData = data;
        notifyDataSetChanged();
    }


}
