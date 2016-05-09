package cn.cloudwalk.sposterformachine.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.cloudwalk.CloudwalkFaceSDK;
import cn.cloudwalk.FaceEnumResult;
import cn.cloudwalk.callback.DetectCallBack;
import cn.cloudwalk.callback.LiveCallBack;
import cn.cloudwalk.callback.RecognizeCallBack;

import cn.cloudwalk.sposterformachine.Bulider;
import cn.cloudwalk.sposterformachine.R;
import cn.cloudwalk.sposterformachine.view.MyCameraSurfaceView;
import cn.cloudwalk.util.ImgUtil;
import cn.cloudwalk.util.LogUtils;
import cn.cloudwalk.util.Util;
import cn.cloudwalk.view.CameraSurfaceView;
import cn.cloudwalk.view.CustomViewPager;
import cn.cloudwalk.view.RoundProgressBarWidthNumber;

/**
 * 自定义集成</br> 自定义打开摄像头和pushFrame使用,代码参见:MyCameraSurfaceView</br>
 * 注意:人脸比对需要一张比对图片,请自己设置faceVerifyType,localPhotoPath,remotePhotoURL等变量进行人脸比对
 * 
 * @author 284891377
 * 
 */
public class PushActivity extends Activity implements DetectCallBack,
		LiveCallBack, RecognizeCallBack {
	private final String TAG = this.getClass().getSimpleName();

	final static int UPDATE_UI = 100, NEXT_STEP = 101, TOAST_TIP = 105,
			UPDATE_STEP_PROCRESS = 106, START_RECOGNIZE_HEAD_D = 110,
			START_RECOGNIZE_HEAD_L = 107, START_RECOGNIZE_HEAD_R = 108,
			START_RECOGNIZE_HEAD_U = 109, START_RECOGNIZE_EYE = 111,
			START_RECOGNIZE_MOUTHOPEN = 113, START_SNAPSHOT = 116,
			START_FACEVERIFY = 117, UPDATE_MESSAGE = 118, BEST_FACE = 121,
			SET_RESULT = 122, DETECT_FACE = 123, UPDATESTEPLAYOUT = 124;

	private boolean is_living = false;
	private boolean is_face_pass = false;
	private String face_session_id = "";
	private String best_face = null;
	private String best_idface = null;
	private double face_score = 0d;
	private int resultType;
	boolean isSetResult;

	SoundPool sndPool;
	private Map<String, Integer> poolMap;
	int currentStreamID;

	CameraSurfaceView mPreview;
	FrameLayout mFlCarema_mask;
	CustomViewPager mViewPager;
	ViewPagerAdapter viewPagerAdapter;
	ImageView mIv_step1Face;

	RoundProgressBarWidthNumber mPb_step;
	ImageView mIv_step;
	TextView mTv_step;

	// 开场动画

	ImageView mIv_cycle;
	Runnable faceTimerRunnable;
	int faceMouthTimerCount;

	private AnimationDrawable animationDrawable;
	float facePitchDegree;
	boolean isStop;
	boolean isPlayNextStep;
	// 认证步骤
	int totalStep;
	int currentStep;
	ArrayList<View> viewList;

	MainHandler mMainHandler;

	// 版权图片
	ImageView mIv_copyright;
	Bitmap mCopyright;

	public CloudwalkFaceSDK cloudwalkFaceSDK;
	public int initRet;

	static PushActivity activity;

	public static List<Integer> execLiveList;

	public static PushActivity getFaceRecognize() {
		return activity;

	}

	private void initCloudwalkFaceSDK() {
		cloudwalkFaceSDK = CloudwalkFaceSDK.getInstance(this);
		// 设置在云从科技开发者平台获取的appid和secretKey
		initRet = cloudwalkFaceSDK.init(Bulider.faceServer, Bulider.authServer,
				10, Bulider.appid, Bulider.appSecret, Bulider.liveLevel);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_dect_push);

		mMainHandler = new MainHandler();
		initSoundPool();
		setAllCallBack();
		initView();
		initStepViews();
		// FaceRecognize单例实例化
		initCloudwalkFaceSDK();
		// 云丛logo
		try {
			mCopyright = BitmapFactory.decodeStream(this.getAssets().open(
					"yc_copyright.png"));
			if (mCopyright != null)
				mIv_copyright.setImageBitmap(mCopyright);
		} catch (IOException e) {
			LogUtils.LOGW(TAG, "Asset中没有mCopyright 图片文件");
			e.printStackTrace();
		}

		activity = this;
	}

	private void initView() {
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);// this指当前activity
		int width = dm.widthPixels;
		int height = dm.heightPixels;

		int contentViewH = height - getStatusBarHeight(this);
		// 根据预览分辨率设置surfaceView尺寸
		mPreview = (CameraSurfaceView) findViewById(R.id.textureview);// 0x7f060000
		mPreview.setKeepScreenOn(true);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,
				width * 640 / 480);
		mPreview.setLayoutParams(params);
		// 版权图片
		mIv_copyright = (ImageView) findViewById(R.id.copyright_iv);// 0x7f06000c
		// ViewPager
		mViewPager = (CustomViewPager) findViewById(R.id.viewpager);// 0x7f060003
		// 转圈
		mIv_cycle = (ImageView) findViewById(R.id.face_timer_cycle_img);// 0x7f060002

		// 设置viewpager和人脸框大小
		mFlCarema_mask = (FrameLayout) findViewById(R.id.carema_mask_fl);
