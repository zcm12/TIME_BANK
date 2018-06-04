package com.timebank.controller.sxq;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.ibatis.jdbc.Null;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
//游客修改信息
@Controller
public class TouristController {
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private ActpartMapper actpartMapper;
    /**
     * 排序函数
     */
    private String GetDatabaseFileName(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) >= 'A' && str.charAt(i) <= 'Z') {
                sb.append('_');
            }
            sb.append(str.charAt(i));
        }
        return sb.toString();
    }
    //修改游客信息
   @RequestMapping(value="/modifyTouristInformationView")
    public String TouristInformationView(Model model){
       System.out.println("修改用户信息");
       Subject account=SecurityUtils.getSubject();
       UsersExample usersExample = new UsersExample();
       usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
       List<Users> users = usersMapper.selectByExample(usersExample);
       Users users2 = users.get(0);
       String role = users2.getUserRole();
       model.addAttribute("role", role);
       model.addAttribute("users",users2);
       //所属小区
       CommunityExample communityExample = new CommunityExample();
       List<Community> communities = communityMapper.selectByExample(communityExample);
       model.addAttribute("communities",communities);
       //加载性别
       TypeExample typeExample = new TypeExample();
       typeExample.or().andTypeGroupIdEqualTo(1);
       List<Type> types = typeMapper.selectByExample(typeExample);
       model.addAttribute("types",types);

        return "updateTouristInformation";
    }
    //查看游客信息
    @RequestMapping(value="/touristInformationView")
    public String touristInformationView(Model model){
        System.out.println("查看用户信息");
        Subject account=SecurityUtils.getSubject();
        UsersExample usersExample=new UsersExample();
        usersExample.or().andUserAccountEqualTo((String)account.getPrincipal());
        List<Users> users=usersMapper.selectByExample(usersExample);
        Users users2=users.get(0);
        String role=users2.getUserRole();
        model.addAttribute("role",role);
        if (users2.getUserTypeGuidGender()!= null)
        {
            //处理性别
            Type type = typeMapper.selectByPrimaryKey(users2.getUserTypeGuidGender());
            users2.setUserTypeGuidGender(type.getTypeTitle());
        }
        if (users2.getUserCommGuid()!=null)
        {
            //所属小区
            Community community = communityMapper.selectByPrimaryKey(users2.getUserCommGuid());
            users2.setUserCommGuid(community.getCommTitle());
        }
        model.addAttribute("users",users2);
        return "touristInformationView";
    }
    //保存按钮
    @RequestMapping(value = "/updateTouristSubmit")
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
        /*//用户持有时间
        users.setUserOwnCurrency(users1.getUserOwnCurrency());*/
        //用户状态
        Type type1 = typeMapper.selectByPrimaryKey(users2.getUserTypeAccountStatus());
        users2.setUserTypeAccountStatus(type1.getTypeTitle());
        //所属小区
        Community community = communityMapper.selectByPrimaryKey(users2.getUserCommGuid());
        users2.setUserCommGuid(community.getCommTitle());
        model.addAttribute("users",users2);

        return "touristInformationView";
    }
    /***************************小区管理员审核游客功能区**************************************/
    //游客列表界面
    @RequestMapping(value = "/createUserRole")
    public String createUserRole(Model model){
        Subject subject=SecurityUtils.getSubject();
        UsersExample usersExample=new UsersExample();
        usersExample.or().andUserAccountEqualTo((String)subject.getPrincipal());
        List<Users> users=usersMapper.selectByExample(usersExample);
        Users users1=users.get(0);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        return "touristList";
    }
    //游客列表界面获取数据
    @RequestMapping(value="/getTouristListJsonData")
    @ResponseBody
    public String getTourstListJsonData(Model model, int offset, int limit, String sortName, String sortOrder){
        //将管理员的角色添加到model中
        Subject subject=SecurityUtils.getSubject();
        UsersExample usersExample=new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) subject.getPrincipal());
        List<Users> users=usersMapper.selectByExample(usersExample);
        Users users1=users.get(0);
        String role=users1.getUserRole();
        String commguid=users1.getUserCommGuid();
        model.addAttribute("role",role);
        //遍历数据库用户表格 得到所有的角色为Tourist的用户
        UsersExample usersExample1=new UsersExample();
        usersExample1.or().andUserRoleEqualTo("Tourist");
        List<Users> users3=usersMapper.selectByExample(usersExample1);
        List<Users> users2=new ArrayList<>();
        //判断是否为一个小区
        for(Users i:users3){
            if(i.getUserCommGuid().equals(commguid)){
                users2.add(i);
            }
        }
        //处理排序信息
        if (sortName != null) {
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            usersExample1.setOrderByClause(order);
        }
        List<Users> usersList = new ArrayList<Users>();
        //遍历所有的游客，处理其中的一些字段
        for (int i = offset; i < offset + limit && i < users2.size(); i++) {
            //处理性别
            Users it=users2.get(i);
            String gender=it.getUserTypeGuidGender();
            TypeExample typeExample=new TypeExample();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(gender);
            List<Type> title=typeMapper.selectByExample(typeExample);
            it.setUserTypeGuidGender(title.get(0).getTypeTitle());
            //处理状态
            String userStatus = it.getUserTypeAccountStatus();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(userStatus);
            List<Type> userstatus = typeMapper.selectByExample(typeExample);
            it.setUserTypeAccountStatus(userstatus.get(0).getTypeTitle());
            //处理小区
            String comm=it.getUserCommGuid();
            CommunityExample communityExample=new CommunityExample();
            communityExample.or().andCommGuidEqualTo(comm);
            List<Community> communities=communityMapper.selectByExample(communityExample);
            it.setUserCommGuid(communities.get(0).getCommTitle());
            usersList.add(it);
        }
        int total = users2.size();
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(usersList, total);
        try {
            String json2 = mapper.writeValueAsString(tableRecordsJson);
            return json2;
        } catch (Exception e) {
            return null;
        }
    }
    //查看详情界面   进行审核
    @RequestMapping(value="/Tourist/{userGuid}")
    public String getTourstListJsonData(Model model,@PathVariable String userGuid){
        System.out.println(userGuid);
        Subject subject=SecurityUtils.getSubject();
        UsersExample usersExample=new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) subject.getPrincipal());
        List<Users> users=usersMapper.selectByExample(usersExample);
        String role=users.get(0).getUserRole();
        model.addAttribute("role",role);

        UsersExample usersExample1=new UsersExample();
        usersExample1.or().andUserGuidEqualTo(userGuid);
        List<Users> users1=usersMapper.selectByExample(usersExample1);
        Users use=users1.get(0);



        String gender=use.getUserTypeGuidGender();
        TypeExample typeExample=new TypeExample();
        typeExample.or().andTypeGuidEqualTo(gender);
        List<Type> types=typeMapper.selectByExample(typeExample);
        use.setUserTypeGuidGender(types.get(0).getTypeTitle());

        String comm=use.getUserCommGuid();
        CommunityExample communityExample=new CommunityExample();
        communityExample.or().andCommGuidEqualTo(comm);
        List<Community> communities=communityMapper.selectByExample(communityExample);
        use.setUserCommGuid(communities.get(0).getCommTitle());

        String status=use.getUserTypeAccountStatus();
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(status);
        List<Type> types1=typeMapper.selectByExample(typeExample);
        use.setUserTypeAccountStatus(types1.get(0).getTypeTitle());

        model.addAttribute("users",use);
        return "createUserRole";
    }
    //保存按钮 通过审核
    @RequestMapping(value = "/passSubmit")
    public String updateTouristInformationSubmit(Model model,Users users){
        Subject subject=SecurityUtils.getSubject();
        UsersExample usersExample=new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) subject.getPrincipal());
        List<Users> users1=usersMapper.selectByExample(usersExample);
        String role=users1.get(0).getUserRole();
        model.addAttribute("role",role);
        usersMapper.updateByPrimaryKeySelective(users);
        return "touristList";
    }
    /***************************平台管理员管理员审核游客功能区**************************************/
    //用户游客列表
    @RequestMapping(value = "/createAllUserRole")
    public String createAllUserRole(Model model){
        Subject subject=SecurityUtils.getSubject();
        UsersExample usersExample=new UsersExample();
        usersExample.or().andUserAccountEqualTo((String)subject.getPrincipal());
        List<Users> users=usersMapper.selectByExample(usersExample);
        Users users1=users.get(0);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        return "usersListByAdmin";
    }
    //列表界面向后台获取数据
    @RequestMapping(value="/getAllUsersListJsonData")
    @ResponseBody
    public String getAllUsersListJsonData(Model model, int offset, int limit, String sortName, String sortOrder){
        //将管理员的角色添加到model中
        Subject subject=SecurityUtils.getSubject();
        UsersExample usersExample=new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) subject.getPrincipal());
        List<Users> users=usersMapper.selectByExample(usersExample);
        Users users1=users.get(0);
        String role=users1.getUserRole();
        String commguid=users1.getUserCommGuid();
        model.addAttribute("role",role);
        //遍历数据库用户表格 得到所有用户
        UsersExample usersExample1=new UsersExample();
        List<Users> users2=usersMapper.selectByExample(usersExample1);
        //处理排序信息
        if (sortName != null) {
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            usersExample1.setOrderByClause(order);
        }
        List<Users> usersList = new ArrayList<Users>();
        //遍历所有的游客，处理其中的一些字段
        for (int i = offset; i < offset + limit && i < users2.size(); i++) {
            //处理性别
            Users it=users2.get(i);
            String gender=it.getUserTypeGuidGender();
            TypeExample typeExample=new TypeExample();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(gender);
            List<Type> title=typeMapper.selectByExample(typeExample);
            it.setUserTypeGuidGender(title.get(0).getTypeTitle());
//            //处理状态
            String userStatus = it.getUserTypeAccountStatus();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(userStatus);
            List<Type> userstatus = typeMapper.selectByExample(typeExample);
            it.setUserTypeAccountStatus(userstatus.get(0).getTypeTitle());
            //处理小区 暂时不能放开 整合到最后在放开 否则出错-----活动 名字转换
//            String comm=it.getUserCommGuid();
//            CommunityExample communityExample=new CommunityExample();
//            communityExample.or().andCommGuidEqualTo(comm);
//            List<Community> communities=communityMapper.selectByExample(communityExample);
//            it.setUserCommGuid(communities.get(0).getCommTitle());
            usersList.add(it);
        }
        int total = users2.size();
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(usersList, total);
        try {
            String json2 = mapper.writeValueAsString(tableRecordsJson);
            return json2;
        } catch (Exception e) {
            return null;
        }
    }
    //查看详情安按钮
    @RequestMapping(value="/AllUser/{userGuid}")
    public String alluser(Model model,@PathVariable String userGuid){
        System.out.println(userGuid);
        Subject subject=SecurityUtils.getSubject();
        UsersExample usersExample=new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) subject.getPrincipal());
        List<Users> users=usersMapper.selectByExample(usersExample);
        String role=users.get(0).getUserRole();
        model.addAttribute("role",role);

        UsersExample usersExample1=new UsersExample();
        usersExample1.or().andUserGuidEqualTo(userGuid);
        List<Users> users1=usersMapper.selectByExample(usersExample1);
        Users use=users1.get(0);

        String role1=use.getUserRole();
        model.addAttribute("message",role1);
        System.out.println(role1);

        String gender=use.getUserTypeGuidGender();
        TypeExample typeExample=new TypeExample();
        typeExample.or().andTypeGuidEqualTo(gender);
        List<Type> types=typeMapper.selectByExample(typeExample);
        use.setUserTypeGuidGender(types.get(0).getTypeTitle());
        //处理小区 暂时不能放开 整合到最后在放开 否则出错-----活动 名字转换
//        String comm=use.getUserCommGuid();
//        CommunityExample communityExample=new CommunityExample();
//        communityExample.or().andCommGuidEqualTo(comm);
//        List<Community> communities=communityMapper.selectByExample(communityExample);
//        use.setUserCommGuid(communities.get(0).getCommTitle());

        String status=use.getUserTypeAccountStatus();
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(status);
        List<Type> types1=typeMapper.selectByExample(typeExample);
        use.setUserTypeAccountStatus(types1.get(0).getTypeTitle());

        model.addAttribute("users",use);
        return "createUserRoleByAdmin";
    }
    //保存按钮
    @RequestMapping(value = "/passUserSubmit")
    public String passUserSubmit(Model model,Users users) {
        Subject subject = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) subject.getPrincipal());
        List<Users> users1 = usersMapper.selectByExample(usersExample);
        String role = users1.get(0).getUserRole();
        model.addAttribute("role", role);
        usersMapper.updateByPrimaryKeySelective(users);
        return "usersListByAdmin";
    }

}
