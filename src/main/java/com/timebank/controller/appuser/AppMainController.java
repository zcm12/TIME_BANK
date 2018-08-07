package com.timebank.controller.appuser;


import com.timebank.appmodel.ResultModel;
import com.timebank.domain.Community;
import com.timebank.domain.Type;
import com.timebank.domain.Users;
import com.timebank.domain.UsersExample;
import com.timebank.mapper.CommunityMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import com.timebank.shiro.ShrioRegister;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Random;
import java.util.UUID;

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
    private CommunityMapper communityMapper;

    /*------------app api------------------------*/
    @RequestMapping(value = "/appLoginUser")
    @ResponseBody
    public ResultModel appLoginUser(Users users) {
        String userName = users.getUserAccount();
        String password = users.getUserPassword();
        Subject subject = SecurityUtils.getSubject();
//        System.out.println(subject);
        if (true) {
            //收集实体凭证信息  也就是常说的用户密码信息
            UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
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
        return new ResultModel(4, "登录成功");
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
        users.setUserRole("USE");//设置用户角色
        users.setUserSalt(salt);//设置密码盐
        users.setUserTypeAccountStatus("22222222-94e3-4eb7-aad3-111111111111");//设置用户状态为 账号正常

        int insert = usersMapper.insertSelective(users);
        System.out.println("注册insert="+insert);
        return new ResultModel(insert, "注册成功");
    }

}