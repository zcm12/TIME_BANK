package com.timebank.controller.yl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.catalina.mbeans.UserMBean;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

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
    @Autowired
    private CommunityMapper communityMapper;

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
    /***********************************申请服务********************************************/
    //左边申请服务导航栏
   @RequestMapping(value = "/createApplyByUserView")
    public String userApply(Model model)
    {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        model.addAttribute("guid",users11.getUserGuid());
        return "startmap1";
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

    //申请服务 向后台数据库索要数据
    @RequestMapping(value="/getREQESTListJsonDataOfVol",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getJsonDataFromReqest(@RequestParam int offset, int limit, String sortName,String sortOrder,Model model){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

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
        //TODO 判断这条请求是不是发给自己的 遍历reqest中reqtragetsuserguid（已做完）
        String ownId = users11.getUserGuid().toLowerCase();//转换成小写
        for(Reqest re :reqests)
        {
            if(role.equals("USE")) {
                String userId = re.getReqTargetsUserGuid();
                if (userId == null || userId.length() == 0) {
                    //跳过
                } else {
                    //拆分
                    String afterStep1 = userId.replace("\"", "");
                    String afterStep2 = afterStep1.replace("[", "");
                    String afterStep3 = afterStep2.replace("]", "");

                    String[] afterSplit = afterStep3.split(",");
                    for (String after : afterSplit) {
                        //如果这个字段包含自己，那就在reqests中加上
                        if (after.equals(ownId)) {
                            reqests1.add(re);
                            break;
                        }
                    }
                }
            }else if(role.equals("Tourist")){
                //遍历三天以内发布的请求
                 Date time=re.getReqIssueTime();
                 Date date=new Date();
               Date beforeDate=getDateBefore(date,3);
                 if(time.after(beforeDate)){
                     System.out.println(beforeDate);
                     reqests1.add(re);
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
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
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

//    @RequestMapping(value = "/viewREQESTOfUser/{reqGuid}")
//    public String viewREQESTOfUser (@PathVariable String reqGuid , Model model) {
//        System.out.println("哪里");
//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);
//        String role = users1.getUserRole();
//        model.addAttribute("role",role);
//        //TODO 根据传递过来的reqGuid
//        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);
//        //处理紧急状态
//        String urgencyId = reqest.getReqTypeGuidUrgency();
//        TypeExample typeExample = new TypeExample();
//        typeExample.or().andTypeGuidEqualTo(urgencyId);
//        List<Type> typex = typeMapper.selectByExample(typeExample);
//        reqest.setReqTypeGuidUrgency(typex.get(0).getTypeTitle());
//        //处理选择分类
//        String typeId = reqest.getReqTypeGuidClass();
//        typeExample.clear();
//        typeExample.or().andTypeGuidEqualTo(typeId);
//        List<Type> typex2 = typeMapper.selectByExample(typeExample);
//        reqest.setReqTypeGuidClass(typex2.get(0).getTypeTitle());
//        model.addAttribute("reqest",reqest);
//        return "detailsViewOfVolunteer";
//    }

    //志愿者点击申请提出服务按钮 applyREQESTofVolunteer
    @RequestMapping(value = "/applyREQESTofVolunteer",method = RequestMethod.POST)
    public String applyREQESTofVolunteer (String resAcceptAddress, Respond respond, Reqest reqest, Model model) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        if(role.equals("USE")){
        UUID guid = UUID.randomUUID();
        respond.setResGuid(guid.toString());
        respond.setResReqGuid(reqest.getReqGuid());
        //userID
        respond.setResUserGuid(users11.getUserGuid());
        //响应接受时间
        Date date = new Date();
        respond.setResAcceptTime(date);
        //响应接受地址
        respond.setResAcceptAddress(resAcceptAddress);
        //只要是申请就是通过，不需要审核
        respond.setResTypeGuidProcessStatus("88888888-94e3-4eb7-aad3-111111111111");
        respondMapper.insert(respond);
        return "responseOfVolunteer";
        }else{
            //所属小区
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
    /****************************我的服务****************************************************/
    //导航栏左边查看服务列表（查看已经申请了的请求）
/*    @RequestMapping(value = "/applyListByUserView")
    public String applyListByUserView(Model model)
    {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        return "responseOfVolunteer";
//        return "responseOfVolunteer1";
    }*/
/*    *//****************************我的服务****************************************************//*
    //导航栏左边查看服务列表（查看已经申请了的请求）
    @RequestMapping(value = "/applyListByUserView")
    public String applyListByUserView(Model model)
    {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        return "responseOfVolunteer";
    }

    @RequestMapping(value="/getRESPONDListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getRESPONDListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder,Model model){

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        RespondExample respondExample=new RespondExample();

        respondExample.clear();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            respondExample.setOrderByClause(order);
        }

        //得到自己申请的请求的respond
        respondExample.clear();
        String ownId = users11.getUserGuid();
        respondExample.or().andResUserGuidEqualTo(ownId);
        List<Respond> responds=respondMapper.selectByExample(respondExample);

        List<Reqest> respondRecordList=new ArrayList<>();
        //遍历
        for(int i=offset;i< offset+limit&&i < responds.size();i++){
            Respond respond1=responds.get(i);
            TypeExample typeExample = new TypeExample();
            //得到reqest
            String reqGuid=respond1.getResReqGuid();
            Reqest reqest=reqestMapper.selectByPrimaryKey(reqGuid);
            respondRecordList.add(reqest);
        }*/
    /****************************我的服务****************************************************/
    //导航栏左边查看服务列表（查看已经申请了的请求）
    @RequestMapping(value = "/applyListByUserView")
    public String applyListByUserView(Model model)
    {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        return "responseOfVolunteer";
    }
    @RequestMapping(value="/getRESPONDListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getRESPONDListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder,Model model, String searchText){

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

        /**10.9添加*/
        if (searchText == "") {
            searchText = null;
        }
        /**10.9添加*/

        RespondExample respondExample=new RespondExample();
//        ReqestExample reqestExample=new ReqestExample();
        respondExample.clear();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            respondExample.setOrderByClause(order);
        }

        //得到自己申请的请求的respond
        respondExample.clear();
        String ownId = users11.getUserGuid();
        respondExample.or().andResUserGuidEqualTo(ownId);
        List<Respond> responds=respondMapper.selectByExample(respondExample);
        List<Reqest> respondRecordList=new ArrayList<>();
        //遍历
//        for(int i=offset;i< offset+limit&&i < responds.size();i++){
        for(int i=0;i < responds.size();i++){
            Respond respond1=responds.get(i);
            TypeExample typeExample = new TypeExample();

            //得到reqest
            String reqGuid=respond1.getResReqGuid();
            Reqest reqest=reqestMapper.selectByPrimaryKey(reqGuid);

            //处理请求分类
            String classId = reqest.getReqTypeGuidClass();
            if (classId != null)
            {
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(classId);
                List<Type> types2 = typeMapper.selectByExample(typeExample);
                reqest.setReqTypeGuidClass(types2.get(0).getTypeTitle());
            }
            //处理请求处理状态
            String approveId = reqest.getReqTypeApproveStatus();
            if (approveId != null)
            {
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(approveId);
                List<Type> types2 = typeMapper.selectByExample(typeExample);
                reqest.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
            }
            //处理请求批准状态
            String processId = reqest.getReqTypeGuidProcessStatus();
            if (processId != null)
            {
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(processId);
                List<Type> types2 = typeMapper.selectByExample(typeExample);
                reqest.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
            }

            //处理请求批准状态
            String ergencyId = reqest.getReqTypeGuidUrgency();
            if (processId != null)
            {
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(ergencyId);
                List<Type> types2 = typeMapper.selectByExample(typeExample);
                reqest.setReqTypeGuidUrgency(types2.get(0).getTypeTitle());
            }

//            respondRecordList.add(reqest);
            /**10.9添加*/
            if (searchText != null) {
                String reqTitle = reqest.getReqTitle();//标题搜索
                if(!(reqTitle!=null)){
                    reqTitle="";
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
                    respondRecordList.add(reqest);
                }
            } else {
                respondRecordList.add(reqest);
            }
            /**10.9添加*/
        }

        /**10.9添加*/
        List<Reqest> reqestsReturn = new ArrayList<>();
        for (int i = offset;i<offset+limit&&i<respondRecordList.size();i++){
            reqestsReturn.add(respondRecordList.get(i));
        }

        /**10.9添加*/



        //全部符合要求的数据的数量
//        int total=responds.size();
        int total=respondRecordList.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
//        TableRecordsJson tableRecordsJson=new TableRecordsJson(respondRecordList,total);
        TableRecordsJson tableRecordsJson=new TableRecordsJson(reqestsReturn,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            // System.out.println(json1);
            return json1;
        }catch (Exception e){
            return null;
        }
    }

/*
    //查看服务列表后向后台数据索要数据
    @RequestMapping(value="/getRESPONDListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getRESPONDListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder){

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
//        model.addAttribute("role",role);
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
        String ownId = users11.getUserGuid();
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
    }*/
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
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
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
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        //TODO 根据传递过来的reqGuid
        respondMapper.updateByPrimaryKeySelective(respond);
        return "responseOfVolunteer";

    }
    //志愿者进行撤单操作
    @RequestMapping(value = "/deleteRESPOND/{resGuid}")
    public String deleteRESPOND (@PathVariable String resGuid , Respond respond, Model model) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        respond = respondMapper.selectByPrimaryKey(resGuid);
        //更新响应表
        respond.setResTypeGuidProcessStatus("77777777-94e3-4eb7-aad3-555555555555");
        respondMapper.updateByPrimaryKeySelective(respond);
        return "responseOfVolunteer";
    }

    /********************10.7添加 关于删除用户系统我的需求列表查看详情中的查看志愿者的删除操作**********************/
    @RequestMapping(value = "/RESPOND/{message}")
       public String DeleteVolunteer(Model model, @PathVariable String message){
        Subject account = SecurityUtils.getSubject();
        String message1=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message1);
        String role=users.getUserRole();
        model.addAttribute("role",role);
        System.out.println(message+"++++++999999999999999999999什么的ID？");
        RespondExample respondExample=new RespondExample();
        respondExample.or().andResGuidEqualTo(message);
        List<Respond> respondList=respondMapper.selectByExample(respondExample);
        Respond respond=respondList.get(0);
        if(respond.getResGuid().equals(message)){
            respondMapper.deleteByPrimaryKey(message);
        }

        return "volunteerList";
    }


}
