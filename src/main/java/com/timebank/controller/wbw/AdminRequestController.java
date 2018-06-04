package com.timebank.controller.wbw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.timebank.domain.*;
import com.timebank.mapper.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.UUID.randomUUID;

@Controller
//@SessionAttributes(value = {"userId", "role"})
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
    //定义请求状态分类 默认查看所有
//    private String requestStateClass = "0";

    /**
     * 模拟平台管理员登录
     *
     * @param model
     * @return
     */
//    @RequestMapping(value = "/0")
//    public String login(Model model) {
//
//        model.addAttribute("userId", "wbw");
//        model.addAttribute("role", 0);
//        return "listRequestByAdminView";
//    }

    //左边代发请求按钮
    @RequestMapping(value = "/createRequestByAdminView")
//    public String createRequestByAdminView(@ModelAttribute("userId") String userId, @ModelAttribute("role") Integer role,
//                                           Model model) {
        public String createRequestByAdminView(Model model) {
//        setUserIdandrole(model, userId, role);
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        //插入type和weight
        insertReqType(model,false);
        return "createRequestByAdminView";
    }

    //代发请求界面中的 “保存”按钮  将数据插入到数据库
    @RequestMapping(value = "/createRequestByAdmin")
//    public String insertRequest(@ModelAttribute("userId") String userId, @ModelAttribute("role") Integer role,
//                                @ModelAttribute @Valid Reqest reqest, Errors errors, Model model) {
    public String insertRequest(@ModelAttribute @Valid Reqest reqest, Errors errors, Model model) {
        if (!errors.hasErrors()) {
            Subject account = SecurityUtils.getSubject();
            UsersExample usersExample=new UsersExample();
            usersExample.or().andUserAccountEqualTo((String)account.getPrincipal());
            List<Users> users=usersMapper.selectByExample(usersExample);
            Users users1=users.get(0);
            String userId=users1.getUserGuid();
            //给发布的请求生成一个GUID,作为该请求的唯一标识
            reqest.setReqGuid(randomUUID().toString());
            String AA=reqest.getReqIssueUserGuid();//前端界面的输入值
            UsersExample usersExample1=new UsersExample();
            usersExample1.or().andUserAccountEqualTo(AA);
            List<Users> users2=usersMapper.selectByExample(usersExample1);//根据账号名找到其用户
            Users users3=users2.get(0);
            String userGuid=users3.getUserGuid();//得到用户的guid 并且插入
            reqest.setReqIssueUserGuid(userGuid);

//            reqest.setReqIssueUserGuid("48abce9f-36f4-4ddb-9891-10842434a688");
            //因为是代发请求,无需审核,提出请求时间和请求发布时间可用一个(当前时间即可)
            Date date = new Date();
            reqest.setReqIssueTime(date);
            reqest.setReqDispatchTime(date);
            //接收请求的用户的guid
            reqest.setReqTargetsUserGuid("48abce9f-36f4-4ddb-9891-10842434a688");
            //管理员的guid
            reqest.setReqProcessUserGuid(userId);
            //管理员代发请求,无需审核,请求批准状态和请求处理状态为"通过"和"未启动"
            reqest.setReqTypeApproveStatus("88888888-94e3-4eb7-aad3-111111111111");
            reqest.setReqTypeGuidProcessStatus("33333333-94e3-4eb7-aad3-111111111111");
            reqestMapper.insert(reqest);
        }
        return "listRequestByAdminView";
    }

    //查看详情
    @RequestMapping(value = "/showReqestDetailViewByAdmin/{reqGuid}")
//    public String showTeacherView1(@ModelAttribute("userId") String userId, @ModelAttribute("role") Integer role,
//                                   @PathVariable String reqGuid, Model model) {
    public String showTeacherView1(@PathVariable String reqGuid, Model model) {
//        setUserIdandrole(model, userId, role);
        System.out.println("查看详情按钮");
        Subject subject=SecurityUtils.getSubject();
        UsersExample usersExample=new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) subject.getPrincipal());
        List<Users> users=usersMapper.selectByExample(usersExample);
        Users users1=users.get(0);
        String role=users1.getUserRole();

        model.addAttribute("role",role);

        //插入type和weight
        TypeExample typeExample = insertReqType(model,true);

        Reqest reqest = reqestMapper.selectByPrimaryKey(reqGuid);
        model.addAttribute("reqest", reqest);
        System.out.println("showReqestDetailViewByAdmin Successful");

        /*保存按钮的显示和隐藏*/
        isHiddenBtnSubmit(reqGuid, model, typeExample);

        return "showReqestDetailViewByAdmin";
    }

    //查看详情中的保存按钮
    @RequestMapping(value = "/updateReqestByAdmin")
