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
    public ResultModel appLoginUser(Users users, Model model) {
        System.out.println(users);
        String userName = users.getUserAccount();
        System.out.println(userName);
        String password = users.getUserPassword();
        System.out.println(password);
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
    private   int hashIterations=10;
    @RequestMapping(value = "/appRegisterUser")
    @ResponseBody
    public ResultModel appRegisterUser(Users users){
        System.out.println("密码加密开始");
        //干扰数据 盐 防破解
        String salt = "";
        System.out.println("原始密码为：" + users.getUserPassword());//注册密码
        SimpleHash hash = new SimpleHash(algorithmName, users.getUserPassword(), salt, hashIterations);
        System.out.println("密码加密结束：" + hash);
        String encodedPassword = hash.toHex();
        users.setUserPassword(encodedPassword);
        UUID userGuid = randomUUID();
        users.setUserGuid(userGuid.toString());
        int insert = usersMapper.insertSelective(users);
        System.out.println("注册insert="+insert);
        return new ResultModel(insert, "注册成功");
    }
    @RequestMapping(value = "/appUserInfo")
    @ResponseBody
    public Users appUserInfo(Users users){
//        String userName = users.getUserAccount();
//        System.out.println(userName);
        Subject subject = SecurityUtils.getSubject();
        String userAccount = (String) subject.getPrincipal();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo(userAccount);
        List<Users> users1 = usersMapper.selectByExample(usersExample);
        Users users2 = users1.get(0);
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
        System.out.println(users2.getUserTypeAccountStatus());
        return users2;
    }
}