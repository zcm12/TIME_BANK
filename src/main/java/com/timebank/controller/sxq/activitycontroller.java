package com.timebank.controller.sxq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.controller.yl.UpdateList;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.catalina.User;
import org.apache.ibatis.jdbc.Null;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Account;
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

@Controller

public class activitycontroller {

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

    /***************************************************************************/
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
/***************************************************************************/
    /**
     * 名字转换函数
     */
    public String IdtoName(String reqtargetuser) {
        if (reqtargetuser != null && !reqtargetuser.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            UsersExample usersExample = new UsersExample();

            //去掉[]
            String r = reqtargetuser.replaceAll("^.*\\[", "").replaceAll("].*", "");
            //分割字符串
            String[] array = r.split(",");
            String respondName = null;
            for (int in = 0; in < array.length; in++) {
                usersExample.clear();
                String jj = array[in].replaceAll(" ", "");
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

    //导航栏发布活动
    @RequestMapping(value = "/publishactivity")
    public String publishactivity(Model model) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        System.out.println("这是发布活动");
        return "publishactivity";
    }

    //发布活动插入数据库请求
    @RequestMapping(value = "/activityinsert")
    private String activityInsert(@ModelAttribute @Valid Activity activity, Errors errors, Model model) {
        System.out.println("222222222222");
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);

        if (!errors.hasErrors()) {

            UUID guid = randomUUID();
            activity.setActivityGuid(guid.toString());
            String comguid = users1.getUserCommGuid();//当前用户活动社区
            String nameguid = users1.getUserGuid();//此用户guid
            activity.setActivityFromCommGuid(comguid);//活动社区
            activity.setActivityProcessUserGuid(nameguid);//活动处理人
            activity.setActivityTypeProcessStatus("33333333-94e3-4eb7-aad3-111111111111");//默认活动处理状态未启动
            UsersExample comusersExample = new UsersExample();
            comusersExample.or().andUserCommGuidEqualTo(comguid);
            //comusersExample.or().andUserFromRoleGuidEqualTo("USE");
            List<Users> comusers = usersMapper.selectByExample(comusersExample);//得到所有相同社区的用户
            List<String> comuser1 = new ArrayList<>();
            for (Users comuser : comusers) {
                if (comuser.getUserRole() != null)
                    if (comuser.getUserRole().equals("USE")) {//在这判断为用户
                        String comuserGuid = comuser.getUserGuid();
                        comuser1.add(comuserGuid);
                    }
            }
            //将同一个社区 并且为用户身份的人插入该字段
            activity.setActivityTargetsUserGuid(comuser1.toString());
            activityMapper.insertSelective(activity);
        }
        return "activitylist";

    }

    //导航栏查看活动
    @RequestMapping(value = "/activitylist/{buttonId1}")
    public String activitylist(Model model, @PathVariable String buttonId1) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        System.out.println("这是查看所有活动，每个查看活动列表都是这个界面，只不过活动状态的区别");
        model.addAttribute("buttonid", buttonId1);
        return "activitylist";
    }

    // 查看所有活动请求 这是list界面中的请求  向后台索要数据
    @RequestMapping(value = "/getActivityListJsonData")
    @ResponseBody
    public String activitylist(Model model, @RequestParam int offset, int limit, String sortName, String sortOrder, String searchText, String button) {
        Subject account = SecurityUtils.getSubject();
        System.out.println("获取数据库数据 显示列表");
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role", role100);
        if (searchText == "") {
            searchText = null;
        }
        ActivityExample activityExample = new ActivityExample();
        activityExample.clear();
        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            //将排序信息添加到example中
            activityExample.setOrderByClause(order);
        }

