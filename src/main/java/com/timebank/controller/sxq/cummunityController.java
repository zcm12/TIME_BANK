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

    //test测试
    @RequestMapping(value = "/createCommunityView")
    public String createCommunityView(Model model){

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role",role100);

        return "creatcommunityview";
    }
    @RequestMapping(value = "/showCommunityView")
    public String showCommunityView(Model model){

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role",role100);

        return "showcommunityview";
    }

    @RequestMapping(value = "/creatcommunityinsert")
    public String createcommunityinsert(@ModelAttribute @Valid Community community, Errors errors, Model model){

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role",role100);

        if (!errors.hasErrors()) {
            UUID guid = randomUUID();
            community.setCommGuid(guid.toString());
            communityMapper.insertSelective(community);
        }
        return "showcommunityview";
    }


    @RequestMapping(value="/getCOMMUNITYListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getCOMMUNITYListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder,String searchText){
        CommunityExample communityExample=new CommunityExample();
        communityExample.clear();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            communityExample.setOrderByClause(order);
        }
        List<Community> communitys=communityMapper.selectByExample(communityExample);
        List<Community> communityRecordList=new ArrayList<>();//搜索框的集合
        for (int i=0;i<communitys.size();i++){
            Community community=communitys.get(i);
            if (searchText!=null){
                String commName=community.getCommTitle();
                String commAddress=community.getCommAddress();
                String commDescribe=community.getCommDesp();
                if (commName.contains(searchText)||commAddress.contains(searchText)||commDescribe.contains(searchText)){
                    communityRecordList.add(community);
                }
            }else {
                communityRecordList.add(community);
            }
        }
        List<Community>communityList=new ArrayList<>();//分页的集合
        for(int i=offset;i< offset+limit&&i < communityRecordList.size();i++){
            Community community1=communityRecordList.get(i);
            communityList.add(community1);
        }
        //全部符合要求的数据的数量
        int total=communityRecordList.size();
        System.out.println("总数："+total);
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(communityList,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
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
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role",role100);

        Community community = communityMapper.selectByPrimaryKey(communityId);
        model.addAttribute("community",community);
        return "showcommunitydetail";
    }

    @RequestMapping(value = "/communityupdate")
    public String activityUpdate(@ModelAttribute @Valid Community community,Errors errors,Model model){


        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample100 = new UsersExample();
        usersExample100.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users10 = usersMapper.selectByExample(usersExample100);
        Users users100 = users10.get(0);
        String role100 = users100.getUserRole();
        model.addAttribute("role",role100);


        if (!errors.hasErrors()){
            communityMapper.updateByPrimaryKeySelective(community);
        }
        return "showcommunityview";
    }
}
