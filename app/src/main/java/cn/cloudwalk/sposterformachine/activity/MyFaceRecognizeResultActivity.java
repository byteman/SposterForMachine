package cn.cloudwalk.sposterformachine.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;


import cn.cloudwalk.sposterformachine.R;
import cn.cloudwalk.sposterformachine.view.RoundProgressBar;
import cn.cloudwalk.util.NullUtils;

/**
 * 结果提示页面
 * 
 * @author ysyhpc
 * 
 */
public class MyFaceRecognizeResultActivity extends Activity {
	public static final int FACEDECT_FAIL = 1, FACEDECTFAIL_TIMEOUT = 3,
			FACEDEC_OK = 4, FACE_VERFY_OK = 5, FACE_VERFY_FAIL = 6,
			NET_FAIL = 7, FACEDECTFAIL_NOFACE = 8;
	public static final int FACEDECTFAIL_MISSFACE = 9001;
	public static final int FACEDECTFAIL_SHAKE = 9002;
	public static final int FACEDECTFAIL_FACKFACE = 9003;
	public static final String FACEDECT_RESULT_TYPE = "facedect_result_type";
	public static final String FACEDECT_RESULT_MSG = "facedect_result_msg";
	public static final int FACEDECTFAIL_APPID = 9;

	Button face_result_cancel, face_result_ok;
	TextView face_result_tv;
	int type;
	SoundPool sndPool;
	int currentStreamID;
	private Map<String, Integer> poolMap;
	TextView face_result_ti;

