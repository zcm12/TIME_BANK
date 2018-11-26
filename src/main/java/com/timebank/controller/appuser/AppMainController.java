package com.timebank.controller.appuser;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ResultModel;
import com.timebank.domain.*;
import com.timebank.mapper.CommunityMapper;
import com.timebank.mapper.ResetMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import com.timebank.shiro.ShrioRegister;
import freemarker.template.Template;
import org.apache.commons.collections.map.HashedMap;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import sun.misc.BASE64Encoder;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.util.UUID.randomUUID;

/**
 * 登录功能
 * 注册功能
 * */
@Controller
public class AppMainController {

    @Autowired
    ShrioRegister shrioRegister;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private ResetMapper ResetMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    /*------------app api------------------------*/
    @RequestMapping(value = "/appLoginUser")
    @ResponseBody
    public ResultModel appLoginUser(Users users) {
        String userName = users.getUserAccount();
        UsersExample usersExample=new UsersExample();
        String userAccount=null;
        String userRole="Tourist";
        String em = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        String ph = "^[1][34578]\\d{9}$";
        if(userName.matches(ph)){
            usersExample.or().andUserPhoneEqualTo(userName);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            userAccount = usersList.get(0).getUserAccount();
            userRole = usersList.get(0).getUserRole();
        }else if( userName.matches(em)){
            usersExample.or().andUserMailEqualTo(userName);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            userAccount = usersList.get(0).getUserAccount();
            userRole = usersList.get(0).getUserRole();
        } else {
            usersExample.or().andUserAccountEqualTo(userName);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            userAccount = usersList.get(0).getUserAccount();
            userRole = usersList.get(0).getUserRole();
        }
        String password = users.getUserPassword();
        Subject subject = SecurityUtils.getSubject();
//        System.out.println(subject);
        if (true) {
            //收集实体凭证信息  也就是常说的用户密码信息
            UsernamePasswordToken token = new UsernamePasswordToken(userAccount, password);
            token.setRememberMe(true);
            //提交认证信息 认证处理
            try {
                subject.login(token);//提交认证信息

            } catch (UnknownAccountException uae) {
                System.out.println("账户不存在!");
                return new ResultModel(0, "账户不存在");
            } catch (IncorrectCredentialsException ice) {
                System.out.println("密码不正确!");
                return new ResultModel(1, "密码不正确");
            } catch (LockedAccountException ae) {
                System.out.println("账户被禁了!");
                return new ResultModel(2, "账户被禁了");
            } catch (AuthenticationException lae) {
                System.out.println("认证错误");
                return new ResultModel(3, "认证错误");
            }
        }
        return new ResultModel(4, userRole);
    }
    //散列算法类型为MD5
    private    String algorithmName ="MD5";
    //hash的次数
    private   int hashIterations=1000;
    @RequestMapping(value = "/appRegisterUser")
    @ResponseBody
    public ResultModel appRegisterUser(Users users){

        //校验账号名是否存在
        String userAccount = users.getUserAccount();
        UsersExample usersExample=new UsersExample();
        List<Users> usersList=usersMapper.selectByExample(usersExample);
        for (Users u : usersList) {
            if (userAccount != null && userAccount.equals(u.getUserAccount())) {
                return new ResultModel(11, "账号名已存在，请重新申请");
            }
        }
        System.out.println("密码加密开始");
        //干扰数据 盐 防破解
        int flag= new Random().nextInt(999999);
        if (flag < 100000)
        {
            flag += 100000;
        }
        String salt=String.valueOf(flag);
        System.out.println("原始密码为：" + users.getUserPassword());//注册密码
        System.out.println("盐值为:"+salt);
        SimpleHash hash = new SimpleHash(algorithmName, users.getUserPassword(), salt, hashIterations);
        System.out.println("密码加密结束：" + hash);
        String encodedPassword = hash.toHex();
        users.setUserPassword(encodedPassword);//设置用户加密后密码
        UUID userGuid = randomUUID();
        users.setUserGuid(userGuid.toString());//设置用户guid
        users.setUserRole("Tourist");//设置用户角色
        users.setUserSalt(salt);//设置密码盐
        users.setUserTypeAccountStatus("22222222-94e3-4eb7-aad3-111111111111");//设置用户状态为 账号正常

        int insert = usersMapper.insertSelective(users);
        System.out.println("注册insert="+insert);
        return new ResultModel(insert, "注册成功");
    }

