package com.bill.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.bill.download.download.DownLoadObserver;
import com.bill.download.download.DownloadManager;
import com.bill.download.model.DownloadInfo;
import com.bill.download.utils.ToastUtils;
import com.bill.download.view.HorizontalProgressBarWithNumber;

import java.io.File;

import static com.bill.download.download.DownloadManager.apkPath;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button downloadBtn1, downloadBtn2, downloadBtn3;
    private Button cancelBtn1, cancelBtn2, cancelBtn3;
    private Button pauseBtn3;
    private HorizontalProgressBarWithNumber progress1, progress2, progress3;
    private DownloadInfo info3;

    private String url1 = "http://192.168.21.119:8080/haha.txt";
    private String url2 = "http://192.168.21.119:8080/18.avi.zip";
    private String url3 = "http://192.168.21.119:8080/qne.apk";

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MyApplication.getInstance();

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        downloadBtn1 = bindView(R.id.main_btn_down1);
        downloadBtn2 = bindView(R.id.main_btn_down2);
        downloadBtn3 = bindView(R.id.main_btn_down3);

        cancelBtn1 = bindView(R.id.main_btn_cancel1);
        cancelBtn2 = bindView(R.id.main_btn_cancel2);
        cancelBtn3 = bindView(R.id.main_btn_cancel3);

        pauseBtn3 = bindView(R.id.main_btn_pause3);

        progress1 = bindView(R.id.main_progress1);
        progress2 = bindView(R.id.main_progress2);
        progress3 = bindView(R.id.main_progress3);

        downloadBtn1.setOnClickListener(this);
        downloadBtn2.setOnClickListener(this);
        downloadBtn3.setOnClickListener(this);

        cancelBtn1.setOnClickListener(this);
        cancelBtn2.setOnClickListener(this);
        cancelBtn3.setOnClickListener(this);

        pauseBtn3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.main_btn_down1:
                DownloadManager.getInstance().download(url1, new DownLoadObserver() {
                    @Override
                    public void onNext(DownloadInfo value) {
                        super.onNext(value);

                        // 设置进度
                        float v1 = value.getProgress() * 100 / value.getTotal();
                        progress1.setProgress((int) ++v1);
                    }

                    @Override
                    public void onComplete() {

                        if (downloadInfo != null) {
                            ToastUtils.showShort(mContext, downloadInfo.getFileName() + "下载完成");
                        }
                    }

                });
                break;

            case R.id.main_btn_down2:
                DownloadManager.getInstance().download(url2, new DownLoadObserver() {
                    @Override
                    public void onNext(DownloadInfo value) {
                        super.onNext(value);

                        // 设置进度
                        float v1 = value.getProgress() * 100 / value.getTotal();
                        progress2.setProgress((int) ++v1);
                    }

                    @Override
                    public void onComplete() {

                        if (downloadInfo != null) {
                            ToastUtils.showShort(mContext, downloadInfo.getFileName() + Uri.encode("下载完成"));
                        }
                    }
                });
                break;

            case R.id.main_btn_down3:
                DownloadManager.getInstance().download(url3, new DownLoadObserver() {
                    @Override
                    public void onNext(DownloadInfo value) {
                        super.onNext(value);

                        info3 = value;

                        // 设置进度
                        float v1 = value.getProgress() * 100 / value.getTotal();
                        progress3.setProgress((int) ++v1);
                    }

                    @Override
                    public void onComplete() {

                        if (downloadInfo != null) {
                            ToastUtils.showShort(mContext, downloadInfo.getFileName() + "下载完成");

                            // 调出安装界面
                            installAPK(downloadInfo);
                        }
                    }

                });
                break;

            // 取消1
            case R.id.main_btn_cancel1:
                DownloadManager.getInstance().cancel(url1);
                break;

            // 取消2
            case R.id.main_btn_cancel2:
                DownloadManager.getInstance().cancel(url2);
                break;

            // 取消3
            case R.id.main_btn_cancel3:
                DownloadManager.getInstance().cancel(url3);
                progress3.setProgress(0);
                // 删除本地文件
                DownloadManager.getInstance().deleteFile(info3);
                break;

            // 暂停3
            case R.id.main_btn_pause3:
                DownloadManager.getInstance().cancel(url3);
                break;
        }
    }

    /**
     * 获取控件
     *
     * @param id
     * @param <T>
     * @return
     */
    private <T extends View> T bindView(@IdRes int id) {
        View viewById = findViewById(id);
        return (T) viewById;
    }

    /**
     * 安装apk
     *
     * @param downloadInfo
     */
    private void installAPK(DownloadInfo downloadInfo) {
        Intent intent = new Intent();
        File file = new File(apkPath, downloadInfo.getFileName());

        // 判断是否是7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uriForFile = FileProvider.getUriForFile(mContext, "com.lanou3g.com.bill.downdemo.fileProvider", file); //在AndroidManifest中的android:authorities值

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件

            intent.setDataAndType(uriForFile, "application/vnd.android.package-archive");
            intent.setAction("android.intent.action.VIEW");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
        }

        mContext.startActivity(intent);
    }

}
