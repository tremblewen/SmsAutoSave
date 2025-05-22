package com.example.smsautosave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsReceiver extends BroadcastReceiver {
    private static final String SMS_LOG_FILE = "sms_log.txt"; // 存储短信的文件名

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // 解析短信（支持分段短信合并）
                Object[] pdus = (Object[]) bundle.get("pdus");
                assert pdus != null;
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String content = smsMessage.getMessageBody();
                    long timestamp = smsMessage.getTimestampMillis();

                    // 格式化时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String date = sdf.format(new Date(timestamp));

                    // 拼接短信日志
                    String smsLog = "------------------------\n" +
                                    "时间: " + date + "\n" +
                                    "发件人: " + sender + "\n" +
                                    "内容: " + content + "\n";

                    // 保存到文件（外部存储的应用私有目录）
                    saveToFile(context, smsLog);
                    Log.d("SMSAutoSave", "已保存短信: " + sender);
                }
            }
        }
    }

    private void saveToFile(Context context, String content) {
        try {
            // 获取外部文件目录（无需申请存储权限，Android 10+ 适用）
            File file = new File(context.getExternalFilesDir(null), SMS_LOG_FILE);
            FileWriter fileWriter = new FileWriter(file, true); // 追加模式
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e("SMSAutoSave", "保存文件失败: " + e.getMessage());
        }
    }
}
