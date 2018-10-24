package com.timebank.controller.appuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ReqestApp;
import com.timebank.appmodel.ResultModel;
import com.timebank.controller.yl.TableRecordsJson;
import com.timebank.domain.*;
import com.timebank.mapper.ReqestMapper;
import com.timebank.mapper.RespondMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Collections;
import java.util.Comparator;
import static java.util.UUID.randomUUID;

@Controller
public class AppUserRequestController {
    @Autowired
    private ReqestMapper reqestMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private RespondMapper respondMapper;
    @Autowired
    private UsersMapper usersMapper;
    String reqGuidOfVol = null;
    String updateRequestGuid = null;
    Date dateStart = null;
    private Double users1Lng;
    private Double users1Lat;

    public  Users GetCurrentUsers(String message){

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
    //插入请求（发布）
    /*---------------app api------------------------*/
    @RequestMapping(value = "/appInsertReq")
    @ResponseBody
    public ResultModel appInsertReq(ReqestApp reqestApp) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1 = GetCurrentUsers(message);
//        System.out.println(reqestApp.getReqAddress());
//        System.out.println(reqestApp.getReqTitle());
//        System.out.println(reqestApp.getReqDesp());
//        System.out.println(reqestApp.getReqComment());
//        System.out.println(reqestApp.getReqTypeGuidClass());
//        System.out.println(reqestApp.getReqTypeGuidUrgency());
//        System.out.println(reqestApp.getReqAvailableStartTime());
//        System.out.println(reqestApp.getReqAvailableEndTime());
//        System.out.println(reqestApp.getReqRreDurationTime());
//        System.out.println(reqestApp.getReqPersonNum());
        String needFormatAvalStartTime = reqestApp.getReqAvailableStartTime();
        String needFormatAvalEndTime = reqestApp.getReqAvailableEndTime();

        Reqest reqest = new Reqest();
        reqest.setReqAddress(reqestApp.getReqAddress());
        reqest.setReqTitle(reqestApp.getReqTitle());
        reqest.setReqDesp(reqestApp.getReqDesp());
        reqest.setReqComment(reqestApp.getReqComment());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //date类型的处理
        try {
            Date parseAvalStartTime = simpleDateFormat.parse(needFormatAvalStartTime);
            Date parseAvalEndTime = simpleDateFormat.parse(needFormatAvalEndTime);
            reqest.setReqAvailableStartTime(parseAvalStartTime);
            reqest.setReqAvailableEndTime(parseAvalEndTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //integer类型的处理
        reqest.setReqRreDurationTime(Integer.valueOf(reqestApp.getReqRreDurationTime()));
        reqest.setReqPersonNum(Integer.valueOf(reqestApp.getReqPersonNum()));

        //type类型的处理
        //请求分类
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeTitleEqualTo(reqestApp.getReqTypeGuidClass());
        List<Type> types = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(types.get(0).getTypeGuid());
        //选择请求紧急程度
        typeExample.clear();
        typeExample.or().andTypeTitleEqualTo(reqestApp.getReqTypeGuidUrgency());
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeGuid());
        //请求提交
        UUID guid = randomUUID();
        reqest.setReqGuid(guid.toString());
        //TODO 此处应该是获取登录者的用户名和密码
        //TODO 在user表中获得该用户名对应的user_duid传到前端页面的请求者ID，先自己设定这个值
        reqest.setReqIssueUserGuid(users1.getUserGuid());
        //请求提出的时间设定为当前时间
        Date date = new Date();
        reqest.setReqIssueTime(date);
        //将请求批准状态先置为待审核
        Type type = typeMapper.selectByPrimaryKey("88888888-94e3-4eb7-aad3-333333333333");
        reqest.setReqTypeApproveStatus(type.getTypeGuid());
        //将请求处理状态先置为未启动
        Type type1 = typeMapper.selectByPrimaryKey("33333333-94e3-4eb7-aad3-222222222222");
        reqest.setReqTypeGuidProcessStatus(type1.getTypeGuid());


        //请求target user  可看到该请求的用户
        UsersExample usersExample = new UsersExample();
        List<Users> usersList = usersMapper.selectByExample(usersExample);
        //所有用户中删除当前用户后的集合usersList
        for (int i = 0; i < usersList.size(); i++) {
            if (usersList.get(i).getUserGuid().equals(users1.getUserGuid())) {
                usersList.remove(i);
            }
        }
        if (users1.getUserCurrentaddr() != null) {
            String[] split1 = users1.getUserCurrentaddr().split(",");
            users1Lat = Double.valueOf(split1[0]);
            users1Lng = Double.valueOf(split1[1]);
        }
        List<String> targetGuidList = new ArrayList<>();
        for (Users users2 : usersList) {
            if (users2.getUserCurrentaddr() != null) {
                String userCurrentaddr = users2.getUserCurrentaddr();
                String[] split = userCurrentaddr.split(",");
                Double users2Lat = Double.valueOf(split[0]);
                Double users2Lng = Double.valueOf(split[1]);
                double v = GetDistance(users1Lng, users1Lat, users2Lng, users2Lat);
                System.out.println("相距："+v+"km");
                if (v <= 10) {
                    targetGuidList.add(users2.getUserGuid());
                }
            }
        }
//        String substring = targetGuidList.toString().substring(1, targetGuidList.toString().length() - 1);//删除[]
        reqest.setReqTargetsUserGuid(targetGuidList.toString());
        System.out.println("target GUID:"+targetGuidList.toString());

        int insert = reqestMapper.insert(reqest);
        return new ResultModel(insert, "request插入成功");
    }

    //我的请求
    @RequestMapping(value = "/appQueryReqMy", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryReqMy() {
        //获得当前用户
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        //获取当前登陆者id
        String userID = users1.getUserGuid();

        ReqestExample reqestExample = new ReqestExample();
        reqestExample.or().andReqIssueUserGuidEqualTo(userID);
        List<Reqest> reqests = reqestMapper.selectByExample(reqestExample);//当前用户的我的需求列表
        List<Reqest> reqestRecordList = new ArrayList<>();
        for (int i = 0; i < reqests.size(); i++) {
            Reqest reqest1 = reqests.get(i);//当前用户-我的需求的某条记录
            TypeExample typeExample = new TypeExample();
            //处理请求分类
            String reqTypeGuidClass = reqest1.getReqTypeGuidClass();
            //获得当前需求的类别reqTypeGuidClass在数据库中的存储为GUID类型的，并不是文字
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(reqTypeGuidClass);
            List<Type> types = typeMapper.selectByExample(typeExample);//获得所有我的需求的分类
            reqest1.setReqTypeGuidClass(types.get(0).getTypeTitle());//显示给用户的请求类别为文字，将当前需求的类别设置为汉字
            //处理请求的紧急程度
            String reqTypeGuidUrgency = reqest1.getReqTypeGuidUrgency();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(reqTypeGuidUrgency);
            List<Type> types1 = typeMapper.selectByExample(typeExample);
            reqest1.setReqTypeGuidUrgency(types1.get(0).getTypeTitle());
            //处理请求的处理状态
            String approveId = reqest1.getReqTypeApproveStatus();
            if (approveId != null) {
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(approveId);
                List<Type> types2 = typeMapper.selectByExample(typeExample);
                reqest1.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
            }
            //处理请求批准状态
            String processId = reqest1.getReqTypeGuidProcessStatus();
            if (processId != null) {
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(processId);
                List<Type> types2 = typeMapper.selectByExample(typeExample);
                reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
            }
            reqestRecordList.add(reqest1);
        }

        //List集合中的sort()方法可根据元素的自然顺序对指定列表按升序进行排序。
        if (reqestRecordList.size() != 0) {
            /*根据发布时间排序*/
            reqestRecordList.sort((o1, o2) -> {
                int flag = o2.getReqIssueTime().compareTo(o1.getReqIssueTime());
                System.out.println("flag:" + flag);
                return flag;
            });

            Date reqIssueTime = reqestRecordList.get(0).getReqIssueTime();
            System.out.println("发布时间："+reqIssueTime);
        }
        //全部符合要求的数据的数量
//        int total = reqests.size();
        int total = reqestRecordList.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(reqestRecordList, total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }

    //查看详情之更新请求
    @RequestMapping(value = "/appUpdateReq")
    @ResponseBody
    public ResultModel appUpdateReq(ReqestApp reqestApp) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        System.out.println(reqestApp.getReqAddress());
        System.out.println(reqestApp.getReqTitle());
        System.out.println(reqestApp.getReqDesp());
        System.out.println(reqestApp.getReqComment());
        System.out.println(reqestApp.getReqTypeGuidClass());
        System.out.println(reqestApp.getReqTypeGuidUrgency());
        System.out.println(reqestApp.getReqAvailableStartTime());
        System.out.println(reqestApp.getReqAvailableEndTime());
        System.out.println(reqestApp.getReqRreDurationTime());
        System.out.println(reqestApp.getReqPersonNum());
        String needFormatAvalStartTime = reqestApp.getReqAvailableStartTime();
        String needFormatAvalEndTime = reqestApp.getReqAvailableEndTime();

        ReqestExample reqestExample = new ReqestExample();
        ReqestExample.Criteria criteria = reqestExample.createCriteria();
        criteria.andReqGuidEqualTo(reqestApp.getReqGuid());

        Reqest reqest = new Reqest();
        reqest.setReqAddress(reqestApp.getReqAddress());
        reqest.setReqTitle(reqestApp.getReqTitle());
        reqest.setReqDesp(reqestApp.getReqDesp());
        reqest.setReqComment(reqestApp.getReqComment());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //date类型的处理
        try {
            Date parseAvalStartTime = simpleDateFormat.parse(needFormatAvalStartTime);
            Date parseAvalEndTime = simpleDateFormat.parse(needFormatAvalEndTime);
            reqest.setReqAvailableStartTime(parseAvalStartTime);
            reqest.setReqAvailableEndTime(parseAvalEndTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //integer类型的处理
        reqest.setReqRreDurationTime(Integer.valueOf(reqestApp.getReqRreDurationTime()));
        reqest.setReqPersonNum(Integer.valueOf(reqestApp.getReqPersonNum()));

        //type类型的处理
        //请求分类
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeTitleEqualTo(reqestApp.getReqTypeGuidClass());
        List<Type> types = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(types.get(0).getTypeGuid());
        //选择请求紧急程度
        typeExample.clear();
        typeExample.or().andTypeTitleEqualTo(reqestApp.getReqTypeGuidUrgency());
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeGuid());
        //请求提交
//        UUID guid = randomUUID();
//        reqest.setReqGuid(guid.toString());
        //TODO 此处应该是获取登录者的用户名和密码
        //TODO 在user表中获得该用户名对应的user_duid传到前端页面的请求者ID，先自己设定这个值
        reqest.setReqIssueUserGuid(users1.getUserGuid());
        //请求提出的时间设定为当前时间
        Date date = new Date();
        reqest.setReqIssueTime(date);
        //将请求批准状态先置为待审核
        Type type = typeMapper.selectByPrimaryKey("88888888-94e3-4eb7-aad3-333333333333");
        reqest.setReqTypeApproveStatus(type.getTypeGuid());
        //将请求处理状态先置为未启动
        Type type1 = typeMapper.selectByPrimaryKey("33333333-94e3-4eb7-aad3-222222222222");
        reqest.setReqTypeGuidProcessStatus(type1.getTypeGuid());


        int update = reqestMapper.updateByExampleSelective(reqest, reqestExample);
        return new ResultModel(update, "request更新成功");
    }

    //查看详情之撤单(并非真的删除，只是更新修改状态)
    @RequestMapping(value = "/appCancelReq")
    @ResponseBody
    public ResultModel appCancelReq(ReqestApp reqestApp) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        String reqGuid = reqestApp.getReqGuid();
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);
        //更新请求表
        reqest.setReqTypeGuidProcessStatus("33333333-94e3-4eb7-aad3-666666666666");
        int update = reqestMapper.updateByPrimaryKeySelective(reqest);
        //TODO 老人进行了撤单的同时，将所有申请过这个单的志愿者的状态也改为撤单，这个撤单是老人的撤单，不是志愿者的撤单
        RespondExample respondExample = new RespondExample();
        respondExample.or().andResReqGuidEqualTo(reqGuid);
        List<Respond> responds = respondMapper.selectByExample(respondExample);
        if (responds != null && responds.size() != 0) {
            for (Respond respondAfter : responds) {
                respondAfter.setResTypeGuidProcessStatus("33333333-94e3-4eb7-aad3-666666666666");
                respondMapper.updateByPrimaryKey(respondAfter);
            }
        }

        return new ResultModel(update, "request撤单成功");
    }

    //查看志愿者接单情况 从后台获取数据
    @RequestMapping(value = "/appQueryVolunteer", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryVolunteer(ReqestApp reqestApp) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        String reqGuid = reqestApp.getReqGuid();

        RespondExample respondExample = new RespondExample();
        respondExample.clear();
        //查询所有对应请求的响应，且响应状态不包含“撤销”
        respondExample.or().andResReqGuidEqualTo(reqGuid)
                .andResTypeGuidProcessStatusNotEqualTo("77777777-94e3-4eb7-aad3-555555555555");
        List<Respond> responds = respondMapper.selectByExample(respondExample);
        List<Respond> respondRecordList = new ArrayList<>();
        for (int i = 0; i < responds.size(); i++) {
            Respond respond1 = responds.get(i);
            TypeExample typeExample = new TypeExample();
            String resUserGuid = respond1.getResUserGuid();
            UsersExample usersExample1 = new UsersExample();
            usersExample1.or().andUserGuidEqualTo(resUserGuid);
            List<Users> users = usersMapper.selectByExample(usersExample1);
            respond1.setResUserGuid(users.get(0).getUserAccount());
            String resTypeGuidProcessStatus = respond1.getResTypeGuidProcessStatus();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(resTypeGuidProcessStatus);
            List<Type> types = typeMapper.selectByExample(typeExample);
            respond1.setResTypeGuidProcessStatus(types.get(0).getTypeTitle());
            respondRecordList.add(respond1);
        }
        //全部符合要求的数据的数量
        int total = responds.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(respondRecordList, total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }

    //评价志愿者
    @RequestMapping(value = "/appUpdateResEvaluate")
    @ResponseBody
    public int updateResEvaluate(String resEvaluate,String resEvaluateDes,String resGuid) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        RespondExample respondExample = new RespondExample();
        respondExample.or().andResGuidEqualTo(resGuid);//找到评价的respond
        List<Respond> responds = respondMapper.selectByExample(respondExample);
        Respond respond = responds.get(0);
        respond.setResEvaluate(Integer.valueOf(resEvaluate));
        respond.setResEvaluateDes(resEvaluateDes);
        int update = respondMapper.updateByPrimaryKeySelective(respond);
        return update;
    }



    //查看详情界面中的  申请启动按钮
    @RequestMapping(value = "/appStartReq")
    @ResponseBody
    public ResultModel appStartReq(ReqestApp reqestApp) {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        String reqGuid = reqestApp.getReqGuid();
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);
        //设置请求处理状态为启动
        reqest.setReqTypeGuidProcessStatus("33333333-94e3-4eb7-aad3-333333333333");
        //设置请求开始时间为现在
        Date startDate = new Date();
        dateStart = startDate;
        reqest.setReqStartTime(startDate);
        int update = reqestMapper.updateByPrimaryKey(reqest);

        return new ResultModel(update, "request启动成功");
    }

