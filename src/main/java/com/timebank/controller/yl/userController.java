package com.timebank.controller.yl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

// 修改和查看个人信息
@Controller
public class userController {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private RespondMapper respondMapper;
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

        UsersExample usersExample=new UsersExample();

        String em = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        String ph = "^[1][34578]\\d{9}$";
        Users users=null;
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
    //查看个人信息
    @RequestMapping(value = "/userInformationView")
    public String userInformationView(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
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
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
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
    //修改用户个人信息界面中保存按钮
    @RequestMapping(value = "/updateUserInformationSubmit")
    public String updateREQESTSave(@ModelAttribute @Valid Users users, Model model){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);

        usersMapper.updateByPrimaryKeySelective(users);

        //从数据库中获取前台提交的字段
        String GUID=users.getUserGuid();
        UsersExample usersExample1=new UsersExample();
        usersExample1.or().andUserGuidEqualTo(GUID);
        List<Users> usersList=usersMapper.selectByExample(usersExample1);
        Users users3=usersList.get(0);
        System.out.println("哈哈哈"+users3.getUserBirthdate());
        if (users3.getUserTypeGuidGender()!= null)
        {
            //处理性别
            Type type = typeMapper.selectByPrimaryKey(users3.getUserTypeGuidGender());
            users3.setUserTypeGuidGender(type.getTypeTitle());
        }
        if (users3.getUserOwnCurrency()!=null)
        {
            //用户持有时间
            users3.setUserOwnCurrency(users3.getUserOwnCurrency());
        }
        if (users3.getUserBirthdate()!=null){
            //出生日期
            String dateString = users.getUserBirthdate().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
            TimeZone tz = TimeZone.getTimeZone("GMT+8");
            sdf.setTimeZone(tz);
            String str = sdf.format(Calendar.getInstance().getTime());
            System.out.println(str);
            Date s;
            try {
                s = sdf.parse(dateString);
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                System.out.println(sdf.format(s));
                users3.setUserBirthdate(sdf.format(s));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        if(users3.getUserTypeAccountStatus()!=null)
        {
            //用户状态

            Type type1 = typeMapper.selectByPrimaryKey(users3.getUserTypeAccountStatus());
            users3.setUserTypeAccountStatus(type1.getTypeTitle());
        }
        if (users3.getUserCommGuid()!=null)
        {
            //所属小区
            Community community = communityMapper.selectByPrimaryKey(users3.getUserCommGuid());
            users3.setUserCommGuid(community.getCommTitle());
        }

        model.addAttribute("users",users3);


        return "userInformation";
    }
    //邮箱重名校验
    @RequestMapping(value = "/jquery/exist4.do")
    @ResponseBody
    public String checkUserEmail(String userMail){
        UsersExample usersExample=new UsersExample();
        List<Users> users=usersMapper.selectByExample(usersExample);
        boolean result = true;
        Map<String, Boolean> map = new HashMap<>();
        for(Users it:users){
            if(it.getUserMail()!=null&&it.getUserMail().equals(userMail)){
                result=false;
            }
        }
        System.out.println(userMail);
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
    //手机号重名校验
    @RequestMapping(value = "/jquery/exist3.do")
    @ResponseBody
    public String checkUserPhone(String userPhone){
        UsersExample usersExample=new UsersExample();
        List<Users> users=usersMapper.selectByExample(usersExample);
        boolean result = true;
        Map<String, Boolean> map = new HashMap<>();
        for(Users it:users){
            if(it.getUserPhone()!=null&&it.getUserPhone().equals(userPhone)){
                result=false;
            }
        }
        System.out.println(userPhone);
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
    /**
     * 名字转换函数
     */
    public String IdtoName(String reqtargetuser) {
        if (reqtargetuser != null && !reqtargetuser.isEmpty()) {
            UsersExample usersExample = new UsersExample();
            StringBuilder sb = new StringBuilder();
            //去掉[]
            String r = reqtargetuser.replaceAll("^.*\\[", "").replaceAll("].*", "");
            //分割字符串
            String[] array = r.split(",");
            String respondName = null;
            for (int in = 0; in < array.length; in++) {
                //去掉空格
                String jj = array[in].replaceAll(" ", "");
                usersExample.clear();
                usersExample.or().andUserGuidEqualTo(jj);
                List<Users> responduser = usersMapper.selectByExample(usersExample);
                respondName = responduser.get(0).getUserName();
                sb.append(respondName).append(" ");
            }
            String sb1 = sb.toString();
            return sb1;
        }

        return null;
    }
    //导航栏查看已完成但未评价的活动
    //导航栏查看活动
    @RequestMapping(value = "/scoreForVolunteer")
    public String scoreForVolunteer(Model model) {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        return "scoreForVolunteer";
    }


    //查看服务列表后向后台数据索要数据
    @RequestMapping(value="/getVolunteerScoreListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getRESPONDListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder){

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
//        model.addAttribute("role",role);
        RespondExample respondExample=new RespondExample();
        respondExample.clear();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            respondExample.setOrderByClause(order);
        }
        //判断自己响应过哪些请求
        String ownId = users1.getUserGuid();
        respondExample.or().andResUserGuidEqualTo(ownId);
        List<Respond> responds=respondMapper.selectByExample(respondExample);
        List<Respond> respondRecordList=new ArrayList<>();
        for(int i=offset;i< offset+limit&&i < responds.size();i++){
            Respond respond1=responds.get(i);
            TypeExample typeExample = new TypeExample();
            if (respond1.getResTypeGuidProcessStatus()!=null) {
                String resTypeGuidProcessStatus = respond1.getResTypeGuidProcessStatus();
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(resTypeGuidProcessStatus);
                List<Type> types = typeMapper.selectByExample(typeExample);
                respond1.setResTypeGuidProcessStatus(types.get(0).getTypeTitle());
            }
            respondRecordList.add(respond1);
        }
        //全部符合要求的数据的数量
        int total=responds.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        com.timebank.controller.yl.TableRecordsJson tableRecordsJson=new com.timebank.controller.yl.TableRecordsJson(respondRecordList,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            // System.out.println(json1);
            return json1;
        }catch (Exception e){
            return null;
        }
    }
    //给志愿者打分
    @RequestMapping(value = "/scoreForVolunteer",method= RequestMethod.GET )
    private String scoreForVolunteer(Model model,String thisPerson1,String finalScore,String id) {
        System.out.println("这是打完分数后 确定按钮");
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        // model.addAttribute("activityid",id);
        //需要将分数插入到actpartb表单中
      /*  ActpartExample actpartExample=new ActpartExample();
        actpartExample.or().andActpartActivityGuidEqualTo(id);
        List<Actpart> activities=actpartMapper.selectByExample(actpartExample);
        for(Actpart it:activities){
            if(it.getActpartUserGuid().equals(thisPerson1)){
                it.setActpartEvaluate(Integer.parseInt(finalScore));
                actpartMapper.updateByPrimaryKeySelective(it);
            }
        }*/
        return "scoreForVolunteer";
    }



}
