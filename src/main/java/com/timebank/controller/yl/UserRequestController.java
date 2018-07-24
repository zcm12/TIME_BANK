package com.timebank.controller.yl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ReqestApp;
import com.timebank.appmodel.ResultModel;
import com.timebank.domain.*;
import com.timebank.mapper.ReqestMapper;
import com.timebank.mapper.RespondMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

@Controller
public class UserRequestController {
    @Autowired
    private ReqestMapper reqestMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private RespondMapper respondMapper;
    @Autowired
    private UsersMapper usersMapper;
    String reqGuidOfVol = null;
    // 请求的guid
    String updateRequestGuid = null;
    //时间
    Date dateStart = null;
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
    //besepage页面的发布请求
    @RequestMapping(value = "/createRequestByUserView")
    public String userApply(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        //请求分类
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGroupIdEqualTo(4);
        List<Type> types = typeMapper.selectByExample(typeExample);
        model.addAttribute("types",types);
        //选择请求紧急程度
        typeExample.clear();
        typeExample.or().andTypeGroupIdEqualTo(5);
        List<Type> typex = typeMapper.selectByExample(typeExample);
        model.addAttribute("typex",typex);
        return "apply";

    }
    @RequestMapping(value = "/applySubmit")
    public String applySubmit(Reqest reqest, Model model)
    {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);

