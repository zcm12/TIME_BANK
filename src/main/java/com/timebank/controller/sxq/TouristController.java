package com.timebank.controller.sxq;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import freemarker.template.Template;
import org.apache.commons.collections.map.HashedMap;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
//import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private JavaMailSender mailSender;
//    @Autowired
//    private VelocityEngine velocityEngine;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

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
    private Users GetCurrentUsers(String message){
        Users users=null;
        UsersExample usersExample=new UsersExample();
        String em = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        String ph = "^[1][34578]\\d{9}$";
        if(message.matches(ph)){
            usersExample.or().andUserPhoneEqualTo(message);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            users = usersList.get(0);

        }else if( message.matches(em)){
            usersExample.or().andUserMailEqualTo(message);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            users = usersList.get(0);
        } else {
            usersExample.or().andUserAccountEqualTo(message);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            users = usersList.get(0);
        }
        return users;
    }
//    //修改游客信息
//   @RequestMapping(value="/modifyTouristInformationView")
//    public String TouristInformationView(Model model){
//       Subject account = SecurityUtils.getSubject();
//       String message=(String) account.getPrincipal();
//       Users users=GetCurrentUsers(message);
//       String role=users.getUserRole();
//       model.addAttribute("role",role);
//       model.addAttribute("users",users);
//       //所属小区
//       CommunityExample communityExample = new CommunityExample();
//       List<Community> communities = communityMapper.selectByExample(communityExample);
//       model.addAttribute("communities",communities);
//       //加载性别
//       TypeExample typeExample = new TypeExample();
//       typeExample.or().andTypeGroupIdEqualTo(1);
//       List<Type> types = typeMapper.selectByExample(typeExample);
//       model.addAttribute("types",types);
//
//        return "updateTouristInformation";
//    }
//    //查看游客信息
//    @RequestMapping(value="/touristInformationView")
//    public String touristInformationView(Model model){
//        Subject account = SecurityUtils.getSubject();
//        String message=(String) account.getPrincipal();
//        Users users2=GetCurrentUsers(message);
//        String role=users2.getUserRole();
//        model.addAttribute("role",role);
//        if (users2.getUserTypeGuidGender()!= null)
//        {
//            //处理性别
//            Type type = typeMapper.selectByPrimaryKey(users2.getUserTypeGuidGender());
//            users2.setUserTypeGuidGender(type.getTypeTitle());
//        }
//        if (users2.getUserCommGuid()!=null)
//        {
//            //所属小区
//            Community community = communityMapper.selectByPrimaryKey(users2.getUserCommGuid());
//            users2.setUserCommGuid(community.getCommTitle());
//        }
//        model.addAttribute("users",users2);
//        return "touristInformationView";
//    }
//    //保存按钮
//    @RequestMapping(value = "/updateTouristSubmit")
//    public String updateREQESTSave(Users users, Model model){
//        Subject account = SecurityUtils.getSubject();
//        String message=(String) account.getPrincipal();
//        Users users1=GetCurrentUsers(message);
//        String role=users1.getUserRole();
//        model.addAttribute("role",role);
//        usersMapper.updateByPrimaryKeySelective(users);
//        Users users2 = usersMapper.selectByPrimaryKey(users.getUserGuid());
//
//        //处理性别
//        Type type = typeMapper.selectByPrimaryKey(users2.getUserTypeGuidGender());
//        users2.setUserTypeGuidGender(type.getTypeTitle());
//        /*//用户持有时间
//        users.setUserOwnCurrency(users1.getUserOwnCurrency());*/
//        //用户状态
//        Type type1 = typeMapper.selectByPrimaryKey(users2.getUserTypeAccountStatus());
//        users2.setUserTypeAccountStatus(type1.getTypeTitle());
//        //所属小区
//        Community community = communityMapper.selectByPrimaryKey(users2.getUserCommGuid());
//        users2.setUserCommGuid(community.getCommTitle());
//        model.addAttribute("users",users2);
//
//        return "touristInformationView";
//    }
    /***************************小区管理员审核游客功能区**************************************/
    //游客列表界面
    @RequestMapping(value = "/createUserRole/{num}")
    public String createUserRole(Model model,@PathVariable int num){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);
        model.addAttribute("num",num);
        return "touristList";
    }
    //游客列表界面获取数据
    @RequestMapping(value="/getTouristListJsonData")
    @ResponseBody
    public String getTourstListJsonData(Model model, int offset, int limit, String sortName, String sortOrder,int num){
        //将管理员的角色添加到model中
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);
        String commguid=users.getUserCommGuid();
        //遍历数据库用户表格 得到所有的角色为Tourist的用户 根据num判断具体列表
        UsersExample usersExample1=new UsersExample();

        if(num==1) {
            usersExample1.or().andUserRoleEqualTo("Tourist");
        }else if(num==2){
            usersExample1.or().andUserRoleEqualTo("USE");
        }else{
            usersExample1.or().andUserRoleEqualTo("Tourist");
        }


        List<Users> users3=usersMapper.selectByExample(usersExample1);
        List<Users> users2=new ArrayList<>();

        for(Users i:users3){
            //判断是否为一个小区
            if(i.getUserCommGuid().equals(commguid)) {
                //判断是否为游客
                if (i.getUserRole().equals("Tourist")) {
                    //数字为3 身份证号不为空   添加至待审核
                    if(num==3&&i.getUserPhone()!=null&&i.getUserMail()!=null){
                        users2.add(i);
                    }else if(num==1){
                        users2.add(i);
                    }
                }else if(i.getUserRole().equals("USE")) {
                    users2.add(i);
                }
            }
        }
        //处理排序信息
        if (sortName != null) {
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            usersExample1.setOrderByClause(order);
        }
        List<Users> usersList = new ArrayList<Users>();
        //遍历，处理其中的一些字段
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
//        Subject account = SecurityUtils.getSubject();
//        String message=(String) account.getPrincipal();
//        Users users=GetCurrentUsers(message);
//        String role=users.getUserRole();
//        model.addAttribute("role",role);
//
//        UsersExample usersExample1=new UsersExample();
//        usersExample1.or().andUserGuidEqualTo(userGuid);
//        List<Users> users1=usersMapper.selectByExample(usersExample1);
//        Users use=users1.get(0);
//
//
//
//        String gender=use.getUserTypeGuidGender();
//        TypeExample typeExample=new TypeExample();
//        typeExample.or().andTypeGuidEqualTo(gender);
//        List<Type> types=typeMapper.selectByExample(typeExample);
//        use.setUserTypeGuidGender(types.get(0).getTypeTitle());
//
//        String comm=use.getUserCommGuid();
//        CommunityExample communityExample=new CommunityExample();
//        communityExample.or().andCommGuidEqualTo(comm);
//        List<Community> communities=communityMapper.selectByExample(communityExample);
//        use.setUserCommGuid(communities.get(0).getCommTitle());
//
//        String status=use.getUserTypeAccountStatus();
//        typeExample.clear();
//        typeExample.or().andTypeGuidEqualTo(status);
//        List<Type> types1=typeMapper.selectByExample(typeExample);
//        use.setUserTypeAccountStatus(types1.get(0).getTypeTitle());
//
//        model.addAttribute("users",use);
//        return "userInformation";
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);


        UsersExample usersExample1=new UsersExample();
        usersExample1.or().andUserGuidEqualTo(userGuid);
        List<Users> users12=usersMapper.selectByExample(usersExample1);
        Users users1=users12.get(0);
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
//        String status=use.getUserTypeAccountStatus();
//        typeExample.clear();
//        typeExample.or().andTypeGuidEqualTo(status);
//        List<Type> types1=typeMapper.selectByExample(typeExample);
//        use.setUserTypeAccountStatus(types1.get(0).getTypeTitle());

