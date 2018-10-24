package com.timebank.controller.appuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.appmodel.ResultModel;
import com.timebank.appmodel.TransferApp;
import com.timebank.controller.yl.TableRecordsJson;
import com.timebank.domain.*;
import com.timebank.mapper.TransferMapper;
import com.timebank.mapper.TypeMapper;
import com.timebank.mapper.UsersMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
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
public class AppTransferController {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private TransferMapper transferMapper;

    //汇款服务
    /*@RequestMapping(value = "/remitServicesView")
    public String remitServicesView(Model model) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        return "transferServices";
    }

    //汇款提交
    @RequestMapping(value = "/transferSubmit")
    public String updateREQESTSave(Transfer transfer, Model model) {

        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);

        //转账ID//转账者
        UUID guid = randomUUID();
        transfer.setTransGuid(guid.toString());
        transfer.setTransFromUserGuid(users1.getUserGuid());
        //对方账号
        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(transfer.getTransToUserAccount());
        List<Users> users11 = usersMapper.selectByExample(usersExample1);
        //如果有这个账户名的话，将这个账户名对应的ID存到trans表中
        transfer.setTransToUserAccount(users11.get(0).getUserGuid());
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
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        return "receiveServices";
    }

    //收款列表请求数据
    @RequestMapping(value = "/getTRANSFERListJsonData", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getTRANSFERListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        TransferExample transferExample = new TransferExample();
        transferExample.or().andTransTypeGuidProcessStatusNotEqualTo("66666666-94e3-4eb7-aad3-666666666666");
        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            //将排序信息添加到example中
            transferExample.setOrderByClause(order);
        }
        //判断
        List<Transfer> transfers1 = transferMapper.selectByExample(transferExample);
        //存储转给当前登陆者的那些记录
        List<Transfer> transfers = new ArrayList<>();
        for (Transfer t : transfers1) {
            if (t.getTransToUserAccount().contains(users1.getUserGuid())) {
                transfers.add(t);
            }
        }
        List<Transfer> transferRecordList = new ArrayList<>();
        for (int i = offset; i < offset + limit && i < transfers.size(); i++) {
            Transfer transfer1 = transfers.get(i);
            //处理转账接受者的ID，装换为接受者的账号名
            usersExample.or().andUserGuidEqualTo(transfer1.getTransToUserAccount());
            List<Users> userx = usersMapper.selectByExample(usersExample);
            transfer1.setTransToUserAccount(userx.get(0).getUserAccount());
            //处理转账进程状态
            TypeExample typeExample = new TypeExample();
            typeExample.or().andTypeGuidEqualTo(transfer1.getTransTypeGuidProcessStatus());
            List<Type> types = typeMapper.selectByExample(typeExample);
            transfer1.setTransTypeGuidProcessStatus(types.get(0).getTypeTitle());
            transferRecordList.add(transfer1);
        }
        //全部符合要求的数据的数量
        int total = transfers.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(transferRecordList, total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            return json1;
        } catch (Exception e) {
            return null;
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

    //接受者确认收款
    @RequestMapping(value = "/confirmTRANSFER/{transGuid}")
    public String confirmTRANSFER(@PathVariable String transGuid, Model model) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        //当前这条转账
        Transfer transfer = transferMapper.selectByPrimaryKey(transGuid);
        //更新转账处理状态为已完成
        transfer.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-222222222222");
        transferMapper.updateByPrimaryKeySelective(transfer);
        //将双方持有的时间进行修改
        //先处理收款人
        String transToUser = transfer.getTransToUserAccount();
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
    public String refuseTRANSFER(@PathVariable String transGuid, Model model) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
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
    public String remitServicesList(Model model) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        return "transferList";
    }

    //汇款列表请求数据
    @RequestMapping(value = "/getRemitListJsonData", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getRemitListJsonData(@RequestParam int offset, int limit, String sortName, String sortOrder) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        TransferExample transferExample = new TransferExample();
        transferExample.clear();
        //处理排序信息
        if (sortName != null) {
            //拼接字符串
            String order = GetDatabaseFileName(sortName) + " " + sortOrder;
            //将排序信息添加到example中
            transferExample.setOrderByClause(order);
        }
        //判断TRANS_FROM_USER_GUID字段中包不包含当前登陆者的ID
        String fromUserId = users1.getUserGuid();
        TransferExample transferExample1 = new TransferExample();
        transferExample1.or().andTransFromUserGuidEqualTo(fromUserId);
        List<Transfer> transfers1 = transferMapper.selectByExample(transferExample1);

        List<Transfer> transferRecordList = new ArrayList<>();
        for (int i = offset; i < offset + limit && i < transfers1.size(); i++) {
            Transfer transfer1 = transfers1.get(i);
            //处理转账接受者的ID，装换为接受者的账号名
            Users users2 = usersMapper.selectByPrimaryKey(transfer1.getTransToUserAccount());
            transfer1.setTransToUserAccount(users2.getUserAccount());
            //处理转账进程状态
            Type type = typeMapper.selectByPrimaryKey(transfer1.getTransTypeGuidProcessStatus());
            transfer1.setTransTypeGuidProcessStatus(type.getTypeTitle());
            transferRecordList.add(transfer1);
        }
        //全部符合要求的数据的数量
        int total = transfers1.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(transferRecordList, total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            // System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }

    //汇款撤回按钮
    @RequestMapping(value = "/delateRemit/{transGuid}")
    public String delateRemit(@PathVariable String transGuid, Model model) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String role = users1.getUserRole();
        model.addAttribute("role", role);
        //双方钱数不变，只是将进程状态改为 撤销
        //当前这条转账
        Transfer transfer = transferMapper.selectByPrimaryKey(transGuid);
        //更新转账处理状态为已完成
        transfer.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-666666666666");
        transferMapper.updateByPrimaryKeySelective(transfer);
        return "transferList";
    }*/


