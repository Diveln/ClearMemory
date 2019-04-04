package clearmemory.bored.cheng.clearmemoryactivity;

import android.app.ActivityManager;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ClearMemoryActivity extends AppCompatActivity {



    public native String getStringFromNative();

    private static final String TAG = "ClearMemoryActivity";

    private TextView mTotalMemoryTextView;
    private TextView mAvailMemoryTextView;
    private Button mCleanButton;
    private TextView mCleanInfoTextView;
    private ActivityManager mActivityManager;
    private StringBuffer mCleanInfoStringBuffer;
    private long availMemory;
    private long totalMemory;
    private List<ActivityManager.RunningAppProcessInfo> mRunningAppProcessInfoList;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        MobileAds.initialize(ClearMemoryActivity.this, getString(R.string.banner_ad_unit_id));
        mAdView = (AdView) findViewById(R.id.adView);
        //mAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        //mAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }





    private void init() {

        mCleanInfoStringBuffer = new StringBuffer();
        mActivityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        mTotalMemoryTextView = (TextView) findViewById(R.id.totalMemoryTextView);
        mAvailMemoryTextView = (TextView) findViewById(R.id.availMemoryTextView);
        mCleanInfoTextView = (TextView) findViewById(R.id.cleanInfoTextView);
        mCleanButton = (Button) findViewById(R.id.cleanButton);

        totalMemory = getTotalMemory();
        availMemory = getAvailMemory();
        mTotalMemoryTextView.setText(totalMemory + "MB");
        mAvailMemoryTextView.setText(availMemory + "MB");

        mCleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityManager.RunningAppProcessInfo runningAppProcessInfo=null;
                mRunningAppProcessInfoList= mActivityManager.getRunningAppProcesses();
                //List<ActivityManager.RunningServiceInfo> serviceInfos = mActivityManager.getRunningServices(100);

                mCleanButton.setEnabled(false);
                new CountDownTimer(3000,1000){

                    @Override
                    public void onFinish() {
                        mCleanButton.setEnabled(true);
                    }

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                }.start();

                if (mRunningAppProcessInfoList != null) {
                    for (int i = 0; i < mRunningAppProcessInfoList.size(); ++i) {
                        runningAppProcessInfo= mRunningAppProcessInfoList.get(i);
                        // 一般数值大于RunningAppProcessInfo.IMPORTANCE_SERVICE
                        // 的进程即为长时间未使用进程或者空进程
                        // 一般数值大于RunningAppProcessInfo.IMPORTANCE_VISIBLE
                        // 的进程都是非可见进程,即在后台运行
                        if (runningAppProcessInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                            String[] pkgList = runningAppProcessInfo.pkgList;
                            for (int j = 0; j < pkgList.length; ++j) {
                                mActivityManager.killBackgroundProcesses(pkgList[j]);
                                mCleanInfoStringBuffer.append(pkgList[j] + " is killed...\n");
                                mCleanInfoTextView.setText(mCleanInfoStringBuffer.toString());
                            }
                        }

                    }
                }

                //再次获得剩余内存以计算清理值
                mCleanInfoStringBuffer.append(getText(R.string.clear_out).toString()+(getAvailMemory() - availMemory) + "MB");
                if(mCleanInfoTextView.getText()==""){
                    mCleanInfoTextView.setText(mCleanInfoStringBuffer.toString());
                }else{
                    mCleanInfoTextView.setText("");
                    mCleanInfoStringBuffer.delete(0,mCleanInfoStringBuffer.length());
                    mCleanInfoStringBuffer.append(getText(R.string.clear_out).toString()+(getAvailMemory() - availMemory) + "MB");
                    mCleanInfoTextView.setText(mCleanInfoStringBuffer.toString());
                }
                mAvailMemoryTextView.setText(getAvailMemory() + "MB");

            }

        });
    }

    private long getTotalMemory() {
        //系统的内存信息文件
        String filePath = "/proc/meminfo";
        String lineString;
        String[] stringArray;
        long totalMemory = 0;
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader,1024 * 8);
            //读取meminfo第一行,获取系统总内存大小
            lineString = bufferedReader.readLine();
            //按照空格拆分
            stringArray = lineString.split("\\s+");
            //获得系统总内存,单位KB
            totalMemory = Integer.valueOf(stringArray[1]).intValue();
            bufferedReader.close();
            System.out.println("------> lineString=" + lineString+ ",stringArray[0]=" + stringArray[0] +
                    ",stringArray[1]="+ stringArray[1] + ",stringArray[2]=" + stringArray[2]);
        } catch (IOException e) {
        }
        return totalMemory / 1024;
    }



    private long getAvailMemory() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem / (1024 * 1024);
    }





}
