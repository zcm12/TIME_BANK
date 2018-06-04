package com.timebank.controller.yl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.domain.*;
import com.timebank.mapper.ReqestMapper;
import com.timebank.mapper.RespondMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import org.apache.catalina.mbeans.UserMBean;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class UserResponseController {
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private ReqestMapper reqestMapper;
    @Autowired
    private RespondMapper respondMapper;
    @Autowired
    private UsersMapper usersMapper;
    //左边申请服务导航栏
    @RequestMapping(value = "/createApplyByUserView")
    public String userApply(Model model)
    {

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        return "applyViewOfVolunteer";
    }
    //申请服务 向后台数据库索要数据
    @RequestMapping(value="/getREQESTListJsonDataOfVol",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getJsonDataFromReqest(@RequestParam int offset, int limit, String sortName,String sortOrder){

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);

        ReqestExample reqestExample=new ReqestExample();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            reqestExample.setOrderByClause(order);
        }
        List<Reqest> reqests=reqestMapper.selectByExample(reqestExample);
        List<Reqest> reqests1 = new ArrayList<>();
        //TODO 判断这条请求是不是发给自己的
        String ownId = users1.getUserGuid().toLowerCase();
        for(Reqest re :reqests)
        {
            String userId = re.getReqTargetsUserGuid();
            if (userId==null||userId.length()==0)
            {
                //跳过
            }else {
                //拆分
                String afterStep1 = userId.replace("\"","");
                String afterStep2 = afterStep1.replace("[","");
                String afterStep3 = afterStep2.replace("]","");

                String[] afterSplit = afterStep3.split(",");
                for (String after : afterSplit)
                {
                    //如果这个字段包含自己，那就在reqests中加上
                    if (after.equals(ownId))
                    {
                        reqests1.add(re);
                        break;
                    }
                }
            }
        }
        List<Reqest> reqestRecordList=new ArrayList<>();
        for(int i=offset;i< offset+limit&&i < reqests1.size();i++){
            Reqest reqest1=reqests1.get(i);
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
            //处理请求剩余需要人数
            int personNum =  reqest1.getReqPersonNum();
            //查已经有几个人进行了申请
            RespondExample respondExample = new RespondExample();
            List<Respond> responds = respondMapper.selectByExample(respondExample);
            int personNumber = 0;
            for (Respond respo : responds)
            {
                if (respo.getResReqGuid().equals(reqest1.getReqGuid()) && respo.getResTypeGuidProcessStatus()!="634b1ade-c97a-42a3-8ed0-ca1fc0a6b69a")
                {
                    //说明有一个志愿者了，与所需人数进行对比
                    personNumber = personNumber+1;
                    System.out.println(personNumber);
                }
            }
            reqest1.setReqPersonNum(personNum-personNumber);
            reqestRecordList.add(reqest1);
        }
        //全部符合要求的数据的数量
        int total=reqests1.size();
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
    //服务列表中右边的申请服务按钮
    @RequestMapping(value = "/viewREQEST/{reqGuid}")
    public String updateREQEST (@PathVariable String reqGuid , Model model) {

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        //TODO 根据传递过来的reqGuid
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);
        //处理紧急状态
        String urgencyId = reqest.getReqTypeGuidUrgency();
        Type type = typeMapper.selectByPrimaryKey(urgencyId);
        reqest.setReqTypeGuidUrgency(type.getTypeTitle());
        //处理选择分类
        String typeId = reqest.getReqTypeGuidClass();
        Type type1 = typeMapper.selectByPrimaryKey(typeId);
        reqest.setReqTypeGuidClass(type1.getTypeTitle());
        model.addAttribute("reqest",reqest);
        return "detailsViewOfVolunteer";
    }

    @RequestMapping(value = "/viewREQESTOfUser/{reqGuid}")
    public String viewREQESTOfUser (@PathVariable String reqGuid , Model model) {
        System.out.println("哪里");
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        //TODO 根据传递过来的reqGuid
        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);
        //处理紧急状态
        String urgencyId = reqest.getReqTypeGuidUrgency();
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGuidEqualTo(urgencyId);
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeTitle());
        //处理选择分类
        String typeId = reqest.getReqTypeGuidClass();
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(typeId);
        List<Type> typex2 = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(typex2.get(0).getTypeTitle());
        model.addAttribute("reqest",reqest);
        return "detailsViewOfVolunteer";
    }

    //志愿者点击申请提出服务按钮 applyREQESTofVolunteer
    @RequestMapping(value = "/applyREQESTofVolunteer",method = RequestMethod.POST)
    public String applyREQESTofVolunteer (String resAcceptAddress, Respond respond, Reqest reqest, Model model) {

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);

        UUID guid = UUID.randomUUID();
        respond.setResGuid(guid.toString());
        respond.setResReqGuid(reqest.getReqGuid());
        //userID
        respond.setResUserGuid(users1.getUserGuid());
        //响应接受时间
        Date date = new Date();
        respond.setResAcceptTime(date);
        //响应接受地址
        respond.setResAcceptAddress(resAcceptAddress);
        //只要是申请就是通过，不需要审核
        respond.setResTypeGuidProcessStatus("88888888-94e3-4eb7-aad3-111111111111");
        respondMapper.insert(respond);
        return "responseOfVolunteer";
    }
    //导航栏左边查看服务列表
    @RequestMapping(value = "/applyListByUserView")
    public String applyListByUserView(Model model)
    {

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        return "responseOfVolunteer";
    }

    //查看服务列表后向后台数据索要数据
    @RequestMapping(value="/getRESPONDListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getRESPONDListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder){

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        RespondExample respondExample=new RespondExample();
        respondExample.clear();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            respondExample.setOrderByClause(order);
        }
        //判断自己响应过哪些请求
        String ownId = users1.getUserGuid();
        respondExample.or().andResUserGuidEqualTo(ownId);
        List<Respond> responds=respondMapper.selectByExample(respondExample);
        List<Respond> respondRecordList=new ArrayList<>();
        for(int i=offset;i< offset+limit&&i < responds.size();i++){
            Respond respond1=responds.get(i);
            TypeExample typeExample = new TypeExample();
            if (respond1.getResTypeGuidProcessStatus()!=null) {
                String resTypeGuidProcessStatus = respond1.getResTypeGuidProcessStatus();
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(resTypeGuidProcessStatus);
                List<Type> types = typeMapper.selectByExample(typeExample);
                respond1.setResTypeGuidProcessStatus(types.get(0).getTypeTitle());
            }
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
    //申请服务列表界面 的更新请求
    @RequestMapping(value = "/updateRESPOND/{resGuid}")
    public String updateRESPOND (@PathVariable String resGuid , Reqest reqest, Respond respond, Model model) {

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        //TODO 根据传递过来的resGuid
        respond = respondMapper.selectByPrimaryKey(resGuid);
        model.addAttribute("respond",respond);
        //根据根据传递过来的resGuid找到对应的这一条服务
        reqest = reqestMapper.selectByPrimaryKey(respond.getResReqGuid());
        model.addAttribute("reqest",reqest);
        //处理紧急状态
        String urgencyId = reqest.getReqTypeGuidUrgency();
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGuidEqualTo(urgencyId);
        List<Type> typex = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeTitle());
        //处理选择分类
        String typeId = reqest.getReqTypeGuidClass();
        typeExample.clear();
        typeExample.or().andTypeGuidEqualTo(typeId);
        List<Type> typex2 = typeMapper.selectByExample(typeExample);
        reqest.setReqTypeGuidClass(typex2.get(0).getTypeTitle());
        return "updateViewOfVolunteer";
    }
    //服务详情界面中的更新按钮
    @RequestMapping(value = "/applyRespondOfVolunteer")
    public String applyRespondOfVolunteer (Respond respond, Model model) {

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        //TODO 根据传递过来的reqGuid
        respondMapper.updateByPrimaryKeySelective(respond);
        return "responseOfVolunteer";

    }
    //志愿者进行撤单操作
    @RequestMapping(value = "/deleteRESPOND/{resGuid}")
    public String deleteRESPOND (@PathVariable String resGuid , Respond respond, Model model) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        respond = respondMapper.selectByPrimaryKey(resGuid);
        //更新响应表
        respond.setResTypeGuidProcessStatus("77777777-94e3-4eb7-aad3-555555555555");
        respondMapper.updateByPrimaryKeySelective(respond);
        return "responseOfVolunteer";
    }
}
