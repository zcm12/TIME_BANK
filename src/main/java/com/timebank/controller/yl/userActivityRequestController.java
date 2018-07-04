package com.timebank.controller.yl;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * 活动的申请
 */
@Controller
public class userActivityRequestController {
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

//    String acticityIDByUser = null;

    //basepage页面申请活动按钮
    @RequestMapping(value = "/createActivityByUserView")
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
        //获得该用户的guid和小区
        model.addAttribute("userguid",users1.getUserGuid());
        model.addAttribute("usercommguid",users1.getUserCommGuid());
        return "activityApplyByUser";
    }
    //活动列表页面后台请求数据
    @RequestMapping(value="/getACTIVITYListByUserJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getACTIVITYListJsonData(@RequestParam int offset, int limit, String sortName,String sortOrder,String usercommguid,String userguid){
        System.out.println("活动列表界面向后台请求数据");
        System.out.println("此用户的guid:"+userguid+" 此用户的小区guid :"+usercommguid);
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
        //所有状态为待启动的记录
    List<Activity> activityRecordList=new ArrayList<>();
    for(int i=offset;i< offset+limit&&i < activitys.size();i++){
        Activity activity1=activitys.get(i);
        //显示社区
        CommunityExample communityExample = new CommunityExample();
        String activityFromCommGuid=activity1.getActivityFromCommGuid();
        communityExample.clear();
        communityExample.or().andCommGuidEqualTo(activityFromCommGuid);
        List<Community> communities = communityMapper.selectByExample(communityExample);
        activity1.setActivityFromCommGuid(communities.get(0).getCommTitle());
        //第一步查询activity中traget字段是否包含此用户
        String activityTraget=activity1.getActivityTargetsUserGuid();
        if(activityTraget!=null&&activityTraget.contains(userguid)){
            //第二步  用户防止修改小区以后 还能看到原先的活动  查询现在的小区 是否跟activity中的fromcommguid一样
            if(activityFromCommGuid!=null&&activityFromCommGuid.equals(usercommguid)){
                activityRecordList.add(activity1);
            }
        }
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
    public String userApply(Model model,String activityGuid) {
        //保存当前登陆者的账号存到actpart表
        System.out.println("申请按钮");
        System.out.println(22222);
        System.out.println(activityGuid);//null
        //得到此用户的guid
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        //插入此条记录的guid
        Actpart actpart=new Actpart();
        UUID guid=randomUUID();
        actpart.setActpartGuid(guid.toString());
        //插入此活动的guid
        System.out.println(activityGuid);
        actpart.setActpartActivityGuid(activityGuid);
        //插入用户的id
        actpart.setActpartUserGuid(users1.getUserGuid());
        actpart.setAcpartTypeGuidProcessStatus("88888888-94E3-4EB7-AAD3-111111111111");
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
    }

}



