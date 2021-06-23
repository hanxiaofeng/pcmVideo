package com.wangkeke.pcmvideotest;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
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

public class SecordActivity extends AppCompatActivity {

    private EditText etTest;
    private Button btnTest;

    static int db = -4;

    //注意：pow里必须强转为double类型，因为两个int类型做除法，结果还是int类型，会损失精度，此处把db强转为
    //double，double/int最后的结果是个double，保证了精度
    private double factor = Math.pow(10, (double)db / 20);

    byte[] byteYuan;
    byte[] byteYuanNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secord_main);

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

    private short getShort(byte[] data, int start)
    {
        return (short)((data[start] & 0xFF) | (data[start+1] << 8));
    }

    //调节PCM数据音量
    public int amplifyPCMData(byte[] pData, int nLen, byte[] data2, int nBitsPerSample, float multiple)
    {
        int nCur = 0;
        if (16 == nBitsPerSample)
        {
            while (nCur < nLen)
            {
                short volum = getShort(pData, nCur);

                volum = (short)(volum * multiple);

                data2[nCur]   = (byte)( volum       & 0xFF);
                data2[nCur+1] = (byte)((volum >> 8) & 0xFF);
                nCur += 2;
            }

        }
        return 0;
    }

    private void startTrans() {

        String testValue = etTest.getText().toString();

        final int test = Integer.parseInt(testValue);
        db = test;
        Log.e("wangkeke", "db = " + db);
        factor = Math.pow(10, (double)db / 20);
        new Thread(new Runnable() {
            @Override
            public void run() {
                byteYuanNew = new byte[byteYuan.length];
                amplifyPCMData(byteYuan,byteYuan.length,byteYuanNew,16,(float) factor);
                byte2file("newdata19.pcm", byteYuanNew);
            }
        }).start();
    }

    private File getYuanFile() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/test/input.pcm");
        return dir;
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
