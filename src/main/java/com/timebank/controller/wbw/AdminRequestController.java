package com.timebank.controller.wbw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.timebank.controller.yl.UpdateList;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
//import org.apache.tomcat.jni.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Security;
import java.util.*;

import static java.util.UUID.randomUUID;

@Controller
public class AdminRequestController {
    @Autowired
    private ReqestMapper reqestMapper;
    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private CommunityMapper communityMapper;

    @Autowired
    private WeightMapper weightMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private RespondMapper respondMapper;

//    String updateRequestGuid1 = null;
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
    //左边代发请求按钮
    @RequestMapping(value = "/createRequestByAdminView")
        public String createRequestByAdminView(Model model) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

        //插入type和weight
        insertReqType(model,true);
        return "createRequestByAdminView";
    }
    //账号名是否存在
    @RequestMapping(value = "/jquery/exist6.do")
    @ResponseBody
    public String checkUserAccount(String reqIssueUserGuid){
        //遍历数据库 查找是否有账号
        UsersExample usersExample=new UsersExample();
        List<Users> users=usersMapper.selectByExample(usersExample);
        boolean result = false;
        Map<String, Boolean> map = new HashMap<>();
        for(Users it:users){
            if(it.getUserAccount().equals(reqIssueUserGuid)){
                result=true;
            }
        }
        System.out.println(reqIssueUserGuid);
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
        System.out.println("就是这里："+resultString);
        return resultString;
    }

    //代发请求界面中的 “保存”按钮  将数据插入到数据库
    @RequestMapping(value = "/createRequestByAdmin")
    public String insertRequest(Reqest reqest, Model model,Errors errors, String jd,String wd) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

        /*************10.18添加 代发请求从地图获取到经纬度并插入到数据库******/
        System.out.println(jd);
        System.out.println(wd);
        if(!(jd!=null||wd!=null)){
            return "createRequestByAdminView";
        }
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(jd);
        stringBuilder.append(",");
        stringBuilder.append(wd);
        stringBuilder.append(",");
        stringBuilder.append(reqest.getReqAddress());
        String add=""+stringBuilder;
        reqest.setReqAddress(add);
        /***************************************/
//        if (!errors.hasErrors()) {
            String userId=users11.getUserGuid();
            //给发布的请求生成一个GUID,作为该请求的唯一标识
            reqest.setReqGuid(randomUUID().toString());
            String AA=reqest.getReqIssueUserGuid();//前端界面的输入值
            UsersExample usersExample1=new UsersExample();
            usersExample1.or().andUserAccountEqualTo(AA);
            List<Users> users2=usersMapper.selectByExample(usersExample1);//根据账号名找到其用户
            Users users3=users2.get(0);
            String userGuid=users3.getUserGuid();//得到用户的guid 并且插入
            reqest.setReqIssueUserGuid(userGuid);

        /*************10.16代发请求之后 请求账户应该扣除估计时间币*****************/
        double premoney=reqest.getReqPreCunsumeCurrency();
        double userOwnCurrency = users3.getUserOwnCurrency();
//        if (userOwnCurrency<0) {
//            return "wrongCurrency";
//        }
        if (userOwnCurrency<0) {
            return "wrongCurrency";
        }else {
            //如果时间币大于0，就先从用户拥有时间币中扣除估计消耗的时间币/
            System.out.println("代发请求是否进入判断？？？？？？？？？");
            Double newuserOwnCurrency=userOwnCurrency-premoney;
            System.out.println("代发请求此时的时间币因该是？"+newuserOwnCurrency);
            //将扣除后的时间币设置到用户的拥有时间币中/
            users3.setUserOwnCurrency(newuserOwnCurrency);
            usersMapper.updateByPrimaryKeySelective(users3);
        }
        /******************************/

//            reqest.setReqIssueUserGuid("48abce9f-36f4-4ddb-9891-10842434a688");
            //因为是代发请求,无需审核,提出请求时间和请求发布时间可用一个(当前时间即可)
            Date date = new Date();
            reqest.setReqIssueTime(date);
            reqest.setReqDispatchTime(date);
            //TODO：接收请求的用户的guid    此处暂时无法处理    应该根据数据库实时位置（坐标）字段来判断谁可以看到   此处先随机添加一个已知的用户
//            reqest.setReqTargetsUserGuid("6807de66-a917-4aa6-aca5-6673245684ed");
            //管理员的guid
            reqest.setReqProcessUserGuid(userId);
            //管理员代发请求,无需审核,请求批准状态和请求处理状态为"通过"和"未启动"
            reqest.setReqTypeApproveStatus("88888888-94e3-4eb7-aad3-111111111111");
            reqest.setReqTypeGuidProcessStatus("33333333-94e3-4eb7-aad3-111111111111");
            reqestMapper.insert(reqest);