    //查看详情界面中的  申请已完成按钮
    @RequestMapping(value = "/appCompletedReq")
    @ResponseBody
    public ResultModel appCompletedReq(ReqestApp reqestApp) {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        String reqGuid = reqestApp.getReqGuid();
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);
        //设置请求处理状态为已完成
        reqest.setReqTypeGuidProcessStatus("33333333-94e3-4eb7-aad3-444444444444");
        //设置请求结束时间为现在
        Date endDate = new Date();
        reqest.setReqEndTime(endDate);
        //设置请求持续时间，单位为分钟
        long endMiles = endDate.getTime();
        long startMiles = reqest.getReqStartTime().getTime();
        long durTime = (endMiles - startMiles) / (60 * 1000);
        reqest.setReqActualDurationTime(new Long(durTime).intValue());
        int update = reqestMapper.updateByPrimaryKey(reqest);

        return new ResultModel(update, "request已完成");
    }

    //查看详情界面中的  申请未完成按钮
    @RequestMapping(value = "/appIncompletedReq")
    @ResponseBody
    public ResultModel appIncompletedReq(ReqestApp reqestApp) {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        String reqGuid = reqestApp.getReqGuid();
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);
        //设置请求处理状态为已完成
        reqest.setReqTypeGuidProcessStatus("33333333-94e3-4eb7-aad3-555555555555");
        int update = reqestMapper.updateByPrimaryKey(reqest);

        return new ResultModel(update, "request未完成");
    }

    /*计算经纬度距离*/
    private static double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static double GetDistance(double lng1, double lat1, double lng2,

                                     double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        return s; //单位km
    }
}
