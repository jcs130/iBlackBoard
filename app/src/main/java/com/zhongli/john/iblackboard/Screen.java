package com.zhongli.john.iblackboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.net.Socket;
import java.util.zip.ZipInputStream;

/**
 * Created by Zhongli on 2015/2/19.
 */
public class Screen extends View implements Runnable {


    private Bitmap cimage, temp,temp1;
    private int picx=0, picy=0, showWidth, showHeight;
    private Socket s;
    private float rate = 1.0f;
    private String picName;
    private Bitmap img_mouse;

    private String IPaddr;
    private int picPort;

    private int screenWidth;
    private int screenHight;
    private Paint mPaint;

    private Rect mBounds;
    private int mCount;
    private Handler handler;

    /**
     * ****************************
     */

    static final int DOUBLE_CLICK_TIME_SPACE = 300; // 双击时间间隔
    static final int DOUBLE_POINT_DISTANCE = 10; // 两点放大两点间最小间距
    static final int NONE = 0;
    static final int DRAG = 1; // 拖动操作
    static final int ZOOM = 2; // 放大缩小操作
    private int mode = NONE; // 当前模式



    float bigScale = 3f; // 默认放大倍数
    Boolean isBig = false; // 是否是放大状态
    long lastClickTime = 0; // 单击时间
    float startX;
    float startY;
    private int teach_x, teach_y;

    /**
     * 按下操作
     *
     * @param event
     */
    public void mouseDown(MotionEvent event) {
        mode = NONE;
        startX = event.getRawX();
        startY = event.getRawY();
        if (event.getPointerCount() == 1) {
            // 如果两次点击时间间隔小于一定值，则默认为双击事件
            if (event.getEventTime() - lastClickTime < DOUBLE_CLICK_TIME_SPACE) {
                changeSize((int) startX, (int) startY);
            } else if (isBig) {
                mode = DRAG;
            }
        }

        lastClickTime = event.getEventTime();
    }

    /**
     * 鼠标抬起事件
     */
    public void mouseUp() {
        mode = NONE;
    }

    /**
     * 图片放大缩小
     *
     * @param x 点击点X坐标
     * @param y 点击点Y坐标
     */
    private void changeSize(int x, int y) {
        try {
            if (isBig) {
                temp = Bitmap.createScaledBitmap(cimage, showWidth, showHeight, true);
                picx = 0;
                picy = 0;
                repaint();
                isBig = false;
            } else {
                picx = x;
                picy = y;
//                System.out.println("双击位置:"+x+"<>"+y);
                int stX=(int)(picx-(screenWidth/bigScale)/2);
                int stY=(int)(picy-(screenHight/bigScale)/2);
                int stW=(int)(screenWidth/bigScale);
                int stH=(int)(screenHight/bigScale);
                if(stX<0){
                    stX=0;
                }
                if(stY<0){
                    stY=0;
                }
                if(stX+stW>showWidth){
                    stX=showWidth-stW;
                }
                if(stY+stY>showHeight){
                    stY=showHeight-stH;
                }
                temp1=Bitmap.createBitmap(temp,stX  ,stY ,stW,stH);
                temp = Bitmap.createScaledBitmap(temp1,screenWidth, screenHight, true);

                repaint();
                isBig = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * ********************************************************
     */


//    private boolean isLand;

//    public Screen(Context context, String IPaddr, int picPort) {
//        super(context);
//        this.IPaddr = IPaddr;
//        this.picPort = picPort;
//        img_mouse = BitmapFactory.decodeResource(getResources(), R.drawable.mouse_pointer);
//        mPaint = new Paint();
//        mBounds = new Rect();
//        System.out.println("diaoyongle");
//    }
    public Screen(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        DisplayMetrics dm2 = getResources().getDisplayMetrics();
        screenWidth = dm2.widthPixels;
        screenHight = dm2.heightPixels;
        //载入鼠标指针图片
        Bitmap temp =BitmapFactory.decodeResource(getResources(), R.drawable.mouse_pointer);
        int picW=temp.getWidth();
        int picH=temp.getHeight();
        int reaW=(int)(screenWidth*0.05);
        int reaH=reaW*picH/picW;
        img_mouse=Bitmap.createScaledBitmap(temp,reaW,reaH,true);
        System.out.println("width: " + screenWidth);

        System.out.println("height: " + screenHight);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mouseDown(event);
                break;

            /**
             * 非第一个点按下
             */
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                mouseUp();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.out.println("测量元件大小");
        if (screenWidth > screenHight) {
            setMeasuredDimension(screenWidth, screenHight);
        } else {
            setMeasuredDimension(screenWidth, screenWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        System.out.println("ssssss");
        mPaint.setColor(Color.BLACK);
        if (temp != null) {
//            System.out.println("temp");
            if (isBig) {
//                canvas.drawBitmap(temp, showWidth / 2 - picx * bigScale, showHeight / 2 - picy * bigScale, mPaint);
                canvas.drawBitmap(temp,0,0,mPaint);
            } else {
                canvas.drawBitmap(temp, (screenWidth - showWidth) / 2,
                        (screenHight - showHeight) / 2, mPaint);
                canvas.drawBitmap(img_mouse, (teach_x * rate + (screenWidth - showWidth) / 2),
                        (teach_y * rate + (screenHight - showHeight) / 2), mPaint);
            }


        }
    }


    //设置鼠标位置
    public void setMouth(int x, int y) {
        this.teach_x = x;
        this.teach_y = y;
    }

    //更新界面显示
    public void repaint() {
        handler.sendEmptyMessage(1);
    }

    //设置截图到来的时间
    public void setPicName(String time) {
        this.picName = "" + time;
    }

    @Override
    public void run() {

        int picWidth, picHeight;
        // 放大或缩小的倍数
        try {
            s = new Socket(IPaddr, picPort);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }// 连接远程IP
        try {

            System.out.println("Pic Start");
            while (true) {
                try {
                    ZipInputStream zis = new ZipInputStream(
                            s.getInputStream());
                    zis.getNextEntry();
                    cimage = BitmapFactory.decodeStream(zis);// 把ZIP流转换为图片
                    if (cimage != null) {
                        picWidth = cimage.getWidth();
                        picHeight = cimage.getHeight();
                        float picRate=picWidth/(float)picHeight;
                        float scRate=screenWidth/(float)screenHight;
                        System.out.println("pic:"+picWidth+"<>"+picHeight+"\nrate:"+picRate+"<>"+scRate);
                        // 如果图片的宽高比例比显示的大，则根据图片的宽度显示
                        if ( picRate>  scRate) {
                            System.out.print("图片更宽");
                            showWidth = screenWidth;
                            showHeight = screenWidth * picHeight
                                    / picWidth;
                            rate = (showWidth / (float) picWidth);
                            System.out.println(showWidth / (double) picWidth);
                        } else if ( picRate<scRate) {
                            System.out.print("屏幕更宽");
                            showHeight = screenHight;
                            showWidth = picHeight * screenWidth
                                    / picWidth;
                            rate = (showWidth / (float) picWidth);
                        } else {
                            System.out.print("比例一样");
                            showWidth = this.getWidth();
                            showHeight = this.getHeight();
                            rate = (showWidth / (float) picWidth);
                        }
                        System.out.println("w:"+showWidth+"h:"+showHeight+"rate"+rate);
                        isBig=!isBig;
                        changeSize(picx,picy);
                    } else {
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
        }

    }


    public void setServer(String iPaddr, int picPort) {
        this.IPaddr = iPaddr;
        this.picPort = picPort;
    }

    public void setHandle(Handler handler) {
        this.handler = handler;
    }
}
