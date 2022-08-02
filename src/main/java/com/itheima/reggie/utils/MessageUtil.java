package com.itheima.reggie.utils;
//import cn.hutool.json.JSONException;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import java.io.IOException;

public class MessageUtil {
    public static void senMessage(String phoneNumber,String MessageCode) throws HTTPException, IOException {
        // 短信应用SDK AppID  --- 对应二、准备的5
        int appid = 1400713013; // 1400开头
        // 短信应用SDK AppKey  --- 对应二、准备的5
        String appkey = "147ebe0d47526c549bf9fbfc56990c7c";
        // 短信模板 ID，需要在短信应用中申请  --- 对应二、准备的6
        int templateId = 1487114; // NOTE: 这里的模板ID`7839`只是一个示例，真实的模板ID需要在短信控制台中申请
        // 签名   --- 对应二、准备的7
        String smsSign = "御坂美琴大大呱"; // NOTE: 这里的签名"腾讯云"只是一个示例，真实的签名需要在短信控制台中申请，另外签名参数使用的是`签名内容`，而不是`签名ID`
        /**
         * 腾讯云发送短信验证码
         * @param phoneNumber 需要发送给哪个手机号码
         *
         */
        // 我们随机生成四位随机数
//        String verificationCode = String.valueOf(ValidateCodeUtils.generateValidateCode(4));
        // 数组具体的元素个数和模板中变量个数必须一致  我的模板中需要填写验证码和有效时间,{1},{2}
        String[] params = {MessageCode , "5"};
        SmsSingleSender singleSender = new SmsSingleSender(appid, appkey);
        SmsSingleSenderResult result = singleSender.sendWithParam("86", phoneNumber, templateId, params, smsSign, "", "");
    }
}

