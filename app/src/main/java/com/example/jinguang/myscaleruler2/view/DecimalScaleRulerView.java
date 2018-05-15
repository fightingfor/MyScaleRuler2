package com.example.jinguang.myscaleruler2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.example.jinguang.myscaleruler2.utils.DrawUtil;
import com.example.jinguang.myscaleruler2.utils.TextUtil;


/**
 * 横向滚动刻度尺
 */

public class DecimalScaleRulerView extends View {

    private int mMinVelocity;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mWidth;
    private int mHeight;

    private float mValue = 0;//默认起始点
    private float mMaxValue = 100;
    private float mMinValue = 0;
    private int mItemSpacing;
    private int mPerSpanValue = 1;
    private int mMaxLineHeight;
    private int mMiddleLineHeight;
    private int mMinLineHeight;
    private int mLineWidth;
    private int mTextMarginTop;
    private float mTextHeight;

    private Paint mTextPaint; // 绘制文本的画笔
    private Paint mLinePaint;

    private int mTotalLine;
    private int mEffLine;//有效的长度
    private int mEffOffset;//有效的偏移量
    private int mMaxOffset;
    private float mOffset; // 默认尺起始点在屏幕中心, offset是指尺起始点的偏移值
    private int mLastX, mMove;
    private OnValueChangeListener mListener;


    public DecimalScaleRulerView(Context context) {
        this(context, null);
    }

    public DecimalScaleRulerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public DecimalScaleRulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        mScroller = new Scroller(context);
        mMinVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        mItemSpacing = DrawUtil.dip2px(2);
        mLineWidth = DrawUtil.dip2px(2);
        mMaxLineHeight = DrawUtil.dip2px(16);
        mMiddleLineHeight = DrawUtil.dip2px(8);
        mMinLineHeight = DrawUtil.dip2px(0);
        mTextMarginTop = DrawUtil.dip2px(10);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(DrawUtil.sp2px(10));
        mTextPaint.setColor(Color.WHITE);
        mTextHeight = TextUtil.getFontHeight(mTextPaint);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setColor(Color.WHITE);
    }

    /**
     *
     * @param defaultValue 默认起始点
     * @param minValue 最小值
     * @param maxValue 最大值
     * @param effValue 有效值
     * @param spanValue
     */
    public void initViewParam(float defaultValue, float minValue, float maxValue, float effValue, int spanValue) {
        this.mValue = defaultValue;
        this.mMaxValue = maxValue;
        this.mMinValue = minValue;
        this.mPerSpanValue = spanValue;
        this.mTotalLine = (int) (maxValue * 10 - minValue * 10) / spanValue + 1;
        this.mEffLine = (int) (effValue * 10 - minValue * 10) / spanValue + 1;
        mMaxOffset = -(mTotalLine - 1) * mItemSpacing;
        mEffOffset = -(mEffLine - 1) * mItemSpacing;
        mOffset = (minValue - defaultValue) / spanValue * mItemSpacing * 10;
        invalidate();
        setVisibility(VISIBLE);
    }

    /**
     * 设置用于接收结果的监听器
     *
     * @param listener
     */
    public void setValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            mWidth = w;
            mHeight = h;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left, height;
        String value;
        int alpha;
        float scale;
        int srcPointX = mWidth / 2; // 默认表尺起始点在屏幕中心
        for (int i = 0; i < mTotalLine; i++) {
            left = srcPointX + mOffset + i * mItemSpacing;

            if (left < 0 || left > mWidth) {
                continue;
            }

            if (i % 60 == 0) {
                height = mMaxLineHeight;
            } else if (i % 30 == 0) {
                height = mMiddleLineHeight;
            } else {
                height = 0;
            }
            if (i>mEffLine){
                mLinePaint.setColor(Color.GRAY);
                mTextPaint.setColor(Color.GRAY);
            }else {
                mLinePaint.setColor(Color.WHITE);
                mTextPaint.setColor(Color.WHITE);
            }
            canvas.drawLine(left, 0, left, height, mLinePaint);
            String format = "";
            if (i % 60 == 0) { // 大指标,要标注文字
                value = String.valueOf((int) (mMinValue + i * mPerSpanValue / (10 * 60)));

                if (Integer.parseInt(value) >= 0 && Integer.parseInt(value) < 10) {
                    format = "0%s:00";
                } else {
                    format = "%s:00";
                }
                value = String.format(format, value);
                canvas.drawText(value, left - mTextPaint.measureText(value) / 2,
                        height + mTextMarginTop + mTextHeight - DrawUtil.dip2px(3), mTextPaint);
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int xPosition = (int) event.getX();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mLastX = xPosition;
                mMove = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                mMove = (mLastX - xPosition);
                changeMoveAndValue();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                countMoveEnd();
                countVelocityTracker();
                return false;
            // break;
            default:
                break;
        }

        mLastX = xPosition;
        return true;
    }

    private void countVelocityTracker() {
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (Math.abs(xVelocity) > mMinVelocity) {
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        }
    }

    private void countMoveEnd() {
        mOffset -= mMove;
        if (mOffset <= mEffOffset) {
            mOffset = mEffOffset;
        } else if (mOffset >= 0) {
            mOffset = 0;
        }

        mLastX = 0;
        mMove = 0;

        mValue = mMinValue + Math.round(Math.abs(mOffset) * 1.0f / mItemSpacing * 60) * mPerSpanValue / 10.0f;
        notifyValueChange();
        postInvalidate();
    }

    private void changeMoveAndValue() {
        mOffset -= mMove;
        if (mOffset <= mEffOffset) {
            mOffset = mEffOffset;
            mMove = 0;
            mScroller.forceFinished(true);
        } else if (mOffset >= 0) {
            mOffset = 0;
            mMove = 0;
            mScroller.forceFinished(true);
        }
        mValue = mMinValue + Math.round(Math.abs(mOffset) * 1.0f / mItemSpacing * 60) * mPerSpanValue / 10.0f;
        notifyValueChange();
        postInvalidate();
    }

    private void notifyValueChange() {
        if (null != mListener) {
            mListener.onValueChange(formatTime((int) mValue));
        }
    }

    private String formatTime(int mss) {
        String format = "%s:%s:%s";
        int hours = (mss % (60 * 60 * 24)) / (60 * 60);
        int minutes = (mss % (60 * 60)) / 60;
        int seconds = mss % 60;
        return String.format(format,getTimeStr(hours),getTimeStr(minutes),getTimeStr(seconds));
    }

    private String getTimeStr(int time) {
        String timeStr = "";
        if (time >= 0 && time < 10) {
            timeStr = "0" + time;
        } else {
            timeStr = time + "";
        }
        return timeStr;
    }

    public interface OnValueChangeListener {
        void onValueChange(String value);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() == mScroller.getFinalX()) { // over
                countMoveEnd();
            } else {
                int xPosition = mScroller.getCurrX();
                mMove = (mLastX - xPosition);
                changeMoveAndValue();
                mLastX = xPosition;
            }
        }
    }
}
