package com.timebank.controller.yl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.domain.*;
import com.timebank.mapper.TransferMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
/***
 * 收款服务
 * 汇款服务
 */

@Controller
public class transferController {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private TransferMapper transferMapper;

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
    //汇款服务
    @RequestMapping(value = "/remitServicesView")
    public String remitServicesView(Model model) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

        return "transferServices";
    }
    //汇款提交
    @RequestMapping(value = "/transferSubmit")
    public String updateREQESTSave(Transfer transfer, Model model) {

        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);

        //转账ID//转账者
        UUID guid=randomUUID();
        transfer.setTransGuid(guid.toString());
        transfer.setTransFromUserGuid(users1.getUserGuid());
        //对方账号
        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(transfer.getTransToUserGuid());
        List<Users> users11 = usersMapper.selectByExample(usersExample1);
        //如果有这个账户名的话，将这个账户名对应的ID存到trans表中
        transfer.setTransToUserGuid(users11.get(0).getUserGuid());
        //转账时间
        transfer.setTransIssueTime(new Date());
        //转账进程先设置为待处理
        transfer.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-111111111111");
        //先不要改变账户的持有的钱数转账(USER表)
        transferMapper.insertSelective(transfer);
        return "transferList";

    }

    //收款按钮
    @RequestMapping(value = "/receiveServicesView")
    public String receiveServicesView(Model model) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        return "receiveServices";
    }
    //收款列表请求数据
    @RequestMapping(value="/getTRANSFERListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getTRANSFERListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
//        model.addAttribute("role",role);
        TransferExample transferExample=new TransferExample();
        transferExample.or().andTransTypeGuidProcessStatusNotEqualTo("66666666-94e3-4eb7-aad3-666666666666");
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            transferExample.setOrderByClause(order);
        }
        //判断
        List<Transfer> transfers1=transferMapper.selectByExample(transferExample);
        //存储转给当前登陆者的那些记录
        List<Transfer> transfers = new ArrayList<>();
        for (Transfer t : transfers1)
        {
            if (t.getTransToUserGuid().contains(users11.getUserGuid()))
            {
                transfers.add(t);
            }
        }
        List<Transfer> transferRecordList=new ArrayList<>();
        UsersExample usersExample=new UsersExample();
        for(int i=offset;i< offset+limit&&i < transfers.size();i++){
            Transfer transfer1=transfers.get(i);
            //处理转账接受者的ID，装换为接受者的账号名
            usersExample.or().andUserGuidEqualTo(transfer1.getTransToUserGuid());
            List<Users> userx =  usersMapper.selectByExample(usersExample);
            transfer1.setTransToUserGuid(userx.get(0).getUserAccount());
            //处理转账进程状态
            TypeExample typeExample = new TypeExample();
            typeExample.or().andTypeGuidEqualTo(transfer1.getTransTypeGuidProcessStatus());
            List<Type> types = typeMapper.selectByExample(typeExample);
            transfer1.setTransTypeGuidProcessStatus(types.get(0).getTypeTitle());
            transferRecordList.add(transfer1);
        }
        //全部符合要求的数据的数量
        int total=transfers.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(transferRecordList,total);
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
    //接受者确认收款
    @RequestMapping(value = "/confirmTRANSFER/{transGuid}")
    public String confirmTRANSFER (@PathVariable String transGuid , Model model) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        //当前这条转账
        Transfer transfer = transferMapper.selectByPrimaryKey(transGuid);
        //更新转账处理状态为已完成
        transfer.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-222222222222");
        transferMapper.updateByPrimaryKeySelective(transfer);
        //将双方持有的时间进行修改
        //先处理收款人
        String transToUser = transfer.getTransToUserGuid();
        Users transUser = usersMapper.selectByPrimaryKey(transToUser);
        double money = transfer.getTransCurrency();
        double initMoney = transUser.getUserOwnCurrency();
        BigDecimal money1 = new BigDecimal(Double.toString(money));
        BigDecimal initMoney1 = new BigDecimal(Double.toString(initMoney));
        double endMoney = initMoney1.add(money1).doubleValue();
        transUser.setUserOwnCurrency(endMoney);
        usersMapper.updateByPrimaryKeySelective(transUser);

        //处理汇款人
        String transFromUserId = transfer.getTransFromUserGuid();
        Users transFromUser = usersMapper.selectByPrimaryKey(transFromUserId);
        double initMoney2 = transFromUser.getUserOwnCurrency();
        BigDecimal initMoney22 = new BigDecimal(Double.toString(initMoney2));
        double endMoneyOfReceive = initMoney22.subtract(money1).doubleValue();
        transFromUser.setUserOwnCurrency(endMoneyOfReceive);
        usersMapper.updateByPrimaryKeySelective(transFromUser);
        return "receiveServices";
    }
    //接受者拒绝收款
    @RequestMapping(value = "/refuseTRANSFER/{transGuid}")
    public String refuseTRANSFER (@PathVariable String transGuid , Model model) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        //双方钱数不变，只是将进程状态改为 拒绝
        //当前这条转账
        Transfer transfer = transferMapper.selectByPrimaryKey(transGuid);
        //更新转账处理状态为已完成
        transfer.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-333333333333");
        transferMapper.updateByPrimaryKeySelective(transfer);
        return "transferList";
    }

    //汇款列表
    @RequestMapping(value = "/remitServicesList")
    public String remitServicesList(Model model)
    {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        return "transferList";
    }
    //汇款列表请求数据
    @RequestMapping(value="/getRemitListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getRemitListJsonData(@RequestParam int offset, int limit, String sortName,String sortOrder){
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
//        model.addAttribute("role",role);
        TransferExample transferExample=new TransferExample();
        transferExample.clear();
        //处理排序信息
        if(sortName!=null){
            //拼接字符串
            String order= GetDatabaseFileName(sortName)+" "+sortOrder;
            //将排序信息添加到example中
            transferExample.setOrderByClause(order);
        }
        //判断TRANS_FROM_USER_GUID字段中包不包含当前登陆者的ID
        String fromUserId = users11.getUserGuid();
        TransferExample transferExample1 = new TransferExample();
        transferExample1.or().andTransFromUserGuidEqualTo(fromUserId);
        List<Transfer> transfers1=transferMapper.selectByExample(transferExample1);

        List<Transfer> transferRecordList=new ArrayList<>();
        for(int i=offset;i< offset+limit&&i < transfers1.size();i++){
            Transfer transfer1=transfers1.get(i);
            //处理转账接受者的ID，装换为接受者的账号名
            Users users2 = usersMapper.selectByPrimaryKey(transfer1.getTransToUserGuid());
            transfer1.setTransToUserGuid(users2.getUserAccount());
            //处理转账进程状态
            Type type = typeMapper.selectByPrimaryKey(transfer1.getTransTypeGuidProcessStatus());
            transfer1.setTransTypeGuidProcessStatus(type.getTypeTitle());
            transferRecordList.add(transfer1);
        }
        //全部符合要求的数据的数量
        int total=transfers1.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(transferRecordList,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            // System.out.println(json1);
            return json1;
        }catch (Exception e){
            return null;
        }
    }
    //汇款撤回按钮
    @RequestMapping(value = "/delateRemit/{transGuid}")
    public String delateRemit (@PathVariable String transGuid , Model model) {
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        //双方钱数不变，只是将进程状态改为 撤销
        //当前这条转账
        Transfer transfer = transferMapper.selectByPrimaryKey(transGuid);
        //更新转账处理状态为已完成
        transfer.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-666666666666");
        transferMapper.updateByPrimaryKeySelective(transfer);
        return "transferList";
    }










}
