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
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
    public String getTourstListJsonData(Model model, int offset, int limit, String sortName, String sortOrder,int num, String searchText){
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
        /**10.11添加*/
        if (searchText == "") {
            searchText = null;
        }
        /**10.11添加*/

        //处理排序信息
        if (sortName != null) {
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            usersExample1.setOrderByClause(order);
        }
        List<Users> usersList = new ArrayList<Users>();
        //遍历，处理其中的一些字段
//        for (int i = offset; i < offset + limit && i < users2.size(); i++) {
        for (int i = 0;  i < users2.size(); i++) {
//            System.out.println("游客列表分页打桩");
//            System.out.println(offset);
//            System.out.println(limit);
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
//            usersList.add(it);
            /**10.11添加*/
            if (searchText != null) {
                String UserAccount = it.getUserAccount();
                if(!(UserAccount!=null)){
                    UserAccount="";
                }
                String UserName=it.getUserName();
                if(!(UserName!=null)){
                    UserName="";
                }
                String Address=it.getUserAddress();
                if(!(Address!=null)){
                    Address="";
                }
                if (UserAccount.contains(searchText) || UserName.contains(searchText) || Address.contains(searchText)) {
                    usersList.add(it);
                }
            } else {
                usersList.add(it);
            }
            /**10.11添加*/

        }

        /**10.11添加*/
        List<Users> usersReturn = new ArrayList<>();
        for (int i = offset;i<offset+limit&&i<usersList.size();i++){
            usersReturn.add(usersList.get(i));
        }
        /**10.11添加*/

//        int total = users2.size();
        int total=usersList.size();
        ObjectMapper mapper = new ObjectMapper();
//        TableRecordsJson tableRecordsJson = new TableRecordsJson(usersList, total);
        TableRecordsJson tableRecordsJson = new TableRecordsJson(usersReturn, total);
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
        }  //处理时间格式
        if (users1.getUserBirthdate()!=null)
        {
            java.util.Date d=new java.util.Date (users1.getUserBirthdate().getTime());
            model.addAttribute("date",d);
            SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd");
            f.format(d);
        }

        String A=users1.getUserIdimage();
        if(A!=null) {
            model.addAttribute("message1", A);
        }else{
            model.addAttribute("message1","/img/qie.jpg");
        }

        String role1=users1.getUserRole();
        model.addAttribute("role1",role1);

        model.addAttribute("users",users1);

        return "createUserRole";
    }
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
                //发送图片
                File file = ResourceUtils.getFile("classpath:static/img/wxgzh.jpg");
                helper.addInline("springcloud", file);
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
    public String getAllUsersListJsonData(Model model, int offset, int limit, String sortName, String sortOrder,String searchText){
        //将管理员的角色添加到model中
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);

        if (searchText == "") {
            searchText = null;
        }

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
//        for (int i = offset; i < offset + limit && i < users2.size(); i++) {
        for (int i = 0;  i < users2.size(); i++) {
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

//            usersList.add(it);
            /**10.12添加*/
            if (searchText != null) {
                String UserAccount = it.getUserAccount();
                if(!(UserAccount!=null)){
                    UserAccount="";
                }
                String UserName=it.getUserName();
                if(!(UserName!=null)){
                    UserName="";
                }
                String Address=it.getUserAddress();
                if(!(Address!=null)){
                    Address="";
                }
                if (UserAccount.contains(searchText) || UserName.contains(searchText) || Address.contains(searchText)) {
                    usersList.add(it);
                }
            } else {
                usersList.add(it);
            }
            /**10.12添加*/

        }
        /**10.12添加*/
        List<Users> usersReturn = new ArrayList<>();
        for (int i = offset;i<offset+limit&&i<usersList.size();i++){
            usersReturn.add(usersList.get(i));
        }
        /**10.12添加*/


        int total=usersList.size();
//        int total = users2.size();
        ObjectMapper mapper = new ObjectMapper();
//        TableRecordsJson tableRecordsJson = new TableRecordsJson(usersList, total);
        TableRecordsJson tableRecordsJson = new TableRecordsJson(usersReturn, total);
        try {
            String json2 = mapper.writeValueAsString(tableRecordsJson);
            return json2;
        } catch (Exception e) {
            return null;
        }
    }
    //查看详情按钮
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



        //读取图片位置路径
        String A=use.getUserIdimage();
        if(A!=null) {
            model.addAttribute("message1", A);
        }else{
            model.addAttribute("message1", "/img/qie.jpg");
        }
        //处理时间格式
        if (use.getUserBirthdate()!=null)
        {
            java.util.Date d=new java.util.Date (use.getUserBirthdate().getTime());
            model.addAttribute("date",d);
            SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd");
            f.format(d);
        }
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
