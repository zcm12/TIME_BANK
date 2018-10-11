package com.timebank.controller.wxxcx.wxxcx;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class wxUserResponseController {
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private ReqestMapper reqestMapper;
    @Autowired
    private RespondMapper respondMapper;
    @Autowired
    private UsersMapper usersMapper;

    public Users GetCurrentUsers(String message) {
        UsersExample usersExample = new UsersExample();
        Users users = null;
        String em = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        String ph = "^[1][34578]\\d{9}$";
        if (message.matches(ph)) {
            usersExample.or().andUserPhoneEqualTo(message);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            users = usersList.get(0);

        } else if (message.matches(em)) {
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

    @RequestMapping(value = "/wxQueryServiceList")
    @ResponseBody
    public String appQueryNearbyReq(Users users) {
        System.out.println(users.getUserCurrentaddr());

         UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(users.getUserAccount());
        List<Users> users2 = usersMapper.selectByExample(usersExample1);
        Users users1 = users2.get(0);

//        Subject account = SecurityUtils.getSubject();
//        String message = (String) account.getPrincipal();
//        Users users1 = GetCurrentUsers(message);

        String userCurrentaddr1 = users.getUserCurrentaddr();
        if (userCurrentaddr1 != null) {
            users1.setUserCurrentaddr(userCurrentaddr1);
            usersMapper.updateByPrimaryKeySelective(users1);
        }


        String userCurrentaddr = users1.getUserCurrentaddr();
        String[] split1 = userCurrentaddr.split(",");
        Double users1Lat = Double.valueOf(split1[0]);//登录用户的纬度
        Double users1Lng = Double.valueOf(split1[1]);//登录用户的经度

        ReqestExample reqestExample = new ReqestExample();
        //不是本人发的且申请通过的
        reqestExample.or().andReqIssueUserGuidNotEqualTo(users1.getUserGuid())
                .andReqTypeApproveStatusEqualTo("88888888-94E3-4EB7-AAD3-111111111111");
        List<Reqest> reqests1 = reqestMapper.selectByExample(reqestExample);


        List<Reqest> reqestRecordList = new ArrayList<>();
        for (int i = 0; i < reqests1.size(); i++) {

            String[] split2 = reqests1.get(i).getReqAddress().split(",");
            Double reqLat = Double.valueOf(split2[0]);//请求的纬度
            Double reqLng = Double.valueOf(split2[1]);//请求的经度
            double v = GetDistance(users1Lng, users1Lat, reqLng, reqLat);
            System.out.println("相距：" + v + "km");
            if (v <= 10) {
                Reqest reqest1 = reqests1.get(i);
                TypeExample typeExample = new TypeExample();
                String reqTypeGuidClass = reqest1.getReqTypeGuidClass();
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(reqTypeGuidClass);
                List<Type> types = typeMapper.selectByExample(typeExample);
                reqest1.setReqTypeGuidClass(types.get(0).getTypeTitle());
                String reqTypeGuidUrgency = reqest1.getReqTypeGuidUrgency();
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(reqTypeGuidUrgency);
                List<Type> types1 = typeMapper.selectByExample(typeExample);
                reqest1.setReqTypeGuidUrgency(types1.get(0).getTypeTitle());
                //处理请求处理状态
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
                //处理请求剩余需要人数
                int personNum = reqest1.getReqPersonNum();
                //查已经有几个人进行了申请
                RespondExample respondExample = new RespondExample();
                respondExample.or().andResReqGuidEqualTo(reqest1.getReqGuid());
                List<Respond> responds = respondMapper.selectByExample(respondExample);
                int personNumber = responds.size();//该request的申请人数
                int shengyuNum = personNum - personNumber;
                if (shengyuNum > 0) { //剩余可申请人数大于0 才能被看到
                    reqest1.setReqPersonNum(shengyuNum);//还可以申请几人
//            int personNumber = 0;
//            for (Respond respo : responds) {
//                if (respo.getResReqGuid().equals(reqest1.getReqGuid()) && respo.getResTypeGuidProcessStatus() != "634b1ade-c97a-42a3-8ed0-ca1fc0a6b69a") {
//                    //说明有一个志愿者了，与所需人数进行对比
//                    personNumber = personNumber + 1;
//                    System.out.println(personNumber);
//                }
//            }
//            reqest1.setReqPersonNum(personNum - personNumber);
                    reqestRecordList.add(reqest1);
                }

            }


        }

        /*根据发布时间排序*/
        reqestRecordList.sort((o1, o2) -> {
            int flag = o2.getReqIssueTime().compareTo(o1.getReqIssueTime());
            System.out.println("flag:" + flag);
            return flag;
        });

        //全部符合要求的数据的数量
        int total = reqests1.size();
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


    //申请服务
//    @RequestMapping(value="/wxQueryServiceList",produces = "application/json;charset=UTF-8")
//    @ResponseBody
//    public String appQueryServiceList(Users users){
//
//        UsersExample usersExample1 = new UsersExample();
//        usersExample1.or().andUserAccountEqualTo(users.getUserAccount());
//        List<Users> users2 = usersMapper.selectByExample(usersExample1);
//        Users users3 = users2.get(0);
//
//        ReqestExample reqestExample=new ReqestExample();
//
//        List<Reqest> reqests=reqestMapper.selectByExample(reqestExample);
//        List<Reqest> reqests1 = new ArrayList<>();
//        //TODO 判断这条请求是不是发给自己的
//        String ownId = users3.getUserGuid().toLowerCase();
//        for(Reqest re :reqests)
//        {
//            String userId = re.getReqTargetsUserGuid();
//            if (userId==null||userId.length()==0)
//            {
//                //跳过
//            }else {
//                //拆分
//                String afterStep1 = userId.replace("\"","");
//                String afterStep2 = afterStep1.replace("[","");
//                String afterStep3 = afterStep2.replace("]","");
//
//                String[] afterSplit = afterStep3.split(",");
//                for (String after : afterSplit)
//                {
//                    //如果这个字段包含自己，那就在reqests中加上
//                    if (after.equals(ownId))
//                    {
//                        reqests1.add(re);
//                        break;
//                    }
//                }
//            }
//        }
//        List<Reqest> reqestRecordList=new ArrayList<>();
//        for(int i=0;i< reqests1.size();i++){
//            Reqest reqest1=reqests1.get(i);
//            TypeExample typeExample = new TypeExample();
//            String reqTypeGuidClass=reqest1.getReqTypeGuidClass();
//            typeExample.clear();
//            typeExample.or().andTypeGuidEqualTo(reqTypeGuidClass);
//            List<Type> types = typeMapper.selectByExample(typeExample);
//            reqest1.setReqTypeGuidClass(types.get(0).getTypeTitle());
//            String reqTypeGuidUrgency=reqest1.getReqTypeGuidUrgency();
//            typeExample.clear();
//            typeExample.or().andTypeGuidEqualTo(reqTypeGuidUrgency);
//            List<Type> types1 = typeMapper.selectByExample(typeExample);
//            reqest1.setReqTypeGuidUrgency(types1.get(0).getTypeTitle());
//            //处理请求处理状态
//            String approveId = reqest1.getReqTypeApproveStatus();
//            if (approveId != null)
//            {
//                typeExample.clear();
//                typeExample.or().andTypeGuidEqualTo(approveId);
//                List<Type> types2 = typeMapper.selectByExample(typeExample);
//                reqest1.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
//            }
//            //处理请求批准状态
//            String processId = reqest1.getReqTypeGuidProcessStatus();
//            if (processId != null)
//            {
//                typeExample.clear();
//                typeExample.or().andTypeGuidEqualTo(processId);
//                List<Type> types2 = typeMapper.selectByExample(typeExample);
//                reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
//            }
//            //处理请求剩余需要人数
//            int personNum =  reqest1.getReqPersonNum();
//            //查已经有几个人进行了申请
//            RespondExample respondExample = new RespondExample();
//            List<Respond> responds = respondMapper.selectByExample(respondExample);
//            int personNumber = 0;
//            for (Respond respo : responds)
//            {
//                if (respo.getResReqGuid().equals(reqest1.getReqGuid()) && respo.getResTypeGuidProcessStatus()!="634b1ade-c97a-42a3-8ed0-ca1fc0a6b69a")
//                {
//                    //说明有一个志愿者了，与所需人数进行对比
//                    personNumber = personNumber+1;
//                    System.out.println(personNumber);
//                }
//            }
//            reqest1.setReqPersonNum(personNum-personNumber);
//            reqestRecordList.add(reqest1);
//        }
//        //全部符合要求的数据的数量
//        int total=reqests1.size();
//        //将所得集合打包
//        ObjectMapper mapper = new ObjectMapper();
//        TableRecordsJson tableRecordsJson=new TableRecordsJson(reqestRecordList,total);
//        //将实体类转换成json数据并返回
//        try {
//            String json1 = mapper.writeValueAsString(tableRecordsJson);
//            System.out.println(json1);
//            return json1;
//        }catch (Exception e){
//            return null;
//        }
//    }
    //志愿者点击 申请服务按钮 相当于数据库插入一条response
    @RequestMapping(value = "/wxInsertRes")
    @ResponseBody
    public String appInsertRes( Users users,Reqest reqest, Respond respond) {
//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);

        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(users.getUserAccount());
        List<Users> users2 = usersMapper.selectByExample(usersExample1);
        Users users3 = users2.get(0);

        String reqGuid = reqest.getReqGuid();
        String resAcceptAddress = respond.getResAcceptAddress();


        RespondExample respondExample = new RespondExample();
        respondExample.or().andResReqGuidEqualTo(reqGuid).andResUserGuidEqualTo(users3.getUserGuid());
        List<Respond> responds = respondMapper.selectByExample(respondExample);
        if (responds.size() == 0) {
            if (users3.getUserRole().equals("Tourist")) {
//                return "请先完善信息，审核通过后执行此操作";
            }
            UUID guid = UUID.randomUUID();
            respond.setResGuid(guid.toString());
            respond.setResReqGuid(reqGuid);
            //userID
            respond.setResUserGuid(users3.getUserGuid());
            //响应接受时间
            Date date = new Date();
            respond.setResAcceptTime(date);
            //响应接受地址
            respond.setResAcceptAddress(resAcceptAddress);
            //只要是申请就是通过，不需要审核
            respond.setResTypeGuidProcessStatus("88888888-94e3-4eb7-aad3-111111111111");
            int insert = respondMapper.insert(respond);
            return "sucess";
        } else {
            return "请不要重复申请！";
        }
    }

    //查看服务列表后向后台数据索要数据
    @RequestMapping(value = "/wxQueryServiceMy", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryServiceMy(String userAccount) {

//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);

        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(userAccount);
        List<Users> users2 = usersMapper.selectByExample(usersExample1);
        Users users3 = users2.get(0);

        RespondExample respondExample = new RespondExample();
        respondExample.clear();
        //判断自己响应过哪些请求
        String ownId = users3.getUserGuid();
//        respondExample.or().andResUserGuidEqualTo(ownId).andResTypeGuidProcessStatusEqualTo("88888888-94E3-4EB7-AAD3-111111111111");
        respondExample.or().andResUserGuidEqualTo(ownId);
        List<Respond> responds = respondMapper.selectByExample(respondExample);
        List<Respond> respondRecordList = new ArrayList<>();
        for (int i = 0; i < responds.size(); i++) {
            Respond respond1 = responds.get(i);
            TypeExample typeExample = new TypeExample();
            if (respond1.getResTypeGuidProcessStatus() != null) {
                String resTypeGuidProcessStatus = respond1.getResTypeGuidProcessStatus();
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(resTypeGuidProcessStatus);
                List<Type> types = typeMapper.selectByExample(typeExample);
                respond1.setResTypeGuidProcessStatus(types.get(0).getTypeTitle());
                respond1.setResUserGuid(users3.getUserAccount());
            }
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
            // System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }
    //志愿者进行更新操作，进入更新界面
    @RequestMapping(value = "/wxUpdateResMy", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appUpdateResMy(Respond respond) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String resGuid = respond.getResGuid();
        Respond respond1 = respondMapper.selectByPrimaryKey(resGuid);
        String resReqGuid = respond1.getResReqGuid();
        ReqestExample reqestExample = new ReqestExample();
        reqestExample.or().andReqGuidEqualTo(resReqGuid);
        List<Reqest> reqests = reqestMapper.selectByExample(reqestExample);
        Reqest reqest1 = reqests.get(0);

        TypeExample typeExample = new TypeExample();
        String reqTypeGuidClass = reqest1.getReqTypeGuidClass();
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqTypeGuidClass);
        List<Type> types = typeMapper.selectByExample(typeExample);
        reqest1.setReqTypeGuidClass(types.get(0).getTypeTitle());
        String reqTypeGuidUrgency = reqest1.getReqTypeGuidUrgency();
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqTypeGuidUrgency);
        List<Type> types1 = typeMapper.selectByExample(typeExample);
        reqest1.setReqTypeGuidUrgency(types1.get(0).getTypeTitle());
        //处理请求处理状态
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

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json1 = mapper.writeValueAsString(reqest1);
            System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }
    //志愿者进行撤单操作
    @RequestMapping(value = "/wxCancelResMy")
    @ResponseBody
    public ResultModel appCancelResMy(Respond respond) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String resGuid = respond.getResGuid();
        Respond respond1 = respondMapper.selectByPrimaryKey(resGuid);
        //更新响应表
        respond1.setResTypeGuidProcessStatus("77777777-94e3-4eb7-aad3-555555555555");
        int update = respondMapper.updateByPrimaryKeySelective(respond1);
        return new ResultModel(update, "撤单成功");
    }

    //志愿者点击 更新请求按钮 相当于更新response数据库的一条数据
    @RequestMapping(value = "/wxUpdateRes", method = RequestMethod.POST)
    @ResponseBody
    public ResultModel appUpdateRes(Respond respond) {

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);

        String resGuid = respond.getResGuid();
        String resAcceptAddress = respond.getResAcceptAddress();
//        String reqAddress = reqest.getReqAddress();

//        Respond respond = new Respond();
        RespondExample respondExample = new RespondExample();
        respondExample.or().andResGuidEqualTo(resGuid);
        List<Respond> responds = respondMapper.selectByExample(respondExample);
        Respond respond1 = responds.get(0);
        //响应接受地址
        respond1.setResAcceptAddress(resAcceptAddress);
        int update = respondMapper.updateByPrimaryKeySelective(respond1);
        return new ResultModel(update, "更新请求地址成功");
    }

    /*计算经纬度距离*/
    private static double EARTH_RADIUS = 6378.137;
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
    public static double GetDistance(double lng1, double lat1, double lng2, double lat2) {
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
