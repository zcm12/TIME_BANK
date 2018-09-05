package com.timebank.controller.yl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        model.addAttribute("users",users1);
        //加载性别
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeGroupIdEqualTo(1);
        List<Type> types = typeMapper.selectByExample(typeExample);
        model.addAttribute("types",types);
        model.addAttribute("message","请按实际情况填写个人信息");
        return "updateUserInformation";
    }
    //修改用户个人信息界面中保存按钮
    @RequestMapping(value = "/updateUserInformationSubmit")
//    @ResponseBody
    public String updateREQESTSave(@ModelAttribute @Valid Users users, Model model,@RequestParam(value="img_z") MultipartFile file,@RequestParam(value="img_f") MultipartFile file1) throws IOException {
        System.out.println("这里");
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);


        //图片的下载与上传
        ClassPathResource resource;
        resource = new ClassPathResource("static/img");
        String absPath=resource.getURL().getPath();
        String absPath1=resource.getURL().getPath();
        String fileName=file.getOriginalFilename();
        String fileName1=file1.getOriginalFilename();
        System.out.println(1111111);
        System.out.println(absPath);
        System.out.println(absPath1);
        System.out.println(fileName);

        //将用户传上去的图片下载到主机 正面
        BufferedOutputStream outputStream=new BufferedOutputStream(new FileOutputStream(absPath+"/"+fileName));
        outputStream.write(file.getBytes());
        outputStream.flush();
        outputStream.close();
        //反面
        BufferedOutputStream outputStream1=new BufferedOutputStream(new FileOutputStream(absPath1+"/"+fileName1));
        outputStream1.write(file1.getBytes());
        outputStream1.flush();
        outputStream1.close();
        //将图片的相对路径保存到数据库
//        String dboPath=absPath+"/"+fileName;
        String dboPath="/img/"+fileName;
        String dboPath1="/img/"+fileName1;
//        byte[] a=(byte)dboPath;
//        users.setUserIdimageZ(dboPath);
//        users.setUserIdimageF(dboPath1);
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
//        if (users3.getUserBirthdate()!=null){
//            //出生日期
//            String dateString = users.getUserBirthdate().toString();
//            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
//            TimeZone tz = TimeZone.getTimeZone("GMT+8");
//            sdf.setTimeZone(tz);
//            String str = sdf.format(Calendar.getInstance().getTime());
//            System.out.println(str);
//            Date s;
//            try {
//                s = sdf.parse(dateString);
//                sdf = new SimpleDateFormat("yyyy-MM-dd");
//                System.out.println(sdf.format(s));
//                users3.setUserBirthdate(sdf.format(s));
//            } catch (ParseException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//        }
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
    //导航栏查看已完成但未评价的活动
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
    //所有参与这个活动的用户的列表
    @RequestMapping(value = "/getRequestListScoreJsonData")
    @ResponseBody
    public String getJsonDataFromReqest(@RequestParam int offset, int limit, String sortName,String sortOrder,Model model){
        //获得当前用户
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

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
    @RequestMapping(value="/getUserScoreJsonData")
    @ResponseBody
    public String getUserListJsonData(Model model, @RequestParam int offset, int limit, String sortName, String sortOrder, String resGuid){
        System.out.println("获取到用户数据");
        System.out.println(resGuid);
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
        List<Users> usersList = new ArrayList<Users>();

        respondExample.or().andResReqGuidEqualTo(resGuid);
        List<Respond> responds=respondMapper.selectByExample(respondExample);
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
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        model.addAttribute("resGuid",id);
        //需要将分数插入到respond表单中
        RespondExample respondExample=new RespondExample();
        respondExample.or().andResReqGuidEqualTo(id);
        List<Respond>responds=respondMapper.selectByExample(respondExample);
        for (Respond it:responds){
             if (it.getResEvaluate().equals(0))
                   if (it.getResUserGuid().equals(thisPerson1)) {
                       it.setResEvaluate(Integer.parseInt(finalScore));
                       respondMapper.updateByPrimaryKeySelective(it);
                   }

        }
        return "scoreForVolunteer";
    }




}
