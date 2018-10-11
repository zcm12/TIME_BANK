package com.timebank.controller.wxxcx.wxxcx;

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
public class wxuserActivityRequestController {
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

    //查看活动
    @RequestMapping(value = "/wxQueryActivityByUser", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryActivityByUser() {
        System.out.println("查看发布的活动列表走这里了");
        ActivityExample activityExample = new ActivityExample();
        activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94e3-4eb7-aad3-333333333333");
        List<Activity> activitys = activityMapper.selectByExample(activityExample);
        List<Activity> activityRecordList = new ArrayList<>();
        for (int i = 0; i < activitys.size(); i++) {
            Activity activity1 = activitys.get(i);
            CommunityExample communityExample = new CommunityExample();
            String activityFromCommGuid = activity1.getActivityFromCommGuid();
            communityExample.clear();
            communityExample.or().andCommGuidEqualTo(activityFromCommGuid);
            List<Community> communities = communityMapper.selectByExample(communityExample);
            activity1.setActivityFromCommGuid(communities.get(0).getCommTitle());
            activityRecordList.add(activity1);
        }
        //全部符合要求的数据的数量
        int total = activitys.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(activityRecordList, total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            return json1;
        } catch (Exception e) {
            return "sucess";
        }
    }

    //已参加活动按钮对应后台请求数据  我的活动
    @RequestMapping(value = "/wxQueryActivityMyByUser", produces = "application/json;charset=UTF-8")
     @ResponseBody
    public String appQueryActivityMyByUser(String userAccount) {
//      System.out.println();
//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);
//        System.out.println();



        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(userAccount);
        List<Users> users2 = usersMapper.selectByExample(usersExample1);
        Users users3 = users2.get(0);

        ActpartExample actpartExample = new ActpartExample();
        actpartExample.or().andActpartUserGuidEqualTo(users3.getUserGuid());
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


