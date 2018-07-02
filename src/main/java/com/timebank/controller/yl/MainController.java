package com.timebank.controller.yl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ResultModel;
import com.timebank.domain.*;
import com.timebank.mapper.CommunityMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import com.timebank.shiro.ShrioRegister;
import org.apache.catalina.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.attoparser.dom.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 登录功能啊啊
 * 注册功能
 * */
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


    //登录界面
    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }

    //登录按钮
    @RequestMapping(value = "/loginUser")
    public String loginUser(Users users, Model model) {
        //当前输入的用户名和密码
        String userName = users.getUserAccount();
        String password = users.getUserPassword();

        Subject subject = SecurityUtils.getSubject();
//        System.out.println(subject);//org.apache.shiro.web.subject.support.WebDelegatingSubject@5658fbfe
//        System.out.println(subject.getPrincipal());//数据库里面账号名
        if (true) {
            //收集实体(界面输入的)凭证信息  也就是常说的用户密码信息 令牌
            UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
            //功能：记住我
            token.setRememberMe(true);
            //提交认证信息 认证处理
            try {
                subject.login(token);//提交认证信息

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
            //根据角色不同  跳往不同界面
            if (subject.hasRole("Tourist")) {
                model.addAttribute("role", "Tourist");
                UsersExample usersExample = new UsersExample();
                usersExample.or().andUserAccountEqualTo(userName);
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
                model.addAttribute("users", users2);
                return "TouristInformationview";
            }
            //用户
            if (subject.hasRole("USE")) {
                model.addAttribute("role", "USE");
                UsersExample usersExample = new UsersExample();
                usersExample.or().andUserAccountEqualTo(userName);
                List<Users> users1 = usersMapper.selectByExample(usersExample);
                Users users2 = users1.get(0);
                //加载性别
                TypeExample typeExample = new TypeExample();
                typeExample.or().andTypeGroupIdEqualTo(1);
                List<Type> types = typeMapper.selectByExample(typeExample);
                model.addAttribute("types", types);
                //所属小区
                CommunityExample communityExample = new CommunityExample();
                List<Community> communities = communityMapper.selectByExample(communityExample);
                model.addAttribute("communities", communities);
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
                model.addAttribute("users", users2);
                return "updateUserInformation";
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

    }

    //注册按钮 跳往注册界面
    @RequestMapping(value = "/register")
    public String resestUser(Model model) {
       /* //所属小区
        System.out.println("点击登录界面注册按钮");

        CommunityExample communityExample = new CommunityExample();
        List<Community> communities = communityMapper.selectByExample(communityExample);
        model.addAttribute("communities", communities);
       //加载手机号
        UsersExample usersExample=new UsersExample();
        List<Users> users=usersMapper.selectByExample(usersExample);
        model.addAttribute("users",users);
*/
        //加载性别
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGroupIdEqualTo(1);
        List<Type> types = typeMapper.selectByExample(typeExample);
        model.addAttribute("types", types);
        return "register";
    }

    //submit按钮 在注册界面中  跳往登录界面
    @RequestMapping("/registerUser")
    public String register(Users users) {
        System.out.println(11111);
        //加载手机号
        UUID guig=UUID.randomUUID();
        users.setUserGuid(guig.toString());
        usersMapper.insertSelective(users);
//       System.out.println(users.getUserPhone());

      /*  //用户的密码加密和插入到数据库aaa
        shrioRegister.register1(users);
        //进行更新  将用户状态置为正常
        Users uu = usersMapper.selectByPrimaryKey(users.getUserGuid());
        uu.setUserOwnCurrency(0d);
        uu.setUserTypeAccountStatus("22222222-94e3-4eb7-aad3-111111111111");
        usersMapper.updateByPrimaryKeySelective(uu);*/
        return "index";
    }
    //用户名重名校验
  /*  @RequestMapping(value = "/jquery/exist2.do")
    @ResponseBody
    public String checkUserAccount1(String userName){
        System.out.println(11111);
        //遍历数据库 查找是否有账号
        UsersExample usersExample=new UsersExample();
        List<Users> users=usersMapper.selectByExample(usersExample);
        boolean result = true;
        Map<String, Boolean> map = new HashMap<>();
        for(Users it:users){
            if(it.getUserName()!=null&&it.getUserName().equals(userName)){
                result=false;
            }
        }
        System.out.println(userName);
        map.put("valid", result);
        ObjectMapper mapper = new ObjectMapper();
        String resultString = "";
        try {
            //将对象转换成json数组  这里是将map<>对象转换成json
            resultString = mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(resultString);
        return resultString;
    }
    //账号名重名校验
    @RequestMapping(value = "/jquery/exist.do")
    @ResponseBody
    public String checkUserAccount(String userAccount){
        System.out.println(2222);
        //遍历数据库 查找是否有账号
        UsersExample usersExample=new UsersExample();
        List<Users> users=usersMapper.selectByExample(usersExample);
        boolean result = true;
        Map<String, Boolean> map = new HashMap<>();
        for(Users it:users){
            if(it.getUserAccount().equals(userAccount)){
                result=false;
            }
        }
        System.out.println(userAccount);
        map.put("valid", result);
        ObjectMapper mapper = new ObjectMapper();
        String resultString = "";
        try {
            //将对象转换成json数组  这里是将map<>对象转换成json
            resultString = mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(resultString);
        return resultString;
    }*/
    /*------------app api------------------------*/
    /*@RequestMapping(value = "/appLoginUser")
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
            UsersExample usersExample1=new UsersExample();
            usersExample1.or().andUserGuidEqualTo("12345");
            List<Users> userList=usersMapper.selectByExample(usersExample1);
            users2.setUserCommGuid(community.getCommTitle());
        }
        System.out.println(users2.getUserTypeAccountStatus());
        return users2;
    }*/

}