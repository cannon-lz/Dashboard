package com.zly.dashboard.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.text.DecimalFormat;


public class Dashboard extends View {

    private final class ViewAnimation extends Animation {

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            setDuration(1000);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if(isDrawCenterText) {
                return;
            }
            if(interpolatedTime < 1.0f) {
                mCurrentScore = mScore * interpolatedTime;
                mCurrentIndicatorRadianRange = interpolatedTime * mActualIndicatorRadianRange;
            } else {
                mCurrentScore = mScore;
                mCurrentIndicatorRadianRange = mActualIndicatorRadianRange;
            }
            mScoreText = mFormat.format(mCurrentScore);
            invalidateIndicator();
        }
    }

    private final DecimalFormat mFormat = new DecimalFormat("##");

    private Animation mAnimation = new ViewAnimation();

    private final static int START_ANGLE = 180;
    private static final String TAG = "Dashboard";

    private int[] markColors = new int[]{Color.rgb(241, 30, 89), Color.rgb(255, 253, 47), Color.rgb(78, 220, 132)};
    private final int mWidthPixels = getResources().getDisplayMetrics().widthPixels;

    private int mViewWidth;
    private int mViewHeight;

    private final Paint mArcPaint;
    private final RectF mArcRect;

    private final TextPaint mScoreTextPaint;

    private final Paint mInnerArcPaint;
    private final RectF mInnerArcRect;

    private final Paint mIndicatorPaint;
    private final Path mIndicatorPath;

    private final TextPaint mTickMarkTextPaint;
    private final Path mTickMarkPath;

    private final Paint mCornerPaint;
    private final TextPaint mTimeTextPaint;
    private final Rect mBottomTextRect;

    private Indicator mIndicator;

    private float mTickMarkWidth;
    private float mInnerArcStroke;

    private float mTickMarkSlitWidth;
    private float mMaxScore;
    private float mMinScore;
    private float mScore;
    private float mCurrentScore;
    private int mScoreTextColor = markColors[2];
    private float mScoreTextSize;
    private String mScoreText = "0";
    private float mTickMarkTextSize;
    private int mTickMarkTextColor = Color.GRAY;
    private float mTickMarkSlitWidthCorner;
    private String[] mTickMarkTexts = new String[]{"较差","中等","良好","优秀","极好"};
    private int mTickMarkCount;
    private int mIndicatorColor = markColors[2];


    // 底部文字
    private String mBottomText;
    private float mBottomTextSize;
    private int mBottomTextColor;

    /** 指示器圆行部分的半径 */
    private float mIndicatorRadius;

    /** 指示器与圆心夹角弧度的范围 */
    private float mCurrentIndicatorRadianRange;
    private float mActualIndicatorRadianRange;

    /** 内部圆环距屏幕的偏移量 */
    private float mOffset;

    private boolean isDrawCenterText;

    /** 控制指示器长短的一个范围值，(0 ~ 1之间)，值越小指示器越长 */
    private float mIndicatorRange;

    public Dashboard(Context context) {
        this(context, null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Dashboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttrs(context, attrs);
        mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mTickMarkWidth);
        mArcRect = new RectF();

        mScoreTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mScoreTextPaint.setTypeface(Typeface.MONOSPACE);

        mInnerArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerArcPaint.setColor(Color.GRAY);
        mInnerArcPaint.setStyle(Paint.Style.STROKE);
        mInnerArcRect = new RectF();

        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPath = new Path();

        mTickMarkTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTickMarkPath = new Path();

        mCornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCornerPaint.setStyle(Paint.Style.FILL);
        mCornerPaint.setStrokeWidth(mTickMarkWidth);

        mTimeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setColor(mBottomTextColor);
        mTimeTextPaint.setTextSize(mBottomTextSize);
        mBottomTextRect = new Rect();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void setBottomText(String text) {
        mBottomText = text;
        mTimeTextPaint.getTextBounds(mBottomText, 0, mBottomText.length(), mBottomTextRect);
        invalidate(mBottomTextRect);
    }

    public void reset() {
        mScore = mMinScore;
        mCurrentIndicatorRadianRange = 0;
        mCurrentScore = mMinScore;
        mScoreText = mFormat.format(mScore);
        isDrawCenterText = false;
        invalidateIndicator();
    }

    public void setCenterText(String text) {
        log(TAG, "setCenterText");
        final Rect rect = new Rect();
        mScoreTextPaint.getTextBounds(mScoreText, 0, mScoreText.length(), rect);
        mScoreText = text;
        isDrawCenterText = true;
        log(TAG, "RECT = " + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom);
        invalidate(rect);
    }

    public void setScore(float score) {
        if(isShown()) {
            mScore = score;
            mScore = mScore > mMaxScore ? mMaxScore : score;
            isDrawCenterText = false;
            mActualIndicatorRadianRange = (mScore - mMinScore) / (mMaxScore - mMinScore);
            startAnimation(mAnimation);
        }
    }

    public String getCenterText() {
        return mScoreText;
    }

    public void setMarkColors(int[] colors) {
        this.markColors = colors;
        invalidate();
    }

    public int[] getDefMarkColors() {
        return new int[]{Color.rgb(241, 30, 89), Color.rgb(255, 253, 47), Color.rgb(78, 220, 132)};
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        log(TAG, "onMeasure");
        final Paint.FontMetrics fm = mTimeTextPaint.getFontMetrics();
        final double textHeight = Math.ceil(fm.descent - fm.top) + 2;
        // 除去半圆的矩形范围高多出来的高度，用于绘制底部的文本信息
        float redundantHeight = (float) (mTickMarkWidth / 2 + textHeight);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = (int) (getMeasuredWidth() / 2 + redundantHeight);
        final int heightSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        super.onMeasure(widthMeasureSpec, heightSpec);

        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();
        final float bottomHeight = mViewHeight - redundantHeight;
        mIndicator = new Indicator(mViewWidth, bottomHeight, mIndicatorRadius, mOffset, 0.4F, mIndicatorRange);
        log(TAG, String.format("Dashboard size: width = %s, height = %s", mViewWidth, mViewHeight));
        mScoreTextSize = mViewWidth / 6;
        mInnerArcStroke  = mViewWidth / 105;
        mIndicatorRadius = mViewWidth / 52.5f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        log(TAG, "onDraw");
        final int center = mViewWidth / 2;
        final int radius = (int) (center - mTickMarkWidth / 2);
        log(TAG, String.format("indicatorRadianRange is %s", mCurrentIndicatorRadianRange));
        drawArc(canvas, center, radius);
        drawInnerArc(canvas);
        drawTickMarkText(canvas);
        drawScoreText(canvas, center);
        drawProgress(canvas);
        drawIndicator(canvas);
        drawBottomText(canvas);
    }

    private void readAttrs(Context context, AttributeSet set) {
        final TypedArray a = context.obtainStyledAttributes(set, R.styleable.Dashboard);
        mTickMarkWidth = mWidthPixels / 20.9F;
        mTickMarkSlitWidth = a.getDimension(R.styleable.Dashboard_tickMarkSlitWidth, 0.2f);
        mMaxScore = a.getFloat(R.styleable.Dashboard_maxScore, 1000.0f);
        mMinScore = a.getFloat(R.styleable.Dashboard_minScore, 0f);
        mScore = a.getFloat(R.styleable.Dashboard_score, mMinScore);
        mScoreTextColor = a.getColor(R.styleable.Dashboard_scoreTextColor, markColors[2]);
        mTickMarkTextSize = a.getDimension(R.styleable.Dashboard_tickMarkTextSize, 15);
        mTickMarkTextColor = a.getColor(R.styleable.Dashboard_tickMarkTextColor, Color.GRAY);
        final String tickMarkArray = a.getString(R.styleable.Dashboard_tickMarkTextArray);
        if (!TextUtils.isEmpty(tickMarkArray)) {
            if(!tickMarkArray.contains(",")) {
                throw new IllegalArgumentException("每个等级用 ',' 隔开.");
            } else {
                mTickMarkTexts = tickMarkArray.split(",");
            }
        }
        mTickMarkCount = a.getInteger(R.styleable.Dashboard_tickMarkCount, 37);
        mIndicatorColor = a.getColor(R.styleable.Dashboard_indicatorColor, markColors[2]);
        mIndicatorRadius = mWidthPixels / 56.84f;
        mInnerArcStroke = mWidthPixels / 120;
        mTickMarkSlitWidthCorner = mWidthPixels / 80;
        mIndicatorRange = 0.83f;
        mOffset = mTickMarkWidth * 2 + mWidthPixels / 36;
        mCurrentScore = mMinScore;
        mActualIndicatorRadianRange = (mScore - mMinScore) / (mMaxScore - mMinScore);
        final String bottomTextAttr = a.getString(R.styleable.Dashboard_bottomText);
        mBottomText = TextUtils.isEmpty(bottomTextAttr) ? "" : bottomTextAttr;
        mBottomTextSize = a.getDimension(R.styleable.Dashboard_bottomTextSize, 15);
        mBottomTextColor = a.getColor(R.styleable.Dashboard_bottomTextColor, Color.GRAY);
        a.recycle();
    }

    /**
     * 为保证不必要的资源浪费，只刷新指定的矩形范围
     */
    private void invalidateIndicator() {
        final int left = (int) mInnerArcRect.left - 50;
        final int top = (int) mInnerArcRect.top - 50;
        final int right = (int) mInnerArcRect.right + 50;
        final int bottom = (int) mInnerArcRect.bottom;
        postInvalidateDelayed(5, left, top, right, bottom);
    }

    private void drawProgress(Canvas canvas) {
        log(TAG, "drawProgress");
        mInnerArcPaint.setColor(mIndicatorColor);
        mInnerArcPaint.setPathEffect(new CornerPathEffect(20));
        final float sweepAngle = mCurrentIndicatorRadianRange * 180;
        canvas.drawArc(mInnerArcRect, START_ANGLE, sweepAngle, false, mInnerArcPaint);
    }

    private void drawIndicator(Canvas canvas) {
        log(TAG, "drawIndicator");
        /* 指示器与圆心夹角弧度 */
        final double mIndicatorRadian = Math.PI * mCurrentIndicatorRadianRange;
        mIndicator.calculation(mIndicatorRadian);
        mIndicatorPaint.setColor(mIndicatorColor);
        mIndicatorPaint.setStyle(Paint.Style.STROKE);
        mIndicatorPaint.setStrokeWidth(mInnerArcStroke);
        canvas.drawCircle(mIndicator.getX(), mIndicator.getY(), mIndicatorRadius, mIndicatorPaint);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mIndicator.getX(), mIndicator.getY(), mInnerArcStroke, mIndicatorPaint);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        mIndicatorPaint.setPathEffect(new CornerPathEffect(toDip(4)));
        mIndicatorPath.moveTo(mIndicator.getPointAx(), mIndicator.getPointAy());
        mIndicatorPath.lineTo(mIndicator.getPointCx(), mIndicator.getPointCy());
        mIndicatorPath.lineTo(mIndicator.getPointBx(), mIndicator.getPointBy());
        canvas.drawPath(mIndicatorPath, mIndicatorPaint);
        mIndicatorPath.reset();
    }

    private void drawInnerArc(Canvas canvas) {
        log(TAG, "drawInnerArc");
        mInnerArcPaint.setColor(Color.GRAY);
        // TODO 考虑适配问题，可把具体的值写到资源文件
        mInnerArcPaint.setStrokeWidth(mInnerArcStroke);
        final float radius = (mViewWidth - 2 * mOffset) / 2;
        final float left = mOffset;
        final float top = (mArcRect.height() / 2 - radius) + mTickMarkWidth / 2;
        final float right = left + 2 * radius;
        final float bottom = top + 2 * radius;
        mInnerArcRect.set(left, top, right, bottom);
        log(TAG, String.format("Rect = %s", mInnerArcRect));
        canvas.drawArc(mInnerArcRect, START_ANGLE, 180, false, mInnerArcPaint);
    }

    private void drawScoreText(Canvas canvas, int center) {
        log(TAG, "drawScoreText");
        mScoreTextPaint.setColor(mScoreTextColor);
        if(isDrawCenterText) {
            mScoreTextPaint.setTextSize(mScoreTextSize / 2);
        } else {
            mScoreTextPaint.setTextSize(mScoreTextSize);
        }
        final float textWidth = mScoreTextPaint.measureText(mScoreText);
        canvas.drawText(mScoreText, center - (textWidth / 2), mArcRect.bottom / 2, mScoreTextPaint);
    }

    private void drawArc(Canvas canvas, int center, int radius) {
        log(TAG, "drawArc");
        final int leftTopOffset = center - radius;
        mArcRect.set(leftTopOffset, leftTopOffset, center + radius, center + radius);
        log(TAG, String.format("[drawArc] ArcRect = %s", mArcRect));
        final float itemSize = (START_ANGLE * 1.0f - mTickMarkCount * mTickMarkSlitWidth) / mTickMarkCount;

        mArcPaint.setShader(new LinearGradient(0, getHeight(), getWidth(), getHeight(), markColors, null,
                Shader.TileMode.MIRROR));
        for(int i = 0; i < mTickMarkCount; i++) {
            final float startAngle = START_ANGLE + (i * (itemSize + mTickMarkSlitWidth));
            canvas.drawArc(mArcRect, startAngle, itemSize, false, mArcPaint);
        }
        drawCornerLeft(canvas);
        drawCornerRight(canvas);
    }

    private void drawTickMarkText(Canvas canvas) {
        log(TAG, "drawTickMarkText");
        mTickMarkTextPaint.setColor(mTickMarkTextColor);
        mTickMarkTextPaint.setTextSize(mTickMarkTextSize);
        mTickMarkPath.addArc(mInnerArcRect, 180, 180);
        final double innerArcPerimeter = Math.PI * mInnerArcRect.height() / 2;
        final double section = innerArcPerimeter / (mTickMarkTexts.length - 1);
        final float vOffset = (mOffset - mTickMarkWidth - mWidthPixels / 48) / 2;
        float hOffset;
        for (int i = 0; i < mTickMarkTexts.length; i++) {
            final float textWidth = mTickMarkTextPaint.measureText(mTickMarkTexts[i]);
            final float textHCenter = (float) ((section - textWidth) / 2);
            log(TAG, String.format("section= %s, textWidth = %s, textHCenter = %s", section, textWidth, textHCenter));
            if (i == 0) {
                hOffset = 0;
            } else if(i + 1 == mTickMarkTexts.length) {
                hOffset = (float) (innerArcPerimeter - textWidth);
            } else {
                hOffset = (float) (i * section - (textWidth / 2));
            }
            canvas.drawTextOnPath(mTickMarkTexts[i], mTickMarkPath, hOffset, -vOffset, mTickMarkTextPaint);
        }
    }

    private void drawBottomText(Canvas canvas) {
        final float textWidth = mTimeTextPaint.measureText(mBottomText);
        final float startX = (mViewWidth - textWidth) / 2;
        canvas.drawText(mBottomText, startX, mViewHeight - 5, mTimeTextPaint);
    }

    private void drawCornerRight(Canvas canvas) {
        final float x = mViewWidth - mTickMarkWidth - 0.5f;
        final float y = mArcRect.bottom / 2 + mTickMarkSlitWidthCorner;
        final Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x + mTickMarkWidth, y);
        path.lineTo(x + mTickMarkWidth, y + mTickMarkWidth / 2);
        path.lineTo(x, y + mTickMarkWidth / 4);
        path.close();
        mCornerPaint.setColor(markColors[2]);
        canvas.drawPath(path, mCornerPaint);
    }

    private void drawCornerLeft(Canvas canvas) {
        final float x = mArcRect.left - mTickMarkWidth / 2;
        final float y = mArcRect.bottom / 2 + mTickMarkSlitWidthCorner + 1;
        final Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x + mTickMarkWidth, y);
        path.lineTo(x + mTickMarkWidth, y + mTickMarkWidth / 4);
        path.lineTo(x, y + mTickMarkWidth / 2);
        path.close();
        mCornerPaint.setColor(markColors[0]);
        canvas.drawPath(path, mCornerPaint);
    }

    private float toDip(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private final static boolean LOG_SWITCH = false;
    private void log(String tag, String msg) {
        if(LOG_SWITCH) {
            Log.i(tag, msg);
        }
    }

}