    @RequestMapping(value = "/appForgetPassword")
    @ResponseBody
    public ResultModel appForgetPassword(Users users,HttpServletRequest reqest){
        String userAccount = users.getUserAccount();
        String userMail = users.getUserMail();
        System.out.println(userAccount + userMail);
        //遍历数据库，查找是否有账号
        UsersExample usersExample = new UsersExample();
        List<Users> users2 = usersMapper.selectByExample(usersExample);
        for (Users it : users2){

            if (it.getUserAccount().equals(userAccount)&&it.getUserMail().equals(userMail)){
                int flag= new Random().nextInt(999999);
                if (flag < 100000)
                {
                    flag += 100000;
                }
                String salt=String.valueOf(flag);
                SimpleHash sid = new SimpleHash("MD5", it.getUserPassword(), salt, 1000);
                //将证据插入到数据库 方便修改密码连接的校验
                //生成过期时间
                Long time = System.currentTimeMillis();//获得系统当前时间的毫秒数
                time +=30*1000*60;//在当前系统时间的基础上往后加30分钟
                Date date=new Date(time);
                System.out.println(date);
                //判断account是否已经存在reset表单中 若存在直接更新
                Boolean biaoshi=true;
                ResetExample resetExample=new ResetExample();
                resetExample.clear();
                List<Reset> resets= ResetMapper.selectByExample(resetExample);
                for(Reset RE:resets){
                    if(RE.getResetAccount().equals(userAccount)){
                        RE.setResetOuttime(date);
                        RE.setResetSid(sid.toString());
                        ResetMapper.updateByPrimaryKey(RE);
                        biaoshi=false;
                    }
                }
                //判断account不存在reset表单中 直接插入
                if(biaoshi) {
                    Reset reset = new Reset();
                    UUID guid = randomUUID();
                    reset.setResetGuid(guid.toString());
                    reset.setResetAccount(userAccount);
                    reset.setResetSid(sid.toString());
                    reset.setResetOuttime(date);
                    ResetMapper.insert(reset);
                }
                //生成url链接  url的拼接
//                String nowUrl=reqest.getRequestURI();
//                String nowUrl=reqest.getRequestURI();
                //base 编码
                String key=userAccount;
                byte[] bt = new byte[0];
                try {
                    bt = key.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String newKey=(new BASE64Encoder()).encodeBuffer(bt);
//                String url="www.chinesetimebank.org.cn"+nowUrl+"reset"+"?"+"sid="+sid+"&&"+"userAcount"+"="+newKey;
                String url="http://192.168.1.142:8080"+"/jquery/forgetPassword"+"reset"+"?"+"sid="+sid+"&&"+"userAcount"+"="+newKey;

                System.out.println(it.getUserAccount());
                System.out.println(it.getUserMail());
                String mail=it.getUserMail();
                MimeMessage message = null;
                try {
                    message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true);
                    helper.setFrom("18765924730@163.com");
                    helper.setTo(mail);
                    helper.setSubject("主题：时间银行密码找回邮件");
                    Map<String, Object> model = new HashedMap();
                    model.put("usename", url);
                    FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
                    configurer.setTemplateLoaderPath("classpath:templates");
                    //读取 html 模板                                                                    mailResetPassword.html
                    Template template = freeMarkerConfigurer.getConfiguration().getTemplate("mailResetPassword.html");
                    String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                    helper.setText(html, true);
                    //发送图片
                    File file = ResourceUtils.getFile("classpath:static/img/wxgzh.jpg");
                    helper.addInline("springcloud", file);
                    mailSender.send(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new ResultModel(0,"用户名和邮箱匹配！");
            }else {
//                return new ResultModel(1,"用户名和邮箱不匹配！");
            }
        }
//        usersExample.or().andUserAccountEqualTo(userAccount);
//        List<Users> usersList = usersMapper.selectByExample(usersExample);
//        Users users1 = usersList.get(0);
//        System.out.println("密码加密开始");
//        //拿出盐值
//        String salt = users1.getUserSalt();
//        System.out.println("原始密码为：" + userPassword);//注册密码
//        System.out.println("盐值为:"+salt);
//        SimpleHash hash = new SimpleHash(algorithmName, userPassword, salt, hashIterations);
//        System.out.println("密码加密结束：" + hash);
//        String encodedPassword = hash.toHex();
//        users1.setUserPassword(encodedPassword);//设置用户加密后密码
//        usersMapper.updateByPrimaryKey(users1);

        return new ResultModel(6,"邮件发送成功");
    }
   /* @RequestMapping(value = "/appUpdateCurrentAddr")
    @ResponseBody
    public int appUpdateCurrentAddr(Users users){
        String userCurrentaddr = users.getUserCurrentaddr();

        Subject subject = SecurityUtils.getSubject();
        String userAccount = (String) subject.getPrincipal();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo(userAccount);
        List<Users> users1 = usersMapper.selectByExample(usersExample);
        Users users2 = users1.get(0);
        users2.setUserCurrentaddr(userCurrentaddr);
        int update = usersMapper.updateByPrimaryKeySelective(users2);
        System.out.println("=========="+update);
        return update;
    }*/
}