package com.example.dell.server5;

/**
 * Created by Dell on 2019/5/12.
 */
public class led
{
    public native int Open();
    public native int Close();
    public native int Ioctl(int num, int en);
}