//    public String updateReqestByAdmin(@ModelAttribute("userId") String userId, @ModelAttribute("role") Integer role,
//                                      @ModelAttribute Reqest reqest, Model model) {
    public String updateReqestByAdmin(@ModelAttribute Reqest reqest, Model model) {
        //TODO:接受请求人ID,这个需要在审核通过后,跳转到专门的页面选择接受人(此处模拟添加一个,否则报错)
        if (reqest.getReqTargetsUserGuid() == null) {
            reqest.setReqTargetsUserGuid("48abce9f-36f4-4ddb-9891-10842434a688");
        }
        Subject subject= SecurityUtils.getSubject();
        UsersExample usersExample=new UsersExample();
        usersExample.or().andUserAccountEqualTo((String)subject.getPrincipal());
        List<Users> users=usersMapper.selectByExample(usersExample);
        Users users1=users.get(0);
        reqest.setReqProcessUserGuid(users1.getUserGuid());


//        setUserIdandrole(model, userId, role);
//        System.out.println("updateReqestByAdmin");
        String reqTypeGuidProcessStatus = reqest.getReqTypeGuidProcessStatus();
//        TypeExample typeExample = new TypeExample();
//        typeExample.or().andTypeGuidEqualTo(reqTypeGuidProcessStatus);
//        String typeTitle = typeMapper.selectByExample(typeExample).get(0).getTypeTitle();
//
//        if ("33333333-94e3-4eb7-aad3-111111111111".equals(reqTypeGuidProcessStatus)) {
//            //TODO:跳转到选择请求接收人的页面
//            System.out.println("跳转到选择请求接收人的页面");
//            return "addTargetsUsers";
//        }
        reqestMapper.updateByPrimaryKeySelective(reqest);
        return "listRequestByAdminView";
    }

/*----------------------我是下划线,以下为查询数据库并展示数据---------------------*/

   //左边查看各种请求
    @RequestMapping(value = "/listRequestByAdminView/{requestState}")
    public String listRequestByAdminView( Model model, @PathVariable String requestState) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role",role);
        model.addAttribute("requestState",requestState);
//        requestStateClass = requestState;
//        System.out.println("requestStateClass:" + requestStateClass);
        return "listRequestByAdminView";
    }

  //listRequestByAdminView界面中的向后台请求数据
    @RequestMapping(value = "/getREQESTListJsonDataByAdmin", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getREQESTListJsonDataByAdmin(Model model, int offset, int limit, String sortName, String sortOrder,String requestState) {
        ReqestExample reqestExample = new ReqestExample();
        System.out.println(requestState);
        TypeExample typeExample = new TypeExample();
        if (requestState.equals("1")){//待审核
            reqestExample.or().andReqTypeGuidProcessStatusEqualTo("88888888-94e3-4eb7-aad3-333333333333");
        }
        if (requestState.equals("2")){//通过
            reqestExample.or().andReqTypeGuidProcessStatusEqualTo("88888888-94e3-4eb7-aad3-111111111111");
        }
        if (requestState.equals("3")){//驳回
            reqestExample.or().andReqTypeGuidProcessStatusEqualTo("88888888-94e3-4eb7-aad3-222222222222");
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

        //查看所有请求
        return getJsonDate(offset, limit, sortName, sortOrder, reqestExample);
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
     *
     * @param offset
     * @param limit
     * @param sortName
     * @param sortOrder
     * @param reqestExample
     * @return
     */
    private String getJsonDate(@RequestParam int offset, int limit, String sortName, String sortOrder, ReqestExample reqestExample) {
//        System.out.println("排序信息：" + sortName + ";" + sortOrder);
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
                // System.out.println(jj);
                usersExample.clear();
                usersExample.or().andUserGuidEqualTo(jj);
                List<Users> responduser = usersMapper.selectByExample(usersExample);
                respondName = responduser.get(0).getUserName();
//                System.out.println(respondName);
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
     *
     * @param reqGuid
     * @param model
     * @param typeExample
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
     * @param role
     */
    public void setUserIdandrole(Model model, String userId, Integer role) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role",role100);
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