//        }
        return "listRequestByAdminView";
    }

    //查看详情
    @RequestMapping(value = "/showReqestDetailViewByAdmin/{reqGuid}")
    public String showTeacherView1(@PathVariable String reqGuid, Model model, UpdateList updateList) {
  //      updateRequestGuid1 = reqGuid;
        model.addAttribute("reqGuid",reqGuid);
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);

        //请求分类
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidClass());
        List<Type> types = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(types.get(0).getTypeTitle());
        //选择请求紧急程度
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidUrgency());
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeTitle());
        //处理请求批准状态
        String approveId = reqest.getReqTypeApproveStatus();
        //String processId = reqest1.getReqTypeGuidProcessStatus();
        if (approveId != null)
        {
            System.out.println("进入查看详情判断请求批准状态");
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(approveId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
            //reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        //处理请求处理状态
        //String approveId = reqest.getReqTypeApproveStatus();
        String processId = reqest.getReqTypeGuidProcessStatus();
        if (processId != null)
        {
            System.out.println("进入查看详情判断请求处理状态");
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(processId); //筛选
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            //reqest.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
            reqest.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        //TODO 请求批准状态为待审核
        if (approveId.equals("88888888-94E3-4EB7-AAD3-333333333333") )
        {
            System.out.println("逻辑判断请求批准状态为待审核");
            updateList.setUpdateId(0);
            updateList.setDeletaId(1);
            updateList.setStartId(0);
            updateList.setViewVolId(0);
            updateList.setFinishId(0);
            updateList.setUnFinishId(0);
            updateList.setWaitId(0);
            updateList.setEvaluateId(0);
            updateList.setShenhe(1);
            model.addAttribute("updateList",updateList);
        }else if(approveId.equals( "88888888-94E3-4EB7-AAD3-222222222222"))
        {
            System.out.println("逻辑判断请求批准状态为驳回");
            //TODO 请求批准状态为驳回
            updateList.setUpdateId(0);
            updateList.setDeletaId(1);
            updateList.setStartId(0);
            updateList.setViewVolId(0);
            updateList.setFinishId(0);
            updateList.setUnFinishId(0);
            updateList.setWaitId(0);
            updateList.setShenhe(0);
            updateList.setEvaluateId(0);
            model.addAttribute("updateList",updateList);
        }else if (approveId.equals("88888888-94E3-4EB7-AAD3-111111111111"))
        {
//            System.out.println("逻辑判断请求批准状态为通过");
//            //TODO 请求批准状态为通过
//            updateList.setUpdateId(1);
//            updateList.setDeletaId(1);
//            updateList.setStartId(0);
//            updateList.setViewVolId(0);
//            updateList.setFinishId(0);
//            updateList.setUnFinishId(0);
//            updateList.setWaitId(1);
//            updateList.setEvaluateId(0);
//            updateList.setShenhe(0);
//            model.addAttribute("updateList",updateList);
//        }
//        else {
            if (processId.equals("33333333-94E3-4EB7-AAD3-666666666666"))
            {
                System.out.println("逻辑判断请求处理状态为撤销");
                //TODO 请求处理状态为撤销
                updateList.setUpdateId(0);
                updateList.setStartId(0);
                updateList.setViewVolId(1);
                updateList.setFinishId(0);
                updateList.setUnFinishId(0);
                updateList.setDeletaId(0);
                updateList.setWaitId(0);
                updateList.setEvaluateId(0);
                model.addAttribute("updateList",updateList);
            }else if (processId.equals("33333333-94E3-4EB7-AAD3-111111111111")){
                System.out.println("逻辑判断请求处理状态为未启动");
                //TODO 请求处理状态为未启动

                updateList.setUnFinishId(0);
                updateList.setWaitId(1);
                updateList.setUpdateId(1);
                updateList.setDeletaId(1);
                updateList.setStartId(0);
                updateList.setViewVolId(0);
                updateList.setFinishId(0);
                updateList.setEvaluateId(0);
                model.addAttribute("updateList",updateList);
            }else if (processId.equals("33333333-94E3-4EB7-AAD3-222222222222")){
                System.out.println("逻辑判断请求处理状态为待启动");
                //TODO 请求处理状态为待启动
                updateList.setUpdateId(1);
                updateList.setDeletaId(1);

                updateList.setEvaluateId(0);
                updateList.setStartId(1);
                updateList.setViewVolId(1);
                updateList.setFinishId(0);
                updateList.setUnFinishId(0);
                updateList.setWaitId(0);
                model.addAttribute("updateList",updateList);
            }else if (processId.equals("33333333-94E3-4EB7-AAD3-333333333333")) {
                //TODO 请求处理状态为启动
                updateList.setUpdateId(0);
                updateList.setDeletaId(1);
                updateList.setStartId(0);
                updateList.setViewVolId(1);
                updateList.setWaitId(0);
                updateList.setEvaluateId(0);
                updateList.setFinishId(1);
                updateList.setUnFinishId(1);
                model.addAttribute("updateList",updateList);

            }  else if (processId.equals("33333333-94E3-4EB7-AAD3-555555555555")) {
                //TODO 请求处理状态为未完成

                updateList.setUnFinishId(0);
                updateList.setWaitId(0);
                updateList.setEvaluateId(0);
                updateList.setUpdateId(0);
                updateList.setDeletaId(1);
                updateList.setStartId(0);
                updateList.setViewVolId(1);
                updateList.setFinishId(1);
                model.addAttribute("updateList",updateList);
            }else if (processId.equals("33333333-94E3-4EB7-AAD3-444444444444")) {
                //TODO 请求处理状态为已完成
                updateList.setUpdateId(0);
                updateList.setDeletaId(1);

                updateList.setWaitId(0);
                updateList.setEvaluateId(1);
                updateList.setStartId(0);
                updateList.setViewVolId(1);
                updateList.setFinishId(0);
                updateList.setUnFinishId(0);
                model.addAttribute("updateList",updateList);
            }
            else if (processId.equals("33333333-94E3-4EB7-AAD3-777777777777")) {
                //TODO 请求处理状态为已完成未评价

                updateList.setEvaluateId(1);
                updateList.setUpdateId(0);
                updateList.setDeletaId(1);
                updateList.setStartId(0);
                updateList.setViewVolId(1);
                updateList.setFinishId(0);
                updateList.setUnFinishId(0);
                updateList.setWaitId(0);
                model.addAttribute("updateList",updateList);
            }
        }
        model.addAttribute("reqest",reqest);
        model.addAttribute("reqGuid",reqGuid);
        return "listRequsetModelByAdm";
    }
    //管理员修改批准状态 通过
    @RequestMapping(value="/changestatuesa")
    public String changestatuea(Model model,UpdateList updateList,String reqGuid7){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        //TODO 根据传递过来的reqGuid
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid7);
        reqest.setReqTypeApproveStatus("88888888-94e3-4eb7-aad3-111111111111");
        //设置请求处理人
        reqest.setReqProcessUserGuid(users11.getUserGuid());

        reqestMapper.updateByPrimaryKeySelective(reqest);
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidClass());
        List<Type> types = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(types.get(0).getTypeTitle());
        //选择请求紧急程度
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidUrgency());
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeTitle());
        //处理请求批准状态

        String approveId = reqest.getReqTypeApproveStatus();
        if (approveId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(approveId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
        }
        //处理请求处理状态
        String processId = reqest.getReqTypeGuidProcessStatus();
        if (processId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(processId); //筛选
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        updateList.setEvaluateId(0);
        updateList.setUpdateId(0);
        updateList.setDeletaId(0);
        updateList.setStartId(0);
        updateList.setViewVolId(0);
        updateList.setFinishId(0);
        updateList.setUnFinishId(0);
        updateList.setWaitId(0);
        model.addAttribute("updateList",updateList);

        model.addAttribute("reqest",reqest);

        return "listRequsetModelByAdm";
    }
    //管理员修改批准状态 驳回
    @RequestMapping(value="/changestatuesb")
    public String changestatueb(Model model,UpdateList updateList,String reqGuid6){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        //TODO 根据传递过来的reqGuid
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid6);
        reqest.setReqTypeApproveStatus("88888888-94e3-4eb7-aad3-222222222222");
        reqestMapper.updateByPrimaryKeySelective(reqest);
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidClass());
        List<Type> types = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(types.get(0).getTypeTitle());
        //选择请求紧急程度
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidUrgency());
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeTitle());
        //处理请求批准状态

        String approveId = reqest.getReqTypeApproveStatus();
        if (approveId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(approveId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
        }
        //处理请求处理状态
        String processId = reqest.getReqTypeGuidProcessStatus();
        if (processId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(processId); //筛选
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        updateList.setEvaluateId(0);
        updateList.setUpdateId(0);
        updateList.setDeletaId(0);
        updateList.setStartId(0);
        updateList.setViewVolId(0);
        updateList.setFinishId(0);
        updateList.setUnFinishId(0);
        updateList.setWaitId(0);
        model.addAttribute("updateList",updateList);

        model.addAttribute("reqest",reqest);

        return "listRequsetModelByAdm";
    }
    //查看详情界面中的更新请求
    @RequestMapping(value = "/updateREQEST1")
    public String updateREQEST1(UpdateList updateList,Model model,String reqGuid5) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        //TODO 根据传递过来的reqGuid
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid5);
        model.addAttribute("reqest",reqest);

        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGroupIdEqualTo(5);
        List<Type> typex = typeMapper.selectByExample(typeExample);
        model.addAttribute("types",typex);
        typeExample.clear();
        typeExample.or().andTypeGroupIdEqualTo(4);
        List<Type> types = typeMapper.selectByExample(typeExample);
        model.addAttribute("types1",types);
        //TODO 点击更新请求按钮以后
        updateList.setUpdateId(0);
        updateList.setDeletaId(1);
        updateList.setStartId(0);
        updateList.setViewVolId(1);
        updateList.setFinishId(0);
        updateList.setUnFinishId(0);
        updateList.setWaitId(1);
        updateList.setEvaluateId(0);
        model.addAttribute("updateList",updateList);
        return "updateResrestViewByAd";

    }
    @RequestMapping(value = "/updateREQESTSaave")
    public String updateREQESTSaave(Reqest reqest1, Model model){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        reqestMapper.updateByPrimaryKeySelective(reqest1);
        System.out.println("hahahahah");
//        reqestMapper.updateByPrimaryKey(reqest);
        return "listRequestByAdminView";
    }
    //管理员将老人进行撤销操作
    @RequestMapping(value = "/deleteREQEST1")
    public String deleteRESPOND1 (UpdateList updateList,Model model,String reqGuid4) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid4);
        //更新请求表
        reqest.setReqTypeGuidProcessStatus("33333333-94E3-4EB7-AAD3-666666666666");
        reqestMapper.updateByPrimaryKeySelective(reqest);
        //TODO 老人进行了撤单的同时，将所有申请过这个单的志愿者的状态也改为撤单，这个撤单是老人的撤单，不是志愿者的撤单
        RespondExample respondExample = new RespondExample();
        respondExample.or().andResReqGuidEqualTo(reqGuid4);
        List<Respond> responds = respondMapper.selectByExample(respondExample);
        if (responds!=null && responds.size()!=0)
        {
            for (Respond respondAfter : responds)
            {
//                respondAfter.setResTypeGuidProcessStatus("33333333-94E3-4EB7-AAD3-666666666666");
                respondAfter.setResTypeGuidProcessStatus("77777777-94E3-4EB7-AAD3-555555555555");
                respondMapper.updateByPrimaryKey(respondAfter);
            }
        }
        //TODO 点击撤销按钮以后
        updateList.setUpdateId(0);
        updateList.setDeletaId(0);
        updateList.setStartId(0);
        updateList.setViewVolId(1);
        updateList.setFinishId(0);
        updateList.setUnFinishId(0);
        updateList.setWaitId(0);
        updateList.setEvaluateId(0);
        model.addAttribute("updateList",updateList);

        return "listRequestByAdminView";
    }
    //查看志愿者接单情况
    @RequestMapping(value = "/volunteerListOfApply1")
    public String volunteerListOfApply1 (Model model,String reqGuidvolunteer) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        model.addAttribute("message",reqGuidvolunteer);