//        String A="/img/qie.jpg";
        String A=users1.getUserIdimageZ();
        String B=users1.getUserIdimageZ();
//        String A1=A.replaceAll(,)
//        A.replaceAll("\\\\","\\");
        model.addAttribute("message1",A);
        System.out.println(A);
        model.addAttribute("message2",B);

        model.addAttribute("users",users1);

        return "createUserRole";
    }
    //保存按钮 通过审核
//    @RequestMapping(value = "/passSubmit")
//    public String updateTouristInformationSubmit(Model model,Users users){
//        Subject account = SecurityUtils.getSubject();
//        String message=(String) account.getPrincipal();
//        Users users1=GetCurrentUsers(message);
//        String role=users1.getUserRole();
//        model.addAttribute("role",role);
//        usersMapper.updateByPrimaryKeySelective(users);
//        return "touristList";
//    }
    //添加自动发送邮件  通过按钮
    @RequestMapping(value = "/passSubmit")
    public String updateTouristInformationSubmit (Model model1,Users users) throws Exception{
        Subject account = SecurityUtils.getSubject();
        String message1=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message1);
        String role=users1.getUserRole();
        model1.addAttribute("role",role);
        usersMapper.updateByPrimaryKeySelective(users);
        Users users2=usersMapper.selectByPrimaryKey(users.getUserGuid());
       String mail=users2.getUserMail();
       String name=users2.getUserName();

        //模板邮件
        MimeMessage message = null;
        try {
            message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("18765924730@163.com");
            helper.setTo(mail);
            helper.setSubject("主题：时间银行审核邮件");

            Map<String, Object> model = new HashedMap();
            model.put("usename", name);
            if(users.getUserRole().equals("USE")) {
                //修改 application.properties 文件中的读取路径
                FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
                configurer.setTemplateLoaderPath("classpath:templates");
                //读取 html 模板
                Template template = freeMarkerConfigurer.getConfiguration().getTemplate("mail.html");
                String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                helper.setText(html, true);
                model1.addAttribute("num",2);
                mailSender.send(message);
            }else{
                SimpleMailMessage message3 = new SimpleMailMessage();
                message3.setFrom("18765924730@163.com");
                message3.setTo(mail);
                message3.setSubject("主题：时间银行审核邮件");
                message3.setText("您好，您的审核未通过,请你按照提示正确填写信息");
                model1.addAttribute("num",1);
                mailSender.send(message3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            return "touristList";
        }catch (Exception e){
        return "fail";}
//
//        SimpleMailMessage message1 = new SimpleMailMessage();
//        if(users.getUserRole().equals("USE")) {
//
//            message1.setFrom("18765924730@163.com");
//            message1.setTo("977758778@qq.com");
////            message1.setTo(mail);
//            message1.setSubject("主题：时间银行审核邮件");
////            message1.setText("您好，您已经通过审核，请登录“http://localhoast:8080”查看");
//            model.addAttribute("num",2);
//        }else{
//            message1.setFrom("18765924730@163.com");
//            message1.setTo("977758778@qq.com");
////            message1.setTo(mail);
//            message1.setSubject("主题：时间银行审核邮件");
////            message1.setText("您好，您的审核未通过,请你按照提示正确填写信息");
//            model.addAttribute("num",1);
//        }
//        try {
//
//            return "touristList";
//        }catch(Exception e){
//            return "fail";
//        }

    }
    /***************************平台管理员管理员审核游客功能区**************************************/
    //用户游客列表
    @RequestMapping(value = "/createAllUserRole")
    public String createAllUserRole(Model model){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);
        return "usersListByAdmin";
    }
    //列表界面向后台获取数据
    @RequestMapping(value="/getAllUsersListJsonData")
    @ResponseBody
    public String getAllUsersListJsonData(Model model, int offset, int limit, String sortName, String sortOrder){
        //将管理员的角色添加到model中
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
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
            //处理小区 暂时不能放开 整合到最后在放开 否则出错-----活动 名字转换
            String comm=it.getUserCommGuid();
            CommunityExample communityExample=new CommunityExample();
            communityExample.or().andCommGuidEqualTo(comm);
            List<Community> communities=communityMapper.selectByExample(communityExample);
            it.setUserCommGuid(communities.get(0).getCommTitle());
//            //处理状态
            String userStatus = it.getUserTypeAccountStatus();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(userStatus);
            List<Type> userstatus = typeMapper.selectByExample(typeExample);
            it.setUserTypeAccountStatus(userstatus.get(0).getTypeTitle());


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
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
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
        return "createUserRoleByAdmin";
    }
    //保存按钮
    @RequestMapping(value = "/passUserSubmit")
    public String passUserSubmit(Model model,Users users) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        usersMapper.updateByPrimaryKeySelective(users);
        return "usersListByAdmin";
    }

}
