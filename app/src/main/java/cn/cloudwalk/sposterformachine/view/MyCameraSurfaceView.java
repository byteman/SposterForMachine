package cn.cloudwalk.sposterformachine.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import cn.cloudwalk.CloudwalkFaceSDK;
import cn.cloudwalk.camera.CameraInterface;
import cn.cloudwalk.camera.CameraInterface.CamOpenOverCallback;
import cn.cloudwalk.util.CameraUtil;
import cn.cloudwalk.util.ImageUtil;
import cn.cloudwalk.util.LogUtils;
import cn.cloudwalk.util.TimeUtil;

public class MyCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = LogUtils.makeLogTag("CameraSurfaceView");
	
	SurfaceHolder mSurfaceHolder;

	private boolean bCapturing = false;
	CloudwalkFaceSDK cloudwalkFaceSDK;

	private Activity act;

	private int rotation;

	private int mCameraId;
	public MyCameraSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		act=(Activity) context;
		 rotation = act.getWindowManager ().getDefaultDisplay ().getRotation ();
		mSurfaceHolder = getHolder();
		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceHolder.addCallback(this);
		bCapturing = false;
		cloudwalkFaceSDK= CloudwalkFaceSDK.getInstance(context);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		if (!bCapturing) {
			mCameraId=CameraInfo.CAMERA_FACING_BACK;
			doOpenCamera(null, mCameraId);
			bCapturing = true;
			isFront = true;
		}
		

	}

	

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		if (bCapturing) {
			doStopPreview();
			doStopCamera();
			bCapturing = false;
		}
	
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		doStartPreview(mSurfaceHolder, true);
		CameraInterface.fixH = 0;
	}

	Thread videoThread;

	private byte[] dest = new byte[CameraInterface.camera_preview_w * CameraInterface.camera_preview_h * 3 / 2];

	Camera mCamera;
	private boolean isPreviewing = false;
	private Camera.Parameters mParams;
	int camera_picture_w = 640;
	int camera_picture_h = 480;
	int camera_preview_afterR_h = 640;
	int camera_preview_afterR_w = 480;

	private byte[] frame;
	private Object lockPreview = new Object(); // 视频预览中, 抓图与处理间的同步对象

	boolean bDetecting = false;
	boolean isFront = false;

	// 打开Camera
	private Camera doOpenCamera(CamOpenOverCallback callback, int cameraId) {
		try {
			if (this.mCamera != null) {
				this.mCamera.setPreviewCallback(null);
				this.mCamera.stopPreview();
				this.mCamera.release();
			}
			mCamera = Camera.open(cameraId);
			if (callback != null) {
				callback.cameraHasOpened();
			}

			if (mCamera != null) {
				mCamera.setPreviewCallback(new Camera.PreviewCallback() {

					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						if (videoThread != null) {
							synchronized (lockPreview) {
								frame = data;
								lockPreview.notify();
							}

						}

					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mCamera;

	}

	// 视频录制线程
	class VideoRecordRunnable implements Runnable {
		@Override
		public void run() {

			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);
			while (bDetecting) {
				synchronized (lockPreview) {
					try {
						lockPreview.wait();

					} catch (InterruptedException e) {

						e.printStackTrace();
					}
					processVideoFrame(frame);
				}

			}
			videoThread.interrupt();
			videoThread = null;
		}
	}

	/**
	 * 处理每一帧图像
	 * 
	 * @param yuv_data
	 */
	protected void processVideoFrame(byte[] yuv_data) {
		if (yuv_data == null)
			return;

		TimeUtil.timeSpanStart();
		// dest=长X宽X3/2
		if (yuv_data.length < dest.length) {
			LogUtils.LOGI(TAG, "processFrame()::yuv_data.length =" + yuv_data.length + "dest.length=" + dest.length);
			return;
		}
		if (isFront) {// 前置
			// 竖屏 水平镜像+旋转90
			ImageUtil.rotateYUV420SP_90R_Mirror(dest, yuv_data, CameraInterface.camera_preview_w,
					CameraInterface.camera_preview_h);
		} else {// 后置
			// 竖屏 旋转90 Time of Rotate: ~15ms
			ImageUtil.nv21Rotate(yuv_data, CameraInterface.camera_preview_w, CameraInterface.camera_preview_h, dest,
					90);
		}
		if (bDetecting) {
			// 竖屏 旋转90xy对调
			// 人脸检测, Time of PushFrame: ~1ms
			cloudwalkFaceSDK.pushFrame(0, dest, dest.length, camera_preview_afterR_w,
					camera_preview_afterR_h, 0);
			// 确保每秒push帧数10左右
			long time = TimeUtil.timeSpanEnd();
			if (time < 100) {
				try {
					Thread.sleep(100 - time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

	// 开启预览
	public void doStartPreview(SurfaceHolder surfaceHolder, Boolean isSetPreview) {
		if (isPreviewing) {
			mCamera.stopPreview();
			return;
		}
		if (mCamera != null) {
			mParams = mCamera.getParameters();
			// 这个API修改的仅仅是Camera的预览方向而已，并不会影响到PreviewCallback回调、生成的JPEG图片和录像文件的方向，这些数据的方向依然会跟图像Sensor的方向一致
			CameraUtil.setCameraDisplayOrientation(rotation, mCameraId, mCamera);

			List<String> focusModes = mParams.getSupportedFocusModes();
			if (focusModes.contains("continuous-video")) {
				mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			}
			// 设置照片预览分辨率
			if (isSetPreview) {
				mParams.setPreviewSize(camera_picture_w, camera_picture_h);

			}
			// 设置图片分辨率

			mParams.setPictureSize(camera_picture_w, camera_picture_h);
			// 设置视频数据格式
			mParams.setPreviewFormat(ImageFormat.NV21);

			mCamera.setParameters(mParams);

			try {
				mCamera.setPreviewDisplay(surfaceHolder);
				mCamera.startPreview();
			} catch (IOException e) {
				e.printStackTrace();
			}

			isPreviewing = true;

		}
	}

	// 停止预览，释放Camera
	public void doStopCamera() {
		if (null != mCamera) {
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	public void doStopPreview() {

		if (this.mCamera != null && this.isPreviewing) {
			this.mCamera.stopPreview();
			this.isPreviewing = false;
		}
	}
	
	public void startDetectFace() {

		bDetecting = true;
		if (null == videoThread) {
			LogUtils.LOGI(TAG, "null == videoThread");
			videoThread = new Thread(new VideoRecordRunnable());
			videoThread.start();
		} else {
			LogUtils.LOGI(TAG, "null != videoThread");

		}

	}

	public void stopDetectFace() {

		if ((null != videoThread) && !bDetecting) {
			try {
				synchronized (lockPreview) {
					lockPreview.notify();
				}
				videoThread.join();
				videoThread = null;
				LogUtils.LOGI("video", "videoThread null");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
