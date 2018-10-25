package com.timebank.controller.yl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sun.deploy.nativesandbox.comm.Request;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

// 修改和查看个人信息
@Controller
public class userController {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private RespondMapper respondMapper;
    @Autowired
    private ReqestMapper reqestMapper;
    /*9.26添加*/
    @Autowired
    private  WeightMapper weightMapper;
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
    private Users GetCurrentUsers(String message){

        UsersExample usersExample=new UsersExample();

        String em = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        String ph = "^[1][34578]\\d{9}$";
        Users users=null;
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
    //查看个人信息
    @RequestMapping(value = "/userInformationView")
    public String userInformationView(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        if (users1.getUserTypeGuidGender()!= null)
        {
            //处理性别
            Type type = typeMapper.selectByPrimaryKey(users1.getUserTypeGuidGender());
            users1.setUserTypeGuidGender(type.getTypeTitle());
        }
        if (users1.getUserOwnCurrency()!=null)
        {
            //用户持有时间
            users1.setUserOwnCurrency(users1.getUserOwnCurrency());
        }
        if(users1.getUserTypeAccountStatus()!=null)
        {
            //用户状态
            Type type1 = typeMapper.selectByPrimaryKey(users1.getUserTypeAccountStatus());
            users1.setUserTypeAccountStatus(type1.getTypeTitle());
        }
        if (users1.getUserCommGuid()!=null)
        {
            //所属小区
            Community community = communityMapper.selectByPrimaryKey(users1.getUserCommGuid());
            users1.setUserCommGuid(community.getCommTitle());
        }
        //处理时间格式
        if (users1.getUserBirthdate()!=null)
        {
//            Date date= (Date) users1.getUserBirthdate();
//            model.addAttribute("date",date);
//            java.sql.Date date=new java.sql.Date();
            java.util.Date d=new java.util.Date (users1.getUserBirthdate().getTime());
            model.addAttribute("date",d);
            System.out.println(d);

            SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd");
            f.format(d);
            System.out.println(d);
        }


        /************10.17添加关于信用度显示****************/
        String userResID=users1.getUserGuid();
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
        /*******************/
        model.addAttribute("users",users1);

        return "userInformation";
    }

    //修改个人信息
    @RequestMapping(value = "/modifyUserInformationView")
    public String modifyUserInformationView(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        //所属小区
        CommunityExample communityExample = new CommunityExample();
        List<Community> communities = communityMapper.selectByExample(communityExample);
        model.addAttribute("communities",communities);

        System.out.println(users1.getUserBirthdate());
        System.out.println(users1.getUserBirthdate());
        System.out.println(users1.getUserBirthdate());

        model.addAttribute("users",users1);
        //加载性别
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGroupIdEqualTo(1);
        List<Type> types = typeMapper.selectByExample(typeExample);
        model.addAttribute("types",types);
        //处理时间格式
        if (users1.getUserBirthdate()!=null)
        {
            java.util.Date d=new java.util.Date (users1.getUserBirthdate().getTime());
            model.addAttribute("date",d);
            SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd");
            f.format(d);
            System.out.println(d);
        }


        model.addAttribute("message","请按实际情况填写个人信息");
        if(users1.getUserIdimage()!=null){
            model.addAttribute("message1",users1.getUserIdimage());

        }else{
           model.addAttribute("message1","/img/qie.jpg");
        }
        if(users1.getUserRole().equals("Tourist")){
            return "updateUserInformation";
        }else{
            return "updateInformation";
        }
    }
    //游客修改用户个人信息界面中保存按钮
    @RequestMapping(value = "/updateUserInformationSubmit")
    public String updateREQESTSave(@ModelAttribute @Valid Users users, Model model,@RequestParam(value="img_z") MultipartFile file) throws IOException {
        System.out.println("这里");
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);

        System.out.println("文件是否为空:"+file.getSize()+"或者"+file.isEmpty());
        if(!file.isEmpty()) {
            //图片的下载与上传
            ClassPathResource resource;
            resource = new ClassPathResource("static/img");
            String absPath = resource.getURL().getPath();
            UUID guid = UUID.randomUUID();
            String fileName = guid + file.getOriginalFilename();
            System.out.println(absPath);

            absPath = absPath.replace("%20", " ");
            //将用户传上去的图片下载到主机 正面
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(absPath + "/" + fileName));
            outputStream.write(file.getBytes());
            outputStream.flush();
            outputStream.close();

            //将图片的相对路径保存到数据库
            String dboPath = "/img/" + fileName;
            users.setUserIdimage(dboPath);
            usersMapper.updateByPrimaryKeySelective(users);
        }
        //从数据库中获取前台提交的字段
        String GUID=users.getUserGuid();
        UsersExample usersExample1=new UsersExample();
        usersExample1.or().andUserGuidEqualTo(GUID);
        List<Users> usersList=usersMapper.selectByExample(usersExample1);
        Users users3=usersList.get(0);
        if (users3.getUserTypeGuidGender()!= null)
        {
            //处理性别
            Type type = typeMapper.selectByPrimaryKey(users3.getUserTypeGuidGender());
            users3.setUserTypeGuidGender(type.getTypeTitle());
        }
        if (users3.getUserOwnCurrency()!=null)
        {
            //用户持有时间
            users3.setUserOwnCurrency(users3.getUserOwnCurrency());
        }
        if (users3.getUserBirthdate()!=null){
            //出生日期
            users3.setUserBirthdate(users3.getUserBirthdate());
        }
        if(users3.getUserTypeAccountStatus()!=null)
        {
            //用户状态

            Type type1 = typeMapper.selectByPrimaryKey(users3.getUserTypeAccountStatus());
            users3.setUserTypeAccountStatus(type1.getTypeTitle());
        }
        if (users3.getUserCommGuid()!=null)
        {
            //所属小区
            Community community = communityMapper.selectByPrimaryKey(users3.getUserCommGuid());
            users3.setUserCommGuid(community.getCommTitle());
        }
        if (users3.getUserBirthdate()!=null)
        {
            java.util.Date d=new java.util.Date (users3.getUserBirthdate().getTime());
            model.addAttribute("date",d);
            SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd");
            f.format(d);
        }