        //请求提交
        UUID guid=randomUUID();
        reqest.setReqGuid(guid.toString());
        reqest.setReqIssueUserGuid(users1.getUserGuid());
        //请求提出的时间设定为当前时间
        Date date = new Date();
        reqest.setReqIssueTime(date);
        //将请求批准状态先置为待审核
        Type type = typeMapper.selectByPrimaryKey("88888888-94e3-4eb7-aad3-333333333333");
        reqest.setReqTypeApproveStatus(type.getTypeGuid());
        //将请求处理状态先置为未启动
        Type type1 = typeMapper.selectByPrimaryKey("33333333-94e3-4eb7-aad3-111111111111");
        reqest.setReqTypeGuidProcessStatus(type1.getTypeGuid());
        reqestMapper.insert(reqest);
        return "applyListView";
    }
    // 从数据库加载数据
    @RequestMapping(value="/getREQESTListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getJsonDataFromReqest(@RequestParam int offset, int limit, String sortName,String sortOrder){
        //获得当前用户
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
//        model.addAttribute("role",role);

        ReqestExample reqestExample=new ReqestExample();
        reqestExample.clear();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            reqestExample.setOrderByClause(order);
        }
        //获取当前登陆者id
        String userID =users11.getUserGuid();
        reqestExample.or().andReqIssueUserGuidEqualTo(userID);
        List<Reqest> reqests=reqestMapper.selectByExample(reqestExample);
        List<Reqest> reqestRecordList=new ArrayList<>();
        for(int i=offset;i< offset+limit&&i < reqests.size();i++){
            Reqest reqest1=reqests.get(i);
            TypeExample typeExample = new TypeExample();
            String reqTypeGuidClass=reqest1.getReqTypeGuidClass();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(reqTypeGuidClass);
            List<Type> types = typeMapper.selectByExample(typeExample);
            reqest1.setReqTypeGuidClass(types.get(0).getTypeTitle());
            String reqTypeGuidUrgency=reqest1.getReqTypeGuidUrgency();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(reqTypeGuidUrgency);
            List<Type> types1 = typeMapper.selectByExample(typeExample);
            reqest1.setReqTypeGuidUrgency(types1.get(0).getTypeTitle());
            //处理请求处理状态
            String approveId = reqest1.getReqTypeApproveStatus();
            if (approveId != null)
            {
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(approveId);
                List<Type> types2 = typeMapper.selectByExample(typeExample);
                reqest1.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
            }
            //处理请求批准状态
            String processId = reqest1.getReqTypeGuidProcessStatus();
            if (processId != null)
            {
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(processId);
                List<Type> types2 = typeMapper.selectByExample(typeExample);
                reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
            }
            reqestRecordList.add(reqest1);
        }
        //全部符合要求的数据的数量
        int total=reqests.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(reqestRecordList,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            // System.out.println(json1);
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
    //导航栏左边 查看请求列表
    @RequestMapping(value = "/requestListByUserView")
    public String requestListByUserView(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        return "applyListView";

    }
    //查看请求列表中的查看详情
    @RequestMapping(value = "/listREQESTModel/{reqGuid}")
    public String listREQESTModel (@PathVariable String reqGuid , UpdateList updateList, Model model) {
        System.out.println("这是查看详情");

        updateRequestGuid = reqGuid;
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
            model.addAttribute("updateList",updateList);
        }else if(approveId == "88888888-94E3-4EB7-AAD3-222222222222")
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
            updateList.setEvaluateId(0);
            model.addAttribute("updateList",updateList);
        }else if (approveId.equals("88888888-94E3-4EB7-AAD3-111111111111"))
        {
            System.out.println("逻辑判断请求批准状态为通过");
            //TODO 请求批准状态为通过
            updateList.setUpdateId(1);
            updateList.setDeletaId(1);
            updateList.setStartId(1);
            updateList.setViewVolId(1);
            updateList.setFinishId(1);
            updateList.setUnFinishId(1);
            updateList.setWaitId(1);
            updateList.setEvaluateId(1);
            model.addAttribute("updateList",updateList);
        }
        else {
            if (processId.equals("33333333-94E3-4EB7-AAD3-666666666666"))
            {
                System.out.println("逻辑判断请求处理状态为撤销");
                //TODO 请求处理状态为撤销
                updateList.setUpdateId(0);
                updateList.setDeletaId(0);
                updateList.setStartId(0);
                updateList.setViewVolId(1);
                updateList.setFinishId(0);
                updateList.setUnFinishId(0);
                updateList.setWaitId(0);
                updateList.setEvaluateId(0);
                model.addAttribute("updateList",updateList);
            }else if (processId.equals("33333333-94E3-4EB7-AAD3-111111111111")){
                System.out.println("逻辑判断请求处理状态为未启动");
                //TODO 请求处理状态为未启动
                updateList.setUpdateId(1);
                updateList.setDeletaId(1);
                updateList.setStartId(1);
                updateList.setViewVolId(0);
                updateList.setFinishId(0);
                updateList.setUnFinishId(0);
                updateList.setWaitId(1);
                updateList.setEvaluateId(0);
                model.addAttribute("updateList",updateList);
            }else if (processId.equals("33333333-94E3-4EB7-AAD3-222222222222")){
                System.out.println("逻辑判断请求处理状态为待启动");
                //TODO 请求处理状态为待启动
                updateList.setUpdateId(1);
                updateList.setDeletaId(1);
                updateList.setStartId(1);
                updateList.setViewVolId(1);
                updateList.setFinishId(0);
                updateList.setUnFinishId(0);
                updateList.setWaitId(0);
                updateList.setEvaluateId(0);
                model.addAttribute("updateList",updateList);
            }else if (processId.equals("33333333-94E3-4EB7-AAD3-333333333333")) {
                //TODO 请求处理状态为启动
                updateList.setUpdateId(0);
                updateList.setDeletaId(1);
                updateList.setStartId(0);
                updateList.setViewVolId(1);
                updateList.setFinishId(1);
                updateList.setUnFinishId(1);
                updateList.setWaitId(0);
                updateList.setEvaluateId(0);
                model.addAttribute("updateList",updateList);

            }  else if (processId.equals("33333333-94E3-4EB7-AAD3-555555555555")) {
                //TODO 请求处理状态为未完成
                updateList.setUpdateId(0);
                updateList.setDeletaId(1);
                updateList.setStartId(0);
                updateList.setViewVolId(1);
                updateList.setFinishId(1);
                updateList.setUnFinishId(0);
                updateList.setWaitId(0);
                updateList.setEvaluateId(0);
                model.addAttribute("updateList",updateList);
            }else if (processId.equals("33333333-94E3-4EB7-AAD3-444444444444")) {
                //TODO 请求处理状态为已完成
                updateList.setUpdateId(0);
                updateList.setDeletaId(1);
                updateList.setStartId(0);
                updateList.setViewVolId(1);
                updateList.setFinishId(0);
                updateList.setUnFinishId(0);
                updateList.setWaitId(0);
                updateList.setEvaluateId(1);
                model.addAttribute("updateList",updateList);
            }
            else if (processId.equals("33333333-94E3-4EB7-AAD3-777777777777")) {
                //TODO 请求处理状态为已完成未评价
                updateList.setUpdateId(0);
                updateList.setDeletaId(1);
                updateList.setStartId(0);
                updateList.setViewVolId(1);
                updateList.setFinishId(0);
                updateList.setUnFinishId(0);
                updateList.setWaitId(0);
                updateList.setEvaluateId(1);
                model.addAttribute("updateList",updateList);
            }
        }
        model.addAttribute("reqest",reqest);
