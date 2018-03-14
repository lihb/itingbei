package com.lihb.babyvoice.model;

/**
 * Created by Administrator on 2017/6/22.
 */

public class Message {
    public Integer id;  //文章id信息

    public String title;  //标题

    public String shortabstract;  //广告消息的摘要。文本消息的主内容

    public String smallpic;  //消息缩略图路径

    public String content;   //消息内容。

    public String realname;

    public String username;

    public long time; //发布时间

    public String type;

    public Integer typecode;  //消息类别代码

    public String hidden;

    public String keywords;

    public String attachment;

    public Integer totalcounts;

    public Integer comments;

    public Integer parentcode;  //父类别代码


    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", shortabstract='" + shortabstract + '\'' +
                ", smallpic='" + smallpic + '\'' +
                ", content='" + content + '\'' +
                ", realname='" + realname + '\'' +
                ", username='" + username + '\'' +
                ", time=" + time +
                ", type='" + type + '\'' +
                ", typecode=" + typecode +
                ", hidden='" + hidden + '\'' +
                ", keywords='" + keywords + '\'' +
                ", attachment='" + attachment + '\'' +
                ", totalcounts=" + totalcounts +
                ", comments=" + comments +
                ", parentcode=" + parentcode +
                '}';
    }
}
