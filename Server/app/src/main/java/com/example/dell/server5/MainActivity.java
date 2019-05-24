package com.example.dell.server5;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import java.io.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity
{
    private TextView receivedData;
    private EditText sendData;

    private ServerSocket serverSocket;
    private Socket socket;

    private int PORT = 20000;

    private InputStream inputStream;
    private OutputStream outputStream;

    private BufferedReader reader = null;

    private String str,string;

    //控制灯停止闪烁
    private boolean flag = false;

    public led Led = new led();

    //加载led库
    static
    {
        System.loadLibrary("led");
    }

    private Button led3_on;
    private Button led3_off;
    private Button led4_on;
    private Button led4_off;

    private Button twoLedOn;
    private Button twoLedOff;
    private Button twinkleOn;
    private Button twinkleOff;

    int label = 0;
    boolean stop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receivedData = (TextView)findViewById(R.id.receivedData);
        sendData = (EditText)findViewById(R.id.editData);

        new connectThread().start();

        Led.Open();

        led3_on = (Button)findViewById(R.id.button3);
        led3_off = (Button)findViewById(R.id.button4);
        led4_on = (Button)findViewById(R.id.button1);
        led4_off = (Button)findViewById(R.id.button2);

        twoLedOn = (Button)findViewById(R.id.buttonTwoOn);
        twoLedOff = (Button)findViewById(R.id.buttonTwoOff);
        twinkleOn = (Button)findViewById(R.id.buttonTwinkleOn);
        twinkleOff = (Button)findViewById(R.id.buttonTwinkleOff);

        led3_on.setOnClickListener(new manager());
        led3_off.setOnClickListener(new manager());
        led4_on.setOnClickListener(new manager());
        led4_off.setOnClickListener(new manager());

        twoLedOn.setOnClickListener(new manager());
        twoLedOff.setOnClickListener(new manager());
        twinkleOn.setOnClickListener(new manager());
        twinkleOff.setOnClickListener(new manager());
    }


    class manager implements View.OnClickListener
    {
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.button1:
                    label = 1;
                    sendMessage(v);
                    break;
                case R.id.button2:
                    label = 2;
                    sendMessage(v);
                    break;
                case R.id.button3:
                    label = 3;
                    sendMessage(v);
                    break;
                case R.id.button4:
                    label = 4;
                    sendMessage(v);
                    break;

                case R.id.buttonTwoOn:
                    label = 5;
                    sendMessage(v);
                    break;

                case R.id.buttonTwoOff:
                    label = 6;
                    sendMessage(v);
                    break;

                case R.id.buttonTwinkleOn:
                    label = 7;
                    sendMessage(v);
                    break;

                case R.id.buttonTwinkleOff:
                    label = 8;
                    sendMessage(v);
                    break;

                default:
                    break;
            }
        }
    }



    class delay extends Thread
    {
        @Override
        public void run()
        {
            super.run();
            try
            {
                while(true)
                {
                    flag = false;
                    Led.Ioctl(0, 1);
                    delay.sleep(1000);
                    Led.Ioctl(0, 0);
                    delay.sleep(1000);

                    if(string.equals("led4 twinkle off"))
                        flag = true;

                    if(flag)
                        break;
                }
            } catch(Exception e) {e.printStackTrace();}
        }
    }

    class connectThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                serverSocket = new ServerSocket(PORT);
                //死循环，持续等待客户端连接
                while(true)
                {
                    socket = serverSocket.accept();

                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    //启动子线程读取客户数据
                    new receivedThread().start();
                    Looper.prepare();
                    Looper.loop();
                }
            } catch(Exception e) {e.printStackTrace();}
        }
    }

    /**
     * 发送信息
     */
    public void sendMessage(View view)
    {
        switch(label)
        {
            case 0:
                //获取发送数据
                str = sendData.getText().toString() + "\n";
                break;
            case 1:
                //一定记得加\n，因为服务器按行读取
                str = "led4 on\n";
                label = 0;
                break;
            case 2:
                str = "led4 off\n";
                label = 0;
                break;
            case 3:
                str = "led3 on\n";
                label = 0;
                break;
            case 4:
                str = "led3 off\n";
                label = 0;
                break;
            case 5:
                str = "both on\n";
                label = 0;
                break;
            case 6:
                str = "both off\n";
                label = 0;
                break;
            case 7:
                str = "led4 twinkle on\n";
                label = 0;
                break;
            case 8:
                str = "led4 twinkle off\n";
                label = 0;
                break;
            default:
                break;
        }

        //发送信息也需要一个线程
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                try
                {
                    outputStream.write(str.getBytes());
                    outputStream.flush();
                } catch(Exception e) {e.printStackTrace();}
            }
        };
        thread.start();
    }


    class receivedThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                String data;

                long lastReceivedTime = System.currentTimeMillis();

                while(!stop)
                {
                    if(System.currentTimeMillis() - lastReceivedTime > 20000)
                    {
                        closeConnect();
                        new connectThread().start();
                        lastReceivedTime = System.currentTimeMillis();
                        //         System.out.println("close normal");
                    }

                    while((data = reader.readLine()) != null)
                    {
                        lastReceivedTime = System.currentTimeMillis();

                        //给主线程传递消息
                        Message msg = Message.obtain();

                        if(data.equals("heart beat"))
                        {
                            msg.what = 1;
                        }
                        else
                        {
                            msg.what = 0;
                        }

                        msg.obj = data;
                        handler.sendMessage(msg);
                    }
                }
            } catch(Exception e) {e.printStackTrace();}
        }
    }

    /**
     * 主界面更新
     */
    public Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                //收到客户信息
                case 0:
                    string = msg.obj.toString();
                    receivedData.append("Client: " + string + "\n");

                    switch(string)
                    {
                        case "led3 on":
                            Led.Ioctl(1, 1);
                            break;
                        case "led3 off":
                            Led.Ioctl(1, 0);
                            break;
                        case "led4 on":
                            Led.Ioctl(0, 1);
                            break;
                        case "led4 off":
                            Led.Ioctl(0, 0);
                            break;
                        case "both on":
                            Led.Ioctl(0,1);
                            Led.Ioctl(1,1);
                            break;
                        case "both off":
                            Led.Ioctl(0,0);
                            Led.Ioctl(1,0);
                            break;
                        case "led4 twinkle on":
                            new delay().start();
                            break;
                        case "led4 twinkle off":
                            Led.Ioctl(0,0);
                            break;
                        default:
                            break;
                    }

                //心跳消息，可以注释掉不处理
                case 1:
                    string = msg.obj.toString();
                    receivedData.append(string + "\n");
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 按下返回键back
     */
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        closeConnect();
        stop = true;
        Toast.makeText(MainActivity.this, "connect quit", Toast.LENGTH_SHORT).show();
    }

    /**
     * 关闭资源
     */
    public void closeConnect()
    {
        Led.Close();
        try
        {
            outputStream.close();
            inputStream.close();
            socket.close();
            serverSocket.close();
        } catch(Exception e) {e.printStackTrace();}
    }
}