    /*-------------------------app API--------------------------------*/
//汇款提交
    @RequestMapping(value = "/appInsertTransfer")
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public ResultModel appInsertTransfer(TransferApp transferApp, String userTransPassword) {

        //获得当前用户users1
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);

        //获得对方账户、转账货币、备注
        String transToUserAccount = transferApp.getTransToUserAccount();
        String currency = transferApp.getTransCurrency();
        String transDesp = transferApp.getTransDesp();

        //获得users1的支付密码
        Integer userTransPassword1 = users1.getUserTransPassword();
        System.out.println("userTransPassword:" + userTransPassword);//userTransPassword是用户传入的密码
        System.out.println("userTransPassword1:" + userTransPassword1);//userTransPassword1是用户真正的密码
        if (!userTransPassword.equals(String.valueOf(userTransPassword1))) {
            return new ResultModel(12, "密码错误，请重新输入");
        }

        double transCurrency = Double.parseDouble(currency);//将String类型的要转账的时间币转为double类型
        double userOwnCurrency = users1.getUserOwnCurrency();//获得当前用户所拥有的时间币
        if (transCurrency > userOwnCurrency) {
            return new ResultModel(11, "所持时间币不足");
        }
        Transfer transfer1 = new Transfer();//转账类
        //转账ID//转账者
        UUID guid = randomUUID();
        transfer1.setTransGuid(guid.toString());
        transfer1.setTransFromUserGuid(users1.getUserGuid());
        //对方账号
        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(transToUserAccount);
        List<Users> usersList = usersMapper.selectByExample(usersExample1);
        //如果有这个账户名的话，将这个账户名对应的ID存到trans表中
        Users users2 = usersList.get(0);
        transfer1.setTransToUserGuid(users2.getUserGuid());
        //转账数目
        transfer1.setTransCurrency(transCurrency);
        //转账描述
        transfer1.setTransDesp(transDesp);
        //转账时间
        transfer1.setTransIssueTime(new Date());
        //转账进程先设置为待处理
//        transfer1.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-111111111111");
        //转账进程设置为已完成
        transfer1.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-222222222222");
        //直接改变账户的持有的钱数转账(USER表)
        return transferTransaction(transfer1, userOwnCurrency, users1, transCurrency, users2);
//        return new ResultModel(insert, "汇款提交成功，等待审核");

    }

    public ResultModel transferTransaction(Transfer transfer1, double userOwnCurrency, Users users1, double transCurrency, Users users2) {
        int insert = transferMapper.insertSelective(transfer1);//表示在transfer表中插入记录，成功返回1，失败返回0
        users1.setUserOwnCurrency(userOwnCurrency - transCurrency);//重置当前用户的时间币
        Double userOwnCurrency2 = users2.getUserOwnCurrency();//对方用户现有的时间币
        if (userOwnCurrency2 == null) {
            userOwnCurrency2 = 0.0;
        }
        users2.setUserOwnCurrency(userOwnCurrency2 + transCurrency);//重置对方用户的时间币
        int update1 = usersMapper.updateByPrimaryKeySelective(users1);//在users表中更新当前用户的时间币
        int update2 = usersMapper.updateByPrimaryKeySelective(users2);//在users表中更新对方账户的时间币
//        if (true) {
        if (insert != 1 || update1 != 1 || update2 != 1) {
            //提示用户“服务器忙，请稍后重试！”
            throw new RuntimeException("至少有一条数据没插入，数据库回滚！");
        } else {
            return new ResultModel(1, "汇款成功");
        }
    }


    //查询是否设置支付密码
    @RequestMapping(value = "/appQueryTransferPW")
    @ResponseBody
    public ResultModel appQueryTransferPW() {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);//获得当前用户

        Integer userTransPassword = users1.getUserTransPassword();//获得当前用户的支付密码
        if (userTransPassword == null) {
            return new ResultModel(21, "请先设置支付密码");
        }
        return new ResultModel(22, "跳转到输入支付密码页面");

    }

    //插入支付密码
    @RequestMapping(value = "/appInsertTransferPW")
    @ResponseBody
    public ResultModel appInsertTransferPW(String userTransPassword) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);

        users1.setUserTransPassword(Integer.valueOf(userTransPassword));//为当前用户设置支付密码
        int update = usersMapper.updateByPrimaryKeySelective(users1);//更新数据库中的用户密码
        return new ResultModel(update, "支付密码设置成功");

    }


    //收款列表
    @RequestMapping(value = "/appQueryTransferGather", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryTransferGather() {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);//获得当前用户
        String userGuid = users1.getUserGuid();//获得当前用户ID
        TransferExample transferExample = new TransferExample();
        transferExample.or().andTransToUserGuidEqualTo(userGuid);
        //判断
        List<Transfer> transfers = transferMapper.selectByExample(transferExample);//当前用户所有的收款列表

        List<Transfer> transferRecordList = new ArrayList<>();
        for (int i = 0; i < transfers.size(); i++) {
            Transfer transfer1 = transfers.get(i);
            //处理转账接受者的ID，装换为接受者的账号名
            usersExample.or().andUserGuidEqualTo(transfer1.getTransToUserGuid());
            List<Users> userx = usersMapper.selectByExample(usersExample);
            transfer1.setTransToUserGuid(userx.get(0).getUserAccount());
            //处理转账者的ID，装换为转账者的账号名
            usersExample.clear();
            usersExample.or().andUserGuidEqualTo(transfer1.getTransFromUserGuid());
            List<Users> users2 = usersMapper.selectByExample(usersExample);
            transfer1.setTransFromUserGuid(users2.get(0).getUserAccount());
            //处理转账进程状态
            TypeExample typeExample = new TypeExample();
            typeExample.or().andTypeGuidEqualTo(transfer1.getTransTypeGuidProcessStatus());
            List<Type> types = typeMapper.selectByExample(typeExample);
            transfer1.setTransTypeGuidProcessStatus(types.get(0).getTypeTitle());
            transferRecordList.add(transfer1);
        }

        if (transferRecordList.size() != 0) {
            /*根据发布时间排序*/
            transferRecordList.sort((o1, o2) -> {
                int flag = o2.getTransIssueTime().compareTo(o1.getTransIssueTime());
                System.out.println("flag:" + flag);
                return flag;
            });

            Date reqIssueTime = transferRecordList.get(0).getTransIssueTime();
            System.out.println("发布时间："+reqIssueTime);
        }

        //全部符合要求的数据的数量
        int total = transfers.size();
        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(transferRecordList, total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }

    //接受者 确认收款
//    @RequestMapping(value = "/appTranGatherOk")
//    @ResponseBody
//    public ResultModel appTranGatherOk(Transfer transfer1) {
//        int update = 0;
//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);
//        String transGuid = transfer1.getTransGuid();
//        //当前这条转账
//        Transfer transfer = transferMapper.selectByPrimaryKey(transGuid);
//        //更新转账处理状态为已完成
//        transfer.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-222222222222");
//        int update1 = transferMapper.updateByPrimaryKeySelective(transfer);
//        //将双方持有的时间进行修改
//        //先处理收款人
//        String transToUser = transfer.getTransToUserGuid();
//        Users transUser = usersMapper.selectByPrimaryKey(transToUser);
//        double money = transfer.getTransCurrency();
//        double initMoney = transUser.getUserOwnCurrency();
//        BigDecimal money1 = new BigDecimal(Double.toString(money));
//        BigDecimal initMoney1 = new BigDecimal(Double.toString(initMoney));
//        double endMoney = initMoney1.add(money1).doubleValue();
//        transUser.setUserOwnCurrency(endMoney);
//        int update2 = usersMapper.updateByPrimaryKeySelective(transUser);
//
//        //处理汇款人
//        String transFromUserId = transfer.getTransFromUserGuid();
//        Users transFromUser = usersMapper.selectByPrimaryKey(transFromUserId);
//        double initMoney2 = transFromUser.getUserOwnCurrency();
//        BigDecimal initMoney22 = new BigDecimal(Double.toString(initMoney2));
//        double endMoneyOfReceive = initMoney22.subtract(money1).doubleValue();
//        transFromUser.setUserOwnCurrency(endMoneyOfReceive);
//        int update3 = usersMapper.updateByPrimaryKeySelective(transFromUser);
//        if (update1 == 1 && update2 == 1 && update3 == 1) {
//            update = 1;
//        }
//        return new ResultModel(update, "收款成功");
//    }

    //接受者拒绝收款
//    @RequestMapping(value = "/appTranGatherCancel")
//    @ResponseBody
//    public ResultModel appTranGatherCancel(String transGuid) {
//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);
//        //双方钱数不变，只是将进程状态改为 拒绝
//        //当前这条转账
//        Transfer transfer = transferMapper.selectByPrimaryKey(transGuid);
//        //更新转账处理状态为拒绝
//        transfer.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-333333333333");
//        int update = transferMapper.updateByPrimaryKeySelective(transfer);
//        return new ResultModel(update, "您已拒绝收款");
//    }

    //汇款列表请求数据
    @RequestMapping(value = "/appQueryTransferRemit", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryTransferRemit() {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);//获得当前用户
//        TransferExample transferExample = new TransferExample();
//        transferExample.clear();//清空？？？

        //判断TRANS_FROM_USER_GUID字段中包不包含当前登陆者的ID
        String fromUserId = users1.getUserGuid();//获得当前用户的ID
        TransferExample transferExample1 = new TransferExample();
        transferExample1.or().andTransFromUserGuidEqualTo(fromUserId);
        List<Transfer> transfers1 = transferMapper.selectByExample(transferExample1);//所有transfer的集合

        List<Transfer> transferRecordList = new ArrayList<>();
        for (int i = 0; i < transfers1.size(); i++) {//遍历所有transfer的集合
            Transfer transfer1 = transfers1.get(i);//获得每一条transfer
            //处理转账接收者的ID，转换为接收者的账号名
            Users users2 = usersMapper.selectByPrimaryKey(transfer1.getTransToUserGuid());
            transfer1.setTransToUserGuid(users2.getUserAccount());
            //处理转账进程状态
            Type type = typeMapper.selectByPrimaryKey(transfer1.getTransTypeGuidProcessStatus());
            transfer1.setTransTypeGuidProcessStatus(type.getTypeTitle());
            transferRecordList.add(transfer1);
        }

        if (transferRecordList.size() != 0) {
            /*根据发布时间排序*/
            transferRecordList.sort((o1, o2) -> {
                int flag = o2.getTransIssueTime().compareTo(o1.getTransIssueTime());
                System.out.println("flag:" + flag);
                return flag;
            });

            Date reqIssueTime = transferRecordList.get(0).getTransIssueTime();
            System.out.println("发布时间："+reqIssueTime);
        }
        //全部符合要求的数据的数量
        int total = transfers1.size();

        //将所得集合打包
        ObjectMapper mapper = new ObjectMapper();
        TableRecordsJson tableRecordsJson = new TableRecordsJson(transferRecordList, total);
        //将实体类转换成json数据并返回
        try {
            String json1 = mapper.writeValueAsString(tableRecordsJson);
            System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }
}
