package com.lihb.babyvoice.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.model.Message;

import java.util.List;

/**
 * Created by lhb on 2017/2/9.
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int ITEM_ADV = 0;
    public static final int ITEM_TEXT = 1;

    private Context mContext;
    private List<Message> mData;

    public MessageAdapter(Context mContext, List<Message> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == ITEM_ADV) {
            view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.message_list_item, parent, false);
            return new MessageViewHolder(view);
        } else {
            view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.message_list_text_item, parent, false);
            return new TextViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = mData.get(position);
        if (holder instanceof MessageViewHolder) {
            MessageViewHolder viewHolder = (MessageViewHolder) holder;
            viewHolder.bindData(message);
        } else {
            TextViewHolder textViewHolder = (TextViewHolder) holder;
            textViewHolder.content_txt.setText(message.shortabstract);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (mData == null) {
            return ITEM_ADV;
        }
        Message message = mData.get(position);
        if (message.typecode == 1201) {
            return ITEM_ADV;
        } else {
            return ITEM_TEXT;
        }

    }

    private class TextViewHolder extends RecyclerView.ViewHolder {
        private TextView content_txt;

        public TextViewHolder(View itemView) {
            super(itemView);
            content_txt = (TextView) itemView.findViewById(R.id.msg_content);
        }

    }


}