        model.addAttribute("users",users3);


        return "userInformation";
    }

    @RequestMapping(value = "/updateUserInformationUser")
    //    @ResponseBody
    public String updateuserInformationSave(@ModelAttribute @Valid Users users, Model model) throws IOException {
        System.out.println("这里");
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);

        usersMapper.updateByPrimaryKeySelective(users);

        //从数据库中获取前台提交的字段
        String GUID=users.getUserGuid();
        UsersExample usersExample1=new UsersExample();
        usersExample1.or().andUserGuidEqualTo(GUID);
        List<Users> usersList=usersMapper.selectByExample(usersExample1);
        Users users3=usersList.get(0);
        if (users3.getUserTypeGuidGender()!= null)
        {
            //处理性别
            Type type = typeMapper.selectByPrimaryKey(users3.getUserTypeGuidGender());
            users3.setUserTypeGuidGender(type.getTypeTitle());
        }
        if (users3.getUserOwnCurrency()!=null)
        {
            //用户持有时间
            users3.setUserOwnCurrency(users3.getUserOwnCurrency());
        }
        if (users3.getUserBirthdate()!=null){
            //出生日期
            users3.setUserBirthdate(users3.getUserBirthdate());
        }
        if(users3.getUserTypeAccountStatus()!=null)
        {
            //用户状态

            Type type1 = typeMapper.selectByPrimaryKey(users3.getUserTypeAccountStatus());
            users3.setUserTypeAccountStatus(type1.getTypeTitle());
        }
        if (users3.getUserCommGuid()!=null)
        {
            //所属小区
            Community community = communityMapper.selectByPrimaryKey(users3.getUserCommGuid());
            users3.setUserCommGuid(community.getCommTitle());
        }  if (users3.getUserBirthdate()!=null)
        {
            java.util.Date d=new java.util.Date (users3.getUserBirthdate().getTime());
            model.addAttribute("date",d);
            SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd");
            f.format(d);
        }

        model.addAttribute("users",users3);


        return "userInformation";
    }
    //邮箱重名校验
    @RequestMapping(value = "/jquery/exist4.do")
    @ResponseBody
    public String checkUserEmail(String userMail){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        String GUID=users1.getUserGuid();
        UsersExample usersExample=new UsersExample();
        List<Users> users=usersMapper.selectByExample(usersExample);
        boolean result = true;
        Map<String, Boolean> map = new HashMap<>();
        for(Users it:users){
            if(!it.getUserGuid().equals(GUID)&&it.getUserMail()!=null&&it.getUserMail().equals(userMail)){
                result=false;
            }
        }
        System.out.println(userMail);
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
        return resultString;
    }
    //手机号重名校验
    @RequestMapping(value = "/jquery/exist3.do")
    @ResponseBody
    public String checkUserPhone(String userPhone){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        String GUID=users1.getUserGuid();
        UsersExample usersExample=new UsersExample();
        List<Users> users=usersMapper.selectByExample(usersExample);
        boolean result = true;
        Map<String, Boolean> map = new HashMap<>();
        for(Users it:users){
            if(!it.getUserGuid().equals(GUID)&&it.getUserPhone()!=null&&it.getUserPhone().equals(userPhone)){
                result=false;
            }
        }
        System.out.println(userPhone);
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
        return resultString;
    }
    /**
     * 名字转换函数
     */
    public String IdtoName(String reqtargetuser) {
        if (reqtargetuser != null && !reqtargetuser.isEmpty()) {
            UsersExample usersExample = new UsersExample();
            StringBuilder sb = new StringBuilder();
            //去掉[]
            String r = reqtargetuser.replaceAll("^.*\\[", "").replaceAll("].*", "");
            //分割字符串
            String[] array = r.split(",");
            String respondName = null;
            for (int in = 0; in < array.length; in++) {
                //去掉空格
                String jj = array[in].replaceAll(" ", "");
                usersExample.clear();
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
    //导航栏查看已完成但未评价的请求
    //导航栏查看活动
    @RequestMapping(value = "/requestScore")
    public String scoreForVolunteer(Model model) {
        System.out.println("导航栏评价");
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        return "requestScore";
    }
/********************9.27添加*************************/
    //导航栏查看已评价的请求
    //导航栏查看活动
    @RequestMapping(value = "/requestScore1")
    public String scoreForVolunteer1(Model model) {
        System.out.println("导航栏评价过的请求");

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);

        return "requestScoreOver";
    }

/***********************9.27添加*************************/

    /********************9.28添加*************************/
    //导航栏查看已评价的请求
    //导航栏查看活动
    @RequestMapping(value = "/requestScore2")
    public String scoreForVolunteer2(Model model) {
        System.out.println("导航栏评价过的请求");

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        return "responseOfVolunteer1";
    }
    @RequestMapping(value="/getRESPONDListJsonData1",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getRESPONDListJsonData1(@RequestParam int offset, int limit, String sortName, String sortOrder,Model model, String searchText) {
//        System.out.println("ZHELI");
//        //页码 和 条数
//        System.out.println(offset);
//        //每页的条数
//        System.out.println(limit);
//        System.out.println("sdsadsad");

        Subject account = SecurityUtils.getSubject();
        String message = (String) account.getPrincipal();
        Users users11 = GetCurrentUsers(message);
        String role = users11.getUserRole();
        model.addAttribute("role", role);
        /**10.10添加*/
        if (searchText == "") {
            searchText = null;
        }
        /**10.10添加*/

        RespondExample respondExample = new RespondExample();
//        ReqestExample reqestExample=new ReqestExample();
        respondExample.clear();

        /**********************10.7添加*****************************/
        int count=0;
        /***************************************************/
        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            //将排序信息添加到example中
            respondExample.setOrderByClause(order);
        }
        //得到自己申请的请求的respond
        respondExample.clear();
        String ownId = users11.getUserGuid();
        respondExample.or().andResUserGuidEqualTo(ownId);
        List<Respond> responds = respondMapper.selectByExample(respondExample);
        List<Respond> respondList = new ArrayList<>();
//        List<Reqest> respondRecordList = new ArrayList<>();
        //遍历
//        for (int i = offset; i < offset + limit && i < responds.size(); i++) {
//            Respond respond1 = responds.get(i);
////            TypeExample typeExample = new TypeExample();
//            String resGuId = respond1.getResGuid();
//            //得到reqest
//            String reqGuid = respond1.getResReqGuid();
//            Respond res = respondMapper.selectByPrimaryKey(resGuId);
//            Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);
////            System.out.println("请求的名称为111111111111111111111111"+reqest.getReqTitle());
//           //处理发起请求的账户名/
//            String resReqSstrtUserGuId=reqest.getReqIssueUserGuid();
//            Users users22=usersMapper.selectByPrimaryKey(resReqSstrtUserGuId);
//            res.setResReqStartUserAccount(users22.getUserAccount());
//            //处理请求的名称/
//            res.setResReqTitle(reqest.getReqTitle());
//            if (respond1.getResUserGuid().equals(users11.getUserGuid())) {
//                if(respond1.getResEvaluate()!=null){
//                int resEvaluateScore = respond1.getResEvaluate();
////                System.out.println("服务得分为33333333333333333333"+resEvaluateScore);
//               //处理参与该请求服务的得分/
//                res.setResEvaluate(resEvaluateScore);
//                respondList.add(res);
//                count++;//10.7添加
//            }
//            }
//        }
        for (int i = 0;  i < responds.size(); i++) {
            Respond respond1 = responds.get(i);
//            TypeExample typeExample = new TypeExample();
            String resGuId = respond1.getResGuid();
            //得到reqest
            String reqGuid = respond1.getResReqGuid();
            Respond res = respondMapper.selectByPrimaryKey(resGuId);
            Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);
//            System.out.println("请求的名称为111111111111111111111111"+reqest.getReqTitle());
            //处理发起请求的账户名/
            String resReqSstrtUserGuId=reqest.getReqIssueUserGuid();
            Users users22=usersMapper.selectByPrimaryKey(resReqSstrtUserGuId);
            res.setResReqStartUserAccount(users22.getUserAccount());
            //处理请求的名称/
            res.setResReqTitle(reqest.getReqTitle());
            if (respond1.getResUserGuid().equals(users11.getUserGuid())) {
                if(respond1.getResEvaluate()!=null){
                    int resEvaluateScore = respond1.getResEvaluate();
//                System.out.println("服务得分为33333333333333333333"+resEvaluateScore);
                    //处理参与该请求服务的得分/
                    res.setResEvaluate(resEvaluateScore);
//                    respondList.add(res);

                    if (searchText != null) {
                        String reqStartUserAccount = res.getResReqStartUserAccount();
                        String reqTitle = res.getResReqTitle();
                        if (reqStartUserAccount.contains(searchText) || reqTitle.contains(searchText)) {
                            respondList.add(res);
                        }
                    } else {
                        respondList.add(res);
                    }
                }
            }
        }
        List<Respond> respondList1=new ArrayList<>();
        for(int i=offset;i<offset+limit&&i<respondList.size();i++){
            Respond respond=respondList.get(i);
            respondList1.add(respond);
        }

            //全部符合要求的数据的数量
//            int total = responds.size();
//        int total=respondList1.size();
        int total=respondList.size();
//             int total=count;
            //将所得集合打包
            ObjectMapper mapper = new ObjectMapper();
            TableRecordsJson tableRecordsJson = new TableRecordsJson(respondList1, total);
//        TableRecordsJson tableRecordsJson = new TableRecordsJson(respondList, total);
            //将实体类转换成json数据并返回
            try {
                String json1 = mapper.writeValueAsString(tableRecordsJson);
                 System.out.println(json1);
                return json1;
            } catch (Exception e) {
                return null;
            }

    }
    /***********************9.28添加*************************/


    //所有该用户发起的请求列表
    @RequestMapping(value = "/getRequestListScoreJsonData1")
    @ResponseBody
    public String getJsonDataFromReqest1(@RequestParam int offset, int limit, String sortName, String sortOrder,Model model,String searchText){
       //获得当前用户
        System.out.println(777777777);
        //0
        System.out.println(offset);
        //页面最大显示数量
      System.out.println(limit);
      Subject account = SecurityUtils.getSubject();
      String message=(String) account.getPrincipal();
      Users users11=GetCurrentUsers(message);
      String role=users11.getUserRole();
      model.addAttribute("role",role);

        /**10.10添加*/
        if (searchText == "") {
            searchText = null;
        }
        /**10.10添加*/

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
      reqestExample.clear();
      String userID =users11.getUserGuid();
      reqestExample.or().andReqIssueUserGuidEqualTo(userID);
      List<Reqest> reqests=reqestMapper.selectByExample(reqestExample);
      List<Reqest> reqestRecordList=new ArrayList<>();
//     for(int i=offset;i< offset+limit&&i < reqests.size();i++){
      for(int i=0;i < reqests.size();i++){
          Reqest reqest1=reqests.get(i);
          TypeExample typeExample = new TypeExample();
/*******************************9.26添加**************************************************/
          //设置接受请求用户的账户/
          String reqTargetsUserGuid=reqest1.getReqTargetsUserGuid();
          if(reqTargetsUserGuid!=null){
              Users users22=usersMapper.selectByPrimaryKey(reqTargetsUserGuid);
//              System.out.println(users22.getUserAccount()+"还是关于请求接受人的问题");
              reqest1.setReqTargetsUserAccount(users22.getUserAccount());
          }

          //设置请求权值/
          String reqFromWeightGuid=reqest1.getReqFromWeightGuid();
          if(reqFromWeightGuid!=null){
              Weight weight=weightMapper.selectByPrimaryKey(reqFromWeightGuid);
              reqest1.setReqFromWeightGuid(weight.getWeightTitle());
          }
          //设置请求处理人//
          String reqProcessUserGuid=reqest1.getReqProcessUserGuid();
          if (reqProcessUserGuid!=null) {
              Users users33=usersMapper.selectByPrimaryKey(reqProcessUserGuid);
              reqest1.setReqProcessUserGuid(users33.getUserAccount());
          }

          /*****10.24添加**/
          //设置实际消耗的时间币
          reqest1.setReqActualConsumeCurrency(reqest1.getReqPreCunsumeCurrency());

/********************************************************************************/
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
         /* //处理请求处理状态
          String approveId = reqest1.getReqTypeApproveStatus();
          if (approveId != null)
          {
              typeExample.clear();
              typeExample.or().andTypeGuidEqualTo(approveId);
              List<Type> types2 = typeMapper.selectByExample(typeExample);
              reqest1.setReqTypeApproveStatus(types2.get(0).getTypeTitle());
          }*/
          //处理请求批准状态
          String processId = reqest1.getReqTypeGuidProcessStatus();
          if(processId.equals("33333333-94E3-4EB7-AAD3-777777777777")){
              typeExample.clear();
              typeExample.or().andTypeGuidEqualTo(processId);
              List<Type> types2 = typeMapper.selectByExample(typeExample);
              reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
              //后边新添加的搜索框，所以先把add代码注释掉/
//                reqestRecordList.add(reqest1);


              /**10.10添加*/
              if (searchText != null) {
                  String reqTitle = reqest1.getReqTitle();//标题搜索
                  if(!(reqTitle!=null)){
                      reqTitle="";
                  }
                  String reqUserAccount = reqest1.getReqIssueUserGuid();//账户搜索
                  if(!(reqUserAccount!=null)){
                      reqUserAccount="";
                  }
                  String reqAddress = reqest1.getReqAddress();//地址搜索
                  if(!(reqAddress!=null)){
                      reqAddress="";
                  }
                  String reqClass=reqest1.getReqTypeGuidClass();//请求分类搜索
                  if(!(reqClass!=null)){
                      reqClass="";
                  }
                  String reqUrgent=reqest1.getReqTypeGuidUrgency();//紧急程度搜索
                  if(!(reqUrgent!=null)){
                      reqUrgent="";
                  }
                  String reqDescribe=reqest1.getReqDesp();//描述搜索
                  if(!(reqDescribe!=null)){
                      reqDescribe="";
                  }
                  String reqComm=reqest1.getReqComment();//补充搜索
                  if(!(reqComm!=null)){
                      reqComm="";
                  }
                  if (reqTitle.contains(searchText) || reqUserAccount.contains(searchText) || reqAddress.contains(searchText)||reqClass.contains(searchText)||reqUrgent.contains(searchText)||reqDescribe.contains(searchText)||reqComm.contains(searchText)) {
                      reqestRecordList.add(reqest1);
                  }
              } else {
                  reqestRecordList.add(reqest1);
              }
              /**10.10添加*/
          }
      }
           List<Reqest> reqestRecordList1=new ArrayList<>();
           for(int i=offset;i< offset+limit&&i < reqestRecordList.size();i++){
//                System.out.println("什么时候执行？？");
                Reqest reqest=reqestRecordList.get(i);
                reqestRecordList1.add(reqest);
         }


      //全部符合要求的数据的数量
      int total=reqestRecordList.size();
//      int total=count;
      //将所得集合打包
      ObjectMapper mapper = new ObjectMapper();
//     TableRecordsJson tableRecordsJson=new TableRecordsJson(reqestRecordList,total);
      TableRecordsJson tableRecordsJson=new TableRecordsJson(reqestRecordList1,total);
      //将实体类转换成json数据并返回
      try {
          String json1 = mapper.writeValueAsString(tableRecordsJson);
          return json1;
      }catch (Exception e){
          return null;
      }
    }


    //所有已完成的请求列表/
    @RequestMapping(value = "/getRequestListScoreJsonData")
    @ResponseBody
    public String getJsonDataFromReqest(@RequestParam int offset, int limit, String sortName,String sortOrder,Model model, String searchText){
        //获得当前用户

        System.out.println("HAHAHAH");
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

        /**10.10添加*/
        if (searchText == "") {
            searchText = null;
        }
        /**10.10添加*/

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
//        for(int i=offset;i< offset+limit&&i < reqests.size();i++){
        for(int i=0;i < reqests.size();i++){
            Reqest reqest1=reqests.get(i);
            TypeExample typeExample = new TypeExample();
/*******************************9.26添加**************************************************/
            //设置接受请求用户的账户/
            String reqTargetsUserGuid=reqest1.getReqTargetsUserGuid();
            if(reqTargetsUserGuid!=null){
                Users users22=usersMapper.selectByPrimaryKey(reqTargetsUserGuid);
                reqest1.setReqTargetsUserAccount(users22.getUserAccount());
            }

            //设置请求权值/
            String reqFromWeightGuid=reqest1.getReqFromWeightGuid();
            if(reqFromWeightGuid!=null){
                Weight weight=weightMapper.selectByPrimaryKey(reqFromWeightGuid);
                reqest1.setReqFromWeightGuid(weight.getWeightTitle());
            }
            //设置请求处理人//
            String reqProcessUserGuid=reqest1.getReqProcessUserGuid();
            if (reqProcessUserGuid!=null) {
                Users users33=usersMapper.selectByPrimaryKey(reqProcessUserGuid);
                reqest1.setReqProcessUserGuid(users33.getUserAccount());
            }

/*********************************************************************************/
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
            if(processId.equals("33333333-94E3-4EB7-AAD3-444444444444")){
                typeExample.clear();
                typeExample.or().andTypeGuidEqualTo(processId);
                List<Type> types2 = typeMapper.selectByExample(typeExample);
                reqest1.setReqTypeGuidProcessStatus(types2.get(0).getTypeTitle());
                //后边新添加的搜索框，所以先把add代码注释掉/
//                reqestRecordList.add(reqest1);


                /**10.10添加*/
                if (searchText != null) {
                    String reqTitle = reqest1.getReqTitle();//标题搜索
                    if(!(reqTitle!=null)){
                        reqTitle="";
                    }
                    String reqUserAccount = reqest1.getReqIssueUserGuid();//账户搜索
                    if(!(reqUserAccount!=null)){
                        reqUserAccount="";
                    }
                    String reqAddress = reqest1.getReqAddress();//地址搜索
                    if(!(reqAddress!=null)){
                        reqAddress="";
                    }
                    String reqClass=reqest1.getReqTypeGuidClass();//请求分类搜索
                    if(!(reqClass!=null)){
                        reqClass="";
                    }
                    String reqUrgent=reqest1.getReqTypeGuidUrgency();//紧急程度搜索
                    if(!(reqUrgent!=null)){
                        reqUrgent="";
                    }
                    String reqDescribe=reqest1.getReqDesp();//描述搜索
                    if(!(reqDescribe!=null)){
                        reqDescribe="";
                    }
                    String reqComm=reqest1.getReqComment();//补充搜索
                    if(!(reqComm!=null)){
                        reqComm="";
                    }
                    if (reqTitle.contains(searchText) || reqUserAccount.contains(searchText) || reqAddress.contains(searchText)||reqClass.contains(searchText)||reqUrgent.contains(searchText)||reqDescribe.contains(searchText)||reqComm.contains(searchText)) {
                        reqestRecordList.add(reqest1);
                    }
                } else {
                    reqestRecordList.add(reqest1);
                }
                /**10.10添加*/

            }

        }

        List<Reqest> reqestRecordList1=new ArrayList<>();
        System.out.println(reqestRecordList.size());
        for(int i=offset;i< offset+limit&&i < reqestRecordList.size();i++){
            Reqest reqest=reqestRecordList.get(i);
            reqestRecordList1.add(reqest);
        }


        //全部符合要求的数据的数量
//        int total=reqests.size();
        int total=reqestRecordList.size();
        System.out.println(total);
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(reqestRecordList1,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
             System.out.println(json1);
            return json1;
        }catch (Exception e){
            return null;
        }
    }
    //评分请求列表中的评分按钮
    @RequestMapping(value = "/requestScore/{reqGuid}")
    public String requestScore(Model model, @PathVariable String reqGuid) {
        Subject account = SecurityUtils.getSubject();
        String message = (String) account.getPrincipal();
        Users users = GetCurrentUsers(message);
        String role = users.getUserRole();
        model.addAttribute("role", role);
        model.addAttribute("resGuid", reqGuid);
        return "scoreForVolunteer";
    }

    //评分请求列表中的评分按钮
    @RequestMapping(value = "/requestScore11/{reqGuid}")
    public String requestScore1(Model model, @PathVariable String reqGuid) {
        Subject account = SecurityUtils.getSubject();
        String message = (String) account.getPrincipal();
        Users users = GetCurrentUsers(message);
        String role = users.getUserRole();
        model.addAttribute("role", role);
        model.addAttribute("resGuid", reqGuid);

        return "scoreForVolunteerOver";
    }

    @RequestMapping(value="/getUserScoreJsonData")
    @ResponseBody
    public String getUserListJsonData(Model model, @RequestParam int offset, int limit, String sortName, String sortOrder, String resGuid){
        System.out.println("获取到用户数据");
        System.out.println(resGuid+"这是请求ID！！！！！！！！！！");
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);

        UsersExample usersExample1 = new UsersExample();
        TypeExample typeExample = new TypeExample();
        RespondExample respondExample=new RespondExample();

        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            //将排序信息添加到example中
            usersExample1.setOrderByClause(order);
        }
        System.out.println(4524);
        List<Users> usersList = new ArrayList<Users>();
        respondExample.clear();
        respondExample.or().andResReqGuidEqualTo(resGuid);
        List<Respond> responds=respondMapper.selectByExample(respondExample);
        System.out.println(4524);
        for(Respond it:responds){
            if(it.getResTypeGuidProcessStatus().equals("88888888-94E3-4EB7-AAD3-111111111111")) {
                String usersguid = it.getResUserGuid();
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

                String userGuid = user.getUserGuid();
                if (userGuid  != null) {
                    respondExample.clear();
                    respondExample.or().andResUserGuidEqualTo(userGuid);
                    List<Respond> respondList = respondMapper.selectByExample(respondExample);
                    user.setUserGuid(respondList.get(0).getResUserGuid());
                    if (user.getUserCommGuid()!=null)
                    {
                        //所属小区
                        Community community = communityMapper.selectByPrimaryKey(users.getUserCommGuid());
                        user.setUserCommGuid(community.getCommTitle());
                    }
                    usersList.add(user);
                }
            }
        }
        int total = usersList.size();
        ObjectMapper mapper = new ObjectMapper();
        com.timebank.controller.sxq.TableRecordsJson tableRecordsJson = new com.timebank.controller.sxq.TableRecordsJson(usersList, total);
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }

    //给志愿者打分
    @RequestMapping(value = "/scoreForVolunteer", method = RequestMethod.POST )
    private String scoreForVolunteer(Model model,String thisPerson1,String finalScore,String id) {
        System.out.println("服务者的评分为："+finalScore);
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        model.addAttribute("resGuid",id);
        //需要将分数插入到respond表单中
        RespondExample respondExample=new RespondExample();
        respondExample.or().andResReqGuidEqualTo(id);
        List<Respond>  responds=respondMapper.selectByExample(respondExample);
        for (Respond it:responds){
//             if (it.getResEvaluate().equals(0))
            if (it.getResEvaluate()==null){
                if (it.getResUserGuid().equals(thisPerson1)) {
                    it.setResEvaluate(Integer.parseInt(finalScore));
                    respondMapper.updateByPrimaryKeySelective(it);
                }
            }
        }
/****************************************************************9.27添加*****************/
        Reqest reqest =reqestMapper.selectByPrimaryKey(id);
        RespondExample respondExample1=new RespondExample();
        respondExample1.or().andResReqGuidEqualTo(id);
        List<Respond> respondList=respondMapper.selectByExample(respondExample1);

        int num=0;
        int num2=0;
        for(Respond it:respondList){
            if(it.getResTypeGuidProcessStatus().equals("88888888-94E3-4EB7-AAD3-111111111111"))//用户没有被删除/
            {
                num++;
                if(it.getResEvaluate()!=null){//已完成评分/
                    num2++;
                }
            }
        }
        if(num==num2){//如果用户没有被删除并且已评完分数/
            reqest.setReqTypeGuidProcessStatus("33333333-94E3-4EB7-AAD3-777777777777");//已评价
            /**********************************10.18添加关于信用度显示****************/
            String userResID=thisPerson1;
//            System.out.println("查信用度的用户是："+userResID);
            RespondExample respondExample33 = new RespondExample();
            respondExample33.or().andResUserGuidEqualTo(userResID);
            List<Respond> respondList33=respondMapper.selectByExample(respondExample33);
            int credit=0;
            int totalScore=0;
            int count=0;
            for (Respond res:respondList33) {
                String userResListId=res.getResUserGuid();
                if (userResID.equals(userResListId)){
                    if (res.getResEvaluate()!=null){
                        totalScore+=res.getResEvaluate();
                        count++;
                    }
                }
            }
            if(count!=0){
                credit=totalScore/count;
            }
            Users userSearch=usersMapper.selectByPrimaryKey(userResID);
            userSearch.setUserCredit(credit);
            usersMapper.updateByPrimaryKeySelective(userSearch);
            /***********************************************************/

            reqestMapper.updateByPrimaryKeySelective(reqest);
            return "scoreForVolunteerOver";
        }
        return "scoreForVolunteer";

//        return "scoreForVolunteer";
    }

    //评价完活动后从 查看志愿者 按钮中点开志愿者列表（包含查分） 也包括评价完成后 点击查分的处理过程/
    @RequestMapping(value = "/LookScoreForVolunteer3/{message}")
    public String DelVolunteerScore2(Model model, @PathVariable String message){
        Subject account = SecurityUtils.getSubject();
        String message1=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message1);
        String role=users.getUserRole();
        model.addAttribute("role",role);
        String[] strings=message.split(",");
        String userGuid=strings[0];
        String reqestid=strings[1];
        model.addAttribute("reqestid", reqestid);//id为要评价的活动的id/
        model.addAttribute("userid", userGuid);//id为要评价的活动的id/

        return "LookScore3";
    }

    //所有参与这个活动的用户的列表
    @RequestMapping(value = "/getEvalListScoreJsonData2")
    @ResponseBody
    public String userEvalList(Model model, @RequestParam int offset, int limit, String sortName, String sortOrder, String reqestid,String userid) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);
        System.out.println(111111);
        System.out.println(userid);
        System.out.println(222222);
        /*************1添加*****************/
        UsersExample usersExample1 = new UsersExample();
        RespondExample respondExample=new RespondExample();
        /**************1添加******************/
        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            //将排序信息添加到example中
            usersExample1.setOrderByClause(order);
        }
        /***************2添加*****************/
        List<Respond> respondList=new ArrayList<>();
        //根据请求id遍历respond数据库  得到用户guid
        respondExample.or().andResReqGuidEqualTo(reqestid);
        List<Respond> responds=respondMapper.selectByExample(respondExample);
        for (Respond it :responds) {
            //判断respond中此人是否已被删除  88888888-94E3-4EB7-AAD3-111111111111 通过
            if(it.getResTypeGuidProcessStatus().equals("88888888-94E3-4EB7-AAD3-111111111111")&&it.getResUserGuid().equals(userid)){
                String resUserId=it.getResGuid();
                int resEvaluateScore=it.getResEvaluate();
                System.out.println("查到的分数显示！！！！！！！！！！！！！！！"+resEvaluateScore);
                Respond res=respondMapper.selectByPrimaryKey(resUserId);
                System.out.println("得到的用户数据2222222222222222222222222222"+res);
                res.setResEvaluate(resEvaluateScore);
                respondList.add(res);
            }
        }
        int total=respondList.size();
        /****************2添加*****************/
        ObjectMapper mapper = new ObjectMapper();
        com.timebank.controller.sxq.TableRecordsJson tableRecordsJson = new com.timebank.controller.sxq.TableRecordsJson(respondList, total);
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }


}