//        reqGuidOfVol = reqGuid6;
        return "volunteerListByAd";
    }
    //查看志愿者接单情况 从后台获取数据
    @RequestMapping(value="/getVolunteerListJsonData1",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getVolunteerListJsonData1(@RequestParam int offset, int limit, String sortName, String sortOrder,String reqGuid){
        System.out.println(222222);
        System.out.println(reqGuid);
        RespondExample respondExample=new RespondExample();
        respondExample.clear();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            respondExample.setOrderByClause(order);
        }
        Reqest reqest=reqestMapper.selectByPrimaryKey(reqGuid);
        respondExample.clear();
        respondExample.or().andResReqGuidEqualTo(reqGuid);
//        respondExample.or().andResReqGuidEqualTo();
        List<Respond> responds=respondMapper.selectByExample(respondExample);
        List<Respond> respondRecordList=new ArrayList<>();
        /**********10.19添加和用户系统 我的需求列表 查看详情 查看志愿者 代码相同**（开始）*************/
        for(int i=offset;i< offset+limit&&i < responds.size();i++){
            Respond respond1=responds.get(i);
            /************10.17添加关于信用度显示****************/
            String userResID=respond1.getResUserGuid();
//            System.out.println("查信用度的用户是："+userResID);
            RespondExample respondExample1 = new RespondExample();
            respondExample1.or().andResUserGuidEqualTo(userResID);
            List<Respond> respondList=respondMapper.selectByExample(respondExample1);
            int credit=0;
            int totalScore=0;
            int count=0;
            for (Respond res:respondList) {
                String userResListId=res.getResUserGuid();
                if (userResID.equals(userResListId)){
                    if (res.getResEvaluate()!=null){
                        totalScore+=res.getResEvaluate();
//                        System.out.println("该用户的分数累加为："+totalScore);
                        count++;
//                        System.out.println("该用户在响应列表中的累加计数为："+count);
                    }
                }
            }
            if(count!=0){
                credit=totalScore/count;
            }
            Users userSearch=usersMapper.selectByPrimaryKey(userResID);
            userSearch.setUserCredit(credit);
            usersMapper.updateByPrimaryKeySelective(userSearch);
            respond1.setResReqStartUserAccount(credit+"");

            /*******************/
            TypeExample typeExample = new TypeExample();
            String resUserGuid=respond1.getResUserGuid();
            UsersExample usersExample = new UsersExample();
            usersExample.or().andUserGuidEqualTo(resUserGuid);
            List<Users> users = usersMapper.selectByExample(usersExample);
            respond1.setResUserGuid(users.get(0).getUserAccount());
            String resTypeGuidProcessStatus=respond1.getResTypeGuidProcessStatus();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(resTypeGuidProcessStatus);
            List<Type> types = typeMapper.selectByExample(typeExample);
            respond1.setResTypeGuidProcessStatus(types.get(0).getTypeTitle());
            respond1.setResReqTitle(reqest.getReqTitle());
            respondRecordList.add(respond1);

        }
        /*************10.19添加修改（结尾）****************/

        /***10.19添加修改 一下代码为注释掉的原始代码**/
/*        for(int i=offset;i< offset+limit&&i < responds.size();i++){
            Respond respond1=responds.get(i);
            TypeExample typeExample = new TypeExample();
            String resUserGuid=respond1.getResUserGuid();
            UsersExample usersExample = new UsersExample();
            usersExample.or().andUserGuidEqualTo(resUserGuid);
            List<Users> users = usersMapper.selectByExample(usersExample);
            respond1.setResUserGuid(users.get(0).getUserAccount());
            String resTypeGuidProcessStatus=respond1.getResTypeGuidProcessStatus();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(resTypeGuidProcessStatus);
            List<Type> types = typeMapper.selectByExample(typeExample);
            respond1.setResTypeGuidProcessStatus(types.get(0).getTypeTitle());

            *//****10.19添加**//*
            String resReqGuid=respond1.getResReqGuid();
            ReqestExample reqestExample=new ReqestExample();
            reqestExample.or().andReqGuidEqualTo(resReqGuid);
            List<Reqest> reqests=reqestMapper.selectByExample(reqestExample);
            Reqest reqest=reqests.get(0);
            respond1.setResReqTitle(reqest.getReqTitle());
            respondRecordList.add(respond1);
        }*/
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
    //查看详情界面中的  申请待启动按钮
    @RequestMapping(value = "/waitRequest1")
    public String waitRequest1 (UpdateList updateList, Model model,String reqGuid4) {
        System.out.println(reqGuid4);
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid4);
        //设置请求处理状态为待启动
        reqest.setReqTypeGuidProcessStatus("33333333-94E3-4EB7-AAD3-222222222222");
        reqestMapper.updateByPrimaryKey(reqest);
        //请求分类
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidClass());
        List<Type> types = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(types.get(0).getTypeTitle());
        //选择请求紧急程度
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidUrgency());
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeTitle());
        //处理请求处理状态
        String approveId = reqest.getReqTypeApproveStatus();
        //String processId = reqest1.getReqTypeGuidProcessStatus();
        if (approveId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(approveId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
            //reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        //处理请求批准状态
        String processId = reqest.getReqTypeGuidProcessStatus();
        //String processId = reqest1.getReqTypeGuidProcessStatus();
        if (processId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(processId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
            //reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }

        model.addAttribute("reqest",reqest);
        //TODO 点击待启动按钮之后
        updateList.setUpdateId(0);
        updateList.setDeletaId(1);
        updateList.setStartId(1);
        updateList.setViewVolId(1);
        updateList.setFinishId(0);
        updateList.setUnFinishId(0);
        updateList.setWaitId(0);
        updateList.setEvaluateId(0);
        model.addAttribute("updateList",updateList);
        return "listRequsetModelByAdm";
    }

    //查看详情界面中的  申请启动按钮
    @RequestMapping(value = "/startRequest1")
    public String startRequest1 (UpdateList updateList, Model model,String reqGuid1) {
        System.out.println(reqGuid1);
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid1);
        //设置请求处理状态为启动
        reqest.setReqTypeGuidProcessStatus("33333333-94E3-4EB7-AAD3-333333333333");
        //设置请求开始时间为现在
        Date startDate = new Date();
//        dateStart =startDate;
        reqest.setReqStartTime(startDate);
        reqestMapper.updateByPrimaryKey(reqest);
        //请求分类
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidClass());
        List<Type> types = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(types.get(0).getTypeTitle());
        //选择请求紧急程度
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidUrgency());
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeTitle());
        //处理请求处理状态
        String approveId = reqest.getReqTypeApproveStatus();
        //String processId = reqest1.getReqTypeGuidProcessStatus();
        if (approveId != null)
        {

            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(approveId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
            //reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        //处理请求批准状态
        String processId = reqest.getReqTypeGuidProcessStatus();
        //String processId = reqest1.getReqTypeGuidProcessStatus();
        if (processId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(processId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
            //reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        model.addAttribute("reqest",reqest);
        //TODO 点击启动按钮之后
        updateList.setUpdateId(0);
        updateList.setDeletaId(1);
        updateList.setStartId(0);
        updateList.setViewVolId(1);
        updateList.setFinishId(1);
        updateList.setUnFinishId(1);
        updateList.setWaitId(0);
        updateList.setEvaluateId(0);
        model.addAttribute("updateList",updateList);
        return "listRequsetModelByAdm";
    }
    //查看详情界面中的申请已完成按钮
    @RequestMapping(value = "/endRequest1")
    public String endRequest1 (UpdateList updateList, Model model,String reqGuid2) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid2);
        //设置请求处理状态为已完成
        reqest.setReqTypeGuidProcessStatus("33333333-94E3-4EB7-AAD3-444444444444");
        //设置请求结束时间为现在
        Date endDate = new Date();
        reqest.setReqEndTime(endDate);
        //设置请求持续时间，单位为分钟 结束的时间
        long endMiles = endDate.getTime();
        System.out.println("结束时间即现在时间"+endDate);
        //获取启动时候的时间
        System.out.println("数据库中存的开始时间"+reqest.getReqAvailableStartTime());
        long startMiles=reqest.getReqAvailableStartTime().getTime();
//        long startMiles =dateStart.getTime();
//        System.out.println("全局变量中的时间"+dateStart);
        long durTime = (endMiles-startMiles)/(60*1000);
        System.out.println("持续的时间为"+durTime);
        reqest.setReqActualDurationTime(new Long(durTime).intValue());
        reqestMapper.updateByPrimaryKey(reqest);
        //请求分类
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidClass());
        List<Type> types = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(types.get(0).getTypeTitle());
        //选择请求紧急程度
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidUrgency());
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeTitle());
        //处理请求处理状态
        String approveId = reqest.getReqTypeApproveStatus();
        //String processId = reqest1.getReqTypeGuidProcessStatus();
        if (approveId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(approveId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
            //reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        //处理请求批准状态
        String processId = reqest.getReqTypeGuidProcessStatus();
        //String processId = reqest1.getReqTypeGuidProcessStatus();
        if (processId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(processId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
            //reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        model.addAttribute("reqest",reqest);
        //TODO 点击已完成按钮之后
        updateList.setUpdateId(0);
        updateList.setDeletaId(1);
        updateList.setStartId(0);
        updateList.setViewVolId(1);
        updateList.setFinishId(0);
        updateList.setUnFinishId(0);
        updateList.setWaitId(0);
        updateList.setEvaluateId(1);
        model.addAttribute("updateList",updateList);
        return "listRequsetModelByAdm";
    }
    //查看详情界面中的申请未完成按钮
    @RequestMapping(value = "/unEndRequest1")
    public String unEndRequest1 (UpdateList updateList, Model model,String reqGuid3) {
        System.out.println(reqGuid3);
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid3);
        reqest.setReqTypeGuidProcessStatus("33333333-94E3-4EB7-AAD3-555555555555");
        reqestMapper.updateByPrimaryKey(reqest);
        //请求分类
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidClass());
        List<Type> types = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(types.get(0).getTypeTitle());
        //选择请求紧急程度
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqest.getReqTypeGuidUrgency());
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeTitle());
        //处理请求处理状态
        String approveId = reqest.getReqTypeApproveStatus();
        //String processId = reqest1.getReqTypeGuidProcessStatus();
        if (approveId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(approveId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
            //reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        //处理请求批准状态
        String processId = reqest.getReqTypeGuidProcessStatus();
        //String processId = reqest1.getReqTypeGuidProcessStatus();
        if (processId != null)
        {
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(processId);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
        }
        model.addAttribute("reqest",reqest);
        //TODO 点击未完成按钮之后
        updateList.setUpdateId(0);
        updateList.setDeletaId(1);
        updateList.setStartId(0);
        updateList.setViewVolId(1);
        updateList.setFinishId(1);
        updateList.setUnFinishId(0);
        updateList.setWaitId(0);
        updateList.setEvaluateId(1);
        model.addAttribute("updateList",updateList);
        return "listRequsetModelByAdm";
    }

/*----------------------我是下划线,以下为查询数据库并展示数据---------------------*/

   //左边查看各种请求
    @RequestMapping(value = "/listRequestByAdminView/{requestState}")
    public String listRequestByAdminView( Model model, @PathVariable String requestState) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

        model.addAttribute("requestState",requestState);
        return "listRequestByAdminView";
    }

  @RequestMapping(value = "/getREQESTListJsonDataByAdmin", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public String getREQESTListJsonDataByAdmin(Model model, int offset, int limit, String sortName, String sortOrder,String searchText,String requestState) {
      Subject account = SecurityUtils.getSubject();
      UsersExample usersExample = new UsersExample();
      usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
      List<Users> users1 = usersMapper.selectByExample(usersExample);
      Users users2 = users1.get(0);
      String role2 = users2.getUserRole();
      model.addAttribute("role", role2);
      if (searchText == "") {
          searchText = null;
      }
      ReqestExample reqestExample = new ReqestExample();
      reqestExample.clear();
      //处理排序信息
      if (sortName != null) {
          //拼接字符串
          String order = GetDatabaseFileName(sortName) + " " + sortOrder;
          //将排序信息添加到example中
          reqestExample.setOrderByClause(order);
      }
      if (requestState.equals("1")){//待审核
          reqestExample.or().andReqTypeApproveStatusEqualTo("88888888-94e3-4eb7-aad3-333333333333");
      }
      if (requestState.equals("2")){//通过
          reqestExample.or().andReqTypeApproveStatusEqualTo("88888888-94e3-4eb7-aad3-111111111111");
      }
      if (requestState.equals("3")){//驳回
          reqestExample.or().andReqTypeApproveStatusEqualTo("88888888-94e3-4eb7-aad3-222222222222");
      }
      if (requestState.equals("4")){//未启动
          reqestExample.or().andReqTypeGuidProcessStatusEqualTo("33333333-94e3-4eb7-aad3-111111111111");
      }
      if (requestState.equals("5")){//待启动
          reqestExample.or().andReqTypeGuidProcessStatusEqualTo("33333333-94e3-4eb7-aad3-222222222222");
      }
      if (requestState.equals("6")){//启动
          reqestExample.or().andReqTypeGuidProcessStatusEqualTo("33333333-94e3-4eb7-aad3-333333333333");
      }
      if (requestState.equals("7")){//未完成
          reqestExample.or().andReqTypeGuidProcessStatusEqualTo("33333333-94e3-4eb7-aad3-555555555555");
      }
      if (requestState.equals("8")){//已完成
          reqestExample.or().andReqTypeGuidProcessStatusEqualTo("33333333-94e3-4eb7-aad3-444444444444");
      }
      if (requestState.equals("9")){//已撤单
          reqestExample.or().andReqTypeGuidProcessStatusEqualTo("33333333-94e3-4eb7-aad3-666666666666");
      }
      List<Reqest> reqests=reqestMapper.selectByExample(reqestExample);
      for (int i = 0;i<reqests.size(); i++) {
          Reqest reqest =reqests.get(i);
          //请求处理状态
          String processStatus = reqest.getReqTypeGuidProcessStatus();
          TypeExample typeExample = new TypeExample();
          typeExample.or().andTypeGuidEqualTo(processStatus);
          List<Type> processStatusType = typeMapper.selectByExample(typeExample);
          reqest.setReqTypeGuidProcessStatus(processStatusType.get(0).getTypeTitle());

          //用户名
          String reqAccount=reqest.getReqIssueUserGuid();
          UsersExample usersExample1=new UsersExample();
          usersExample1.or().andUserGuidEqualTo(reqAccount);
          List<Users> users=usersMapper.selectByExample(usersExample1);
          Users users3=users.get(0);
          reqest.setReqIssueUserGuid(users3.getUserAccount());

          //请求分类
          String reqTypeGuidClass=reqest.getReqTypeGuidClass();
          TypeExample typeExample1=new TypeExample();
          typeExample1.or().andTypeGuidEqualTo(reqTypeGuidClass);
          List<Type> reqTypeGuidClassType=typeMapper.selectByExample(typeExample1);
          Type Type1=reqTypeGuidClassType.get(0);
          reqest.setReqTypeGuidClass(Type1.getTypeTitle());

          //请求批准状态
          String approveStatus=reqest.getReqTypeApproveStatus();
          TypeExample typeExample2=new TypeExample();
          typeExample2.or().andTypeGuidEqualTo(approveStatus);
          List<Type> approveType=typeMapper.selectByExample(typeExample2);
          Type Type2=approveType.get(0);
          reqest.setReqTypeApproveStatus(Type2.getTypeTitle());

          //请求紧急程度
          String urgent=reqest.getReqTypeGuidUrgency();
          TypeExample typeExample3=new TypeExample();
          typeExample3.or().andTypeGuidEqualTo(urgent);
          List<Type> urgentType=typeMapper.selectByExample(typeExample3);
          Type type3=urgentType.get(0);
          reqest.setReqTypeGuidUrgency(type3.getTypeTitle());

          //请求权值
          String weight=reqest.getReqFromWeightGuid();
          WeightExample weightExample=new WeightExample();
          weightExample.or().andWeightGuidEqualTo(weight);
          List<Weight> weights=weightMapper.selectByExample(weightExample);
          Weight weight1=weights.get(0);
          reqest.setReqFromWeightGuid(weight1.getWeightTitle());

          //请求处理人
          String processUserId=reqest.getReqProcessUserGuid();
          UsersExample usersExample12=new UsersExample();
          usersExample12.or().andUserGuidEqualTo(processUserId);
          List<Users> processuser =usersMapper.selectByExample(usersExample12);
          Users users4 =processuser.get(0);
          reqest.setReqProcessUserGuid(users4.getUserAccount());

      }

      List<Reqest> reqestsNew = new ArrayList<>();//搜索框集合
      List<Reqest> reqestsReturn = new ArrayList<>();//分页返回的集合
      for (int i = 0;i<reqests.size();i++){
          Reqest reqest =reqests.get(i);
          if (searchText != null) {

              String reqTitle = reqest.getReqTitle();//标题搜索
              if(!(reqTitle!=null)){
                  reqTitle="";
//                  System.out.println(55555);
              }
              String reqUserAccount = reqest.getReqIssueUserGuid();//账户搜索
              if(!(reqUserAccount!=null)){
                  reqUserAccount="";
              }
              String reqAddress = reqest.getReqAddress();//地址搜索
              if(!(reqAddress!=null)){
                  reqAddress="";
              }
              String reqClass=reqest.getReqTypeGuidClass();//请求分类搜索
              if(!(reqClass!=null)){
                  reqClass="";
              }
              String reqUrgent=reqest.getReqTypeGuidUrgency();//紧急程度搜索
              if(!(reqUrgent!=null)){
                  reqUrgent="";
              }
              String reqDescribe=reqest.getReqDesp();//描述搜索
              if(!(reqDescribe!=null)){
                  reqDescribe="";
              }
              String reqComm=reqest.getReqComment();//补充搜索
              if(!(reqComm!=null)){
                  reqComm="";
              }
              if (reqTitle.contains(searchText) || reqUserAccount.contains(searchText) || reqAddress.contains(searchText)||reqClass.contains(searchText)||reqUrgent.contains(searchText)||reqDescribe.contains(searchText)||reqComm.contains(searchText)) {
                  reqestsNew.add(reqest);
              }
          }else {
              reqestsNew.add(reqest);
          }
      }
      //分页，一页数据最多20个
      System.out.println(reqests.size());
      for (int i = offset;i<offset+limit&&i<reqestsNew.size();i++){
          reqestsReturn.add(reqestsNew.get(i));
      }
      //全部符合要求的数据的数量
      int total = reqestsNew.size();
      System.out.println("总数："+total);
      //将所得集合打包
      ObjectMapper mapper = new ObjectMapper();
      com.timebank.controller.sxq.TableRecordsJson tableRecordsJson = new com.timebank.controller.sxq.TableRecordsJson(reqestsReturn, total);
      //将实体类转换成json数据并返回
      try {
          String json1 = mapper.writeValueAsString(tableRecordsJson);
          System.out.println("json");
//            System.out.println(json1);
          System.out.println("每次上传20条记录");
          return json1;
      } catch (Exception e) {
          return null;
      }
      //查看所有请求
//        return getJsonDate(offset, limit, sortName, sortOrder, reqestExample);
  }
/*----------------------------------------------------------我是下划线,以下为工具类,可单独封装-------------------------------------------------------*/

//    private void getReqExample(ReqestExample reqestExample, TypeExample typeExample, int typeId, String typeTitle) {
//        typeExample.or().andTypeGroupIdEqualTo(typeId);
//        String typeGuid = "";
//        for (Type type : typeMapper.selectByExample(typeExample)) {
//            if (type.getTypeTitle().equals(typeTitle)) {
//                typeGuid = type.getTypeGuid();
//            }
//        }
//        if (typeId == 3) {
//            reqestExample.or().andReqTypeGuidProcessStatusEqualTo(typeGuid);
//        } else if (typeId == 8) {
//            reqestExample.or().andReqTypeApproveStatusEqualTo(typeGuid);
//        }
//    }

    /**
     * 通过条件查找对应json数据,并实现排序功能
     */
    private String getJsonDate(@RequestParam int offset, int limit, String sortName, String sortOrder, ReqestExample reqestExample) {
        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            //将排序信息添加到example中
            reqestExample.setOrderByClause(order);
        }

        List<Reqest> reqests = reqestMapper.selectByExample(reqestExample);
        List<Reqest> reqestRecordList = new ArrayList<>();
        for (int i = offset; i < offset + limit && i < reqests.size(); i++) {
            Reqest reqest = reqests.get(i);
            /*处理请求分类(将展示TypeGuid转换为展示TypeTitle)*/
            String reqTypeGuidClass = reqest.getReqTypeGuidClass();
            TypeExample typeExample = new TypeExample();
            typeExample.or().andTypeGuidEqualTo(reqTypeGuidClass);
            List<Type> types = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeGuidClass(types.get(0).getTypeTitle());

            typeExample.clear();
            /*处理请求紧急状态分类(将展示TypeGuid转换为展示TypeTitle)*/
            String reqTypeGuidUrgency = reqest.getReqTypeGuidUrgency();
            typeExample.or().andTypeGuidEqualTo(reqTypeGuidUrgency);
            List<Type> types1 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeGuidUrgency(types1.get(0).getTypeTitle());

            typeExample.clear();
            /*处理请求状态分类(将展示TypeGuid转换为展示TypeTitle)*/
            String reqTypeGuidProcessStatus = reqest.getReqTypeGuidProcessStatus();
            typeExample.or().andTypeGuidEqualTo(reqTypeGuidProcessStatus);
            List<Type> types2 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());

            typeExample.clear();
            /*处理请求批准状态分类(将展示TypeGuid转换为展示TypeTitle)*/
            String reqTypeApproveStatus = reqest.getReqTypeApproveStatus();
            typeExample.or().andTypeGuidEqualTo(reqTypeApproveStatus);
            List<Type> types3 = typeMapper.selectByExample(typeExample);
            reqest.setReqTypeApproveStatus(types3.get(0).getTypeTitle());

            /*处理请求权重分类(将展示WeightGuid转换为展示WeightTitle)*/
//            String reqFromWeightGuid = reqest.getReqFromWeightGuid();
//            WeightExample weightExample = new WeightExample();
//            weightExample.or().andWeightGuidEqualTo(reqFromWeightGuid);
//            List<Weight> weights = weightMapper.selectByExample(weightExample);
//            reqest.setReqFromWeightGuid(weights.get(0).getWeightTitle());

            //把接收者的Id换成名字显示在页面
            String reqTargetUsers = reqest.getReqTargetsUserGuid();
            String sb1 = IdtoName(reqTargetUsers);
            reqest.setReqTargetsUserGuid(sb1);
            //把请求者的Id换成名字显示在页面
            String issueUser = reqest.getReqIssueUserGuid();
            String sb2 = IdtoName(issueUser);
            reqest.setReqIssueUserGuid(sb2);
            //把请求处理人的Id转换成名字；
            String issueProcessPerson = reqest.getReqProcessUserGuid();
            String sb3 = IdtoName(issueProcessPerson);
            reqest.setReqProcessUserGuid(sb3);


            reqestRecordList.add(reqest);
        }
        //全部符合要求的数据的数量
        int total = reqestRecordList.size();
        //System.out.println("总数："+total);
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(reqestRecordList, total);
        //将实体类转换成json数据并返回
        try {
            String json = mapper.writeValueAsString(tableRecordsJson);
//            System.out.println("jaon:" + json);
            return json;
        } catch (Exception e) {
            return null;
        }
    }

   //名字转换函数
    public String IdtoName(String reqtargetuser) {
        if (reqtargetuser != null) {
            UsersExample usersExample = new UsersExample();

            StringBuilder sb = new StringBuilder();
            //正则表达式，取出方括号里面的值
            String r = reqtargetuser.replaceAll("^.*\\[", "").replaceAll("].*", "");
            //     System.out.println(r);
            //分割字符串
            String[] array = r.split(",");
            String respondName = null;
            for (int in = 0; in < array.length; in++) {
                //去掉双引号
                String jj = array[in].replaceAll("\"", "");
                usersExample.clear();
                usersExample.or().andUserGuidEqualTo(jj);
                List<Users> responduser = usersMapper.selectByExample(usersExample);
                respondName = responduser.get(0).getUserName();
                sb.append(respondName).append(" ");
            }
            String sb1 = sb.toString();
            return  sb1;
        }
        return null;
    }
    /**
     * 根据不同的请求处理状态显示和隐藏保存按钮
     * 只有在请求状态为待审核,或请求通过,请求处理状态为未启动时才可以显示保存按钮,目的是选择添加接收该请求的对象
     */
    private void isHiddenBtnSubmit(@PathVariable String reqGuid, Model model, TypeExample typeExample) {
        ReqestExample reqestExample = new ReqestExample();
        reqestExample.or().andReqGuidEqualTo(reqGuid);
        List<Reqest> reqests = reqestMapper.selectByExample(reqestExample);
        String reqTypeApproveStatus = reqests.get(0).getReqTypeApproveStatus();
        String reqTypeGuidProcessStatus = reqests.get(0).getReqTypeGuidProcessStatus();

        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqTypeApproveStatus);
        List<Type> types = typeMapper.selectByExample(typeExample);
        String typeTitle1= types.get(0).getTypeTitle();

        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(reqTypeGuidProcessStatus);
        String typeTitle2 = typeMapper.selectByExample(typeExample).get(0).getTypeTitle();
        if ("待审核".equals(typeTitle1)) {
            model.addAttribute("isHidden", false);
        } else if ("通过".equals(typeTitle1) && "未启动".equals(typeTitle2)) {
            model.addAttribute("isHidden", false);
        } else {
            model.addAttribute("isHidden", true);
        }