	RoundProgressBar mCircle_pb;
	private int progress = 0;
	ImageView face_result_iv;
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0, 1.0f);
			}
			if (msg.what == 1) {
				mCircle_pb.setProgress(progress);
				if (Math.abs(progress) <= mCircle_pb.getMax()) {
					progress=progress-2;
					mHandler.sendEmptyMessageDelayed(1, 1);
				}
			}

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_facedect_fail);
		type = getIntent().getIntExtra(FACEDECT_RESULT_TYPE, FACEDECT_FAIL);

		face_result_cancel = (Button) findViewById(R.id.face_result_cancel);
		face_result_ok = (Button) findViewById(R.id.face_result_ok);
		face_result_iv = (ImageView) findViewById(R.id.face_result_iv);
		face_result_ti = (TextView) findViewById(R.id.face_result_ti);
		mCircle_pb = (RoundProgressBar) findViewById(R.id.circle_pb);
		mCircle_pb.setMax(100);
		progress--;
		mHandler.sendEmptyMessageDelayed(1, 1000);
		face_result_tv = (TextView) findViewById(R.id.face_result_tv);
         if(type==FACE_VERFY_OK||type==FACEDEC_OK){
        	 mCircle_pb.setArcColor(getResources().getColor(R.color.face_result_ok));
         }else{
        	 mCircle_pb.setArcColor(getResources().getColor(R.color.face_result_fail));
         }
		if (type == FACEDECT_FAIL) {
			face_result_tv.setText(R.string.facedect_fail_tip);
		}
		if (type == FACEDECTFAIL_MISSFACE) {

			face_result_tv.setText(R.string.facedectfail_missface);
		}
		if (type == FACEDECTFAIL_SHAKE) {
			face_result_tv.setText(R.string.facedectfail_shake);
		}
		if (type == FACEDECTFAIL_TIMEOUT) {
			face_result_tv.setText(R.string.facedectfail_timeout);

		}
		if (type == FACEDECTFAIL_NOFACE) {
			face_result_tv.setText(R.string.facedectfail_noface);

		}
		if (type == FACEDEC_OK) {
			face_result_tv.setText(R.string.facedect_ok_tip);
			face_result_iv.setImageResource(R.drawable.cloudwalk_face_result_success);
			face_result_cancel.setVisibility(View.INVISIBLE);
			face_result_ti.setVisibility(View.INVISIBLE);
			face_result_ok.setText("确定");
		}
		if (type == NET_FAIL) {
			face_result_tv.setText(R.string.facedec_net_fail);
		}
		if (type == FACEDECTFAIL_APPID) {

			face_result_tv.setText(R.string.facedectfail_appid);

			face_result_cancel.setVisibility(View.INVISIBLE);
			face_result_ti.setVisibility(View.INVISIBLE);
			face_result_ok.setText("确定");
		}
		if (type == FACE_VERFY_OK) {
			face_result_tv.setText(R.string.face_verfy_ok_tip);

			face_result_iv.setImageResource(R.drawable.cloudwalk_face_result_success);
			face_result_cancel.setVisibility(View.INVISIBLE);
			face_result_ti.setVisibility(View.INVISIBLE);
			face_result_ok.setText("确定");
		}

		if (type == FACE_VERFY_FAIL) {
			face_result_tv.setText(R.string.face_verfy_fail_tip);

		}
		/**
		 * 取消按钮点击事件
		 */
		face_result_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult();
			}

		});
		face_result_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {


				if (type == FACE_VERFY_OK || type == FACEDEC_OK
						|| type == FACEDECTFAIL_APPID) {
					setResult();
				} else {

					Intent mIntent = new Intent();
					setResult(32, mIntent);
					finish();

				}

			}
		});

		initSoundPool();
		String msg = getIntent().getStringExtra(FACEDECT_RESULT_MSG);
		if (NullUtils.isNotEmpty(msg)) {
			face_result_tv.setText(msg);
			return;
		}
		// 播放欢迎语

		if (type == FACEDECT_FAIL) {
			if (sndPool != null) {
				currentStreamID = poolMap.get("failed");

			}
		}
		if (type == FACEDECTFAIL_MISSFACE || type == FACEDECTFAIL_SHAKE
				|| type == FACEDECTFAIL_FACKFACE) {
			if (sndPool != null) {
				currentStreamID = poolMap.get("failed_actionblend");

			}
		}
		if (type == FACEDECTFAIL_TIMEOUT) {
			if (sndPool != null) {
				currentStreamID = poolMap.get("failed_timeout");

			}
		}
		if (type == FACEDECTFAIL_NOFACE) {
			if (sndPool != null) {
				currentStreamID = poolMap.get("failed_noface");

			}
		}
		if (type == FACEDEC_OK) {
			if (sndPool != null) {
				currentStreamID = poolMap.get("success");

			}
		}
		if (type == NET_FAIL) {
			if (sndPool != null) {
				currentStreamID = poolMap.get("net_fail");

			}
		}
		if (type == FACE_VERFY_OK) {
			if (sndPool != null) {
				currentStreamID = poolMap.get("verfy_suc");

			}
		}
		if (type == FACE_VERFY_FAIL) {
			if (sndPool != null) {
				currentStreamID = poolMap.get("verfy_fail");

			}
		}
		if (type == FACEDECT_FAIL) {
			if (sndPool != null) {
				currentStreamID = poolMap.get("failed");

			}
		}
		int playId = sndPool.play(currentStreamID, 1.0f, 1.0f, 0, 0, 1.0f);
		if (playId == 0)
			mHandler.sendEmptyMessageDelayed(0, 1000);
	}

	protected void setResult() {
		Intent it = new Intent();
		setResult(31, it);
		finish();

	}

	private void initSoundPool() {
		poolMap = new HashMap<String, Integer>();
		sndPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
		poolMap.put("success", sndPool.load(this, R.raw.cloudwalk_success, 1));
		poolMap.put("failed", sndPool.load(this, R.raw.cloudwalk_failed, 1));
		poolMap.put("failed_actionblend", sndPool.load(this, R.raw.cloudwalk_failed_actionblend, 1));
		poolMap.put("failed_timeout", sndPool.load(this, R.raw.cloudwalk_failed_timeout, 1));
		poolMap.put("net_fail", sndPool.load(this, R.raw.cloudwalk_net_fail, 1));
		poolMap.put("verfy_fail", sndPool.load(this, R.raw.cloudwalk_verfy_fail, 1));
		poolMap.put("verfy_suc", sndPool.load(this, R.raw.cloudwalk_verfy_suc, 1));
		poolMap.put("failed_noface", sndPool.load(this, R.raw.cloudwalk_failed_noface, 1));

	}

	@Override
	protected void onStop() {

		sndPool.stop(currentStreamID);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		sndPool.release();
		super.onDestroy();
	}
}