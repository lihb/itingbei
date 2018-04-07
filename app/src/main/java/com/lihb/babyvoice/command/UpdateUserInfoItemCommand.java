package com.lihb.babyvoice.command;

/**
 * Created by lihb on 2018/4/7.
 */

public class UpdateUserInfoItemCommand extends BaseAndroidCommand {

    public String content;
    public int itemIndex;

    public UpdateUserInfoItemCommand(String content, int itemIndex) {
        this.content = content;
        this.itemIndex = itemIndex;
    }
}