//        switch (typeTitle) {
//            //待审核 isHidden=false
//            case "待审核":
//                model.addAttribute("isHidden", false);
//                break;
//            //通过 驳回 isHidden=true
//            case "通过":
//            case "驳回":
//                model.addAttribute("isHidden", true);
//                break;
//        }
    }

    /**
     * 插入type和weight
     * @param model
     * @return
     */
    private TypeExample insertReqType(Model model,boolean showProAndAppStatus) {
        TypeExample typeExample = new TypeExample();
        if (showProAndAppStatus) {
            //请求处理状态
            typeExample.or().andTypeGroupIdEqualTo(3);
            List<Type> type1 = typeMapper.selectByExample(typeExample);
            model.addAttribute("type1", type1);

            typeExample.clear();
            //请求批准状态
            typeExample.or().andTypeGroupIdEqualTo(8);
            List<Type> type4 = typeMapper.selectByExample(typeExample);
            model.addAttribute("type4", type4);
        }
        typeExample.clear();
        //请求分类
        typeExample.or().andTypeGroupIdEqualTo(4);
        List<Type> type2 = typeMapper.selectByExample(typeExample);
        model.addAttribute("type2", type2);

        typeExample.clear();
        //请求紧急程度
        typeExample.or().andTypeGroupIdEqualTo(5);
        List<Type> type3 = typeMapper.selectByExample(typeExample);
        model.addAttribute("type3", type3);

        //请求权重
        WeightExample weightExample = new WeightExample();
        List<Weight> weights = weightMapper.selectByExample(weightExample);
        model.addAttribute("weights", weights);

        return typeExample;
    }
    /**
     * 后台向前端传入userId和role
     *
     * @param model
     * @param userId
     */
    public void setUserIdandrole(Model model, String userId, Integer role1) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);


    }

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
}
