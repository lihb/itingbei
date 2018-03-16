package com.lihb.babyvoice.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.model.BabyBirthDay;
import com.lihb.babyvoice.utils.StringUtils;

/**
 * Created by lhb on 2017/2/9.
 */

public class BabyInfoViewHolder extends RecyclerView.ViewHolder {


    public ImageView babyAvatarImg = null;
    public TextView babyNickName = null;
    public TextView babyDate = null;
    public ImageView babySelectImg = null;
    private BabyBirthDay birthDay;
    private View itemView;

    public BabyInfoViewHolder(final View itemView) {
        super(itemView);
        this.itemView = itemView;
        babyAvatarImg = (ImageView) itemView.findViewById(R.id.baby_avatar);
        babyNickName = (TextView) itemView.findViewById(R.id.baby_nick_name_txt);
        babyDate = (TextView) itemView.findViewById(R.id.baby_date_txt);
        babySelectImg = (ImageView) itemView.findViewById(R.id.baby_select_img);

        itemView.setOnClickListener(v -> showDetail(birthDay));
    }

    private void showDetail(BabyBirthDay birthDay) {
        if (birthDay == null) {
            return;
        }
        // 跳转到网站activity
//        String url = ServiceGenerator.API_BASE_URL + "mobile/article/detailInfo.do?id=" + mMsg.id + "&rows=10";
//        WebViewActivity.navigate(itemView.getContext(), url, null);
    }

    public void bindData(BabyBirthDay birthDay) {
        this.birthDay = birthDay;
        babyNickName.setText(birthDay.username);
        babyDate.setText(birthDay.birthday);

        if (!StringUtils.isBlank(birthDay.babyAvatar)) {
            Glide.with(itemView.getContext())
                    .load(birthDay.babyAvatar)
                    .placeholder(R.mipmap.logo)
                    .error(R.mipmap.logo)
                    .into(babyAvatarImg);
        } else {
            babyAvatarImg.setImageResource(R.mipmap.logo);
        }
        babySelectImg.setImageResource(birthDay.isSelected ? R.mipmap.selected : R.mipmap.normal);
    }


}
