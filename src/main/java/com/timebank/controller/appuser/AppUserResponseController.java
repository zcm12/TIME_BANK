package com.timebank.controller.appuser;

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

import java.util.*;

@Controller
public class AppUserResponseController {
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private ReqestMapper reqestMapper;
    @Autowired
    private RespondMapper respondMapper;
    @Autowired
    private UsersMapper usersMapper;
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
    /*---------------------app API-------------------------*/
    //申请服务 周围订单
    @RequestMapping(value = "/appQueryServiceList", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryServiceList() {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        ReqestExample reqestExample = new ReqestExample();

        List<Reqest> reqests = reqestMapper.selectByExample(reqestExample);
        List<Reqest> reqests1 = new ArrayList<>();
        //TODO 判断这条请求是不是发给自己的
        String ownId = users1.getUserGuid();
        for (Reqest re : reqests) {
            String userId = re.getReqTargetsUserGuid();
            if (userId == null || userId.length() == 0) {
                //跳过
            } else {
                System.out.println("re批准状态:"+re.getReqTypeApproveStatus());
                System.out.println("re处理状态:"+re.getReqTypeGuidProcessStatus());
                //审批通过的请求才可以看到
                if (re.getReqTypeApproveStatus().equals("88888888-94E3-4EB7-AAD3-111111111111")) {
                    //拆分
                    System.out.println("userId:" + userId);
                    String afterStep1 = userId.replace(" ", "");
                    String afterStep2 = afterStep1.replace("[", "");
                    String afterStep3 = afterStep2.replace("]", "");

                    String[] afterSplit = afterStep3.split(",");
                    System.out.println("afterSplit:" + Arrays.toString(afterSplit));
                    for (String after : afterSplit) {
                        System.out.println("after:" + after);
                        //如果这个字段包含自己，那就在reqests中加上
                        if (after.equals(ownId)) {
                            reqests1.add(re);
                            System.out.println("reqests1:" + reqests1.toString());
                            break;
                        }
                    }
                }

            }
        }
        List<Reqest> reqestRecordList = new ArrayList<>();
        for (int i = 0; i < reqests1.size(); i++) {
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
    /*获取几天前的时间*/
    public  Date getDateBefore(int day){
        Calendar now =Calendar.getInstance();
        Date d=new Date();
        now.setTime(d);
        now.set(Calendar.DATE,now.get(Calendar.DATE)-day);
        return now.getTime();
    }
    //申请服务 最近订单
    @RequestMapping(value = "/appQueryNewList", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryNewList() {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        ReqestExample reqestExample = new ReqestExample();

        List<Reqest> reqests = reqestMapper.selectByExample(reqestExample);
        List<Reqest> reqests1 = new ArrayList<>();
        //TODO 判断这条请求是不是自己发的
        String ownId = users1.getUserGuid();
        for (Reqest re : reqests) {
            if (!re.getReqIssueUserGuid().equals(ownId) && re.getReqIssueTime().after(getDateBefore(3))
                    && re.getReqTypeApproveStatus().equals("88888888-94E3-4EB7-AAD3-111111111111")) {
                //获取3天内审核通过的，且不是本人发的需求订单
                reqests1.add(re);
            }
        }
        List<Reqest> reqestRecordList = new ArrayList<>();
        for (int i = 0; i < reqests1.size(); i++) {
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

    //志愿者点击 申请服务按钮 相当于数据库插入一条response
    @RequestMapping(value = "/appInsertRes", method = RequestMethod.POST)
    @ResponseBody
    public ResultModel appInsertRes(Reqest reqest, Respond respond) {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        String reqGuid = reqest.getReqGuid();
        /*reqGuid对应的reqest*/
        ReqestExample reqestExample = new ReqestExample();
        reqestExample.or().andReqGuidEqualTo(reqGuid);
        List<Reqest> reqests = reqestMapper.selectByExample(reqestExample);
        Reqest reqest1 = reqests.get(0);

        String resAcceptAddress = respond.getResAcceptAddress();

        RespondExample respondExample = new RespondExample();
        respondExample.or().andResReqGuidEqualTo(reqGuid).andResUserGuidEqualTo(users1.getUserGuid());
        List<Respond> responds = respondMapper.selectByExample(respondExample);
        if (responds.size() == 0) {
            if (users1.getUserRole().equals("Tourist")) {
                return new ResultModel(12, "请先完善信息，审核通过后执行此操作");
            }
            UUID guid = UUID.randomUUID();
            respond.setResGuid(guid.toString());
            respond.setResReqGuid(reqGuid);
            //userID
            respond.setResUserGuid(users1.getUserGuid());
            //响应接受时间
            Date date = new Date();
            respond.setResAcceptTime(date);
            //响应对应请求的title
            respond.setResReqTitle(reqest1.getReqTitle());
            //响应对应请求的经纬度地址
            respond.setResReqAddr(reqest1.getReqAddress());
            //响应接受地址
            respond.setResAcceptAddress(resAcceptAddress);
            //TODO:只要是申请就是通过，不需要审核
            respond.setResTypeGuidProcessStatus("88888888-94e3-4eb7-aad3-111111111111");
            int insert = respondMapper.insert(respond);
            return new ResultModel(insert, "申请服务成功,可在\"我的服务\"中查看");
        } else {
            return new ResultModel(11, "请不要重复申请！");
        }
    }

    //查看服务列表后向后台数据索要数据
    @RequestMapping(value = "/appQueryServiceMy", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryServiceMy() {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);

        RespondExample respondExample = new RespondExample();
        respondExample.clear();
        //判断自己响应过哪些请求
        String ownId = users1.getUserGuid();
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
                respond1.setResUserGuid(users1.getUserAccount());
            }
            respondRecordList.add(respond1);
        }

        /*根据接单时间排序*/
        respondRecordList.sort((o1, o2) -> {
            int flag = o2.getResAcceptTime().compareTo(o1.getResAcceptTime());
            System.out.println("flag:" + flag);
            return flag;
        });

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
    @RequestMapping(value = "/appUpdateResMy", produces = "application/json;charset=UTF-8")
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
    @RequestMapping(value = "/appCancelResMy")
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
    @RequestMapping(value = "/appUpdateRes", method = RequestMethod.POST)
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
}
