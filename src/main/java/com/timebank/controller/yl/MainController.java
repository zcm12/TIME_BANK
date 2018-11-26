package com.timebank.controller.yl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ResultModel;
import com.timebank.domain.*;
import com.timebank.mapper.CommunityMapper;
import com.timebank.mapper.ResetMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import com.timebank.shiro.ShrioRegister;
//import org.apache.catalina.User;
import freemarker.template.Template;
import org.apache.commons.collections.map.HashedMap;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.attoparser.dom.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.xml.validation.Validator;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.UUID.randomUUID;

/**
 * 登录功能
 * 注册功能
 */
@Controller
public class MainController {

    @Autowired
    ShrioRegister shrioRegister;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private ResetMapper ResetMapper;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    //登录界面
    @RequestMapping(value = "/")
    public String index() {
        return "index1";
    }


    @RequestMapping(value = "/index1")
    public String index1() {
        return "index1";
    }

    @RequestMapping(value = "/css/login")
    public String inde() {
        return "index";
    }

    @RequestMapping(value = "/css/register")
    public String regist() {

        return "register";
    }


    /**
     * 用户的登录功能
     */

    //登录按钮
    @RequestMapping(value = "/loginUser")
    public String loginUser(Users users, Model model) {
        try {
            String userName = users.getUserAccount();
            System.out.println(userName);
            String password = users.getUserPassword(); //数据库中的密码
            Subject subject = SecurityUtils.getSubject();
            if (true) {
                UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
                token.setRememberMe(true);
                try {
                    subject.login(token);//提交认证信息
                    System.out.println();

                } catch (UnknownAccountException uae) {
                    System.out.println("账户不存在!");
                    return "fail";
                } catch (IncorrectCredentialsException ice) {
                    System.out.println("密码不正确!");
                    return "fail";
                } catch (LockedAccountException ae) {
                    System.out.println("账户被禁了!");
                    return "fail";
                } catch (AuthenticationException lae) {
                    System.out.println("认证错误");
                    return "fail";
                }
                //用户
                if (subject.hasRole("USE") || subject.hasRole("Tourist")) {
                    model.addAttribute("role", "USE");
                    //处理当前用户的个人信息
                    UsersExample usersExample11 = new UsersExample();
                    Users users2 = null;
                    String em = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
                    String ph = "^[1][34578]\\d{9}$";
                    if (userName.matches(ph)) {
                        usersExample11.or().andUserPhoneEqualTo(userName);
                        List<Users> usersList = usersMapper.selectByExample(usersExample11);
                        users2 = usersList.get(0);

                    }
                    if (userName.matches(em)) {
                        usersExample11.or().andUserMailEqualTo(userName);
                        List<Users> usersList = usersMapper.selectByExample(usersExample11);
                        users2 = usersList.get(0);
                    } else {
                        usersExample11.or().andUserAccountEqualTo(userName);
                        List<Users> usersList = usersMapper.selectByExample(usersExample11);
                        users2 = usersList.get(0);
                    }
                    String role = users2.getUserRole();
                    model.addAttribute("role", role);
                    if (users2.getUserTypeGuidGender() != null) {
                        //处理性别
                        Type type = typeMapper.selectByPrimaryKey(users2.getUserTypeGuidGender());
                        users2.setUserTypeGuidGender(type.getTypeTitle());
                    }
                    if (users2.getUserTypeAccountStatus() != null) {
                        //用户状态
                        Type type1 = typeMapper.selectByPrimaryKey(users2.getUserTypeAccountStatus());
                        users2.setUserTypeAccountStatus(type1.getTypeTitle());
                    }
                    if (users2.getUserCommGuid() != null) {
                        //所属小区
                        Community community = communityMapper.selectByPrimaryKey(users2.getUserCommGuid());
                        users2.setUserCommGuid(community.getCommTitle());
                    }
                    model.addAttribute("guid", users2.getUserGuid());
                    model.addAttribute("users", users2);
                    return "startmap1";
                }
                //小区管理员
                if (subject.hasRole("ADMIN")) {
                    model.addAttribute("role", "ADMIN");
                    return "listRequestByAdminView";
                }
                //总台管理员
                if (subject.hasRole("MADMIN")) {
                    model.addAttribute("role", "MADMIN");
                    return "activitylist";
                }
            }
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    //注册按钮 跳往注册界面
    @RequestMapping(value = "/register")
    public String resestUser(Model model) {
        try {
            CommunityExample communityExample = new CommunityExample();
            List<Community> communities = communityMapper.selectByExample(communityExample);
            model.addAttribute("communities", communities);
            //加载性别
            TypeExample typeExample = new TypeExample();
            typeExample.or().andTypeGroupIdEqualTo(1);
            List<Type> types = typeMapper.selectByExample(typeExample);
            model.addAttribute("types", types);
            return "register";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    //submit按钮 在注册界面中  跳往登录界面
    @RequestMapping("/registerUser")
    public String register(Users users) {

        try {
            CommunityExample communityExample = new CommunityExample();
            communityExample.or().andCommTitleEqualTo(users.getUserCommGuid());
            List<Community> communityList = communityMapper.selectByExample(communityExample);
            System.out.println(communityList.get(0).getCommGuid() + communityList.get(0).getCommTitle());
            users.setUserCommGuid(communityList.get(0).getCommGuid());
            //用户的密码加密和插入到数据库
            shrioRegister.register(users);
            //进行更新  将用户状态置为正常
            Users uu = usersMapper.selectByPrimaryKey(users.getUserGuid());
            uu.setUserOwnCurrency(0d);
            uu.setUserTypeAccountStatus("22222222-94e3-4eb7-aad3-111111111111");
            uu.setUserRole("Tourist");
            usersMapper.updateByPrimaryKeySelective(uu);
            return "index";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    //注册界面中的协议地址
    @RequestMapping(value = "/agreementAdress")
    public String agreementAdres() {
        return "agreement";
    }

    //账号名重名校验
    @RequestMapping(value = "/jquery/exist.do")
    @ResponseBody
    public String checkUserAccount(String userAccount) {
        try {
            //遍历数据库 查找是否有账号
            UsersExample usersExample = new UsersExample();
            List<Users> users = usersMapper.selectByExample(usersExample);
            boolean result = true;
            Map<String, Boolean> map = new HashMap<>();
            for (Users it : users) {
                if (it.getUserAccount().equals(userAccount)) {
                    result = false;
                }
            }
            map.put("valid", result);
            ObjectMapper mapper = new ObjectMapper();
            String resultString = "";
            try {
                //将对象转换成json数组  这里是将map<>对象转换成json
                resultString = mapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return resultString;
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }


    /*******************************************TODO:找回密码******************************************/
    //index界面忘记密码按钮
    @RequestMapping(value = "/jquery/forget")
    public String forgetPassword() {
        return "forgetPassword";
    }

    //forgetPassword界面的校验    账号名与邮箱是否匹配
    @RequestMapping(value = "/jquery/checkexist")
    @ResponseBody
    public String forgetCheckUserAccount(String Account, String Email) {
        //遍历数据库 查找是否有账号
        try {
            UsersExample usersExample = new UsersExample();
            List<Users> users = usersMapper.selectByExample(usersExample);
            boolean result = false;
            Map<String, Boolean> map = new HashMap<>();
            for (Users it : users) {
                if (it.getUserAccount().equals(Account) && it.getUserMail().equals(Email)) {
                    result = true;
                }
            }
            map.put("valid", result);
            ObjectMapper mapper = new ObjectMapper();
            String resultString = "";
            try {
                //将对象转换成json数组  这里是将map<>对象转换成json
                resultString = mapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return resultString;
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    //forgetPassword界面中的保存按钮
    @RequestMapping(value = "/jquery/forgetPassword")
    public String checkAccount(String userAccount, String userEmail, HttpServletRequest reqest) throws UnsupportedEncodingException {
        //生成sid的值
        try {
            UsersExample usersExample = new UsersExample();
            usersExample.or().andUserAccountEqualTo(userAccount);
            List<Users> users = usersMapper.selectByExample(usersExample);
            Users users1 = users.get(0);
            int flag = new Random().nextInt(999999);
            if (flag < 100000) {
                flag += 100000;
            }
            String salt = String.valueOf(flag);
            SimpleHash sid = new SimpleHash("MD5", users1.getUserPassword(), salt, 1000);
            //将证据插入到数据库 方便修改密码连接的校验
            //生成过期时间
            Long time = System.currentTimeMillis();//获得系统当前时间的毫秒数
            time += 30 * 1000 * 60;//在当前系统时间的基础上往后加30分钟
            Date date = new Date(time);
            System.out.println(date);
            //判断account是否已经存在reset表单中 若存在直接更新
            Boolean biaoshi = true;
            ResetExample resetExample = new ResetExample();
            resetExample.clear();
            List<Reset> resets = ResetMapper.selectByExample(resetExample);
            for (Reset it : resets) {
                if (it.getResetAccount().equals(userAccount)) {
                    it.setResetOuttime(date);
                    it.setResetSid(sid.toString());
                    ResetMapper.updateByPrimaryKey(it);
                    biaoshi = false;
                }
            }
            //判断account不存在reset表单中 直接插入
            if (biaoshi) {
                Reset reset = new Reset();
                UUID guid = randomUUID();
                reset.setResetGuid(guid.toString());
                reset.setResetAccount(userAccount);
                reset.setResetSid(sid.toString());
                reset.setResetOuttime(date);
                ResetMapper.insert(reset);
            }
            //生成url链接  url的拼接
            String nowUrl = reqest.getRequestURI();
            //base 编码
            String key = userAccount;
            byte[] bt = key.getBytes("UTF-8");
            String newKey = (new BASE64Encoder()).encodeBuffer(bt);
            String url = "www.chinesetimebank.org.cn" + nowUrl + "reset" + "?" + "sid=" + sid + "&&" + "userAcount" + "=" + newKey;
            //发送邮件
            String mail = users1.getUserMail();
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
                //读取 html 模板
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
            //邮件发送成功跳转到的界面
            return "forgetSuccess";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    //邮箱里面的地址的链接的校验
    @RequestMapping(value = "/jquery/forgetPasswordreset")
    public String checkAccount(HttpServletRequest reqest, Model model) {
        try {
            String sid = reqest.getParameter("sid");
            //base  解码
            String newkey = reqest.getParameter("userAcount");
            byte[] bt1 = new byte[0];
            try {
                bt1 = (new BASE64Decoder()).decodeBuffer(newkey);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String newkey1 = "";
            try {
                newkey1 = new String(bt1, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ResetExample resetExample = new ResetExample();
            resetExample.or().andResetAccountEqualTo(newkey1);
            List<Reset> resetList = ResetMapper.selectByExample(resetExample);

            for (Reset it : resetList) {
                if (it.getResetSid().equals(sid)) {
                    Date date = it.getResetOuttime();
                    Date date1 = new Date();
                    if (date.before(date1)) {
                        System.out.println("时间过期");
                        return "fail";
                    } else {
                        System.out.println("符合修改密码所有条件");
                        model.addAttribute("userAccount", newkey1);
                        return "forgetReset";
                    }
                } else {
                    System.out.println("sid值错误");
                    return "fail";
                }
            }
            return "fail";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    //forgetReset.html界面进行密码重置
    @RequestMapping(value = "/jquery/forgetResrt")
    public String forgetResrt(String userAccount1, String userPassword) {
        try{
        //重置密码   并且进行加密
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo(userAccount1);
        List<Users> usersList = usersMapper.selectByExample(usersExample);
        Users user = usersList.get(0);
        String salt = user.getUserSalt();
        SimpleHash newPassword = new SimpleHash("MD5", userPassword, salt, 1000);
        user.setUserPassword(newPassword.toHex());
        usersMapper.updateByPrimaryKey(user);
        return "index";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

}