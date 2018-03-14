package com.lihb.babyvoice;

import android.app.Application;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.facebook.stetho.Stetho;
import com.lihb.babyvoice.command.HeadSetPluginChangedCommand;
import com.lihb.babyvoice.command.LoginStateChangedCommand;
import com.lihb.babyvoice.utils.BroadcastWatcher;
import com.lihb.babyvoice.utils.FileUtils;
import com.lihb.babyvoice.utils.NotificationCenter;
import com.lihb.babyvoice.utils.RxBus;
import com.lihb.babyvoice.utils.SingleOkHttpClient;
import com.lihb.babyvoice.utils.UserProfileChangedNotification;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.store.PersistentCookieStore;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.InputStream;

public class BabyVoiceApp extends Application {


    public static String currUserName;

    private static BabyVoiceApp instance = null;

    private static String cachePath;

    private boolean mScreenOn = false;

    private BroadcastWatcher mBroadcastWatcher;

    private boolean mIsLogin = false;

    /**
     * 耳机是否插入
     */
    private boolean mIsPlugIn = false;

    public String getCachePath() {
        return cachePath;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        //Stetho
        Stetho.initializeWithDefaults(this);
        //Glide与OkHttpClient集成

        //Glide与OkHttpClient集成
        Glide.get(this)
                .register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(SingleOkHttpClient.getInstance()));
//        NotificationCenter.INSTANCE.addCallbacks(PhoneStateChangedCallback.class);
        NotificationCenter.INSTANCE.addCallbacks(UserProfileChangedNotification.class);

        mBroadcastWatcher = new BroadcastWatcher(this);
        mBroadcastWatcher.startWatch();

        //友盟
        MobclickAgent.setDebugMode(true);

        initOKGO();

        // 初始化缓存路径
        initPath();

    }

    private void initOKGO() {
        //必须调用初始化
        OkGo.init(this);

        //以下设置的所有参数是全局参数,同样的参数可以在请求的时候再设置一遍,那么对于该请求来讲,请求中的参数会覆盖全局参数
        //好处是全局参数统一,特定请求可以特别定制参数
        try {
            //以下都不是必须的，根据需要自行选择,一般来说只需要 debug,缓存相关,cookie相关的 就可以了
            OkGo.getInstance()

                    //打开该调试开关,控制台会使用 红色error 级别打印log,并不是错误,是为了显眼,不需要就不要加入该行
                    .debug("OkGo")

                    //如果使用默认的 60秒,以下三行也不需要传
                    .setConnectTimeout(OkGo.DEFAULT_MILLISECONDS)  //全局的连接超时时间
                    .setReadTimeOut(OkGo.DEFAULT_MILLISECONDS)     //全局的读取超时时间
                    .setWriteTimeOut(OkGo.DEFAULT_MILLISECONDS)    //全局的写入超时时间

                    //可以全局统一设置缓存模式,默认是不使用缓存,可以不传,具体其他模式看 github 介绍 https://github.com/jeasonlzy/
                    .setCacheMode(CacheMode.NO_CACHE)

                    //可以全局统一设置缓存时间,默认永不过期,具体使用方法看 github 介绍
                    .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)

                    //如果不想让框架管理cookie,以下不需要
//                .setCookieStore(new MemoryCookieStore())                //cookie使用内存缓存（app退出后，cookie消失）
                    .setCookieStore(new PersistentCookieStore());          //cookie持久化存储，如果cookie不过期，则一直有效

            //可以设置https的证书,以下几种方案根据需要自己设置,不需要不用设置
//                    .setCertificates()                                  //方法一：信任所有证书
//                    .setCertificates(getAssets().open("srca.cer"))      //方法二：也可以自己设置https证书
//                    .setCertificates(getAssets().open("aaaa.bks"), "123456", getAssets().open("srca.cer"))//方法三：传入bks证书,密码,和cer证书,支持双向加密

            //可以添加全局拦截器,不会用的千万不要传,错误写法直接导致任何回调不执行
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        return chain.proceed(chain.request());
//                    }
//                })

            //这两行同上,不需要就不要传
//                    .addCommonHeaders(headers)                                         //设置全局公共头
//                    .addCommonParams(params);                                          //设置全局公共参数
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BabyVoiceApp getInstance() {
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if (mBroadcastWatcher != null) {
            mBroadcastWatcher.stopWatch();
            mBroadcastWatcher = null;
        }
    }

    public boolean getLogin() {
        return mIsLogin;
    }

    public void setLogin(boolean value) {
        if (mIsLogin != value) {
            mIsLogin = value;
            LoginStateChangedCommand.LoginState loginState = LoginStateChangedCommand.LoginState.LOGIN_OFF;
            if (value) {
                loginState = LoginStateChangedCommand.LoginState.LOGIN_ON;
            }
            LoginStateChangedCommand loginStateChangedCommand = new LoginStateChangedCommand(loginState);
            RxBus.getDefault().post(loginStateChangedCommand);
        }
    }

    public boolean isPlugIn() {
        return mIsPlugIn;
    }

    public void setPlugIn(boolean isPlugIn) {
        if (mIsPlugIn != isPlugIn) {
            mIsPlugIn = isPlugIn;
            HeadSetPluginChangedCommand.HeadSetPluginState headSetPluginState = HeadSetPluginChangedCommand.HeadSetPluginState.HEAD_SET_OUT;
            if (isPlugIn) {
                headSetPluginState = HeadSetPluginChangedCommand.HeadSetPluginState.HEAD_SET_IN;
            }
            HeadSetPluginChangedCommand headSetPluginChangedCommand = new HeadSetPluginChangedCommand(headSetPluginState);
            RxBus.getDefault().post(headSetPluginChangedCommand);
        }
    }

    public boolean isScreenOn() {
        return mScreenOn;
    }

    public void setScreenOn(boolean mScreenOn) {
        this.mScreenOn = mScreenOn;
    }

    public static void initPath() {
        cachePath = instance.getCacheDir().getAbsolutePath() + "/";
        if (FileUtils.canWriteExternal()) {
            cachePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/babyVoice/Caches/";
        }
        Logger.d("[cache path] init cachePath is %s", cachePath);
        File f = new File(cachePath);
        if (!f.exists())
            f.mkdirs();
    }


}
