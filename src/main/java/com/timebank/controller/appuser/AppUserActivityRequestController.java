package com.timebank.controller.appuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ResultModel;
import com.timebank.controller.yl.TableRecordsJson;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

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
    private Users GetCurrentUsers(String message){

        UsersExample usersExample=new UsersExample();
        Users users=null;
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
    /*---------------app api------------------------*/
    //查看活动
    @RequestMapping(value="/appQueryActivityByUser",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryActivityByUser() {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        ActivityExample activityExample=new ActivityExample();
        //查看未启动活动
        activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94e3-4eb7-aad3-111111111111");

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
            //第一步查询activity中traget字段是否包含此用户
            String activityTraget=activity1.getActivityTargetsUserGuid();
            if(activityTraget!=null&&activityTraget.contains(users1.getUserGuid())){
                //第二步  用户防止修改小区以后 还能看到原先的活动  查询现在的小区 是否跟activity中的fromcommguid一样
                if(activityFromCommGuid!=null&&activityFromCommGuid.equals(users1.getUserCommGuid())){

                    //处理请求剩余需要人数
                    int personNum = activity1.getActivityPersonNum();
                    //查已经有几个人进行了申请
                    ActpartExample actpartExample = new ActpartExample();
                    actpartExample.or().andActpartActivityGuidEqualTo(activity1.getActivityGuid());
                    List<Actpart> actparts = actpartMapper.selectByExample(actpartExample);
                    int personNumber = actparts.size();//该request的申请人数
                    int shengyuNum = personNum - personNumber;
                    if (shengyuNum > 0) { //剩余可申请人数大于0 才能被看到
                        activity1.setActivityPersonNum(shengyuNum);//还可以申请几人
                        activityRecordList.add(activity1);
                    }

                }
            }
        }

        /*根据开始时间排序*/
        activityRecordList.sort((o1, o2) -> {
            int flag = o2.getActivityStartTime().compareTo(o1.getActivityStartTime());
            System.out.println("flag:" + flag);
            return flag;
        });
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

    //申请活动按钮
    @RequestMapping(value = "/appInsertActpart")
    @ResponseBody
    public ResultModel userApply(Activity activity) {
        //保存当前登陆者的账号存到actpart表
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        ActpartExample actpartExample = new ActpartExample();
        actpartExample.or().andActpartUserGuidEqualTo(users1.getUserGuid()).andActpartActivityGuidEqualTo(activity.getActivityGuid());
        List<Actpart> actparts = actpartMapper.selectByExample(actpartExample);
        if (actparts.size() == 0) {
            //插入此条记录的guid
            Actpart actpart = new Actpart();
            UUID guid = randomUUID();
            actpart.setActpartGuid(guid.toString());
            //插入此活动的guid
            actpart.setActpartActivityGuid(activity.getActivityGuid());
            //插入用户的id
            actpart.setActpartUserGuid(users1.getUserGuid());
            actpart.setAcpartTypeGuidProcessStatus("88888888-94E3-4EB7-AAD3-111111111111");
            int insert = actpartMapper.insert(actpart);
            return new ResultModel(insert, "申请活动成功,可在\"我的活动\"中查看");
        } else {
            return new ResultModel(11, "请不要重复申请！");
        }

    }

    //已参加活动按钮对应后台请求数据  我的活动
    @RequestMapping(value="/appQueryActivityMyByUser",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryActivityMyByUser(){
//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
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

        /*根据开始时间排序*/
        activityRecordList.sort((o1, o2) -> {
            int flag = o2.getActivityStartTime().compareTo(o1.getActivityStartTime());
            System.out.println("flag:" + flag);
            return flag;
        });

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



