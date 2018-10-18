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
import java.util.*;

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
/***********************************用户申请活动*************************************************************/
    //basepage页面申请活动按钮
    @RequestMapping(value = "/createActivityByUserView")
    public String userApply(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        //获得该用户的guid和小区
        System.out.println("申请活动");
        model.addAttribute("userguid",users11.getUserGuid());
        model.addAttribute("usercommguid",users11.getUserCommGuid());
        return "activityApplyByUser";
    }
    /**
     * 得到几天前的时间
     */
    public  Date getDateBefore(Date d,int day){
        Calendar now =Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE,now.get(Calendar.DATE)-day);
        return now.getTime();
    }

    /**
     * 得到几天后的时间
     */
    public  Date getDateAfter(Date d,int day){
        Calendar now =Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE,now.get(Calendar.DATE)+day);
        return now.getTime();
    }


    //活动列表页面后台请求数据
    @RequestMapping(value="/getACTIVITYListByUserJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getACTIVITYListJsonData(@RequestParam int offset,Model model, int limit, String searchText, String sortName,String sortOrder,String usercommguid,String userguid){
        System.out.println("活动列表界面向后台请求数据");
        System.out.println("此用户的guid:"+userguid+" 此用户的小区guid :"+usercommguid);
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        /**10.11添加*/
        if (searchText == "") {
            searchText = null;
        }
        /**10.11添加*/

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
//    for(int i=offset;i< offset+limit&&i < activitys.size();i++){
        for(int i=0;i < activitys.size();i++){
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
        if(role.equals("USE")) {
            if (activityTraget != null && activityTraget.contains(userguid)) {
                //第二步  用户防止修改小区以后 还能看到原先的活动  查询现在的小区 是否跟activity中的fromcommguid一样
                if (activityFromCommGuid != null && activityFromCommGuid.equals(usercommguid)) {
//                    activityRecordList.add(activity1);
                    /**10.11添加*/
                    if (searchText != null) {
                        String activityId = activity1.getActivityGuid();
                        String activityTile = activity1.getActivityTitle();
                        String activityDes = activity1.getActivityDesp();
                        String activityCom = activity1.getActivityComment();
                        if (activityId.contains(searchText) || activityTile.contains(searchText) || activityDes.contains(searchText) || activityCom.contains(searchText)) {
                            activityRecordList.add(activity1);
                        }
                    } else {
                        activityRecordList.add(activity1);
                    }
                    /**10.11添加*/
                }
            }
        }else if(role.equals("Tourist")){
            //遍历三天以内发布的请求
            Date time=activity1.getActivityStartTime();
            Date date=new Date();
            Date beforeDate=getDateBefore(date,3);
            if(time.after(beforeDate)){
//                System.out.println(beforeDate);

//                activityRecordList.add(activity1);

                /**10.11添加*/
                if (searchText != null) {
                    String activityId = activity1.getActivityGuid();
                    String activityTile = activity1.getActivityTitle();
                    String activityDes = activity1.getActivityDesp();
                    String activityCom = activity1.getActivityComment();
                    if (activityId.contains(searchText) || activityTile.contains(searchText) || activityDes.contains(searchText) || activityCom.contains(searchText)) {
                        activityRecordList.add(activity1);
                    }
                } else {
                    activityRecordList.add(activity1);
                }
                /**10.11添加*/

            }
        }
    }

        /**10.11添加*/
        List<Activity> activityReturn = new ArrayList<>();
        for (int i = offset;i<offset+limit&&i<activityRecordList.size();i++){
            activityReturn.add(activityRecordList.get(i));
        }
        /**10.11添加*/
    //全部符合要求的数据的数量
//    int total=activitys.size();
        int total=activityRecordList.size();
    //将所得集合打包
    ObjectMapper mapper = new ObjectMapper();
//    TableRecordsJson tableRecordsJson=new TableRecordsJson(activityRecordList,total);
        TableRecordsJson tableRecordsJson=new TableRecordsJson(activityReturn,total);
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
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
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
        int num=0;
        for(Actpart it:actparts1) {
            if(it.getAcpartTypeGuidProcessStatus().equals("88888888-94E3-4EB7-AAD3-111111111111")) {
                num++;
            }
        }
        model.addAttribute("amountMessage", num);
        //选择性锁定申请按钮（只能申请一次）(参与活动的人数达到上限)
        ActpartExample actpartExample = new ActpartExample();
        actpartExample.or().andActpartUserGuidEqualTo(users11.getUserGuid());
        List<Actpart> actparts = actpartMapper.selectByExample(actpartExample);
        int renshu=activity.getActivityPersonNum();
        for(Actpart it: actparts){
            if(it.getActpartActivityGuid().equals(activityGuid)||renshu<=num){//字符串比较  用equals方法
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
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        //插入此条记录的guid
       if(role.equals("USE")) {
           Actpart actpart = new Actpart();
           UUID guid = randomUUID();
           actpart.setActpartGuid(guid.toString());
           //插入此活动的guid
           System.out.println(activityGuid);
           actpart.setActpartActivityGuid(activityGuid);
           //插入用户的id
           actpart.setActpartUserGuid(users11.getUserGuid());
           actpart.setAcpartTypeGuidProcessStatus("88888888-94E3-4EB7-AAD3-111111111111");
           //从activity中得到活动处理人 插入到actpart表格
           Activity activity=activityMapper.selectByPrimaryKey(activityGuid);
           String processguid=activity.getActivityProcessUserGuid();
           actpart.setAcpartProcessUserGuid(processguid);

           actpartMapper.insert(actpart);
           return "applyActivityListByUserView";
       }else{
           CommunityExample communityExample = new CommunityExample();
           List<Community> communities = communityMapper.selectByExample(communityExample);
           model.addAttribute("communities",communities);
           model.addAttribute("users",users11);
           //加载性别
           TypeExample typeExample = new TypeExample();
           typeExample.or().andTypeGroupIdEqualTo(1);
           List<Type> types = typeMapper.selectByExample(typeExample);
           model.addAttribute("types",types);
           model.addAttribute("users", users11);
           model.addAttribute("message","请先完善个人信息");
           return "updateUserInformation";
       }
    }

    //basepage页面查看活动列表
    @RequestMapping(value = "/applyActivityListByUserView")
    public String applyActivityListByUserView(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

        return "applyActivityListByUserView";
    }
    //已参加活动按钮对应后台请求数据
    @RequestMapping(value="/getApplyActivityListByUserViewJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getApplyActivityListByUserViewJsonData(@RequestParam int offset, int limit, String sortName,String sortOrder, String searchText){

        ActivityExample activityExample=new ActivityExample();
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        ActpartExample actpartExample = new ActpartExample();
        actpartExample.or().andActpartUserGuidEqualTo(users11.getUserGuid());
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
        /**10.9添加*/
        if (searchText == "") {
            searchText = null;
        }
        /**10.9添加*/

        //处理排序信息.
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            activityExample.setOrderByClause(order);
        }

        List<Activity> activityRecordList=new ArrayList<>();
//        for(int i=offset;i< offset+limit&&i < activitys.size();i++){
        for(int i=0;i < activitys.size();i++){
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
//            activityRecordList.add(activity1);

            /**10.9添加*/
            if (searchText != null) {
                String activityId = activity1.getActivityGuid();
                String activityTile = activity1.getActivityTitle();
                String activityDes = activity1.getActivityDesp();
                String activityCom = activity1.getActivityComment();
                if (activityId.contains(searchText) || activityTile.contains(searchText) || activityDes.contains(searchText) || activityCom.contains(searchText)) {
                    activityRecordList.add(activity1);
                }
            } else {
                activityRecordList.add(activity1);
            }
        /**10.9添加*/
        }

        /**10.9添加*/
        List<Activity> activityReturn = new ArrayList<>();
        for (int i = offset;i<offset+limit&&i<activityRecordList.size();i++){
            activityReturn.add(activityRecordList.get(i));
        }
        /**10.9添加*/


        //全部符合要求的数据的数量
//        int total=activitys.size();
        int total =activityRecordList.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
//        TableRecordsJson tableRecordsJson=new TableRecordsJson(activityRecordList,total);
        TableRecordsJson tableRecordsJson=new TableRecordsJson(activityReturn,total);
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



