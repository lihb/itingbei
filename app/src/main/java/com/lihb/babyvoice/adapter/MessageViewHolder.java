package com.lihb.babyvoice.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.action.ServiceGenerator;
import com.lihb.babyvoice.model.Message;
import com.lihb.babyvoice.utils.StringUtils;
import com.lihb.babyvoice.view.WebViewActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lhb on 2017/2/9.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public TextView titleText = null;
    public TextView timeText = null;
    public TextView contentText = null;
    public ImageView messageImg = null;
    private Message mMsg;
    private View itemView;

    public MessageViewHolder(final View itemView) {
        super(itemView);
        this.itemView = itemView;
        titleText = (TextView) itemView.findViewById(R.id.msg_title_txt);
        timeText = (TextView) itemView.findViewById(R.id.msg_date_txt);
        contentText = (TextView) itemView.findViewById(R.id.msg_content_txt);
        messageImg = (ImageView) itemView.findViewById(R.id.msg_image);

        itemView.setOnClickListener(v -> showDetail(mMsg));
    }

    private void showDetail(Message mMsg) {
        if (mMsg == null) {
            return;
        }
        // 跳转到网站activity
        String url = ServiceGenerator.API_BASE_URL + "mobile/article/detailInfo.do?id=" + mMsg.id + "&rows=10";
        WebViewActivity.navigate(itemView.getContext(), url, null);
    }

    public void bindData(Message msg) {
        mMsg = msg;
        titleText.setText(msg.title);
        contentText.setText(msg.shortabstract);
        Date date = new Date(msg.time);
        timeText.setText(sdf.format(date));
        if (!StringUtils.isBlank(msg.smallpic)) {
            Glide.with(itemView.getContext())
                    .load(ServiceGenerator.API_BASE_URL + msg.smallpic.substring(1))
                    .placeholder(R.mipmap.logo)
                    .error(R.mipmap.logo)
                    .into(messageImg);
        } else {
            messageImg.setImageResource(R.mipmap.logo);
        }
//        setCategoryImg(voice.category);
    }

//    private void setCategoryImg(String category) {
//        String[] items = itemView.getResources().getStringArray(R.array.voice_type);
//        int[] ids = {R.mipmap.catetory_heart, R.mipmap.category_lung, R.mipmap.category_baby_voice, R.mipmap.category_other_voice};
//        if (StringUtils.areEqual(category, items[0])) {
//            catergoryImg.setImageResource(ids[0]);
//        } else if (StringUtils.areEqual(category, items[1])) {
//            catergoryImg.setImageResource(ids[1]);
//        } else if (StringUtils.areEqual(category, items[2])) {
//            catergoryImg.setImageResource(ids[2]);
//        } else {
//            catergoryImg.setImageResource(ids[3]);
//        }
//    }


}
