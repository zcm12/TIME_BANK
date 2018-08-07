package com.timebank.controller.appuser;

import com.timebank.appmodel.ResultModel;
import com.timebank.domain.*;
import com.timebank.mapper.CommunityMapper;
import com.timebank.mapper.RoleMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

// 修改和查看个人信息
@Controller
public class AppUserController {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private CommunityMapper communityMapper;



    /*---------------app api------------------------*/
    //查看个人信息
    @RequestMapping(value = "/appUserInfo")
    @ResponseBody
    public Users appUserInfo(){
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
    @RequestMapping(value = "/appGetCom")
    @ResponseBody
    public List<Community> appGetCom() {
        Subject account = SecurityUtils.getSubject();
        System.out.println("account"+account);
        System.out.println("principal"+account.getPrincipal().toString());
        CommunityExample communityExample = new CommunityExample();
        List<Community> communities = communityMapper.selectByExample(communityExample);
        System.out.println("小区集合："+communities);
        return communities;
    }
    //更新用户信息
    @RequestMapping(value = "/appUpdateUserInfo")
    @ResponseBody
    public ResultModel appUpdateUserInfo(Users u) {



        Subject account = SecurityUtils.getSubject();
        String userAccount = (String) account.getPrincipal();
        UsersExample usersExample=new UsersExample();
        List<Users> usersList=usersMapper.selectByExample(usersExample);//所有用户
        usersExample.clear();
        usersExample.or().andUserAccountEqualTo(userAccount);
        List<Users> users1 = usersMapper.selectByExample(usersExample);
        Users user = users1.get(0);//当前用户
        //所有用户中删除当前用户
        for (int i = 0;i < usersList.size();i++) {
            if (usersList.get(i).getUserGuid().equals(user.getUserGuid())) {
                usersList.remove(i);
            }
        }

        //不包括当前用户的集合
        for (Users users : usersList) {
            //邮箱重名校验
            if (u.getUserMail() != null && u.getUserMail().equals(users.getUserMail())) {
                return new ResultModel(11, "该邮箱已使用，请更换");
            }
            //手机号重名校验
            if (u.getUserPhone() != null && u.getUserPhone().equals(users.getUserPhone())) {
                return new ResultModel(12, "该手机号已使用，请更换");
            }
        }


        String userTypeGuidGender = u.getUserTypeGuidGender();
        String userCommGuid = u.getUserCommGuid();

        //处理性别
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeTitleEqualTo(userTypeGuidGender);
        List<Type> types = typeMapper.selectByExample(typeExample);
        Type type = types.get(0);
        user.setUserTypeGuidGender(type.getTypeGuid());
        //处理小区
        CommunityExample communityExample = new CommunityExample();
        communityExample.or().andCommTitleEqualTo(userCommGuid);
        List<Community> communities = communityMapper.selectByExample(communityExample);
        Community community = communities.get(0);
        user.setUserCommGuid(community.getCommGuid());
        //处理其他
        user.setUserAddress(u.getUserAddress());
        user.setUserName(u.getUserName());
        user.setUserMail(u.getUserMail());
        user.setUserPhone(u.getUserPhone());
        user.setUserIdnum(u.getUserIdnum());
        user.setUserBirthdate(u.getUserBirthdate());
        user.setUserEmerperson(u.getUserEmerperson());
        user.setUserEmercontact(u.getUserEmercontact());
        user.setUserProvince(u.getUserProvince());
        user.setUserCity(u.getUserCity());
        user.setUserDistrict(u.getUserDistrict());
        user.setUserBirthdate(u.getUserBirthdate());

        //更新数据库
        int update = usersMapper.updateByPrimaryKey(user);
        return new ResultModel(update, "信息保存成功");

    }
}