//        model.addAttribute("reqGuid",reqGuid);
        return "listRequestModel";
    }
  /*  //查看详情界面中的更新请求
    @RequestMapping(value = "/updateREQEST")
    public String updateREQEST (UpdateList updateList,Model model,String reqGuid1) {

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);

        //TODO 根据传递过来的reqGuid
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid1);
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
        updateList.setUpdateId(1);
        updateList.setDeletaId(1);
        updateList.setStartId(1);
        updateList.setViewVolId(0);
        updateList.setFinishId(0);
        updateList.setUnFinishId(0);
        model.addAttribute("updateList",updateList);
        return "updateReqestView";

    }*/

    //查看详情界面中的更新请求
    @RequestMapping(value = "/updateREQEST")
    public String updateREQEST (UpdateList updateList,Model model) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        //TODO 根据传递过来的reqGuid
        Reqest reqest = reqestMapper.selectByPrimaryKey(updateRequestGuid);
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
        return "updateReqestView";

    }
    @RequestMapping(value = "/updateREQESTSave")
    public String updateREQESTSave(Reqest reqest, Model model){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

        reqestMapper.updateByPrimaryKeySelective(reqest);
        return "applyListView";
    }
    //老人进行撤单操作
    @RequestMapping(value = "/deleteREQEST")
    public String deleteRESPOND (UpdateList updateList,Model model,String reqGuid4) {
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
                respondAfter.setResTypeGuidProcessStatus("33333333-94E3-4EB7-AAD3-666666666666");
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

        return "applyListView";
    }
    //查看志愿者接单情况
    @RequestMapping(value = "/volunteerListOfApply")
    public String volunteerListOfApply (Model model) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        model.addAttribute("message",updateRequestGuid);
//        reqGuidOfVol = reqGuid6;
        return "volunteerList";
    }
    //查看志愿者接单情况 从后台获取数据
    @RequestMapping(value="/getVolunteerListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getVolunteerListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder){
        System.out.println(222222);
        System.out.println(updateRequestGuid);
        RespondExample respondExample=new RespondExample();
        respondExample.clear();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            respondExample.setOrderByClause(order);
        }
        respondExample.or().andResReqGuidEqualTo(updateRequestGuid);
//        respondExample.or().andResReqGuidEqualTo();
        List<Respond> responds=respondMapper.selectByExample(respondExample);
        List<Respond> respondRecordList=new ArrayList<>();
        for(int i=offset;i< offset+limit&&i < responds.size();i++){
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
            respondRecordList.add(respond1);

        }
        //全部符合要求的数据的数量
        int total=responds.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(respondRecordList,total);
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
    @RequestMapping(value = "/waitRequest")
    public String waitRequest (UpdateList updateList, Model model) {
        System.out.println(updateRequestGuid);
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        Reqest reqest = reqestMapper.selectByPrimaryKey(updateRequestGuid);
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
        return "listRequestModel";
    }

    //查看详情界面中的  申请启动按钮
    @RequestMapping(value = "/startRequest")
    public String startRequest (UpdateList updateList, Model model,String reqGuid1) {
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
        return "listRequestModel";
    }
    //查看详情界面中的申请已完成按钮
    @RequestMapping(value = "/endRequest")
    public String endRequest (UpdateList updateList, Model model,String reqGuid2) {
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
        return "listRequestModel";
    }
    //查看详情界面中的申请未完成按钮
    @RequestMapping(value = "/unEndRequest")
    public String unEndRequest (UpdateList updateList, Model model,String reqGuid3) {
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
            //reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
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
        return "listRequestModel";
    }

}
