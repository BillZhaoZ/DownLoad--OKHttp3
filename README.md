# DownLoad--OKHttp3

使用OKHttp3进行下载和断点续传；

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

开启java8,使用Lambada表达式简化代码；

    //为了开启Java8
          jackOptions {
                enabled true;
          }

下载完成的安装，增加7.0权限的判定；

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

    package com.bill.download.view;

    import android.content.Context;
    import android.content.res.TypedArray;
    import android.graphics.Canvas;
    import android.graphics.Paint;
    import android.util.AttributeSet;
    import android.util.TypedValue;
    import android.widget.ProgressBar;

    import com.bill.download.R;

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

