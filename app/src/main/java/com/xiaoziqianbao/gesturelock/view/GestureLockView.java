package com.xiaoziqianbao.gesturelock.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.xiaoziqianbao.gesturelock.DotBean;
import com.xiaoziqianbao.gesturelock.R;

import java.util.ArrayList;

/**
 * Created by liaopenghui on 2016/11/21.
 */

public class GestureLockView extends View {

    private static final String TAG = "GestureLockView";
    private int mPadding = 40;//圆环与屏幕边框的内边距值
    private int maxRadius;
    private int defaultWidth;
    ArrayList<DotBean> dotsList = new ArrayList<>();
    ArrayList<DotBean> selectedDotsList = new ArrayList<>();
    private Paint mPaint = new Paint();
    private int circleRadius;
    private DotBean currentPoint;//选中的当前点
    private float moveX;
    private float moveY;
    Path path = new Path();
    private boolean isFirst = true;

    public GestureLockView(Context context) {
        super(context);
    }

    public GestureLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int mScreenWidth = getWidth();
        int mScreenHeight = getHeight();
        defaultWidth = mScreenHeight>mScreenWidth?mScreenWidth:mScreenHeight;//保证横竖屏时候手势区域以最小的为准。
        initConfig();
        initDot();//先初始化9个圆点
    }

    private void initConfig() {
        //初始化默认参数：知道屏幕的宽度和内边距值就知道了圆心的坐标，和9个圆的最大半径（当9个圆相邻互切的时候，圆半径为最大值）
maxRadius = (defaultWidth-(mPadding*2))/6;
circleRadius = (defaultWidth-(mPadding*2))/12;
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }

    private void initDot() {
        for(int i = 0;i<9;i++){
            DotBean dotBean = new DotBean();
            dotBean.radius = circleRadius;
            dotBean.id = i;
                if(i<3){
                    dotBean.pointX =mPadding+maxRadius+(2*i)*maxRadius;
                    dotBean.pointY =mPadding+maxRadius;
                }else if(i<6){
                    dotBean.pointX =mPadding+maxRadius+(2*(i-3))*maxRadius;
                    dotBean.pointY =mPadding+maxRadius+2*maxRadius;
                }else{
                    dotBean.pointX =mPadding+maxRadius+(2*(i-6))*maxRadius;
                    dotBean.pointY =mPadding+maxRadius+2*2*maxRadius;
                }

            dotsList.add(dotBean);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(DotBean dotBean :dotsList){
            canvas.drawCircle(dotBean.pointX,dotBean.pointY,dotBean.radius,mPaint);
            Bitmap selectedBitmap = getBitmapFor(R.mipmap.gesture_pattern_selected);
            //画圆
            if(dotBean.hasSelected) {
                canvas.drawBitmap(selectedBitmap, dotBean.pointX - (selectedBitmap.getWidth() / 2) - 5, dotBean.pointY - (selectedBitmap.getHeight() / 2), mPaint);
            }

            if(selectedDotsList.size()>1) { //当选择的点大于2个时候才开始画path  否则会有飞线的bug
            //画线
            for(int i = 0;i<selectedDotsList.size();i++){
                    if(i==0&&selectedDotsList.size()>1){
                        path.reset();
                        path.moveTo(selectedDotsList.get(i).pointX,selectedDotsList.get(i).pointY);
                    }else{
                        path.lineTo(selectedDotsList.get(i).pointX,selectedDotsList.get(i).pointY);
                    }
            }

                canvas.drawPath(path, mPaint);
            }
//            for(DotBean dotBean1:selectedDotsList){
//                if()
//                path.lineTo();
//            }
            if(null!=currentPoint){
                Log.d(TAG,"LINE");
                canvas.drawLine(currentPoint.pointX,currentPoint.pointY,(int)moveX,(int)moveY,mPaint);
                canvas.save();


            }
        }


    }



    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"ACTION_MOVE");

        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG,"ACTION_MOVE");
                 moveX = event.getX(); // 这里要实时记录手指的坐标
                 moveY = event.getY();
              DotBean   currentPoint = getCurrentPoint(moveX, moveY);
                if(null!= currentPoint&&!currentPoint.hasSelected){

                    this.currentPoint =currentPoint;
                    if(!currentPoint.hasSelected) {//如果未被选中的变为选中，已经选中的就不用添加至被选中的数组中了
                        currentPoint.hasSelected = true;
                        selectedDotsList.add(currentPoint);
                    }
            }
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG,"ACTION_DOWN");
                break;

            case MotionEvent.ACTION_UP:
                Log.d(TAG,"ACTION_UP");
                for(DotBean dotBean:selectedDotsList){
                    Log.d(TAG,"得到的手势密码为："+dotBean.id);
                }

                break;



            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG,"ACTION_CANCEL");
                break;
        }




        return true;   //必须用return true 不然识别不了move 等场景。
    }



    //获取选中的点
    private DotBean getCurrentPoint(float moveX, float moveY) {
        for(DotBean point:dotsList){
            //两点之间的距离<=外圆半径
            if( Math.sqrt( Math.pow((moveX-point.pointX),2) + Math.pow((moveY-point.pointY),2) )<=circleRadius){
                return point;
            }
        }
        return null;
    }

}
