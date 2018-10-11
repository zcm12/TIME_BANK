package com.timebank.controller.wxxcx.wxxcx;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ResultModel;
import com.timebank.controller.yl.TableRecordsJson;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;



// 修改和查看个人信息
@Controller
public class wxUserController {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private CommunityMapper communityMapper;

    //修改头像
    @ResponseBody
    @RequestMapping(value = "/wximageUpload")
    public Object imageUpload(HttpServletRequest request, HttpServletResponse response,Users users) throws IllegalStateException, IOException {
         System.out.println(users.getUserAccount());
        MultipartHttpServletRequest req =(MultipartHttpServletRequest)request;
        MultipartFile multipartFile =  req.getFile("file");

        System.out.println( multipartFile );

        String realPath = "F:/image";
        try {
            File dir = new File(realPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file  =  new File(realPath,"aaa.jpg");
            System.out.println(file.toString());
            multipartFile.transferTo(file);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }


    //修改查看个人信息
    @RequestMapping(value = "/wxUserInfo")
    @ResponseBody
    public Users appUserInfo(Users users) {
//        Subject subject = SecurityUtils.getSubject();
//        String userAccount = (String) subject.getPrincipal();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo(userAccount);
//        List<Users> users1 = usersMapper.selectByExample(usersExample);
//        Users users2 = users1.get(0);

        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(users.getUserAccount());
        List<Users> users1 = usersMapper.selectByExample(usersExample1);
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


    //更新用户信息
    @RequestMapping(value = "/wxUpdateUserInfo")
    @ResponseBody
    public void appUpdateUserInfo(Users u) {
        Subject account = SecurityUtils.getSubject();
        String userAccount = (String) account.getPrincipal();
        System.out.println("account"+account);
        System.out.println("userAccount"+userAccount);
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo(userAccount);
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users user = users.get(0);

        String userTypeGuidGender = u.getUserTypeGuidGender();
        System.out.println("userTypeGuidGender"+userTypeGuidGender);
        String userAddress = u.getUserAddress();
        System.out.println("userAddress"+userAddress);
        String userCommGuid = u.getUserCommGuid();
        System.out.println("userCommGuid"+userCommGuid);

        //处理性别
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeTitleEqualTo(userTypeGuidGender);
        List<Type> types = typeMapper.selectByExample(typeExample);
        Type type = types.get(0);
        user.setUserTypeGuidGender(type.getTypeGuid());
        System.out.println("userGender"+user.getUserTypeGuidGender());
        //处理小区
        CommunityExample communityExample = new CommunityExample();
        communityExample.or().andCommTitleEqualTo(userCommGuid);
        List<Community> communities = communityMapper.selectByExample(communityExample);
        Community community = communities.get(0);
        user.setUserCommGuid(community.getCommGuid());
        //处理地址
        user.setUserAddress(userAddress);
        //更新数据库
        usersMapper.updateByPrimaryKey(user);
    }
}


