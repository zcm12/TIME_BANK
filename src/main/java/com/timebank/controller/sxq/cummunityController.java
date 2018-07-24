package com.timebank.controller.sxq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.domain.*;
import com.timebank.mapper.CommunityMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

@Controller
public class cummunityController {
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private TypeMapper typeMapper;
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
    //test测试
    @RequestMapping(value = "/createCommunityView")
    public String createCommunityView(Model model){

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);

        return "creatcommunityview";
    }
    @RequestMapping(value = "/showCommunityView")
    public String showCommunityView(Model model){


        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);
        return "showcommunityview";
    }

    @RequestMapping(value = "/creatcommunityinsert")
    public String createcommunityinsert(@ModelAttribute @Valid Community community, Errors errors, Model model){

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);

        if (!errors.hasErrors()) {
            UUID guid = randomUUID();
            community.setCommGuid(guid.toString());
            communityMapper.insertSelective(community);
        }
        return "showcommunityview";
    }


    @RequestMapping(value="/getCOMMUNITYListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getCOMMUNITYListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder){
        CommunityExample communityExample=new CommunityExample();
        communityExample.clear();
        System.out.println("排序信息："+sortName+";"+sortOrder);

        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            communityExample.setOrderByClause(order);
        }

        List<Community> communitys=communityMapper.selectByExample(communityExample);
        List<Community> communityRecordList=new ArrayList<Community>();
        for(int i=offset;i< offset+limit&&i < communitys.size();i++){

            Community community1=communitys.get(i);
            TypeExample typeExample = new TypeExample();
            communityRecordList.add(community1);
        }
        //全部符合要求的数据的数量
        int total=communitys.size();
        System.out.println("总数："+total);
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(communityRecordList,total);
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


    @RequestMapping(value = "/COMMUNITY/{communityId}")
    public String cummunityDetail(Model model,@PathVariable String communityId){


        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);

        Community community = communityMapper.selectByPrimaryKey(communityId);
        model.addAttribute("community",community);
        return "showcommunitydetail";
    }

    @RequestMapping(value = "/communityupdate")
    public String activityUpdate(@ModelAttribute @Valid Community community,Errors errors,Model model){

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users=GetCurrentUsers(message);
        String role=users.getUserRole();
        model.addAttribute("role",role);

        if (!errors.hasErrors()){
            communityMapper.updateByPrimaryKeySelective(community);
        }
        return "showcommunityview";
    }
}
