package cn.cloudwalk.sposterformachine;

import cn.cloudwalk.callback.DefineRecognizeCallBack;


public class PhoneApp {
	public static String appSecret="test";//
	public static String appid="test";
	
	public static String serverStr;
	public static  double scoreThreshold = 0.7f;
	public static  int faceVerifyType = 1;
	public static byte[] localPhotoByte;
	public static String remotePhotoURL = "http://127.0.0.1/face.jpg";
	
	public static  boolean isSound = true;
	public static  int timerCount=8;
	public static boolean isResultPage=true;
	
	
	public static int WidthPx;
	public static int HeightPx;
	public static float Density;
	
	public static String publicFilePath;//公共存储目录
	
	public static String ocrServer;
	public static int liveCount=3;
	public static int livingLevel=2;
	public static DefineRecognizeCallBack defineRecognizeCallBack;
	
	//小云考勤对接的地址
	public static String ATTENDANCEADDR;

	

	
}
