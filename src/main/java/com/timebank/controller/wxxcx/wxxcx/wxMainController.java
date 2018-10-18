package com.timebank.controller.wxxcx.wxxcx;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ResultModel;
import com.timebank.domain.*;
import com.timebank.mapper.CommunityMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import com.timebank.shiro.ShrioRegister;
//import org.apache.catalina.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.attoparser.dom.Text;
import org.json.JSONStringer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import javax.xml.validation.Validator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;

/**
 * 登录功能
 * 注册功能
 * */
@Controller
public class wxMainController {

    @Autowired
    ShrioRegister shrioRegister;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private CommunityMapper communityMapper;
    //登录按钮
    @RequestMapping(value = "/wxlogin")
    @ResponseBody
    public String loginUser(Users u) {
            String userName = u.getUserAccount();
            String password = u.getUserPassword();
            Subject subject = SecurityUtils.getSubject();
          if (true) {
            UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
            token.setRememberMe(true);
              System.out.println(token);
            try {
                subject.login(token);//提交认证信息
                System.out.println("登录成功");
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
        }

        return "sucess";
    }
}
