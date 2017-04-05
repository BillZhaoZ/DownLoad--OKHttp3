# DownLoad--OKHttp3

使用OKHttp3进行下载和断点续传；（详细内容，请下载源码）

文件的存储和写入；

     // 写文件到本地
             try {
                 is = response.body().byteStream();
                 fileOutputStream = new FileOutputStream(file, true);
                 byte[] buffer = new byte[2048];//缓冲数组2kB

                 int len;
                 while ((len = is.read(buffer)) != -1) {
                     fileOutputStream.write(buffer, 0, len);
                     downloadLength += len;

                     downloadInfo.setProgress(downloadLength);
                     e.onNext(downloadInfo);
                 }

                 fileOutputStream.flush();
                 downCalls.remove(url);

             } finally {
                 //关闭IO流
                 IOUtil.closeAll(is, fileOutputStream);

             }

             e.onComplete();//完成

集成RXJava和RxAndroid进行线程切换；

      //RxJava和RxAndroid 用来做线程切换的
       compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
       compile 'io.reactivex.rxjava2:rxjava:2.0.1'

     // 下载
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

     ****************************************
     /**
      * 下载管理类
      */
     public class DownloadManager {

         private static final AtomicReference<DownloadManager> INSTANCE = new AtomicReference<>();
         private HashMap<String, Call> downCalls;//用来存放各个下载的请求
         private OkHttpClient mClient;//OKHttpClient;

         // 文件下载的保存路径
         public static final String apkPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

         /**
          * 获得一个单例类
          *
          * @return
          */
         public static DownloadManager getInstance() {
             for (; ; ) {
                 DownloadManager current = INSTANCE.get();

                 if (current != null) {
                     return current;
                 }
                 current = new DownloadManager();

                 if (INSTANCE.compareAndSet(null, current)) {
                     return current;
                 }
             }
         }

         private DownloadManager() {
             downCalls = new HashMap<>();
             mClient = new OkHttpClient.Builder().build();
         }

         /**
          * 开始下载
          *
          * @param url              下载请求的网址
          * @param downLoadObserver 用来回调的接口
          */
         public void download(String url, DownLoadObserver downLoadObserver) {
             Observable.just(url)
                     .filter(s -> !downCalls.containsKey(s))//call的map已经有了,就证明正在下载,则这次不下载
                     .flatMap(s -> Observable.just(createDownInfo(s)))
                     .map(this::getRealFileName)//检测本地文件夹,生成新的文件名
                     .flatMap(downloadInfo -> Observable.create(new DownloadSubscribe(downloadInfo, downCalls, mClient)))//下载
                     .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                     .subscribeOn(Schedulers.io())//在子线程执行
                     .subscribe(downLoadObserver);//添加观察者
         }

         /**
          * 取消
          *
          * @param url
          */
         public void cancel(String url) {
             Call call = downCalls.get(url);

             if (call != null) {
                 call.cancel();
             }

             downCalls.remove(url);
         }

         /**
          * 取消后  删除本地下载文件
          *
          * @param info
          */
         public void deleteFile(DownloadInfo info) {
             String fileName = info.getFileName();
             File file = new File(apkPath, fileName);

             if (file.exists()) {
                 file.delete();
             }
         }

         /**
          * 创建DownInfo
          *
          * @param url 请求网址
          * @return DownInfo
          */
         private DownloadInfo createDownInfo(String url) {
             DownloadInfo downloadInfo = new DownloadInfo(url);
             long contentLength = getContentLength(url);//获得文件大小
             downloadInfo.setTotal(contentLength);

             String fileName = url.substring(url.lastIndexOf("/"));
             downloadInfo.setFileName(fileName);
             return downloadInfo;
         }

         /**
          * 获取真实文件名
          *
          * @param downloadInfo
          * @return
          */
         private DownloadInfo getRealFileName(DownloadInfo downloadInfo) {

             String fileName = downloadInfo.getFileName();
             long downloadLength = 0, contentLength = downloadInfo.getTotal();

             File file = new File(apkPath, fileName);

             if (file.exists()) {
                 //找到了文件,代表已经下载过,则获取其长度
                 downloadLength = file.length();
             }

             //之前下载过,需要重新来一个文件
             int i = 1;
             while (downloadLength >= contentLength) {
                 int dotIndex = fileName.lastIndexOf(".");
                 String fileNameOther;

                 if (dotIndex == -1) {
                     fileNameOther = fileName + "(" + i + ")";
                 } else {
                     fileNameOther = fileName.substring(0, dotIndex)
                             + "(" + i + ")" + fileName.substring(dotIndex);
                 }

                 File newFile = new File(apkPath, fileNameOther);
                 file = newFile;

                 downloadLength = newFile.length();
                 i++;
             }

             //设置改变过的文件名/大小
             downloadInfo.setProgress(downloadLength);
             downloadInfo.setFileName(file.getName());
             return downloadInfo;
         }

         /**
          * 获取下载长度
          *
          * @param downloadUrl
          * @return
          */
         private long getContentLength(String downloadUrl) {
             Request request = new Request.Builder()
                     .url(downloadUrl)
                     .build();

             try {
                 Response response = mClient.newCall(request).execute();

                 if (response != null && response.isSuccessful()) {
                     long contentLength = response.body().contentLength();
                     response.close();
                     return contentLength == 0 ? DownloadInfo.TOTAL_ERROR : contentLength;
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }

             return DownloadInfo.TOTAL_ERROR;
         }

     }


开启java8,使用Lambada表达式简化代码；

    apply plugin: 'com.android.application'

    android {
        compileSdkVersion 24
        buildToolsVersion "24.0.3"

        defaultConfig {
            applicationId "com.bill.download"
            minSdkVersion 15
            targetSdkVersion 24
            versionCode 1
            versionName "1.0"
            testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"

            //为了开启Java8
            jackOptions {
                enabled true;
            }
        }

        buildTypes {
            release {
                minifyEnabled false
                proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            }
        }

        //开启Java1.8 能够使用lambda表达式
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }

    }

    dependencies {
        compile fileTree(dir: 'libs', include: ['*.jar'])

        androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
            exclude group: 'com.android.support', module: 'support-annotations'
        })

        compile 'com.android.support:appcompat-v7:24.1.1'
        testCompile 'junit:junit:4.12'

        //OKHttp
        compile 'com.squareup.okhttp3:okhttp:3.6.0'

        //RxJava和RxAndroid 用来做线程切换的
        compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
        compile 'io.reactivex.rxjava2:rxjava:2.0.1'
    }


