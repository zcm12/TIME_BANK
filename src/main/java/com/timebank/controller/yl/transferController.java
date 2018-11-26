package com.timebank.controller.yl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ResultModel;
import com.timebank.appmodel.TransferApp;
import com.timebank.domain.*;
import com.timebank.mapper.TransferMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import com.timebank.shiro.ShrioRegister;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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
    ShrioRegister shrioRegister;
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
        try{
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        return "transferServices";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
    //汇款提交
    @RequestMapping(value = "/transferSubmit")
    public String updateREQESTSave(Transfer transfer, Model model) {
        try{
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);
        String transToUserAccount = transfer.getTransToUserAccount();
        Double currency = transfer.getTransCurrency();
        String transDesp = transfer.getTransDesp();

        double transCurrency = Double.parseDouble(String.valueOf(currency));
        double userOwnCurrency = users1.getUserOwnCurrency();

        Integer userTransPassword1 = users1.getUserTransPassword();
        if (userTransPassword1 == null) {
            return "transferPassword";
        }
        Transfer transfer1 = new Transfer();
        //转账ID//转账者
        UUID guid = randomUUID();
        transfer1.setTransGuid(guid.toString());
        transfer1.setTransFromUserGuid(users1.getUserGuid());
        //发起转账者
        transfer1.setTransFromUserAccount(users1.getUserAccount());


        // 对方账号
        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(transToUserAccount);
        List<Users> usersList = usersMapper.selectByExample(usersExample1);
        //如果有这个账户名的话，将这个账户名对应的ID存到trans表中
        Users users2 = usersList.get(0);
        transfer1.setTransToUserGuid(users2.getUserGuid());
        //转账数目
        transfer1.setTransCurrency(currency);
        //转账描述
        transfer1.setTransDesp(transDesp);
        //转账时间
        transfer1.setTransIssueTime(new Date());
        //转账账户
        transfer1.setTransToUserAccount(users2.getUserAccount());
        //转账进程先设置为待处理
        transfer1.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-111111111111");


        //先不要改变账户的持有的钱数转账(USER表)
        transferMapper.insertSelective(transfer1);
      return "transferPassword2";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }

    }
    //第一次转账要设置密码
    @RequestMapping(value="/password")
    public  String transferPassword(Model model,String userTransPassword){
        try{
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);

        users11.setUserTransPassword(Integer.valueOf(userTransPassword));
        usersMapper.updateByPrimaryKeySelective(users11);
        return "transferServices";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
    //设置过转账密码
    @RequestMapping(value="/password2")
    public  String transferPassword2(Model model,String userTransPassword){
        try{
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users1=GetCurrentUsers(message);
        String role=users1.getUserRole();
        model.addAttribute("role",role);

        Integer userTransPassword1 = users1.getUserTransPassword();
        if (!userTransPassword.equals(String.valueOf(userTransPassword1))) {
            return "wrongPassword";
        }
        return "transferList";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
    //收款按钮
    @RequestMapping(value = "/receiveServicesView")
    public String receiveServicesView(Model model) {
        try{
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        return "receiveServices";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
    //收款列表请求数据
    @RequestMapping(value="/getTRANSFERListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getTRANSFERListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder, String searchText){
        try{
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();

        /**10.10添加*/
        if (searchText == "") {
            searchText = null;
        }
        /**10.10添加*/

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
//        for(int i=offset;i< offset+limit&&i < transfers.size();i++){
        for(int i=0;i < transfers.size();i++){
            Transfer transfer1=transfers.get(i);


            String transFromUserGuid=transfer1.getTransFromUserGuid();
            // 对方账号
            UsersExample usersExample1 = new UsersExample();
            usersExample1.or().andUserGuidEqualTo(transFromUserGuid);
            List<Users> usersList = usersMapper.selectByExample(usersExample1);
            //如果有这个账户名的话，将这个账户名对应的ID存到trans表中
            Users users2 = usersList.get(0);
            //转账账户
            transfer1.setTransFromUserAccount(users2.getUserAccount());


            //处理转账接受者的ID，装换为接受者的账号名
            usersExample.or().andUserGuidEqualTo(transfer1.getTransToUserGuid());
            List<Users> userx =  usersMapper.selectByExample(usersExample);
            transfer1.setTransToUserGuid(userx.get(0).getUserAccount());
            //处理转账进程状态
            TypeExample typeExample = new TypeExample();
            typeExample.or().andTypeGuidEqualTo(transfer1.getTransTypeGuidProcessStatus());
            List<Type> types = typeMapper.selectByExample(typeExample);
            transfer1.setTransTypeGuidProcessStatus(types.get(0).getTypeTitle());
//            transferRecordList.add(transfer1);

            /**10.10添加*/
            if (searchText != null) {
                String fromUserAccount = transfer1.getTransFromUserAccount();
                String toUserAccount=transfer1.getTransToUserAccount();
                if (fromUserAccount.contains(searchText) || toUserAccount.contains(searchText) ) {
                    transferRecordList.add(transfer1);
                }
            } else {
                transferRecordList.add(transfer1);
            }
            /**10.10添加*/

        }

        /**10.10添加*/
        List<Transfer> transferReturn = new ArrayList<>();
        for (int i = offset;i<offset+limit&&i<transferRecordList.size();i++){
            transferReturn.add(transferRecordList.get(i));
        }
        int total=transferRecordList.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson=new TableRecordsJson(transferReturn,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            return json1;
        }catch (Exception e){
            return null;
        }
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
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
    @Transactional
    @RequestMapping(value = "/confirmTRANSFER/{transGuid}")
    public String confirmTRANSFER (@PathVariable String transGuid , Model model) {
        try{
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
        Double money = transfer.getTransCurrency();
        double initMoney = transUser.getUserOwnCurrency();
        BigDecimal money1 = new BigDecimal(money);
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
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            return "fail";
        }
    }
    //接受者拒绝收款
    @RequestMapping(value = "/refuseTRANSFER/{transGuid}")
    public String refuseTRANSFER (@PathVariable String transGuid , Model model) {
        try{
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
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    //汇款列表
    @RequestMapping(value = "/remitServicesList")
    public String remitServicesList(Model model)
    {
        try{
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
        model.addAttribute("role",role);
        return "transferList";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
    //汇款列表请求数据
    @RequestMapping(value="/getRemitListJsonData",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getRemitListJsonData(@RequestParam int offset, int limit, String sortName,String sortOrder, String searchText){
        try{
        Subject account = SecurityUtils.getSubject();
        String message=(String) account.getPrincipal();
        Users users11=GetCurrentUsers(message);
        String role=users11.getUserRole();
//        model.addAttribute("role",role);

        /**10.11添加*/
        if (searchText == "") {
            searchText = null;
        }
        /**10.11添加*/

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
//        for(int i=offset;i< offset+limit&&i < transfers1.size();i++){
        for(int i=0;i < transfers1.size();i++){
            Transfer transfer1=transfers1.get(i);
            //处理转账接受者的ID，装换为接受者的账号名
            Users users2 = usersMapper.selectByPrimaryKey(transfer1.getTransToUserGuid());
            transfer1.setTransToUserGuid(users2.getUserAccount());
            //装换为发起者的账号名/
            transfer1.setTransFromUserAccount(users11.getUserAccount());

            //处理转账进程状态
            Type type = typeMapper.selectByPrimaryKey(transfer1.getTransTypeGuidProcessStatus());
            transfer1.setTransTypeGuidProcessStatus(type.getTypeTitle());
//            transferRecordList.add(transfer1);
            /**10.11添加*/
            if (searchText != null) {
                String fromUserAccount = transfer1.getTransFromUserAccount();
                String toUserAccount=transfer1.getTransToUserAccount();
                if (fromUserAccount.contains(searchText) || toUserAccount.contains(searchText) ) {
                    transferRecordList.add(transfer1);
                }
            } else {
                transferRecordList.add(transfer1);
            }
            /**10.11添加*/
        }
        /**10.11添加*/
        List<Transfer> transferReturn = new ArrayList<>();
        for (int i = offset;i<offset+limit&&i<transferRecordList.size();i++){
            transferReturn.add(transferRecordList.get(i));
        }
        /**10.11添加*/

        //全部符合要求的数据的数量
//        int total=transfers1.size();
        int total=transferRecordList.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
//        TableRecordsJson tableRecordsJson=new TableRecordsJson(transferRecordList,total);
        TableRecordsJson tableRecordsJson=new TableRecordsJson(transferReturn,total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            // System.out.println(json1);
            return json1;
        }catch (Exception e){
            return null;
        }
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
    //汇款撤回按钮
    @RequestMapping(value = "/delateRemit/{transGuid}")
    public String delateRemit (@PathVariable String transGuid , Model model) {
        try{
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
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

}
