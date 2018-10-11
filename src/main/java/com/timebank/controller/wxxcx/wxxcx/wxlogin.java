package com.timebank.controller.wxxcx.wxxcx;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;



@Controller
public class wxlogin {


    private ShiroHttpServletRequest request;

    //登录按钮
    @RequestMapping(value = "/wxgetopenid")
    @ResponseBody
    public void getopenid(String code){


//        String ID= request.getSession().getId();
//        System.out.println(ID);

        String appid="wxfce3f0bc5a908ac4";
        String secret="6b547b9f97c90e4058917dd5ce76a627";


        String requestUrl="https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+secret+"&js_code="+code+"&grant_type=authorization_code"+"connect_redirect=1";
        String  returnvalue=GET(requestUrl);
        System.out.println(requestUrl);//打印发起请求的urlF
        System.out.println(returnvalue);//打印调用GET方法返回值



    }



    //发起get请求的方法。
    public  String GET(String url) {
        String result = "";
        BufferedReader in = null;
        InputStream is = null;
        InputStreamReader isr = null;
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.connect();
            Map<String, List<String>> map = conn.getHeaderFields();
            is = conn.getInputStream();
            isr = new InputStreamReader(is);
            in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("异常："+e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (is != null) {
                    is.close();
                }
                if (isr != null) {
                    isr.close();
                }
            } catch (Exception e2) {
                System.out.println("异常："+e2);
                // 异常记录
            }
        }
        return result;
    }


}
