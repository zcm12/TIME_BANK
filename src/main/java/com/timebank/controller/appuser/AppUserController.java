package com.timebank.controller.appuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ResultModel;
import com.timebank.controller.yl.TableRecordsJson;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

// 修改和查看个人信息
@Controller
public class AppUserController {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private ReqestMapper reqestMapper;
    @Autowired
    private RespondMapper respondMapper;

    /*---------------app api------------------------*/
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

    //查看个人信息
    @RequestMapping(value = "/appUserInfo")
    @ResponseBody
    public Users appUserInfo() {
        Subject subject = SecurityUtils.getSubject();
        String userAccount = (String) subject.getPrincipal();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo(userAccount);
        List<Users> users1 = usersMapper.selectByExample(usersExample);
        Users users2 = users1.get(0);
        if (users2.getUserTypeGuidGender() != null) {
            //处理性别
            Type type = typeMapper.selectByPrimaryKey(users2.getUserTypeGuidGender());
            users2.setUserTypeGuidGender(type.getTypeTitle());
        }
        if (users2.getUserTypeAccountStatus() != null) {
            //用户状态
            Type type1 = typeMapper.selectByPrimaryKey(users2.getUserTypeAccountStatus());
            users2.setUserTypeAccountStatus(type1.getTypeTitle());
        }
        if (users2.getUserCommGuid() != null) {
            //所属小区
            Community community = communityMapper.selectByPrimaryKey(users2.getUserCommGuid());
            users2.setUserCommGuid(community.getCommTitle());
        }
        System.out.println(users2.getUserTypeAccountStatus());
        return users2;
    }

    @RequestMapping(value = "/appGetCom")
    @ResponseBody
    public List<Community> appGetCom() {
        Subject account = SecurityUtils.getSubject();
        System.out.println("account" + account);
        System.out.println("principal" + account.getPrincipal().toString());
        CommunityExample communityExample = new CommunityExample();
        List<Community> communities = communityMapper.selectByExample(communityExample);
        System.out.println("小区集合：" + communities);
        return communities;
    }

    //更新用户信息
    @RequestMapping(value = "/appUpdateUserInfo")
    @ResponseBody
    public ResultModel appUpdateUserInfo(Users u) {

        Subject account = SecurityUtils.getSubject();
        String userAccount = (String) account.getPrincipal();
        UsersExample usersExample = new UsersExample();
        List<Users> usersList = usersMapper.selectByExample(usersExample);//所有用户
        usersExample.clear();
        usersExample.or().andUserAccountEqualTo(userAccount);
        List<Users> users1 = usersMapper.selectByExample(usersExample);
        Users user = users1.get(0);//当前用户
        //所有用户中删除当前用户
        for (int i = 0; i < usersList.size(); i++) {
            if (usersList.get(i).getUserGuid().equals(user.getUserGuid())) {
                usersList.remove(i);
            }
        }

        //不包括当前用户的集合
        for (Users users : usersList) {
            //邮箱重名校验
            if (u.getUserMail() != null && u.getUserMail().equals(users.getUserMail())) {
                return new ResultModel(11, "该邮箱已使用，请更换");
            }
            //手机号重名校验
            if (u.getUserPhone() != null && u.getUserPhone().equals(users.getUserPhone())) {
                return new ResultModel(12, "该手机号已使用，请更换");
            }
        }


        String userTypeGuidGender = u.getUserTypeGuidGender();
        String userCommGuid = u.getUserCommGuid();

        //处理性别
        TypeExample typeExample = new TypeExample();
        typeExample.or().andTypeTitleEqualTo(userTypeGuidGender);
        List<Type> types = typeMapper.selectByExample(typeExample);
        Type type = types.get(0);
        user.setUserTypeGuidGender(type.getTypeGuid());
        //处理小区
        CommunityExample communityExample = new CommunityExample();
        communityExample.or().andCommTitleEqualTo(userCommGuid);
        List<Community> communities = communityMapper.selectByExample(communityExample);
        Community community = communities.get(0);
        user.setUserCommGuid(community.getCommGuid());
        //处理其他
        user.setUserAddress(u.getUserAddress());
        user.setUserName(u.getUserName());
        user.setUserMail(u.getUserMail());
        user.setUserPhone(u.getUserPhone());
        user.setUserIdnum(u.getUserIdnum());
        user.setUserBirthdate(u.getUserBirthdate());
        user.setUserEmerperson(u.getUserEmerperson());
        user.setUserEmercontact(u.getUserEmercontact());
        user.setUserProvince(u.getUserProvince());
        user.setUserCity(u.getUserCity());
        user.setUserDistrict(u.getUserDistrict());
        user.setUserBirthdate(u.getUserBirthdate());

        //更新数据库
        int update = usersMapper.updateByPrimaryKey(user);
        return new ResultModel(update, "信息保存成功");

    }

    @RequestMapping(value = "/appQueryNearbyReq")
    @ResponseBody
    public String appQueryNearbyReq(Users users) {
        Subject account = SecurityUtils.getSubject();
        String message = (String) account.getPrincipal();
        Users users1 = GetCurrentUsers(message);

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


    @ResponseBody
    @RequestMapping(value = "/imageUpload")
    public Object imageUpload(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException, IOException {


//        ShiroHttpServletRequest shiroRequest = (ShiroHttpServletRequest) request;
//        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
//        MultipartHttpServletRequest req = commonsMultipartResolver.resolveMultipart((HttpServletRequest) shiroRequest.getRequest());
        MultipartHttpServletRequest req =(MultipartHttpServletRequest)request;
        MultipartFile multipartFile =  req.getFile("userIdimage");
        String realPath = "F:/image";
        try {
            File dir = new File(realPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file  =  new File(realPath,"aaa.jpg");
            multipartFile.transferTo(file);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }
/*
@ResponseBody
    @RequestMapping(value = "/imageUpload")
    public Object imageUpload(@RequestParam(value = "photo") String photo, String actiontype, @RequestParam(value = "userId") String userId, HttpServletRequest request, HttpServletResponse response) throws IllegalStateException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html");
        Map<String, Object> map = new HashMap<String, Object>();
        System.out.println("userId:" + userId);
        System.out.println("photo:" + photo);
        try {

            // 对base64数据进行解码 生成 字节数组，不能直接用Base64.decode（）；进行解密
            byte[] photoimg = new BASE64Decoder().decodeBuffer(photo);
            for (int i = 0; i < photoimg.length; ++i) {
                if (photoimg[i] < 0) {
                    // 调整异常数据
                    photoimg[i] += 256;
                }
            }
            // 获取项目运行路径
            String pathRoot = request.getSession().getServletContext().getRealPath("") + "/images/";
            File dir = new File(pathRoot);
            if (!dir.exists() && !dir.isDirectory()) {
                System.out.println("//不存在");
                dir.mkdir();
            }
            // 生成uuid作为文件名称
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");

            String path = uuid + "head.png";
            // byte[] photoimg =
            // Base64.decode(photo);//此处不能用Base64.decode（）方法解密，我调试时用此方法每次解密出的数据都比原数据大
            // 所以用上面的函数进行解密，在网上直接拷贝的，花了好几个小时才找到这个错误（菜鸟不容易啊）
            System.out.println("图片的大小：" + photoimg.length);
            File file = new File(pathRoot + path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            out.write(photoimg);
            out.flush();
            out.close();
            map.put("updateImage", "filed");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "Success";

    }
*/

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