//		if (mFlCarema_mask != null) {
//			params = new FrameLayout.LayoutParams(width, width);
//			params.gravity = Gravity.TOP;
//			mFlCarema_mask.setLayoutParams(params);
//			mFlCarema_mask
//					.setBackgroundResource(R.drawable.cloudwalk_face_main_camera_mask);
//
//			params = new FrameLayout.LayoutParams(width, contentViewH - width);
//			params.gravity = Gravity.BOTTOM;
//			mViewPager.setLayoutParams(params);
//		}

	}

	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		java.lang.reflect.Field field = null;
		int x = 0;
		int statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
			return statusBarHeight;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusBarHeight;
	}

	/**
	 * 返回比对结果
	 * 
	 * @param result
	 *            是否通过
	 * @param faceScore
	 *            比对分数
	 * @param faceSessionId
	 *            sessionId
	 * @param tipMsg
	 *            自定义提示信息
	 */
	public void setFaceResult(boolean result, double faceScore,
			String faceSessionId, String tipMsg) {

		face_score = faceScore;
		face_session_id = faceSessionId;
		is_face_pass = result;
		if (result) {
			resultType = MyFaceRecognizeResultActivity.FACE_VERFY_OK;
		} else {
			resultType = MyFaceRecognizeResultActivity.FACE_VERFY_FAIL;
		}
		if (Bulider.isResultPage) {
			Intent mIntent = new Intent(this,
					MyFaceRecognizeResultActivity.class);
			mIntent.putExtra(
					MyFaceRecognizeResultActivity.FACEDECT_RESULT_TYPE,
					resultType);
			mIntent.putExtra(MyFaceRecognizeResultActivity.FACEDECT_RESULT_MSG,
					tipMsg);
			startActivityForResult(mIntent, 23);
		} else {
			Bulider.mResultCallBack.result(is_living, is_face_pass,
					face_session_id, best_face, best_idface, face_score,
					resultType);
			finish();
		}

	}

	private void initStepViews() {
		// 计算随机活体
		if (Bulider.isLivesRandom)
			Collections.shuffle(Bulider.totalLiveList);
		execLiveList = Bulider.totalLiveList.subList(0, Bulider.execLiveCount);
		// viewList
		LayoutInflater lf = getLayoutInflater().from(this);
		viewList = new ArrayList<View>();
		View view;
		// 检测人脸item
		view = lf.inflate(R.layout.cloudwalk_layout_facedect_step1, null);// 0x7f030001
		mIv_step1Face = (ImageView) view
				.findViewById(R.id.cloudwalk_step1_start_img);// 0x7f060004
		AnimationDrawable animationDrawable = (AnimationDrawable) mIv_step1Face
				.getDrawable();
		animationDrawable.start();
		addView(view);
		// 活体item

		int size = execLiveList.size();
		for (int i = 0; i < size; i++) {

			view = lf.inflate(R.layout.cloudwalk_layout_facedect_step3, null);// 0x7f030002
			addView(view);
		}

		// 人证合一item
		// view = lf.inflate(R.layout.layout_face_dect_step3, null);//
		// 0x7f030002
		// addView(view);

		viewPagerAdapter = new ViewPagerAdapter(viewList);
		mViewPager.setAdapter(viewPagerAdapter);

	}

	private void addView(View view) {
		viewList.add(view);
		totalStep++;
	}

	private void initSoundPool() {
		poolMap = new HashMap<String, Integer>();
		sndPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
		poolMap.put("main", sndPool.load(this, R.raw.cloudwalk_main, 1));// 0x7f050007
		poolMap.put("mouth_open",
				sndPool.load(this, R.raw.cloudwalk_detection_mouth_open, 1));// 0x7f050002
		poolMap.put("head_left",
				sndPool.load(this, R.raw.cloudwalk_detection_yaw_left, 1));// 0x7f050005
		poolMap.put("head_right",
				sndPool.load(this, R.raw.cloudwalk_detection_yaw_right, 1));// 0x7f050006
		poolMap.put("head_up",
				sndPool.load(this, R.raw.cloudwalk_detection_pitch_up, 1));// 0x7f050004
		poolMap.put("head_down",
				sndPool.load(this, R.raw.cloudwalk_detection_pitch_down, 1));// 0x7f050003
		poolMap.put("next_step",
				sndPool.load(this, R.raw.cloudwalk_next_step, 1));// 0x7f050008
		poolMap.put("eye_blink",
				sndPool.load(this, R.raw.cloudwalk_detection_eye_blink, 1));// 0x7f050000
		poolMap.put("good", sndPool.load(this, R.raw.cloudwalk_good, 1));// 0x7f050000

	}

	/**
	 * 获取最佳人脸
	 */
	private void getBestFace() {

		ArrayList<byte[]> bestFaces = cloudwalkFaceSDK.getBestFaceImgBuf();
		int size = bestFaces.size();
		String cacheDir = Util.getDiskCacheDir(this);
		if (size > 0) {
			best_face = cacheDir + "/best_face.jpg";
			ImgUtil.saveJPGE_After(ImgUtil.bytesToBimap(bestFaces.get(0)),
					best_face, 90);
		}
		if (size > 1) {
			best_idface = cacheDir + "/best_idface.jpg";
			ImgUtil.saveJPGE_After(ImgUtil.bytesToBimap(bestFaces.get(1)),
					best_face, 90);
		}

	}

	private void setAllCallBack() {
		// 设置回调监听
		CloudwalkFaceSDK.setDetectCallBack(this);
		CloudwalkFaceSDK.setLiveCallBack(this);
		CloudwalkFaceSDK.setRecognizeCallBack(this);

	}

	public void executeStep() {
		// total=1 直接比对
		// total=2 有离散活体
		// total=3
		if (totalStep == currentStep) {// 人脸比对页面
			doFaceVerify();

		} else {
			executeLiving(execLiveList.get(currentStep - 1));

		}

	}

	private void doFaceVerify() {
		mMainHandler.removeCallbacks(faceTimerRunnable);

		is_living = true;
		if (Bulider.FACE_VERIFY_NONE == Bulider.faceVerifyType) {

			// tip_msg = "活体检测通过!";
			mMainHandler.obtainMessage(SET_RESULT,
					MyFaceRecognizeResultActivity.FACEDEC_OK).sendToTarget();

			return;
		} else if (Bulider.FACE_VERIFY_LOCAL == Bulider.faceVerifyType) {
			// TODO 自定义图片路径
			Bulider.facePicPath = best_idface;

			cloudwalkFaceSDK.faceVerifyByImg(Bulider.facePicPath);

		} else if (Bulider.FACE_VERIFY_DEFINE == Bulider.faceVerifyType) {
			if (Bulider.dfvCallBack != null) {
				cloudwalkFaceSDK.defineFaceVerify();
				Bulider.dfvCallBack.OnDefineFaceVerifyResult(best_idface,
						best_face);
			}

		}

		showCycleImg();
		mViewPager.setCurrentItem(currentStep, true);

	}

	private void setFaceResult(int result, boolean isPage) {

		if (isSetResult) {
			return;
		}
		isSetResult = true;
		resultType = result;
		if (isPage) {
			Intent mIntent = new Intent(this,
					MyFaceRecognizeResultActivity.class);
			mIntent.putExtra(
					MyFaceRecognizeResultActivity.FACEDECT_RESULT_TYPE, result);
			startActivityForResult(mIntent, 23);
		} else {

			Bulider.mResultCallBack.result(is_living, is_face_pass,
					face_session_id, best_face, best_idface, face_score,
					resultType);
			finish();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 31) {

			Bulider.mResultCallBack.result(is_living, is_face_pass,
					face_session_id, best_face, best_idface, face_score,
					resultType);
			finish();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void executeLiving(int i) {
		// 离散活体随机打乱 1:l 2:r 3: u3:nod 5: eye 6:mouth
		switch (i) {
		case 1:// l

			if (Bulider.isSound) {
				currentStreamID = poolMap.get("head_left");
				sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0, 1.0f);
			}
			startTimerRunnable(2);
			mMainHandler.sendEmptyMessageDelayed(START_RECOGNIZE_HEAD_L, 600);
			break;
		case 2:// r

			if (Bulider.isSound) {
				currentStreamID = poolMap.get("head_right");
				sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0, 1.0f);
			}
			startTimerRunnable(2);
			mMainHandler.sendEmptyMessageDelayed(START_RECOGNIZE_HEAD_R, 600);

			break;
		case 6:// 张嘴

			if (Bulider.isSound) {
				currentStreamID = poolMap.get("mouth_open");
				sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0, 1.0f);
			}

			startTimerRunnable(3);
			mMainHandler
					.sendEmptyMessageDelayed(START_RECOGNIZE_MOUTHOPEN, 600);
			break;
		case 3:// u

			if (Bulider.isSound) {
				currentStreamID = poolMap.get("head_up");
				sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0, 1.0f);
			}

			startTimerRunnable(2);

			mMainHandler.sendEmptyMessageDelayed(START_RECOGNIZE_HEAD_U, 600);

			break;
		case 5:// 眨眼
			mIv_step.setImageResource(R.drawable.cloudwalk_eye_anim);// 0x7f040002

			if (Bulider.isSound) {
				currentStreamID = poolMap.get("eye_blink");
				sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0, 1.0f);
			}

			startTimerRunnable(4);
			mMainHandler.sendEmptyMessageDelayed(START_RECOGNIZE_EYE, 600);

			break;
		case 4:// d

			if (Bulider.isSound) {
				currentStreamID = poolMap.get("head_down");
				sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0, 1.0f);
			}

			startTimerRunnable(2);
			mMainHandler.sendEmptyMessageDelayed(START_RECOGNIZE_HEAD_D, 600);

			break;
		}

	}

	@Override
	public void OnDetectFaceStatus(int ret) {// 人脸状态
		// Message msg = mMainHandler.obtainMessage(UPDATE_FACE_STATE);
		// msg.obj = cloudwalkFaceSDK.getTipMsg(ret);
		// mMainHandler.sendMessage(msg);

	}

	@Override
	public void OnFaceVerifyResult(int ret, double score, String sessionID) {
		face_session_id = sessionID;
		face_score = score;

		// 返回分数与本地设置人脸阀值进行比对 >通过;人脸阀值越小,识别越严格
		if (ret == FaceEnumResult.EnumOK && score > Bulider.scoreThreshold) {// 通过

			is_face_pass = true;

			// tip_msg = "验证通过!";
			mMainHandler.obtainMessage(SET_RESULT,
					MyFaceRecognizeResultActivity.FACE_VERFY_OK).sendToTarget();

		} else if (ret == 807) {
			is_face_pass = false;

			// tip_msg = "网络失败,请检测网络!";
			mMainHandler.obtainMessage(SET_RESULT,
					MyFaceRecognizeResultActivity.NET_FAIL).sendToTarget();

		} else {

			is_face_pass = false;

			// tip_msg = "验证失败!";
			mMainHandler.obtainMessage(SET_RESULT,
					MyFaceRecognizeResultActivity.FACE_VERFY_FAIL)
					.sendToTarget();

		}
	}

	@Override
	public void OnHeadRotateDetectResult(int ret) {
		if (isActionNotStandard || isStop)
			return;
		if (ret == FaceEnumResult.EnumLocalHeadRotateDetectYawR) {// 检测到右转头

			doNextStep();
		} else if (ret == FaceEnumResult.EnumLocalHeadRotateDetectYawL) {// 检测到左转头

			doNextStep();
		} else if (ret == FaceEnumResult.EnumLocalHeadRotateDetectPitchU) {// 检测到抬头

			doNextStep();
		} else if (ret == FaceEnumResult.EnumLocalHeadRotateDetectPitchD) {// 检测到点头

			doNextStep();
		}

	}

	private void doNextStep() {

		LogUtils.LOGI("OnActionNotStandard", "doNextStep");
		int nextDelayTime = 100;
		if (sndPool != null) {

			if (currentStep == 0) {// 不播放下一步
			} else if (totalStep - 1 == currentStep
					&& Bulider.FACE_VERIFY_NONE == Bulider.faceVerifyType) {
				// 播放很好

				if (Bulider.isSound) {
					isPlayNextStep = true;
					currentStreamID = poolMap.get("good");
					sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0, 1.0f);
					nextDelayTime = 1000;
				}
			} else {
				if (Bulider.isSound) {
					isPlayNextStep = true;
					currentStreamID = poolMap.get("next_step");
					sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0, 1.0f);
					nextDelayTime = 2000;
				}

			}

		}
		if (faceTimerRunnable != null)
			mMainHandler.removeCallbacks(faceTimerRunnable);
		currentStep++;
		mMainHandler.sendEmptyMessageDelayed(NEXT_STEP, nextDelayTime);
		if (totalStep != currentStep)
			mMainHandler.sendEmptyMessageDelayed(UPDATESTEPLAYOUT,
					nextDelayTime == 2000 ? nextDelayTime - 1000 : 10);
	}

	@Override
	public void OnMouthDetectResult(final int ret) {
		if (isActionNotStandard || isStop)
			return;
		if (ret == FaceEnumResult.EnumLocalDetectMouthOpen) {// 检查到张嘴

			doNextStep();

		}
	}

	@Override
	public void OnEyeDetectResult(int ret) {
		if (isActionNotStandard || isStop)
			return;
		if (ret == FaceEnumResult.EnumLocalDetectBlinkEye) {// 检查到眨眼

			doNextStep();

		}

	}

	@Override
	protected void onPause() {

		super.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();
		isFirstDetectFace = false;
		isSetResult = false;
		isStop = false;
		isActionNotStandard = false;

		currentStep = 0;

		faceMouthTimerCount = Bulider.timerCount;

		mViewPager.setCurrentItem(0);
		if (initRet == 0) {
			// 进度展示
			showCycleImg();
			startTimerRunnable(1);// 检测人脸角度
			mMainHandler.sendEmptyMessageDelayed(DETECT_FACE, 1800);// 开始人脸侦测
		} else {

			new AlertDialog.Builder(this)
					.setMessage("appid/appSecret已过期,\n请联系云丛技术支持!")
					.setNegativeButton("确定", new AlertDialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							finish();
						}
					}).show();

		}

	}

	private void showCycleImg() {
		mIv_cycle.setVisibility(View.VISIBLE);
		Animation operatingAnim = AnimationUtils.loadAnimation(this,
				R.anim.cloudwalk_cycle_anim);// 0x7f040000
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		mIv_cycle.startAnimation(operatingAnim);
	}

	@Override
	protected void onRestart() {
		super.onRestart();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cloudwalkFaceSDK.destroy();
		sndPool.release();
	}

	@Override
	protected void onStop() {
		super.onStop();
		isStop = true;

		mMainHandler.removeCallbacksAndMessages(null);
		sndPool.stop(currentStreamID);
		cloudwalkFaceSDK.stopDetectFace();
		hideCycleImg();
		// 随机取值
		if (Bulider.isLivesRandom)
			Collections.shuffle(Bulider.totalLiveList);
		execLiveList = Bulider.totalLiveList.subList(0, Bulider.execLiveCount);
	}

	public class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case BEST_FACE:
				getBestFace();
				break;
			case SET_RESULT:
				Integer resultCode = (Integer) msg.obj;
				setFaceResult(resultCode, Bulider.isResultPage);
				hideCycleImg();
				break;

			case UPDATE_STEP_PROCRESS:

				Integer progress = (Integer) msg.obj;
				mPb_step.setProgress(progress);
				break;

			case DETECT_FACE:
				cloudwalkFaceSDK.startDetectFace();
				break;
			case UPDATESTEPLAYOUT:
				updateStepLayout(execLiveList.get(currentStep - 1));
			case START_SNAPSHOT:// 拍照

				break;

			case START_RECOGNIZE_HEAD_L:

				cloudwalkFaceSDK.startHeadRotateDetect(1, 15,
						Bulider.timerCount);

				break;
			case START_RECOGNIZE_HEAD_R:

				cloudwalkFaceSDK.startHeadRotateDetect(2, 15,
						Bulider.timerCount);

				break;
			case START_RECOGNIZE_HEAD_D:

				cloudwalkFaceSDK.startHeadRotateDetect(4, 15,
						Bulider.timerCount);

				break;
			case START_RECOGNIZE_HEAD_U:

				cloudwalkFaceSDK.startHeadRotateDetect(3, 15,
						Bulider.timerCount);

				break;
			case START_RECOGNIZE_EYE:

				cloudwalkFaceSDK.startEyeDetect(Bulider.timerCount);

				break;

			case START_RECOGNIZE_MOUTHOPEN:// 張嘴检测
				// 张嘴超时默认3秒,建议设置范围在3-5秒

				cloudwalkFaceSDK.startOpenMouthDetect(Bulider.timerCount);

				break;

			case START_FACEVERIFY:
				mMainHandler.sendEmptyMessage(NEXT_STEP);
				break;

			// case UPDATE_FACE_RECT:
			// Face[] faces = (Face[]) msg.obj;
			// surfaceView.setFaces(faces);
			// break;

			case UPDATE_MESSAGE:
				String strMsg = (String) msg.obj;

				// Toast.makeText(getApplicationContext(), strMsg,
				// Toast.LENGTH_SHORT).show();
				break;

			// case FACE_CLEAR:
			// surfaceView.clearFaces();
			// break;

			// case VERIFYFACE_FAILED:
			//
			// break;

			case NEXT_STEP:
				isPlayNextStep = false;
				if (!isStop) {
					mMainHandler.obtainMessage(BEST_FACE).sendToTarget();
					executeStep();
				}

				break;

			}

			super.handleMessage(msg);
		}
	}

	private void updateStepLayout(int i) {

		View view = viewList.get(currentStep);
		mPb_step = (RoundProgressBarWidthNumber) view
				.findViewById(R.id.cloudwalk_face_step_procress);

		mTv_step = (TextView) view.findViewById(R.id.cloudwalk_face_step_tv);
		mIv_step = (ImageView) view.findViewById(R.id.cloudwalk_face_step_img);
		mPb_step.setVisibility(View.VISIBLE);
		mPb_step.setMax(Bulider.timerCount);
		mPb_step.setProgress(Bulider.timerCount);
		// 离散活体随机打乱 1:l 2:r 3: u3:nod 5: eye 6:mouth
		switch (i) {
		case 1:// l

			mIv_step.setImageResource(R.drawable.cloudwalk_left_anim);// 0x7f040003
			mTv_step.setText("向左转头");
			animationDrawable = (AnimationDrawable) mIv_step.getDrawable();
			animationDrawable.start();

			break;
		case 2:// r

			mIv_step.setImageResource(R.drawable.cloudwalk_right_anim);// 0x7f040004
			mTv_step.setText("向右转头");
			animationDrawable = (AnimationDrawable) mIv_step.getDrawable();
			animationDrawable.start();

			break;
		case 6:// 张嘴
			mIv_step.setImageResource(R.drawable.cloudwalk_face_mouth);// 0x7f02000b
			mTv_step.setText("张嘴");

			break;
		case 3:// u
			mIv_step.setImageResource(R.drawable.cloudwalk_up_anim);// 0x7f040005
			mTv_step.setText("抬头");

			animationDrawable = (AnimationDrawable) mIv_step.getDrawable();
			animationDrawable.start();

			break;
		case 5:// 眨眼
			mIv_step.setImageResource(R.drawable.cloudwalk_eye_anim);// 0x7f040002
			mTv_step.setText("眨眼");

			animationDrawable = (AnimationDrawable) mIv_step.getDrawable();
			animationDrawable.start();

			break;
		case 4:// d
			mIv_step.setImageResource(R.drawable.cloudwalk_down_anim);// 0x7f040001
			mTv_step.setText("点头");

			animationDrawable = (AnimationDrawable) mIv_step.getDrawable();
			animationDrawable.start();

			break;
		}

		mViewPager.setCurrentItem(currentStep, true);
	}

	@Override
	public void OnVerifyTwoImgResult(int ret, double score, String sessionID) {
	}

	boolean isFirstDetectFace;

	@Override
	public void OnDetectFaceInfo(int left, int top, int width, int height,
			float facePitch, float faceYaw, float faceRoll, float dMouthOpened,
			float dEyeClosed, float dWearGlasses, float dSmile,
			float faceQuality) {

		if (width > 0) {
			if (!isFirstDetectFace)
				isFirstDetectFace = true;

			facePitchDegree = facePitch;
			// 检测到脸, 画脸框
			// int[] faces = new int[4];
			// faces[0] = left;
			// faces[1] = top;
			// faces[2] = width;
			// faces[3] = height;
			// new SetFaceView(this).UpdateUI(faces); // 更新脸框
			// if (unVivo) {// 检测到人脸,进入活体验证过程
			// mMainHandler.obtainMessage(START_FACEVERIFY)
			// .sendToTarget();
			// isFristDetectFace = true;
			// unVivo = false;
			// // 启动人脸录像
			// if (Config.recordSetting) {
			// String path = new StringBuilder(Environment
			// .getExternalStorageDirectory().getAbsolutePath())
			// .append(File.separator)
			// .append("cloudwalk_record.mp4").toString();
			// cloudwalkFaceSDK.recordStart(path);
			// }
			// }
		} else {
			facePitchDegree = 0;
			// mMainHandler.obtainMessage(FACE_CLEAR).sendToTarget();//
			// 未检测到脸,清除脸框
		}

	}

	boolean isActionNotStandard = false;

	@Override
	public void OnActionNotStandard(int type) {
		if (!isActionNotStandard && !isPlayNextStep && totalStep != currentStep) {
			mMainHandler.obtainMessage(SET_RESULT, type).sendToTarget();
			isActionNotStandard = true;
		}
		// tip_msg = "动作不符合规范!";

	}

	int daojsCount = 0;

	private void startTimerRunnable(final int type) {
		final int tempType = type;
		daojsCount = 0;
		faceTimerRunnable = new Runnable() {

			@Override
			public void run() {
				if (tempType == 1) {// 进度倒数

					if (Math.abs(facePitchDegree) < 15
							&& Math.abs(facePitchDegree) > 1) {// 度数在-15至15

						AnimationDrawable animationDrawable = (AnimationDrawable) mIv_step1Face
								.getDrawable();
						animationDrawable.stop();

						hideCycleImg();

						mMainHandler.removeCallbacks(faceTimerRunnable);
						doNextStep();
					} else {

						if (daojsCount >= 16) {// 3次语音人脸退出
							mMainHandler
									.obtainMessage(
											SET_RESULT,
											MyFaceRecognizeResultActivity.FACEDECTFAIL_NOFACE)
									.sendToTarget();
						} else if (daojsCount % 6 == 0) {

							if (Bulider.isSound) {
								currentStreamID = poolMap.get("main");
								sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0,
										1.0f);
							}
						}
						mMainHandler.postDelayed(faceTimerRunnable, 1000);
						daojsCount++;
					}
				}
				if (tempType == 2) {// 2-转头
					// 更新数据

					mMainHandler.obtainMessage(UPDATE_STEP_PROCRESS,
							faceMouthTimerCount).sendToTarget();
					faceMouthTimerCount--;

					if (faceMouthTimerCount >= 0) {
						mMainHandler.postDelayed(faceTimerRunnable, 1000);
					} else {
						mMainHandler.removeCallbacks(faceTimerRunnable);

						is_living = false;
						// tip_msg = "转头超时!";
						if (isFirstDetectFace) {
							mMainHandler
									.obtainMessage(
											SET_RESULT,
											MyFaceRecognizeResultActivity.FACEDECTFAIL_TIMEOUT)
									.sendToTarget();
						} else {
							mMainHandler
									.obtainMessage(
											SET_RESULT,
											MyFaceRecognizeResultActivity.FACEDECTFAIL_NOFACE)
									.sendToTarget();
						}

						// 活体失败
					}
				}
				if (tempType == 3) { // 3-张嘴
					// 更新数据
					mMainHandler.obtainMessage(UPDATE_STEP_PROCRESS,
							faceMouthTimerCount).sendToTarget();
					faceMouthTimerCount--;

					if (faceMouthTimerCount >= 0) {
						mMainHandler.postDelayed(faceTimerRunnable, 1000);
					} else {
						mMainHandler.removeCallbacks(faceTimerRunnable);

						is_living = false;
						// tip_msg = "张嘴超时!";
						if (isFirstDetectFace) {
							mMainHandler
									.obtainMessage(
											SET_RESULT,
											MyFaceRecognizeResultActivity.FACEDECTFAIL_TIMEOUT)
									.sendToTarget();
						} else {
							mMainHandler
									.obtainMessage(
											SET_RESULT,
											MyFaceRecognizeResultActivity.FACEDECTFAIL_NOFACE)
									.sendToTarget();
						}

						// 活体失败
					}
				}

				if (tempType == 4) {
					mMainHandler.obtainMessage(UPDATE_STEP_PROCRESS,
							faceMouthTimerCount).sendToTarget();
					faceMouthTimerCount--;

					if (faceMouthTimerCount >= 0) {
						mMainHandler.postDelayed(faceTimerRunnable, 1000);
					} else {
						mMainHandler.removeCallbacks(faceTimerRunnable);

						is_living = false;
						// ctip_msg = "眨眼超时!";
						if (isFirstDetectFace) {
							mMainHandler
									.obtainMessage(
											SET_RESULT,
											MyFaceRecognizeResultActivity.FACEDECTFAIL_TIMEOUT)
									.sendToTarget();
						} else {
							mMainHandler
									.obtainMessage(
											SET_RESULT,
											MyFaceRecognizeResultActivity.FACEDECTFAIL_NOFACE)
									.sendToTarget();
						}

						// 活体失败
					}
				}

			}
		};
		if (type == 1) {
			mMainHandler.postDelayed(faceTimerRunnable, 1000);
		}
		if (type == 2 || type == 3 || type == 4) {
			mMainHandler.postDelayed(faceTimerRunnable, 1000);
			faceMouthTimerCount = Bulider.timerCount;

		}
	}

	private void hideCycleImg() {
		mIv_cycle.clearAnimation();
		mIv_cycle.setVisibility(View.GONE);
	}

	/**
	 * 随机指定范围内N个不重复的数 最简单最基本的方法
	 * 
	 * @param min
	 *            指定范围最小值
	 * @param max
	 *            指定范围最大值
	 * @param n
	 *            随机数个数
	 */
	public static int[] randomCommon(int min, int max, int n) {
		if (n > (max - min + 1) || max < min) {
			return null;
		}
		int[] result = new int[n];
		int count = 0;
		while (count < n) {
			int num = (int) (Math.random() * (max - min)) + min;
			if (num < 5 && count == 0) {

			} else {
				boolean flag = true;
				for (int j = 0; j < n; j++) {
					if (num == result[j]) {
						flag = false;
						break;
					}
				}
				if (flag) {
					result[count] = num;
					count++;
				}
			}

		}
		return result;
	}

	private class ViewPagerAdapter extends PagerAdapter {
		private List<View> mListViews;

		public ViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mListViews.get(position), 0);
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

}