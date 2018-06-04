package com.timebank.controller.sxq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;


@Controller
public class YluserController {

    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private ReqestMapper reqestMapper;

    @RequestMapping(value = "/getUSERSListJsonData")
    @ResponseBody
    public String userList(Model model, @RequestParam int offset, int limit, String sortName, String sortOrder){

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role",role100);


        UsersExample usersExample = new UsersExample();
        RoleExample roleExample = new RoleExample();
        TypeExample typeExample = new TypeExample();
        CommunityExample communityExample = new CommunityExample();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            usersExample.setOrderByClause(order);
        }
        List<Users> users = usersMapper.selectByExample(usersExample);
        List<Users> usersList = new ArrayList<Users>();

        for(int i=offset;i<offset+limit&&i<users.size();i++){
            Users user = users.get(i);

            String userRole = user.getUserFromRoleGuid();
            roleExample.clear();
            roleExample.or().andRoleGuidEqualTo(userRole);
            List<Role> userrole1 = roleMapper.selectByExample(roleExample);
            user.setUserFromRoleGuid(userrole1.get(0).getRoleTitle());

            String userGender = user.getUserTypeGuidGender();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(userGender);
            List<Type> gender = typeMapper.selectByExample(typeExample);
            user.setUserTypeGuidGender(gender.get(0).getTypeTitle());

            String userStatus = user.getUserTypeAccountStatus();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(userStatus);
            List<Type> userstatus = typeMapper.selectByExample(typeExample);
            user.setUserTypeAccountStatus(userstatus.get(0).getTypeTitle());

            String userCommunicity = user.getUserCommGuid();
            communityExample.clear();
            communityExample.or().andCommGuidEqualTo(userCommunicity);
            List<Community> usercommunicity = communityMapper.selectByExample(communityExample);
            user.setUserCommGuid(usercommunicity.get(0).getCommTitle());

            usersList.add(user);
        }
//全部符合要求的数据的数量
        int total=users.size();
        System.out.println("总数："+total);
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(usersList,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
             System.out.println(json1);
            return json1;
        }catch (Exception e){
            return null;
        }
    }
    private String GetDatabaseFileName(String str)
    {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<str.length();++i)
        {
            if(str.charAt(i)>='A'&&str.charAt(i)<='Z')
            {
                sb.append('_');
            }
            sb.append(str.charAt(i));
        }
        return sb.toString();
    }

    @RequestMapping(value = "volunteerchoose")
    public String volchoose(Model model){

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role",role100);

        return "volunteerchoose";
    }

    @RequestMapping(value = "/volunteer/{userid}")
    public String volunteerdetail(Model model, @PathVariable String userid){

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role",role100);


        Users users = usersMapper.selectByPrimaryKey(userid);
        model.addAttribute("users",users);

       TypeExample typeExample = new TypeExample();
       UsersExample usersExample = new UsersExample();
       RoleExample roleExample = new RoleExample();
//        CommunityExample communityExample = new CommunityExample();

        roleExample.clear();
        roleExample.or().andRoleGuidEqualTo(users.getUserFromRoleGuid());
        List<Role> userrole = roleMapper.selectByExample(roleExample);
        users.setUserFromRoleGuid(userrole.get(0).getRoleTitle());

        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(users.getUserTypeGuidGender());
        List<Type> usergender = typeMapper.selectByExample(typeExample);
        users.setUserTypeGuidGender(usergender.get(0).getTypeTitle());

        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(users.getUserTypeAccountStatus());
        List<Type> userstatus = typeMapper.selectByExample(typeExample);
        users.setUserTypeAccountStatus(userstatus.get(0).getTypeTitle());
//
//        List<Role> roles = roleMapper.selectByExample(roleExample);
//        model.addAttribute("roles",roles);
//
//        typeExample.clear();
//        typeExample.or().andTypeGroupIdEqualTo(1);
//        List<Type> type1 = typeMapper.selectByExample(typeExample);
//        model.addAttribute("type1",type1);
//
//        typeExample.clear();
//        typeExample.or().andTypeGroupIdEqualTo(2);
//        List<Type> type2 = typeMapper.selectByExample(typeExample);
//        model.addAttribute("type2",type2);

        return "volunteerdetail";
    }


    @RequestMapping(value = "/setId")
    public String setData(Reqest reqest, String reqTargetsUserGuid, Model model,String reqGuid){

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role",role100);

        //后台接收前台的文本框内容
        System.out.print("文本框内容："+reqGuid);
        System.out.print("文本框内容："+reqTargetsUserGuid);

        //先设置一个给数据库，一开始内容为null，而数据库中不能为null
        //reqest.setReqTargetsUserGuid(reqest.getReqTargetsUserGuid());

        Reqest  request=reqestMapper.selectByPrimaryKey(reqGuid);
//        //后台把内容保存到数据库字段里
        request.setReqTargetsUserGuid(reqTargetsUserGuid);
        reqestMapper.updateByPrimaryKeySelective(request);
        //跳转到checkProjectView.html页面
        return "reqlistprocess";
    }
}
