package cn.cloudwalk.sposterformachine;

import android.app.Activity;
import android.content.Intent;

import java.io.Serializable;
import java.util.ArrayList;

import cn.cloudwalk.callback.DefineRecognizeCallBack;
import cn.cloudwalk.callback.ResultCallBack;

public class Bulider implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2457253871557464276L;
    // 1:l 2:r 3: u3:d 5: eye 6:mouth
    public final static int HEAD_L = 1, HEAD_R = 2, HEAD_U = 3, HEAD_D = 4,
            EYE = 5, MOUTH = 6;
    public final static int FACE_VERIFY_NONE = 0,
            FACE_VERIFY_LOCAL = 1,
            FACE_VERIFY_DEFINE = 2;

    public static final int LIVE_LEVEL_EASY = 1, LIVE_LEVEL_STANDARD = 2,
            LIVE_LEVEL_HARD = 3;

    //设置值
    public static String appid;
    public static String appSecret;
    public static String faceServer;
    public static ArrayList<Integer> totalLiveList;
    public static int execLiveCount;
    public static boolean isLivesRandom;
    public static int liveLevel;
    public static boolean isResultPage;
    public static ResultCallBack mResultCallBack;
    public static int faceVerifyType;
    public static String facePicPath;
    public static DefineRecognizeCallBack dfvCallBack;
    public static double scoreThreshold;
    public static String authServer;

    public static int timerCount = 8;
    public static boolean isSound = true;

    public static String num = null;//手机号码
    public static boolean isRegist;//是否是注册
    // 设置信息

    /**
     * 设置用户授权信息
     *
     * @param authServer
     * @param appid
     * @param appSecret
     * @return
     */
    public Bulider initAuth(String authServer, String appid, String appSecret) {
        Bulider.authServer = authServer;
        Bulider.appid = appid;
        Bulider.appSecret = appSecret;
        return this;
    }

    ;

    /**
     * 设置人脸比对信息
     *
     * @param faceServer     比对服务器
     * @param faceVerifyType 比对方式
     * @param facePicPath    本地比对图片地址
     * @param dfvCallBack    自定义比对回掉
     * @return
     */
    public Bulider initFaceVerify(String faceServer, double scoreThreshold, int faceVerifyType, String facePicPath, DefineRecognizeCallBack dfvCallBack) {

        Bulider.faceServer = faceServer;
        Bulider.faceVerifyType = faceVerifyType;
        Bulider.facePicPath = facePicPath;
        Bulider.dfvCallBack = dfvCallBack;
        Bulider.scoreThreshold = scoreThreshold;
        return this;
    }

    ;

    /**
     * 设置活体
     *
     * @param liveList      总活体值
     * @param execLiveCount 执行活体数
     * @param isLivesRandom 活体是否随机
     * @param liveLevel     活体等级
     * @return
     */
    public Bulider setLives(ArrayList<Integer> liveList, int execLiveCount, boolean isLivesRandom, int liveLevel) {
        Bulider.totalLiveList = liveList;
        Bulider.execLiveCount = execLiveCount;
        Bulider.isLivesRandom = isLivesRandom;
        Bulider.liveLevel = liveLevel;
        return this;
    }

    ;

    /**
     * 是否显示结果页面
     *
     * @param isResultPage
     * @return
     */
    public Bulider isResultPage(boolean isResultPage) {
        Bulider.isResultPage = isResultPage;
        return this;
    }

    /**
     * 设置结果回调
     *
     * @param mResultCallBack
     * @return
     */
    public Bulider setResultCallBack(ResultCallBack mResultCallBack) {
        Bulider.mResultCallBack = mResultCallBack;
        return this;
    }

    public void startActivity(Activity act, Class cls) {
        Intent it = new Intent(act, cls);
        act.startActivity(it);

    }

    ;

    /**
     * 设置电话号码
     */
    public Bulider setPhoneNum(String num) {
        Bulider.num = num;
        return this;
    }

    /**
     * 判断是否是注册
     */
    public Bulider isRegistFace(boolean isRegist){
        Bulider.isRegist = isRegist;
        return this;
    }
}
