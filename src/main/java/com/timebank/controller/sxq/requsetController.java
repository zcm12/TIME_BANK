package com.timebank.controller.sxq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.domain.*;
import com.timebank.mapper.ReqestMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import com.timebank.mapper.WeightMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class requsetController {
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private ReqestMapper reqestMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private WeightMapper weightMapper;
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

    @RequestMapping(value = "/waitsomebody")
    public String gotorequestlist(Model model){



        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);
        return "reqlistprocess";
    }

    @RequestMapping(value="/getREQESTListJsonDataActivity")
    @ResponseBody
    public String getREQESTListJsonData(Model model,@RequestParam int offset, int limit, String sortName, String sortOrder){

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);


        ReqestExample reqestExample=new ReqestExample();
        reqestExample.clear();
        System.out.println("排序信息："+sortName+";"+sortOrder);

        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            reqestExample.setOrderByClause(order);
        }

        reqestExample.or().andReqTypeApproveStatusEqualTo("88888888-94e3-4eb7-aad3-111111111111").andReqTypeGuidProcessStatusEqualTo("33333333-94e3-4eb7-aad3-111111111111");
        List<Reqest> reqests=reqestMapper.selectByExample(reqestExample);
        List<Reqest> reqestRecordList=new ArrayList<Reqest>();
        UsersExample usersExample=new UsersExample();
        for(int i=offset;i< offset+limit&&i < reqests.size();i++){

            Reqest reqest1=reqests.get(i);
           TypeExample typeExample = new TypeExample();
            UsersExample usersExample1 = new UsersExample();
            WeightExample weightExample = new WeightExample();
//
            String reqIssueUserGuid=reqest1.getReqIssueUserGuid();
            usersExample1.clear();
            usersExample1.or().andUserGuidEqualTo(reqIssueUserGuid);
            List<Users> users1 = usersMapper.selectByExample(usersExample1);
            reqest1.setReqIssueUserGuid(users1.get(0).getUserName());
//
            String reqTypeGuidClass=reqest1.getReqTypeGuidClass();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(reqTypeGuidClass);
            List<Type> typeclass = typeMapper.selectByExample(typeExample);
            reqest1.setReqTypeGuidClass(typeclass.get(0).getTypeTitle());
//
////            String reqTypeApproveStatus=reqest1.getReqTypeApproveStatus();
////            typeExample.clear();
////            typeExample.or().andTypeGuidEqualTo(reqTypeApproveStatus);
////            List<Type> types = typeMapper.selectByExample(typeExample);
////            reqest1.setReqTypeApproveStatus(types.get(0).getTypeTitle());
//
            String reqTypeGuidUrgency=reqest1.getReqTypeGuidUrgency();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(reqTypeGuidUrgency);
            List<Type> typesurgency = typeMapper.selectByExample(typeExample);
            reqest1.setReqTypeGuidUrgency(typesurgency.get(0).getTypeTitle());
//
            String reqFromWeightGuid=reqest1.getReqFromWeightGuid();
            weightExample.clear();
            weightExample.or().andWeightGuidEqualTo(reqFromWeightGuid);
            List<Weight> weights = weightMapper.selectByExample(weightExample);
            System.out.println(weights);
            reqest1.setReqFromWeightGuid(weights.get(0).getWeightTitle());

            String reqTypeGuidProcessStatus=reqest1.getReqTypeGuidProcessStatus();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(reqTypeGuidProcessStatus);
            List<Type> types = typeMapper.selectByExample(typeExample);
            reqest1.setReqTypeGuidProcessStatus(types.get(0).getTypeTitle());

            String reqTypeApproveStatus = reqest1.getReqTypeApproveStatus();
            typeExample.clear();
            typeExample.or().andTypeGuidEqualTo(reqTypeApproveStatus);
            List<Type> typeApprove = typeMapper.selectByExample(typeExample);
            reqest1.setReqTypeApproveStatus(typeApprove.get(0).getTypeTitle());
//
            String reqProcessUserGuid=reqest1.getReqProcessUserGuid();
            usersExample.clear();
            usersExample.or().andUserGuidEqualTo(reqProcessUserGuid);
            List<Users> typeprocessstates = usersMapper.selectByExample(usersExample);
            reqest1.setReqProcessUserGuid(typeprocessstates.get(0).getUserName());


            //把接收者的Id换成名字显示在页面
                String reqtargetuser = reqest1.getReqTargetsUserGuid();
                String sb1 = IdtoName(reqtargetuser);
                reqest1.setReqTargetsUserGuid(sb1);
//            //把请求处理人的Id换成名字显示在页面
//                String reqProcessPerson = reqest1.getReqProcessUserGuid();
//                String sb2 = IdtoName(reqProcessPerson);
//                reqest1.setReqProcessUserGuid(sb2);


            reqestRecordList.add(reqest1);
        }
        //全部符合要求的数据的数量
        int total=reqests.size();
        System.out.println("总数："+total);
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


        @RequestMapping(value = "/REQEST/{request1}")
        public String reqdetailshow(Model model,@PathVariable String request1){
            Subject account = SecurityUtils.getSubject();
            String message=(String) account.getPrincipal();
            Users users=GetCurrentUsers(message);
            String role=users.getUserRole();
            model.addAttribute("role",role);

            Reqest reqest = reqestMapper.selectByPrimaryKey(request1);
            model.addAttribute("reqest",reqest);

            TypeExample typeExample = new TypeExample();
            typeExample.clear();
            typeExample.or().andTypeGroupIdEqualTo(3);
            List<Type> type3 = typeMapper.selectByExample(typeExample);
            model.addAttribute("type3",type3);

            //多个type映射到前台的时候必须要加上clear，否则一个下拉框里会出现其他的type
            typeExample.clear();
            typeExample.or().andTypeGroupIdEqualTo(4);
            List<Type> type4 = typeMapper.selectByExample(typeExample);
            model.addAttribute("type4",type4);

            typeExample.clear();
            typeExample.or().andTypeGroupIdEqualTo(5);
            List<Type> type5 = typeMapper.selectByExample(typeExample);
            model.addAttribute("type5",type5);

            typeExample.clear();
            typeExample.or().andTypeGroupIdEqualTo(6);
            List<Type> type6 = typeMapper.selectByExample(typeExample);
            model.addAttribute("type6",type6);

            WeightExample weightExample = new WeightExample();
         //   weightExample.or().andWeightGuidEqualTo()
            List<Weight> weights = weightMapper.selectByExample(weightExample);
            model.addAttribute("weight",weights);

            return "reqdetailprocess";
           }


/***************************************************************************/
    /**
     名字转换函数
     */
    private String IdtoName(String reqtargetuser) {
        if (reqtargetuser != null) {
            UsersExample usersExample = new UsersExample();
                StringBuilder sb = new StringBuilder();
                //正则表达式，取出方括号里面的值
                String r = reqtargetuser.replaceAll("^.*\\[", "").replaceAll("].*", "");
                //     System.out.println(r);
                //分割字符串
                String[] array = r.split(",");
          //  System.out.println(array.length);
                String respondName = null;
                for (int in = 0; in < array.length; in++) {
                    //去掉双引号
                    String jj = array[in].replaceAll("\"", "");
                //    System.out.println(jj);
                    usersExample.clear();
                    usersExample.or().andUserGuidEqualTo(jj);
                    List<Users> responduser = usersMapper.selectByExample(usersExample);
                    respondName = responduser.get(0).getUserName();
             //       System.out.println(respondName);
                    sb.append(respondName).append(" ");

                }
            String sb1 = sb.toString();
            return  sb1;
        }
        return null;
    }



}
