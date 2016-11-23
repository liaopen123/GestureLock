package com.xiaoziqianbao.gesturelock.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;
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
    private  Context context;
    private int mPadding = 40;//圆环与屏幕边框的内边距值
    private int maxRadius;
    private int defaultWidth;
    ArrayList<DotBean> dotsList = new ArrayList<>();
    ArrayList<DotBean> selectedDotsList = new ArrayList<>();
    private Paint mPaint = new Paint();
    private int circleRadius;
    public   boolean mSettingGesture = false;//true代表设置手势界面  false 代表解锁界面
    private DotBean currentPoint;//选中的当前点
    private float moveX;
    private float moveY;
    Path path = new Path();
    private boolean isFirst = true;
    private GestureStateListener setOnGestureStateListener;//手势的监听
    private int mCount=1;//设置手势密码是时候用来计数
    private String firstInputPWD;//第一次输入的手势密码

    public GestureLockView(Context context) {
        super(context);
        this.context = context;
    }

    public GestureLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public GestureLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
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

            if(selectedDotsList.size()>1) { //当选择的点大于2个时候才开始画path  否则会有飞线的bug
                //画path线
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

            //画动的线
            if(null!=currentPoint){
                Log.d(TAG,"LINE");
                canvas.drawLine(currentPoint.pointX,currentPoint.pointY,(int)moveX,(int)moveY,mPaint);
                canvas.save();


            }

        for(DotBean dotBean :dotsList){
            canvas.drawCircle(dotBean.pointX,dotBean.pointY,dotBean.radius,mPaint);
            Bitmap selectedBitmap = getBitmapFor(R.mipmap.gesture_pattern_selected);
            //画圆
            if(dotBean.hasSelected) {
                canvas.drawBitmap(selectedBitmap, dotBean.pointX - (selectedBitmap.getWidth() / 2) - 5, dotBean.pointY - (selectedBitmap.getHeight() / 2), mPaint);
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
                //清楚数据
              //  dotsList.clear();
                break;

            case MotionEvent.ACTION_UP:
                Log.d(TAG,"ACTION_UP");
                clearLine();//松手的时候   清楚毛刺线drawline();

                //长度小于4的时候
                if(selectedDotsList.size()<4){
                    if(null!=setOnGestureStateListener) {
                        setOnGestureStateListener.setCountLess4();
                    }
                    clearGresture();
                    return true;
                }
                //第二次确认手势密码
                if(mSettingGesture&&mCount==2){
                    Log.d(TAG,"第而次输入手势密码");
                    String secondPWD = transPassword2String(selectedDotsList);
                    if(!secondPWD.equals(firstInputPWD)){
                        //l两次密码不相同
                        if(null!=setOnGestureStateListener) {
                            setOnGestureStateListener.secondSettingFailed();
                        }
                        clearGresture();
                    }else{
                        //两次密码相同
                        if(null!=setOnGestureStateListener) {
                            setOnGestureStateListener.secondSettingSuccess(secondPWD);
                        }
                    }


                }
                //设置密码 并且第一次输入
                if(mSettingGesture&&mCount==1){
                    Log.d(TAG,"第一次输入手势密码");
                    if(null!=setOnGestureStateListener) {
                        setOnGestureStateListener.fisrtSettingSuccess();

                    }
                     firstInputPWD = transPassword2String(selectedDotsList);
                    mCount++;
                    clearGresture();
                }



                //确认密码
                if(!mSettingGesture){
                    if(compareWithNativePWD(selectedDotsList)){
                        if(null!=setOnGestureStateListener) {
                            setOnGestureStateListener.inputGestureCorrect();
                        }
                    }else{
                        setOnGestureStateListener.inputGestureWrong();
                    }





                }

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



   public  interface GestureStateListener{
        void setCountLess4();//手势密码少于4位
        void fisrtSettingSuccess();//第一次设置成功
        void secondSettingSuccess(String secondPWD);//第二次设置成功
        void secondSettingFailed();//第二次设置手势密码错误
        void inputGestureWrong();//输入手势密码失败
        void inputGestureCorrect();//输入手势密码失败
    }

    public void setOnGestureStateListener(GestureStateListener gestureStateListener){
        this.setOnGestureStateListener =gestureStateListener;
    }


    public void  setMode(boolean settingGesture){
        this.mSettingGesture = settingGesture;
    }



    public void clearGresture(){
        postDelayed(new Runnable() {
            @Override
            public void run() {
                for(DotBean dotBean:selectedDotsList){
                    dotBean.hasSelected = false;
                }
                selectedDotsList.clear();
                invalidate();
            }
        },1000);

    }
    /**消除线**/
    public  void clearLine(){
        currentPoint=null;//这个可以消除画线
        invalidate();
    }


    /**把密码转换成String类型进行比较**/
    public  String transPassword2String(ArrayList<DotBean> dotsList){
        StringBuilder sb = new StringBuilder();
        for(DotBean dotBean:dotsList){
            sb.append(dotBean.id);
        }
        Log.d(TAG,"SB:"+sb);
        return sb.toString();
    }
    /**本地SP保存密码与输入密码进行比较**/
    public Boolean compareWithNativePWD(ArrayList<DotBean> dotsList){
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String pwd = sharedPreferences.getString("pwd", "");
        if(TextUtils.isEmpty(pwd)){
            throw new RuntimeException("please set your gesturePwd first!");
        }else{
           if(pwd.equals(transPassword2String(dotsList))){//sp中的密码与输入密码比较
               return true;
           }else{
               return false;
           }
        }

    }
}
