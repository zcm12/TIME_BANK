package com.timebank.controller.sxq;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    private Users GetCurrentUsers(String message) {
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

    //test测试
    @RequestMapping(value = "/createCommunityView")
    public String createCommunityView(Model model) {
        try {
            Subject account = SecurityUtils.getSubject();
            String message = (String) account.getPrincipal();
            Users users = GetCurrentUsers(message);
            String role = users.getUserRole();
            model.addAttribute("role", role);

            return "creatcommunityview";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @RequestMapping(value = "/showCommunityView")
    public String showCommunityView(Model model) {
        try {
            Subject account = SecurityUtils.getSubject();
            String message = (String) account.getPrincipal();
            Users users = GetCurrentUsers(message);
            String role = users.getUserRole();
            model.addAttribute("role", role);
            return "showcommunityview";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @RequestMapping(value = "/creatcommunityinsert")
    public String createcommunityinsert(@ModelAttribute @Valid Community community, Errors errors, Model model) {
        try{
        Subject account = SecurityUtils.getSubject();
        String message = (String) account.getPrincipal();
        Users users = GetCurrentUsers(message);
        String role = users.getUserRole();
        model.addAttribute("role", role);

        if (!errors.hasErrors()) {
            UUID guid = randomUUID();
            community.setCommGuid(guid.toString());
            communityMapper.insertSelective(community);
        }
        return "showcommunityview";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }


    @RequestMapping(value = "/getCOMMUNITYListJsonData", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getCOMMUNITYListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder, String searchText) {
        try{
        CommunityExample communityExample = new CommunityExample();
        communityExample.clear();
        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            //将排序信息添加到example中
            communityExample.setOrderByClause(order);
        }
        List<Community> communitys = communityMapper.selectByExample(communityExample);
        List<Community> communityRecordList = new ArrayList<>();//搜索框的集合
        for (int i = 0; i < communitys.size(); i++) {
            Community community = communitys.get(i);
            if (searchText != null) {
                String commName = community.getCommTitle();
                String commAddress = community.getCommAddress();
                String commDescribe = community.getCommDesp();
                if (commName.contains(searchText) || commAddress.contains(searchText) || commDescribe.contains(searchText)) {
                    communityRecordList.add(community);
                }
            } else {
                communityRecordList.add(community);
            }
        }
        List<Community> communityList = new ArrayList<>();//分页的集合
        for (int i = offset; i < offset + limit && i < communityRecordList.size(); i++) {
            Community community1 = communityRecordList.get(i);
            communityList.add(community1);
        }
        //全部符合要求的数据的数量
        int total = communityRecordList.size();
        System.out.println("总数：" + total);
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(communityList, total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            return json1;
        } catch (Exception e) {
            return null;
        }
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
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


    @RequestMapping(value = "/COMMUNITY/{communityId}")
    public String cummunityDetail(Model model, @PathVariable String communityId) {
        try{
        Subject account = SecurityUtils.getSubject();
        String message = (String) account.getPrincipal();
        Users users = GetCurrentUsers(message);
        String role = users.getUserRole();
        model.addAttribute("role", role);

        Community community = communityMapper.selectByPrimaryKey(communityId);
        model.addAttribute("community", community);
        return "showcommunitydetail";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @RequestMapping(value = "/communityupdate")
    public String activityUpdate(@ModelAttribute @Valid Community community, Errors errors, Model model) {
        try {
            Subject account = SecurityUtils.getSubject();
            String message = (String) account.getPrincipal();
            Users users = GetCurrentUsers(message);
            String role = users.getUserRole();
            model.addAttribute("role", role);

            if (!errors.hasErrors()) {
                communityMapper.updateByPrimaryKeySelective(community);
            }
            return "showcommunityview";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
    //通过省市区定位获取小区  事件 ajax
    @RequestMapping("/css/CummTestAjax")
    @ResponseBody
    public String getCummunityType(String province,String city,String district, Model model) throws JsonProcessingException {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(province);
        stringBuilder.append(city);
        stringBuilder.append(district);
        String qianAddress=""+stringBuilder;
        //先获取到所有的小区列表
        CommunityExample communityExample=new CommunityExample();
        List<Community> communityList=communityMapper.selectByExample(communityExample);
        List<Community> communityList1=new ArrayList<>();
        //筛选通过中包含前端省市区下拉选中的地址的 小区列表  /
        for (Community it:communityList){
            String comAddress=it.getCommAddress();
//            System.out.println("全部的小区地址："+comAddress);
            if(!(comAddress!=null)){
                comAddress="";
            }
            if(comAddress.contains(qianAddress)){
                communityList1.add(it);
            }
        }
        int total=0;
        for (Community it2:communityList1){
            System.out.println("输出筛选出含有所选地址的小区："+it2.getCommAddress());
            total++;
        }
        //将类的集合封装成json
        ObjectMapper mapper = new ObjectMapper();
        com.timebank.controller.sxq.TableRecordsJson tableRecordsJson = new com.timebank.controller.sxq.TableRecordsJson(communityList1, total);
        String json1 = mapper.writeValueAsString(tableRecordsJson);
        System.out.println(json1);
        return json1;

    }
    //通过省市区定位获取小区  事件 ajax
    @RequestMapping("/css/CummTestAjaxb")
    @ResponseBody
    public String getCummunityTypeb(String province,String city,String district, Model model) throws JsonProcessingException {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(province);
        stringBuilder.append(city);
        stringBuilder.append(district);
        String qianAddress=""+stringBuilder;
        //先获取到所有的小区列表
        CommunityExample communityExample=new CommunityExample();
        List<Community> communityList=communityMapper.selectByExample(communityExample);
        List<Community> communityList1=new ArrayList<>();
        //筛选通过中包含前端省市区下拉选中的地址的 小区列表  /
        for (Community it:communityList){
            String comAddress=it.getCommAddress();
//            System.out.println("全部的小区地址："+comAddress);
            if(!(comAddress!=null)){
                comAddress="";
            }
            if(comAddress.contains(qianAddress)){
                communityList1.add(it);
            }
        }
        int total=0;
        for (Community it2:communityList1){
            System.out.println("输出筛选出含有所选地址的小区："+it2.getCommAddress());
            total++;
        }
        //将类的集合封装成json
        ObjectMapper mapper = new ObjectMapper();
        com.timebank.controller.sxq.TableRecordsJson tableRecordsJson = new com.timebank.controller.sxq.TableRecordsJson(communityList1, total);
        String json1 = mapper.writeValueAsString(tableRecordsJson);
        System.out.println(json1);
        return json1;

    }
    //通过省市区定位获取小区  事件 ajax
    @RequestMapping("/css/CummTestAjaxc")
    @ResponseBody
    public String getCummunityTypec(String province,String city,String district, Model model) throws JsonProcessingException {
//        Subject account = SecurityUtils.getSubject();
//        String message=(String) account.getPrincipal();
//        Users users11=GetCurrentUsers(message);
//        String role=users11.getUserRole();
//        model.addAttribute("role",role);
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(province);
        stringBuilder.append(city);
        stringBuilder.append(district);
        String qianAddress=""+stringBuilder;
        //先获取到所有的小区列表
        CommunityExample communityExample=new CommunityExample();
        List<Community> communityList=communityMapper.selectByExample(communityExample);
        List<Community> communityList1=new ArrayList<>();
        //筛选通过中包含前端省市区下拉选中的地址的 小区列表  /
        for (Community it:communityList){
            String comAddress=it.getCommAddress();
//            System.out.println("全部的小区地址："+comAddress);
            if(!(comAddress!=null)){
                comAddress="";
            }
            if(comAddress.contains(qianAddress)){
                communityList1.add(it);
            }
        }
        int total=0;
        for (Community it2:communityList1){
            System.out.println("输出筛选出含有所选地址的小区："+it2.getCommAddress());
            total++;
        }
        //将类的集合封装成json
        ObjectMapper mapper = new ObjectMapper();
        com.timebank.controller.sxq.TableRecordsJson tableRecordsJson = new com.timebank.controller.sxq.TableRecordsJson(communityList1, total);
        String json1 = mapper.writeValueAsString(tableRecordsJson);
        System.out.println(json1);
        return json1;

    }
}
