package cn.cloudwalk.sposterformachine.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.cloudwalk.activity.FaceRecognizeResultActivity;
import cn.cloudwalk.callback.DefineRecognizeCallBack;
import cn.cloudwalk.callback.ResultCallBack;
import cn.cloudwalk.sposterformachine.Bulider;
import cn.cloudwalk.sposterformachine.PhoneApp;
import cn.cloudwalk.sposterformachine.R;
import cn.cloudwalk.sposterformachine.util.GetData;
import cn.cloudwalk.util.FileUtil;

public class SendLoginNumActivity extends AppCompatActivity implements View.OnClickListener,ResultCallBack {
    Button btn_phone_save;
    EditText edit_phone_num;
    String phoneNum;
    String bestface;
    boolean result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_login_num);
        //设置返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initData();
        btn_phone_save.setOnClickListener((View.OnClickListener) this);
    }

    private void initData() {
        btn_phone_save = (Button) findViewById(R.id.phone_save);
        edit_phone_num = (EditText) findViewById(R.id.phone_reg);
    }

    @Override
    public void onClick(View v) {
        phoneNum = edit_phone_num.getText().toString();
        switch (v.getId()){
            case R.id.phone_save:
                if(!phoneNum.equals("")) {
                    ArrayList<Integer> liveList = new ArrayList<Integer>();
                    liveList.add(Bulider.HEAD_L);
                    liveList.add(Bulider.HEAD_U);
                    liveList.add(Bulider.HEAD_D);
                    liveList.add(Bulider.EYE);
                    liveList.add(Bulider.MOUTH);
                    new Bulider().initFaceVerify("120.25.253.47:7000", PhoneApp.scoreThreshold, Bulider.FACE_VERIFY_DEFINE, "", new DefineRecognizeCallBack() {
                        @Override
                        public void OnDefineFaceVerifyResult(String best_idface, final String best_face) {

                            //在这里面做网络请求
                            new AsyncTask<String,Void,Boolean>(){
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    try {
                                        bestface =  Base64.encodeToString(FileUtil.file2byte(new File(best_face)),Base64.DEFAULT);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                protected Boolean doInBackground(String... params) {
                                    HashMap<String,String> map = new HashMap<String, String>();
                                    map.put("phone",phoneNum);
                                    map.put("img",bestface);
                                    try {
                                        JSONObject obj = GetData.post("http://"+PhoneApp.ATTENDANCEADDR+"/easySpeedExpressage_manage/regist/registUser",map);
                                        int status = obj.getInt("status");
                                        if(status==0){
                                            result = true;
                                        }else{
                                            result = false;
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return result;
                                }

                                @Override
                                protected void onPostExecute(Boolean result) {
                                    super.onPostExecute(result);
                                    PushActivity.getFaceRecognize().setFaceResult(result,0,"","");
                                }
                            }.execute();
                        }
                    })
                            .initAuth("120.24.63.174:8792", "appid", "appSecret")
                            .isResultPage(true)
                            .setLives(liveList, 3, true, Bulider.LIVE_LEVEL_STANDARD)
                            .setResultCallBack(this)
                            .isRegistFace(false)
                            .startActivity(this, PushActivity.class);
                    edit_phone_num.setText("");
                }else{
                    Toast.makeText(this,"请输入手机号码",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void result(boolean isLivePass, boolean isVerfyPass,
                       String faceSessionId, String best_face, String best_idface,
                       double face_score, int resultType) {
        String tip_msg = null;
        switch (resultType) {
            case FaceRecognizeResultActivity.FACEDECT_FAIL:
                tip_msg = "检测失败,请按照提示完成相应的动作!";
                break;

            case FaceRecognizeResultActivity.FACEDECTFAIL_MISSFACE:
                tip_msg = "检查失败,请保持人脸在取景框中!";
                break;
            case FaceRecognizeResultActivity.FACEDECTFAIL_SHAKE:
                tip_msg = "检查失败,请不要晃动!";
                break;
            case FaceRecognizeResultActivity.FACEDECTFAIL_FACKFACE:
                tip_msg = "检查失败,您的动作不符合规范!";
                break;
            case FaceRecognizeResultActivity.FACEDECTFAIL_TIMEOUT:
                tip_msg = "检测超时,请按照提示完成相应的动作!";
                break;
            case FaceRecognizeResultActivity.FACEDEC_OK:
                tip_msg = "检测成功,感谢您的配合!";
                break;
            case FaceRecognizeResultActivity.FACE_VERFY_OK:
                tip_msg = "验证成功,感谢您的配合!";
                break;
            case FaceRecognizeResultActivity.FACEDECTFAIL_NOFACE:
                tip_msg = "抱歉,您未按图示将人脸放入取景框中,请稍后再试!";
                break;
            case FaceRecognizeResultActivity.FACE_VERFY_FAIL:
                tip_msg = "抱歉,人脸验证失败";
                break;
            case FaceRecognizeResultActivity.NET_FAIL:
                tip_msg = "网络连接失败,请检查网络后再试.";
                break;
            case FaceRecognizeResultActivity.FACEDECTFAIL_APPID:
                tip_msg = "appid/appSecret认证不成功";
                break;
        }
        Toast.makeText(
                this,
                "提示：" + tip_msg + ";活体" + (isLivePass ? "通过" : "失败") + ";人证合一"
                        + (isVerfyPass ? "通过" : "失败") + "分数:" + face_score,
                Toast.LENGTH_LONG).show();
    }
}