下载完成的安装，增加7.0文件权限的判定；

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

自定义横向进度条；

    /**
     * 自定义横向进度条 （仿鸿洋的）
     * Created by Bill on 2017/3/14.
     */

    public class HorizontalProgressBarWithNumber extends ProgressBar {

        private static final int DEFAULT_TEXT_SIZE = 10;
        private static final int DEFAULT_TEXT_COLOR = 0XFFFC00D1;
        private static final int DEFAULT_COLOR_UNREACHED_COLOR = 0xFFd3d6da;
        private static final int DEFAULT_HEIGHT_REACHED_PROGRESS_BAR = 2;
        private static final int DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR = 2;
        private static final int DEFAULT_SIZE_TEXT_OFFSET = 10;

        /**
         * painter of all drawing things
         */
        protected Paint mPaint = new Paint();
        /**
         * color of progress number
         */
        protected int mTextColor = DEFAULT_TEXT_COLOR;
        /**
         * size of text (sp)
         */
        protected int mTextSize = sp2px(DEFAULT_TEXT_SIZE);

        /**
         * offset of draw progress
         */
        protected int mTextOffset = dp2px(DEFAULT_SIZE_TEXT_OFFSET);

        /**
         * height of reached progress bar
         */
        protected int mReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_REACHED_PROGRESS_BAR);

        /**
         * color of reached bar
         */
        protected int mReachedBarColor = DEFAULT_TEXT_COLOR;
        /**
         * color of unreached bar
         */
        protected int mUnReachedBarColor = DEFAULT_COLOR_UNREACHED_COLOR;
        /**
         * height of unreached progress bar
         */
        protected int mUnReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR);
        /**
         * view width except padding
         */
        protected int mRealWidth;

        protected boolean mIfDrawText = true;

        protected static final int VISIBLE = 0;

        public HorizontalProgressBarWithNumber(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public HorizontalProgressBarWithNumber(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);

            obtainStyledAttributes(attrs);

            mPaint.setTextSize(mTextSize);
            mPaint.setColor(mTextColor);
        }

        @Override
        protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = measureHeight(heightMeasureSpec);
            setMeasuredDimension(width, height);

            mRealWidth = getMeasuredWidth() - getPaddingRight() - getPaddingLeft();
        }

        /**
         * 测量高度
         *
         * @param measureSpec
         * @return
         */
        private int measureHeight(int measureSpec) {
            int result = 0;
            int specMode = MeasureSpec.getMode(measureSpec);
            int specSize = MeasureSpec.getSize(measureSpec);
            if (specMode == MeasureSpec.EXACTLY) {
                result = specSize;
            } else {
                float textHeight = (mPaint.descent() - mPaint.ascent());
                result = (int) (getPaddingTop() + getPaddingBottom() + Math.max(
                        Math.max(mReachedProgressBarHeight,
                                mUnReachedProgressBarHeight), Math.abs(textHeight)));
                if (specMode == MeasureSpec.AT_MOST) {
                    result = Math.min(result, specSize);
                }
            }
            return result;
        }

        /**
         * 获取自定义属性
         *
         * @param attrs
         */
        private void obtainStyledAttributes(AttributeSet attrs) {
            // init values from custom attributes
            final TypedArray attributes = getContext().obtainStyledAttributes(
                    attrs, R.styleable.HorizontalProgressBarWithNumber);

            mTextColor = attributes
                    .getColor(
                            R.styleable.HorizontalProgressBarWithNumber_progress_text_color,
                            DEFAULT_TEXT_COLOR);
            mTextSize = (int) attributes.getDimension(
                    R.styleable.HorizontalProgressBarWithNumber_progress_text_size,
                    mTextSize);

            mReachedBarColor = attributes
                    .getColor(
                            R.styleable.HorizontalProgressBarWithNumber_progress_reached_color,
                            mTextColor);
            mUnReachedBarColor = attributes
                    .getColor(
                            R.styleable.HorizontalProgressBarWithNumber_progress_unreached_color,
                            DEFAULT_COLOR_UNREACHED_COLOR);
            mReachedProgressBarHeight = (int) attributes
                    .getDimension(
                            R.styleable.HorizontalProgressBarWithNumber_progress_reached_bar_height,
                            mReachedProgressBarHeight);
            mUnReachedProgressBarHeight = (int) attributes
                    .getDimension(
                            R.styleable.HorizontalProgressBarWithNumber_progress_unreached_bar_height,
                            mUnReachedProgressBarHeight);
            mTextOffset = (int) attributes
                    .getDimension(
                            R.styleable.HorizontalProgressBarWithNumber_progress_text_offset,
                            mTextOffset);

            int textVisible = attributes
                    .getInt(R.styleable.HorizontalProgressBarWithNumber_progress_text_visibility,
                            VISIBLE);
            if (textVisible != VISIBLE) {
                mIfDrawText = false;
            }
            attributes.recycle();
        }

        @Override
        protected synchronized void onDraw(Canvas canvas) {

            canvas.save();
            canvas.translate(getPaddingLeft(), getHeight() / 2);

            boolean noNeedBg = false;
            float radio = getProgress() * 1.0f / getMax();
            float progressPosX = (int) (mRealWidth * radio);
            String text = getProgress() + "%";
            // mPaint.getTextBounds(text, 0, text.length(), mTextBound);

            float textWidth = mPaint.measureText(text);
            float textHeight = (mPaint.descent() + mPaint.ascent()) / 2;

            if (progressPosX + textWidth > mRealWidth) {
                progressPosX = mRealWidth - textWidth;
                noNeedBg = true;
            }

            // draw reached bar
            float endX = progressPosX - mTextOffset / 2;
            if (endX > 0) {
                mPaint.setColor(mReachedBarColor);
                mPaint.setStrokeWidth(mReachedProgressBarHeight);
                canvas.drawLine(0, 0, endX, 0, mPaint);
            }
            // draw progress bar
            // measure text bound
            if (mIfDrawText) {
                mPaint.setColor(mTextColor);
                canvas.drawText(text, progressPosX, -textHeight, mPaint);
            }

            // draw unreached bar
            if (!noNeedBg) {
                float start = progressPosX + mTextOffset / 2 + textWidth;
                mPaint.setColor(mUnReachedBarColor);
                mPaint.setStrokeWidth(mUnReachedProgressBarHeight);
                canvas.drawLine(start, 0, mRealWidth, 0, mPaint);
            }

            canvas.restore();

        }

        /**
         * dp 2 px
         *
         * @param dpVal
         */
        protected int dp2px(int dpVal) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    dpVal, getResources().getDisplayMetrics());
        }

        /**
         * sp 2 px
         *
         * @param spVal
         * @return
         */
        protected int sp2px(int spVal) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    spVal, getResources().getDisplayMetrics());
        }
    }

