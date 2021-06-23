package com.wangkeke.pcmvideotest;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    private EditText etTest;
    private Button btnTest;

    byte[] byteYuan;
    short[] byteShortYuan;

    short[] byteNewShortData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTest = findViewById(R.id.et_test);
        btnTest = findViewById(R.id.btn_reWrite);

        byteYuan = fileConvertToByteArray(getYuanFile());

        Log.e("wangkeke", "byteYuan.length = " + byteYuan.length);

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrans();
            }
        });
    }

    private void startTrans() {

        String testValue = etTest.getText().toString();

        final int test = Integer.parseInt(testValue);
        Log.e("wangkeke", "test = " + test);
        new Thread(new Runnable() {
            @Override
            public void run() {
                byteShortYuan = toShortArray(byteYuan);
                byteNewShortData = new short[byteShortYuan.length];
                for (int i = 0; i < byteShortYuan.length; i++) {
                    byteNewShortData[i] = controlVoice(byteShortYuan[i], test);
                }
                Log.e("wangkeke", "音频字节音量减小成功 = " + byteNewShortData.length);
                byte2file("newdata.pcm", toByteArray(byteNewShortData));

            }
        }).start();
    }

    public static short[] toShortArray(byte[] src) {

        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) (src[i * 2] << 8 | src[2 * i + 1] & 0xff);
        }
        return dest;
    }

    public byte[] toByteArray(short[] src) {

        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2 + 1] = (byte) (src[i] >> 8);
            dest[i * 2] = (byte) (src[i] >> 0);
        }

        return dest;
    }


    private File getYuanFile() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/test/input.pcm");
        return dir;
    }


    public short controlVoice(short data, int per) {
        return (short) formatDoubleTOInt((data * Math.pow(10, per / 20)),100.0);
    }

    public static int formatDoubleTOInt(double dou1, double dou2) {
        BigDecimal big1 = new BigDecimal(Double.valueOf(dou1)).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal big2 = new BigDecimal(Double.valueOf(dou2));
        return big1.multiply(big2).intValue();

    }


    /**
     * 把一个文件转化为byte字节数组。
     *
     * @return
     */
    private byte[] fileConvertToByteArray(File file) {
        byte[] data = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }

            data = baos.toByteArray();

            fis.close();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


    public static void byte2file(String path, byte[] data) {
        Log.e("wangkeke", "------开始写入文件");
        BufferedOutputStream bos = null;

        File file = null;
        try {
            File dir = new File(Environment.getExternalStorageDirectory() + "/test/");
            if (!dir.exists()) {//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(Environment.getExternalStorageDirectory() + "/test/" + path);
            /* 使用以下2行代码时，不追加方式*/
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(data);

            /* 使用以下3行代码时，追加方式*/
            /*bos = new BufferedOutputStream(new FileOutputStream(file, true));
            bos.write(data);*/
            bos.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                    Log.e("wangkeke", "------写入成功");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }


}
