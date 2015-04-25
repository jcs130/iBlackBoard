package com.zhongli.john.iblackboard;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Zhongli on 2015/2/21.
 */
public class mainActivity extends Activity {

    private Screen sc = null;
    private int chatPort = 6001;
    private int audioPort = 6000;
    private int picPort = 6002;
    private String IPaddr = "localhost";
    private Handler handler;
    private Button send_but;
    private TextView rec_text;
    private TextView send_text;
    private NetClient nc;
    private String name, pwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        chatPort = b.getInt("ChatPort");
        audioPort = b.getInt("AudioPort");
        picPort = b.getInt("PicPort");
        IPaddr = b.getString("IPaddr");
        name = b.getString("UserName");
        pwd = b.getString("PassWord");

        setContentView(R.layout.activity_client);
        sc = (Screen) findViewById(R.id.Screen);
        sc.setServer(IPaddr, picPort);
        send_but = (Button) findViewById(R.id.send_but);

        rec_text = (TextView) findViewById(R.id.receiveText);
//        rec_text.setEnabled(false);
        send_text = (TextView) findViewById(R.id.sendText);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //创建属于主线程的handler
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    sc.invalidate();
//                    System.out.println("更新界面");
                }
                if (msg.what == 2) {
                    String message = (String) msg.obj;
//                    System.out.println("收到：" + message);
                    rec_text.append(message + "\n");
                }
            }
        };
        sc.setHandle(handler);
        nc = new NetClient(handler, IPaddr, chatPort, audioPort, picPort);
        nc.setNamePwd(name, pwd);
        nc.setScreen(sc);
        Thread t = new Thread(sc);
        t.start();
        nc.start();
        send_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg2Host = send_text.getText().toString().trim();
                if (msg2Host == "") {
                    send_text.setError("Can't send null message..");
                } else {
//                    String temp= null;
//                    try {
//                        temp = new String(msg2Host.getBytes(), "ISO-8859-1").trim();
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }

                    nc.send2Host("<type>chat</type><body>" + msg2Host
                            + "</body>");
                    send_text.setText("");
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        nc.send2Host("exit");
        finish();

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
        System.exit(0);
    }

    //    @Override
//    public void onConfigurationChanged(Configuration newConfig)
//    {
//        // 一定要先调用父类的同名函数，让框架默认函数先处理
//        // 下面这句一定不能省去，否则将引发：android.app.SuperNotCalledException 异常。
//        super.onConfigurationChanged( newConfig );
//
//        // 检测屏幕的方向：纵向或横向
//        if ( this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
//        {
//            // 当前为横屏， 在此处添加额外的处理代码
//        }
//        else if ( this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT )
//        {
//            //当前为竖屏， 在此处添加额外的处理代码
//        }
//
//        //检测实体键盘的状态：推出或者合上
//        if ( newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO )
//        {
//            // 实体键盘处于推出状态，在此处添加额外的处理代码
//        }
//        else if ( newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES )
//        {
//            // 实体键盘处于合上状态，在此处添加额外的处理代码
//        }
//    }

}
