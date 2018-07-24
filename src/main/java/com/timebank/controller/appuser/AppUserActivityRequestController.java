package com.timebank.controller.appuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.controller.yl.TableRecordsJson;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动的申请
 */
@Controller
public class AppUserActivityRequestController {
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private ActpartMapper actpartMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private TypeMapper typeMapper;

    String acticityIDByUser = null;

    //basepage页面申请活动按钮
    /*@RequestMapping(value = "/createActivityByUserView")
    public String userApply(Model model)
    {
        System.out.println("这是左边申请活动");
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        return "activityApplyByUser";
    }
    //活动列表页面后台请求数据
    @RequestMapping(value="/getACTIVITYListByUserJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getACTIVITYListJsonData(@RequestParam int offset, int limit, String sortName,String sortOrder,Model model){
        System.out.println("活动列表界面向后台请求数据");
        ActivityExample activityExample=new ActivityExample();
        activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94e3-4eb7-aad3-222222222222");

    //处理排序信息
    if(sortName!=null){
        //拼接字符串
        String order= GetDatabaseFileName(sortName)+" "+sortOrder;
        //将排序信息添加到example中
        activityExample.setOrderByClause(order);
    }

    List<Activity> activitys=activityMapper.selectByExample(activityExample);
    List<Activity> activityRecordList=new ArrayList<>();//所有状态为待启动的记录
    for(int i=offset;i< offset+limit&&i < activitys.size();i++){
        Activity activity1=activitys.get(i);
        //显示社区
        CommunityExample communityExample = new CommunityExample();
        String activityFromCommGuid=activity1.getActivityFromCommGuid();
        communityExample.clear();
        communityExample.or().andCommGuidEqualTo(activityFromCommGuid);
        List<Community> communities = communityMapper.selectByExample(communityExample);
        activity1.setActivityFromCommGuid(communities.get(0).getCommTitle());
        //筛选活动界面中有此用户的id的记录
//        if(activity1.getActivityTargetsUserGuid().contains(Guid)) {
            activityRecordList.add(activity1);
//        }
    }
    //全部符合要求的数据的数量
    int total=activitys.size();
    //将所得集合打包
    ObjectMapper mapper = new ObjectMapper();
    TableRecordsJson tableRecordsJson=new TableRecordsJson(activityRecordList,total);
    //将实体类转换成json数据并返回
    try {
        String json1 = mapper.writeValueAsString(tableRecordsJson);
        return json1;
    }catch (Exception e){
        return null;
    }
}
    //活动详情页面查看详情按钮
    @RequestMapping(value = "/activityApply/{activityGuid}")
    public String activityApply (@PathVariable String activityGuid, Model model) {
        System.out.println("查看详情界面");
        acticityIDByUser = activityGuid;
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        Activity activity = activityMapper.selectByPrimaryKey(activityGuid);
        CommunityExample communityExample = new CommunityExample();
        String activityFromCommGuid=activity.getActivityFromCommGuid();
        communityExample.clear();
        communityExample.or().andCommGuidEqualTo(activityFromCommGuid);
        List<Community> communities = communityMapper.selectByExample(communityExample);
        activity.setActivityFromCommGuid(communities.get(0).getCommTitle());
        model.addAttribute("activity",activity);
        //显示人数 可独立写成函数
        ActpartExample actpartExample1=new ActpartExample();
        actpartExample1.or().andActpartActivityGuidEqualTo(activityGuid);
        List<Actpart> actparts1=actpartMapper.selectByExample(actpartExample1);
        model.addAttribute("amountMessage",actparts1.size());

        //选择性锁定申请按钮（只能申请一次）(参与活动的人数达到上限)
        ActpartExample actpartExample = new ActpartExample();
        actpartExample.or().andActpartUserGuidEqualTo(users1.getUserGuid());
        List<Actpart> actparts = actpartMapper.selectByExample(actpartExample);
        int renshu=activity.getActivityPersonNum();
        for(Actpart it: actparts){
            if(it.getActpartActivityGuid().equals(activityGuid)||renshu<=actparts1.size()){//字符串比较  用equals方法
                model.addAttribute("message","1");
                break;
            }
        }
        return "activityListViewByUser";

    }
    //活动详情页面申请活动按钮
    @RequestMapping(value = "/ActivitySaveByUser")
    public String userApply(Actpart actpart, Model model) {
        //保存当前登陆者的账号存到actpart表
        System.out.println("申请按钮");
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        System.out.println(users);
        System.out.println(users1);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        UUID guid=randomUUID();
        actpart.setActpartGuid(guid.toString());
        actpart.setActpartActivityGuid(acticityIDByUser);
        actpart.setActpartUserGuid(users1.getUserGuid());
        System.out.println(users1.getUserGuid());
        actpartMapper.insert(actpart);
        return "applyActivityListByUserView";
    }

    //basepage页面查看活动列表
    @RequestMapping(value = "/applyActivityListByUserView")
    public String applyActivityListByUserView(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);

        return "applyActivityListByUserView";
    }
    //已参加活动按钮对应后台请求数据
    @RequestMapping(value="/getApplyActivityListByUserViewJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getApplyActivityListByUserViewJsonData(@RequestParam int offset, int limit, String sortName,String sortOrder){

        ActivityExample activityExample=new ActivityExample();
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        ActpartExample actpartExample = new ActpartExample();
        actpartExample.or().andActpartUserGuidEqualTo(users1.getUserGuid());
        List<Actpart> actparts = actpartMapper.selectByExample(actpartExample);
        List<Activity> activitys=new ArrayList<>();
        activitys.clear();
        for (Actpart actpart :actparts)
        {
            ActivityExample activityExample1=new ActivityExample();
            activityExample1.or().andActivityGuidEqualTo( actpart.getActpartActivityGuid());
            List<Activity> activities1 = activityMapper.selectByExample(activityExample1);
            if(!activities1.isEmpty()) {
                activitys.add(activities1.get(0));
            }
        }
        //处理排序信息.
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            activityExample.setOrderByClause(order);
        }

        List<Activity> activityRecordList=new ArrayList<>();
        for(int i=offset;i< offset+limit&&i < activitys.size();i++){
            Activity activity1=activitys.get(i);
            CommunityExample communityExample = new CommunityExample();
            String activityFromCommGuid=activity1.getActivityFromCommGuid();
            communityExample.clear();
            communityExample.or().andCommGuidEqualTo(activityFromCommGuid);
            List<Community> communities = communityMapper.selectByExample(communityExample);
            activity1.setActivityFromCommGuid(communities.get(0).getCommTitle());
            //活动处理状态
            Type type = typeMapper.selectByPrimaryKey(activity1.getActivityTypeProcessStatus());
            activity1.setActivityTypeProcessStatus(type.getTypeTitle());
            activityRecordList.add(activity1);
        }
        //全部符合要求的数据的数量
        int total=activitys.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(activityRecordList,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
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
    }*/
    /*---------------app api------------------------*/
    //查看活动
    @RequestMapping(value="/appQueryActivityByUser",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryActivityByUser() {
        ActivityExample activityExample=new ActivityExample();
        activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94e3-4eb7-aad3-333333333333");

        List<Activity> activitys=activityMapper.selectByExample(activityExample);
        List<Activity> activityRecordList=new ArrayList<>();
        for(int i=0;i < activitys.size();i++){
            Activity activity1=activitys.get(i);
            CommunityExample communityExample = new CommunityExample();
            String activityFromCommGuid=activity1.getActivityFromCommGuid();
            communityExample.clear();
            communityExample.or().andCommGuidEqualTo(activityFromCommGuid);
            List<Community> communities = communityMapper.selectByExample(communityExample);
            activity1.setActivityFromCommGuid(communities.get(0).getCommTitle());
            activityRecordList.add(activity1);
        }
        //全部符合要求的数据的数量
        int total=activitys.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(activityRecordList,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            return json1;
        }catch (Exception e){
            return null;
        }
    }

    //已参加活动按钮对应后台请求数据  我的活动
    @RequestMapping(value="/appQueryActivityMyByUser",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryActivityMyByUser(){
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        ActpartExample actpartExample = new ActpartExample();
        actpartExample.or().andActpartUserGuidEqualTo(users1.getUserGuid());
        List<Actpart> actparts = actpartMapper.selectByExample(actpartExample);
        List<Activity> activitys=new ArrayList<>();
        activitys.clear();
        for (Actpart actpart :actparts)
        {
            ActivityExample activityExample1=new ActivityExample();
            activityExample1.or().andActivityGuidEqualTo( actpart.getActpartActivityGuid());
            List<Activity> activities1 = activityMapper.selectByExample(activityExample1);
            if(!activities1.isEmpty()) {
                activitys.add(activities1.get(0));
            }
        }

        List<Activity> activityRecordList=new ArrayList<>();
        for(int i=0; i< activitys.size();i++){
            Activity activity1=activitys.get(i);
            CommunityExample communityExample = new CommunityExample();
            String activityFromCommGuid=activity1.getActivityFromCommGuid();
            communityExample.clear();
            communityExample.or().andCommGuidEqualTo(activityFromCommGuid);
            List<Community> communities = communityMapper.selectByExample(communityExample);
            activity1.setActivityFromCommGuid(communities.get(0).getCommTitle());
            //活动处理状态
            Type type = typeMapper.selectByPrimaryKey(activity1.getActivityTypeProcessStatus());
            activity1.setActivityTypeProcessStatus(type.getTypeTitle());
            activityRecordList.add(activity1);
        }
        //全部符合要求的数据的数量
        int total=activitys.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(activityRecordList,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            return json1;
        }catch (Exception e){
            return null;
        }
    }
}