        //判断点击的是查看未启动活动   从数据库依据其类型的GUID筛选记录
        if (button.equals("2")) {
            activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94e3-4eb7-aad3-111111111111");
        }
        //判断点击的是查看待启动活动
        if (button.equals("3")) {
            activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94e3-4eb7-aad3-222222222222");
        }
        //判断点击的是启动活动
        if (button.equals("4")) {
            activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94E3-4EB7-AAD3-333333333333");
        }
        //判断点击的是未完成活动
        if (button.equals("5")) {
            activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94E3-4EB7-AAD3-555555555555");
        }
        //判断点击的是已完成活动
        if (button.equals("6")) {
            activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94E3-4EB7-AAD3-444444444444");
        }
        //判断点击的是已撤销活动
        if (button.equals("7")) {
            activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94e3-4eb7-aad3-666666666666");
        }
        //判断点击的是评价完成的活动活动
        if (button.equals("8")) {
            activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94e3-4eb7-aad3-777777777777");
        }
        List<Activity> activities = activityMapper.selectByExample(activityExample);
        List<Activity> activityRecordList = new ArrayList<Activity>();
        for (int i = offset; i < offset + limit && i < activities.size(); i++) {

            Activity act = activities.get(i);

            //活动处理人
            String processUserId = act.getActivityProcessUserGuid();
            UsersExample usersExample = new UsersExample();
            usersExample.or().andUserGuidEqualTo(processUserId);
            List<Users> processuser = usersMapper.selectByExample(usersExample);
            act.setActivityProcessUserGuid(processuser.get(0).getUserName());

            //活动小区
            String activityComm = act.getActivityFromCommGuid();
            CommunityExample communityExample = new CommunityExample();
            communityExample.or().andCommGuidEqualTo(activityComm);
            List<Community> comm = communityMapper.selectByExample(communityExample);
            act.setActivityFromCommGuid(comm.get(0).getCommTitle());

            //活动处理状态
            String processStatus = act.getActivityTypeProcessStatus();//得到活动状态对应的guid
            TypeExample typeExample = new TypeExample();
            typeExample.or().andTypeGuidEqualTo(processStatus);
            List<Type> processStatusType = typeMapper.selectByExample(typeExample);
            //使用guid查询得到关于启动的状态
            act.setActivityTypeProcessStatus(processStatusType.get(0).getTypeTitle());

            // 把接收者的Id换成名字显示在页面
            String reqtargetusers = act.getActivityTargetsUserGuid();//能够得到每条记录的guid
            String sb1 = IdtoName(reqtargetusers);
            act.setActivityTargetsUserGuid(sb1);
            if (searchText != null) {
                String activityId = act.getActivityGuid();
                String activityTile = act.getActivityTitle();
                String activityDes = act.getActivityDesp();
                String activityCom = act.getActivityComment();
                if (activityId.contains(searchText) || activityTile.contains(searchText) || activityDes.contains(searchText) || activityCom.contains(searchText)) {
                    activityRecordList.add(act);
                }
            } else {
                activityRecordList.add(act);
            }
        }

        //全部符合要求的数据的数量
        int total = activities.size();
        //System.out.println("总数："+total);
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(activityRecordList, total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }

    //导航栏评价活动
    @RequestMapping(value = "/activitylistscore")
    public String activitylistscore(Model model) {

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        System.out.println("这是查看已完成但是没有评价");
        return "activitylistscore";
    }

