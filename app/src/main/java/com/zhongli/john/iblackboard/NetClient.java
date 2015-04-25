package com.zhongli.john.iblackboard;

/**
 * Created by Zhongli on 2015/2/19.
 */

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 客户端线程
 *
 * @author Micro
 */
public class NetClient extends Thread {
    private int port, audioPort, picPort;
    private String ip;
    private InputStream ins;
    private OutputStream ous;
    //	private DrawPanel dp;
    private Screen sc;
    private Socket socket;
    private String name, pwd;

    private Handler handler;

    // 重载构造器
    public NetClient(Handler handler, String ip, int port, int audioPort, int picPort) {
        this.handler = handler;
        this.ip = ip;
        this.port = port;
        this.audioPort = audioPort;
        this.picPort = picPort;
    }

    /**
     * 是否连接上服务器
     *
     * @return
     */
    public boolean isconnect() {
        try {
            System.out.println(ip + "<><>" + port);
            socket = new Socket(ip, port);

            ins = socket.getInputStream();
            ous = socket.getOutputStream();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Socket getSocket() {
        return socket;
    }


    public void setNamePwd(String name, String pwd) {
        this.name = name;
        this.pwd = pwd;
    }

    public void changePort(String ip, int port, int audioPort, int picPort) {
        this.ip = ip;
        this.port = port;
        this.audioPort = audioPort;
        this.picPort = picPort;
    }

    /**
     * 检测用户名和密码
     *
     * @param name 用户名
     * @param pwd  密码
     * @return 0 没有此用户 1 登陆成功 2 登录失败
     */
    public int istrue(String name, String pwd) {
        String s;
        try {
            String send = "<type>login</type><name>" + name + "</name><pwd>"
                    + pwd + "</pwd>";

            send2Host(send);
            // System.out.println(send);
            s = readString();
            String key = getXMLValue("key", s);
            System.out.println("返回的值是" + key);
            if (key.equals("nouser")) {
                System.out.println("没有用户");
                return 0;
            } else if (key.equals("yes")) {
                System.out.println("登陆成功");
                return 1;
            } else {
                System.out.println("密码错误");
                return 2;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 2;
    }

    public boolean canReg(String name, String pwd) {
        String s;
        try {
            String send = "<type>reg</type><name>" + name + "</name><pwd>"
                    + pwd + "</pwd>";
            // System.out.println(send);
            send2Host(send);
            s = readString();
            if (getXMLValue("key", s).equals("yes")) {
                return true;
            } else if (getXMLValue("key", s).equals("no")) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 读取服务器消息的线程
    public void run() {
        if (isconnect()) {
            istrue(name, pwd);
            while (true) {
                readFromServer();
            }
        } else {
            System.out.println("没有连接服务器");
        }
    }

    // 从服务器读取消息
    private void readFromServer() {

        String input;
        try {
            String temp = readString();
            String type = getXMLValue("type", temp);
//            System.out.println("类型是" + type);
            if (type.equals("chat")) {
                input = getXMLValue("body", temp);
                if (input != null) {
                    System.out.println("从服务器接收到的消息是:" + input);
                    //向界面输出接收到的消息
                    Message msg = Message.obtain();
                    msg.obj = input;
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
            }
            if (type.equals("mouse")) {
                //更新鼠标坐标
                int x = Integer.parseInt(getXMLValue("x", temp));
                int y = Integer.parseInt(getXMLValue("y", temp));
                sc.setMouth(x, y);
                sc.repaint();
            }
            if (type.equals("picName")) {
                //更新截图时间
                String time = getXMLValue("time", temp);
                sc.setPicName(time);

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("遗失对主机的连接！");
            //向界面输出接收到的消息

        }
    }


    // 发送给服务器消息
    public void send2Host(String s) {

        try {

            String send = "<msg>" + s + "</msg>";
            ous.write(send.getBytes("GBK"));
            ous.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 从输入流中读取一条xml消息，以</msg>结尾
     *
     * @return 从流中读取到的消息
     * @throws Exception
     */
    private String readString() throws Exception {
        String msg = "";
        int i = ins.read();// 从输入流中读取数据
        StringBuffer stb = new StringBuffer();// 字符串缓冲区
        boolean end = false;
        while (!end) {
            char c = (char) i;
            stb.append(c);
            msg = stb.toString().trim();// 去掉尾部的空格
            if (msg.endsWith("</msg>")) {
                end = true;
                continue;
            }
            i = ins.read();// 继续读取字节
        }
        // 转化为GBK编码，可以显示中文
        msg = new String(msg.getBytes("ISO-8859-1"), "GBK").trim();
        return msg;
    }

    // 解析xml的内容
    public String getXMLValue(String flagName, String xmlMsg) throws Exception {
        try {
            // 1.<标记>头出现的位置
            int start = xmlMsg.indexOf("<" + flagName + ">");
            start += flagName.length() + 2;// 计算向后偏移的长度
            // 2.</标记> 结束符出现的位置
            int end = xmlMsg.indexOf("</" + flagName + ">");
            // 3.截取标记之间的消息
            String value = xmlMsg.substring(start, end).trim();
            return value;
        } catch (Exception e) {
            throw new Exception("解析" + flagName + "失败:" + xmlMsg);
        }
    }


    public void setScreen(Screen sc) {
        this.sc = sc;
        //同时打开音频服务器
        try {
            Playback player = new Playback(ip, audioPort);
//            player.init();
            player.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
