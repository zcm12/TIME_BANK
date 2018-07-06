package com.timebank.controller.appuser;

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

    //查看个人信息
    /*@RequestMapping(value = "/userInformationView")
    public String userInformationView(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);

        if (users1.getUserTypeGuidGender()!= null)
        {
            //处理性别
            Type type = typeMapper.selectByPrimaryKey(users1.getUserTypeGuidGender());
            users1.setUserTypeGuidGender(type.getTypeTitle());
        }
        if (users1.getUserOwnCurrency()!=null)
        {
            //用户持有时间
            users1.setUserOwnCurrency(users1.getUserOwnCurrency());
        }
        if(users1.getUserTypeAccountStatus()!=null)
        {
            //用户状态
            Type type1 = typeMapper.selectByPrimaryKey(users1.getUserTypeAccountStatus());
            users1.setUserTypeAccountStatus(type1.getTypeTitle());
        }
        if (users1.getUserCommGuid()!=null)
        {
            //所属小区
            Community community = communityMapper.selectByPrimaryKey(users1.getUserCommGuid());
            users1.setUserCommGuid(community.getCommTitle());
        }

        model.addAttribute("users",users1);

        return "userInformation";
    }

    //修改个人信息
    @RequestMapping(value = "/modifyUserInformationView")
    public String modifyUserInformationView(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);

        //所属小区
        CommunityExample communityExample = new CommunityExample();
        List<Community> communities = communityMapper.selectByExample(communityExample);
        model.addAttribute("communities",communities);
        model.addAttribute("users",users1);
        //加载性别
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGroupIdEqualTo(1);
        List<Type> types = typeMapper.selectByExample(typeExample);
        model.addAttribute("types",types);
        return "updateUserInformation";
    }
    //用户个人信息更新提交
    @RequestMapping(value = "/updateUserInformationSubmit")
    public String updateREQESTSave(Users users, Model model){
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users0 = usersMapper.selectByExample(usersExample);
        Users users1 = users0.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        usersMapper.updateByPrimaryKeySelective(users);
        Users users2 = usersMapper.selectByPrimaryKey(users.getUserGuid());

        //处理性别
        Type type = typeMapper.selectByPrimaryKey(users2.getUserTypeGuidGender());
        users2.setUserTypeGuidGender(type.getTypeTitle());
//        //用户状态
//        Type type1 = typeMapper.selectByPrimaryKey(users2.getUserTypeAccountStatus());
//        users2.setUserTypeAccountStatus(type1.getTypeTitle());
        //所属小区
        Community community = communityMapper.selectByPrimaryKey(users2.getUserCommGuid());
        users2.setUserCommGuid(community.getCommTitle());
        model.addAttribute("users",users2);

        return "userInformation";
    }*/

    /*---------------app api------------------------*/
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