    //找出已完成的活动   searchText 搜索框
    @RequestMapping(value = "/getActivityListScoreJsonData")
    @ResponseBody
    public String activitylistscore(Model model, @RequestParam int offset, int limit, String sortName, String sortOrder, String searchText) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);

        if (searchText == "") {
            searchText = null;
        }

        ActivityExample activityExample = new ActivityExample();
        activityExample.clear();

        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            System.out.println(order);
            //将排序信息添加到example中 此处表示先按sortName排序   再按asc（从小到大）排序
            activityExample.setOrderByClause(order);
        }

        activityExample.or().andActivityTypeProcessStatusEqualTo("33333333-94E3-4EB7-AAD3-444444444444");


        List<Activity> activities = activityMapper.selectByExample(activityExample);
        List<Activity> activityRecordList = new ArrayList<Activity>();
        for (int i = offset; i < offset + limit && i < activities.size(); i++) {


            Activity act1 = activities.get(i);

            //活动处理人
            String processUserId = act1.getActivityProcessUserGuid();

            UsersExample usersExample3 = new UsersExample();
            usersExample3.or().andUserGuidEqualTo(processUserId);
            List<Users> processuser = usersMapper.selectByExample(usersExample3);
            act1.setActivityProcessUserGuid(processuser.get(0).getUserAccount());

            //活动小区
            String activityComm = act1.getActivityFromCommGuid();
            CommunityExample communityExample = new CommunityExample();
            communityExample.or().andCommGuidEqualTo(activityComm);
            List<Community> comm = communityMapper.selectByExample(communityExample);
            act1.setActivityFromCommGuid(comm.get(0).getCommTitle());

            //活动处理状态
            String processStatus = act1.getActivityTypeProcessStatus();
            TypeExample typeExample = new TypeExample();
            typeExample.or().andTypeGuidEqualTo(processStatus);
            List<Type> processStatusType = typeMapper.selectByExample(typeExample);
            act1.setActivityTypeProcessStatus(processStatusType.get(0).getTypeTitle());
            //处理名字
            String reqtargetusers = act1.getActivityTargetsUserGuid();//能够得到每条记录的guid
            String sb1 = IdtoName(reqtargetusers);
            act1.setActivityTargetsUserGuid(sb1);
            if (searchText != null) {
                String activityId = act1.getActivityGuid();
                String activityTile = act1.getActivityTitle();
                String activityDes = act1.getActivityDesp();
                String activityCom = act1.getActivityComment();
                if (activityId.contains(searchText) || activityTile.contains(searchText) || activityDes.contains(searchText) || activityCom.contains(searchText)) {
                    activityRecordList.add(act1);
                }
            } else {
                activityRecordList.add(act1);
            }
            //         activityRecordList.add(act);
        }

        //全部符合要求的数据的数量
        int total = activities.size();
        //System.out.println("总数："+total);
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(activityRecordList, total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            System.out.println(222);
            System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }


    //某个活动查看详情请求
    @RequestMapping(value = "/ACTIVITY/{activityGuid}")
    public String activityshow(Model model, @PathVariable String activityGuid, UpdateList updateList) {
        System.out.println("查看详情界面");
        //将用户的角色插入到模型中
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);

        Activity activity = activityMapper.selectByPrimaryKey(activityGuid);//根据guid获得这条记录
        model.addAttribute("activity", activity);
        //将能看到此条记录的用户的id转换成姓名
        String Name = activity.getActivityTargetsUserGuid();
        String name = IdtoName(Name);
        model.addAttribute("idmessage", name);
        //遍历actpart 将申请过此条记录的人插入到前端model
        ActpartExample actpartExample = new ActpartExample();
        actpartExample.or().andActpartActivityGuidEqualTo(activityGuid);
        List<Actpart> actparts = actpartMapper.selectByExample(actpartExample);
        StringBuilder stringBuilder = new StringBuilder();
        //计数
        int count = 0;
        for (Actpart actpart : actparts) {
            String userGuid = actpart.getActpartUserGuid();
            UsersExample usersExample1 = new UsersExample();
            usersExample1.or().andUserGuidEqualTo(userGuid);
            List<Users> userList = usersMapper.selectByExample(usersExample1);
            String userName = userList.get(0).getUserName();
            stringBuilder.append(userName + "   ");
            count++;
        }
        //参加人的姓名
        model.addAttribute("joinId", stringBuilder);
        //参加的人数
        model.addAttribute("count", count);
        //活动处理状态按钮逻辑显示

        String Type = activity.getActivityTypeProcessStatus();
        System.out.println(Type);
        //未启动
        if (Type.equals("33333333-94E3-4EB7-AAD3-111111111111")) {
            //更新按钮
            updateList.setUpdateId(1);
            //查看志愿者
            updateList.setDeletaId(0);
            //启动按钮
            updateList.setStartId(0);
            //待启动按钮
            updateList.setViewVolId(1);
            //完成按钮
            updateList.setFinishId(0);
            //未完成按钮
            updateList.setUnFinishId(0);
            model.addAttribute("updateList", updateList);
        }
        // 待启动
        if (Type.equals("33333333-94E3-4EB7-AAD3-222222222222")) {
            //更新按钮
            updateList.setUpdateId(0);
            //查看志愿者
            updateList.setDeletaId(1);
            //启动按钮
            updateList.setStartId(1);
            //待启动按钮
            updateList.setViewVolId(0);
            //完成按钮
            updateList.setFinishId(0);
            //未完成按钮
            updateList.setUnFinishId(0);
            model.addAttribute("updateList", updateList);
        }
        // 启动
        if (Type.equals("33333333-94E3-4EB7-AAD3-333333333333")) {
            //更新按钮
            updateList.setUpdateId(0);
            //查看志愿者
            updateList.setDeletaId(1);
            //启动按钮
            updateList.setStartId(0);
            //待启动按钮
            updateList.setViewVolId(0);
            //完成按钮
            updateList.setFinishId(1);
            //未完成按钮
            updateList.setUnFinishId(1);
            model.addAttribute("updateList", updateList);
        }
        // 未完成
        if (Type.equals("33333333-94E3-4EB7-AAD3-555555555555")) {
            //更新按钮
            updateList.setUpdateId(0);
            //查看志愿者
            updateList.setDeletaId(1);
            //启动按钮
            updateList.setStartId(0);
            //未完成按钮
            updateList.setUnFinishId(0);
            //待启动按钮
            updateList.setViewVolId(0);
            //完成按钮
            updateList.setFinishId(0);

            model.addAttribute("updateList", updateList);
        }
        // 已评价
        if (Type.equals("33333333-94E3-4EB7-AAD3-777777777777")) {
            //查看志愿者
            updateList.setDeletaId(1);
            //启动按钮
            updateList.setStartId(0);
            //待启动按钮
            updateList.setViewVolId(0);
            //更新按钮
            updateList.setUpdateId(0);

            //打分按钮（完成）
            updateList.setFinishId(0);
            //未完成按钮
            updateList.setUnFinishId(0);
            model.addAttribute("updateList", updateList);
        }
        // 已完成
        if (Type.equals("33333333-94E3-4EB7-AAD3-444444444444")) {
            //更新按钮
            updateList.setUpdateId(0);
            //查看志愿者
            updateList.setDeletaId(1);
            //启动按钮
            updateList.setStartId(0);
            //待启动按钮
            updateList.setViewVolId(0);
            //完成按钮
            updateList.setFinishId(0);
            //未完成按钮
            updateList.setUnFinishId(0);
            model.addAttribute("updateList", updateList);
        }
        // 撤销
        if (Type.equals("33333333-94E3-4EB7-AAD3-666666666666")) {
            //未完成按钮
            updateList.setUnFinishId(0);
            //更新按钮
            updateList.setUpdateId(0);
            // 查看志愿者
            updateList.setDeletaId(0);
            //启动按钮
            updateList.setStartId(0);
            //待启动按钮
            updateList.setViewVolId(0);
            //完成按钮
            updateList.setFinishId(0);

            model.addAttribute("updateList", updateList);
        }

        //处理活动状态 改成单选框
        TypeExample typeExample = new TypeExample();          //声明数据库对象
        typeExample.or().andTypeGroupIdEqualTo(3);     //查找数据库中value=3的值
        List<Type> typex = typeMapper.selectByExample(typeExample); //将查找的值存到集合中  组成下拉框
        model.addAttribute("typex", typex);                  //添加到模型中


        return "activityshow";
    }

    //待启动按钮
    @RequestMapping(value = "/activityupdate4")
    public String activityupdate4(Model model, String activityGuid4) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        System.out.println(2222222);
        System.out.println(activityGuid4);
        ActivityExample activityExample = new ActivityExample();
        activityExample.or().andActivityGuidEqualTo(activityGuid4);
        List<Activity> activities = activityMapper.selectByExample(activityExample);
        Activity activity = activities.get(0);
        activity.setActivityTypeProcessStatus("33333333-94E3-4EB7-AAD3-222222222222");
        //数据库更新 前端显示待启动
        activityMapper.updateByPrimaryKeySelective(activity);
        if (activity.getActivityTypeProcessStatus() != null) {
            Type type1 = typeMapper.selectByPrimaryKey("33333333-94E3-4EB7-AAD3-222222222222");
            activity.setActivityTypeProcessStatus(type1.getTypeTitle());
        }
        model.addAttribute("activity", activity);
        model.addAttribute("message",0);
        return "activityShowNoSbumit";
    }

    //启动按钮
    @RequestMapping(value = "/activityupdate3")
    public String activityupdate3(Model model, String activityGuid3) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        ActivityExample activityExample = new ActivityExample();
        activityExample.or().andActivityGuidEqualTo(activityGuid3);
        List<Activity> activities = activityMapper.selectByExample(activityExample);
        Activity activity = activities.get(0);
        activity.setActivityTypeProcessStatus("33333333-94E3-4EB7-AAD3-333333333333");
        //数据库更新 前端显示待启动
        activityMapper.updateByPrimaryKeySelective(activity);
        if (activity.getActivityTypeProcessStatus() != null) {
            Type type1 = typeMapper.selectByPrimaryKey("33333333-94E3-4EB7-AAD3-333333333333");
            activity.setActivityTypeProcessStatus(type1.getTypeTitle());
        }
        model.addAttribute("message",0);
        model.addAttribute("activity", activity);

        return "activityShowNoSbumit";
    }

    //完成按钮
    @RequestMapping(value = "/activityupdate5")
    public String activityupdate5(Model model, String activityGuid5) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        ActivityExample activityExample = new ActivityExample();
        activityExample.or().andActivityGuidEqualTo(activityGuid5);
        List<Activity> activities = activityMapper.selectByExample(activityExample);
        Activity activity = activities.get(0);
        activity.setActivityTypeProcessStatus("33333333-94E3-4EB7-AAD3-444444444444");
        //数据库更新 前端显示待启动
        activityMapper.updateByPrimaryKeySelective(activity);
        if (activity.getActivityTypeProcessStatus() != null) {
            Type type1 = typeMapper.selectByPrimaryKey("33333333-94E3-4EB7-AAD3-444444444444");
            activity.setActivityTypeProcessStatus(type1.getTypeTitle());
        }
        model.addAttribute("message",0);
        model.addAttribute("activity", activity);
        return "activityShowNoSbumit";
    }

    //未完成按钮
    @RequestMapping(value = "/activityupdate6")
    public String activityupdate6(Model model, String activityGuid6) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        ActivityExample activityExample = new ActivityExample();
        activityExample.or().andActivityGuidEqualTo(activityGuid6);
        List<Activity> activities = activityMapper.selectByExample(activityExample);
        Activity activity = activities.get(0);
        activity.setActivityTypeProcessStatus("333333333-94E3-4EB7-AAD3-555555555555");
        //数据库更新 前端显示待启动
        activityMapper.updateByPrimaryKeySelective(activity);
        if (activity.getActivityTypeProcessStatus() != null) {
            Type type1 = typeMapper.selectByPrimaryKey("33333333-94E3-4EB7-AAD3-555555555555");
            activity.setActivityTypeProcessStatus(type1.getTypeTitle());
        }
        model.addAttribute("activity", activity);
        model.addAttribute("message",0);
        return "activityShowNoSbumit";
    }

    //撤销按钮
    @RequestMapping(value = "/activityupdate7")
    public String activityupdate7(Model model, String activityGuid7) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        ActivityExample activityExample = new ActivityExample();
        activityExample.or().andActivityGuidEqualTo(activityGuid7);
        List<Activity> activities = activityMapper.selectByExample(activityExample);
        Activity activity = activities.get(0);
        activity.setActivityTypeProcessStatus("33333333-94E3-4EB7-AAD3-666666666666");
        //数据库更新 前端显示待启动
        activityMapper.updateByPrimaryKeySelective(activity);
        if (activity.getActivityTypeProcessStatus() != null) {
            Type type1 = typeMapper.selectByPrimaryKey("33333333-94E3-4EB7-AAD3-666666666666");
            activity.setActivityTypeProcessStatus(type1.getTypeTitle());
        }
        model.addAttribute("activity", activity);
        model.addAttribute("message",0);
        return "activityShowNoSbumit";
    }

    //查看志愿者按钮 申请此单的人
    @RequestMapping(value = "/activityupdate2")
    public String activityupdate2(Model model, String activityGuid2) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        model.addAttribute("message",activityGuid2);
        System.out.println("查看志愿者");
        return "volunteerListByMa";
    }
    @RequestMapping(value="/getUSERSList",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getVolunteerListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder,String activityid,Model model){
        System.out.println(222222);
        System.out.println(activityid);
        model.addAttribute("message",activityid);
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role", role100);


        ActivityExample activityExample = new ActivityExample();
        UsersExample usersExample = new UsersExample();
        RoleExample roleExample = new RoleExample();
        TypeExample typeExample = new TypeExample();
        CommunityExample communityExample = new CommunityExample();
        ActpartExample actpartExample = new ActpartExample();
        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            //将排序信息添加到example中
            usersExample.setOrderByClause(order);
        }
        List<Users> usersList = new ArrayList<Users>();
        //根据活动id遍历actpart数据库  得到用户guid
        actpartExample.or().andActpartActivityGuidEqualTo(activityid);
        List<Actpart> actparts=actpartMapper.selectByExample(actpartExample);
        for(Actpart it:actparts){
            if(it.getAcpartTypeGuidProcessStatus().equals("88888888-94E3-4EB7-AAD3-111111111111")) {
                String usersguid = it.getActpartUserGuid();
                Users user = usersMapper.selectByPrimaryKey(usersguid);

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
                if (userCommunicity != null) {
//                String userCommunicity = user.getUserCommGuid();
                    communityExample.clear();
                    communityExample.or().andCommGuidEqualTo(userCommunicity);
                    List<Community> usercommunicity = communityMapper.selectByExample(communityExample);
                    user.setUserCommGuid(usercommunicity.get(0).getCommTitle());

                }
                usersList.add(user);
            }
        }

        int total = usersList.size();
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(usersList, total);
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }
    //删除志愿者
    @RequestMapping(value = "/UserGuid/{message}")
    public String DelVolunteer(Model model, @PathVariable String message){
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        System.out.println("就是这： ");
        System.out.println(message);

        String[] strings=message.split(",");
        String userGuid=strings[0];
        String activity=strings[1];
        ActpartExample actpartExample=new ActpartExample();
        actpartExample.or().andActpartActivityGuidEqualTo(activity);
        List<Actpart> actpartList=actpartMapper.selectByExample(actpartExample);
        Actpart actpart=actpartList.get(0);
        if(actpart.getActpartUserGuid().equals(userGuid)) {
            actpart.setAcpartTypeGuidProcessStatus("88888888-94E3-4EB7-AAD3-222222222222");
            actpartMapper.updateByPrimaryKeySelective(actpart);
        }

        return "volunteerListByMa";
    }
    //更新按钮
    @RequestMapping(value = "/activityupdate1")
    public String activityupdate1(Model model, String activityGuid1) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        model.addAttribute("message",1);
        ActivityExample activityExample = new ActivityExample();
        activityExample.or().andActivityGuidEqualTo(activityGuid1);
        List<Activity> activities = activityMapper.selectByExample(activityExample);
        Activity activity = activities.get(0);
        if (activity.getActivityTypeProcessStatus() != null) {
            Type type1 = typeMapper.selectByPrimaryKey("33333333-94E3-4EB7-AAD3-111111111111");
            activity.setActivityTypeProcessStatus(type1.getTypeTitle());
        }
        model.addAttribute("activity",activity);
        return "activityShowNoSbumit";
    }
    //更新界面中的保存按钮
    @RequestMapping(value = "/updateactivity")
    public  String updateactivity(Model model,Activity activity,String activityGuid){
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        model.addAttribute("message",0);
        activity.setActivityTypeProcessStatus("33333333-94E3-4EB7-AAD3-111111111111");
        activityMapper.updateByPrimaryKeySelective(activity);
        if (activity.getActivityTypeProcessStatus() != null) {
            Type type1 = typeMapper.selectByPrimaryKey("33333333-94E3-4EB7-AAD3-111111111111");
            activity.setActivityTypeProcessStatus(type1.getTypeTitle());
        }
        model.addAttribute("activity",activity);
        return  "activityShowNoSbumit";
    }


    //给活动参与者打分  评分按钮
    @RequestMapping(value = "/activityscore/{activityGuid}")
    public String activityscore(Model model, @PathVariable String activityGuid) {
        System.out.println("评分按钮");
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        model.addAttribute("activityid", activityGuid);


        return "activitypersonscore";
    }

    //所有参与这个活动的用户的列表
    @RequestMapping(value = "/getUSERSListScoreJsonData")
    @ResponseBody
    public String userList(Model model, @RequestParam int offset, int limit, String sortName, String sortOrder, String activityid) {
        System.out.println("点击完评分按钮   跳转到个人 每个人后面都有 打分按钮");
        System.out.println(activityid);
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role", role100);


        ActivityExample activityExample = new ActivityExample();
        UsersExample usersExample = new UsersExample();
        RoleExample roleExample = new RoleExample();
        TypeExample typeExample = new TypeExample();
        CommunityExample communityExample = new CommunityExample();
        ActpartExample actpartExample = new ActpartExample();
        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            //将排序信息添加到example中
            usersExample.setOrderByClause(order);
        }
        List<Users> usersList = new ArrayList<Users>();
        //根据活动id遍历actpart数据库  得到用户guid
        actpartExample.or().andActpartActivityGuidEqualTo(activityid);
        List<Actpart> actparts=actpartMapper.selectByExample(actpartExample);
        for(Actpart it:actparts){
            //todo:  先做删除功能  做完以后 放开此处
            if(it.getAcpartTypeGuidProcessStatus()=="88888888-94E3-4EB7-AAD3-111111111111") {
                String usersguid = it.getActpartUserGuid();
                Users user = usersMapper.selectByPrimaryKey(usersguid);

                String userStatus = user.getUserTypeAccountStatus();
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(userStatus);
                List<Type> userstatus = typeMapper.selectByExample(typeExample);
                user.setUserTypeAccountStatus(userstatus.get(0).getTypeTitle());
                String userGender = user.getUserTypeGuidGender();
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(userGender);
                List<Type> gender = typeMapper.selectByExample(typeExample);
                user.setUserTypeGuidGender(gender.get(0).getTypeTitle());


                String userCommunicity = user.getUserCommGuid();
                if (userCommunicity != null) {
//                String userCommunicity = user.getUserCommGuid();
                    communityExample.clear();
                    communityExample.or().andCommGuidEqualTo(userCommunicity);
                    List<Community> usercommunicity = communityMapper.selectByExample(communityExample);
                    user.setUserCommGuid(usercommunicity.get(0).getCommTitle());
                    usersList.add(user);
                }
            }
        }
        int total = usersList.size();
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(usersList, total);
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }

    //给服务打分
    @RequestMapping(value = "/scoreForPerson", method = RequestMethod.POST)
    private String scoreForPerson(Model model, String thisPerson1, String finalScore, String id, Errors errors) {
        System.out.println("这是打完分数后 确定按钮");
        System.out.println(finalScore + "间隔" + id + "间隔" + thisPerson1);
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        model.addAttribute("activityid", id);
        //需要将分数插入到actpartb表单中
        ActpartExample actpartExample = new ActpartExample();
        actpartExample.or().andActpartActivityGuidEqualTo(id);
        List<Actpart> activities = actpartMapper.selectByExample(actpartExample);
        for (Actpart it : activities) {
            if (it.getActpartUserGuid().equals(thisPerson1)) {
                it.setActpartEvaluate(Integer.parseInt(finalScore));
                actpartMapper.updateByPrimaryKeySelective(it);
            }
        }
        //打分完成后修改处理状态的字段  改成33333333-94E3-4EB7-AAD3-777777777777
        return "activitypersonscore";
    }

}