package com.timebank.controller.wxxcx.wxxcx;

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
public class WxTransferController {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private TransferMapper transferMapper;
    //发起转账
    @RequestMapping(value = "/wxInsertTransfer")
    @ResponseBody
    public String appInsertTransfer(TransferApp transferApp) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);

//       String transToUserGuid = transferApp.getTransToUserGuid();
        String transCurrency = transferApp.getTransCurrency();
        String transDesp = transferApp.getTransDesp();

        Transfer transfer1 = new Transfer();
        //转账ID//转账者
        UUID guid = randomUUID();
        transfer1.setTransGuid(guid.toString());
        transfer1.setTransFromUserGuid(users1.getUserGuid());
        //对方账号
        UsersExample usersExample1 = new UsersExample();
//        usersExample1.or().andUserAccountEqualTo(transToUserGuid);
        List<Users> users11 = usersMapper.selectByExample(usersExample1);
        //如果有这个账户名的话，将这个账户名对应的ID存到trans表中
        transfer1.setTransToUserGuid(users11.get(0).getUserGuid());
        //转账数目
        transfer1.setTransCurrency(Double.parseDouble(transCurrency));
        //转账描述
        transfer1.setTransDesp(transDesp);
        //转账时间
        transfer1.setTransIssueTime(new Date());
        //转账进程先设置为待处理
        transfer1.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-111111111111");
        //先不要改变账户的持有的钱数转账(USER表)
        int insert = transferMapper.insertSelective(transfer1);
        return "汇款提交成功";

    }
    //查询是否设置支付密码
    @RequestMapping(value = "/wxQueryTransferPW")
    @ResponseBody
    public Boolean appQueryTransferPW(Users u) {
//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);
        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(u.getUserAccount());
        List<Users> users = usersMapper.selectByExample(usersExample1);
        Users users1 = users.get(0);
        System.out.println("她是："+users1.getUserAccount());
        System.out.println("她的登录密码:"+users1.getUserPassword());
        System.out.println("她的支付密码是"+users1.getUserTransPassword());
        Integer userTransPassword = users1.getUserTransPassword();
        System.out.println(userTransPassword);
        if (userTransPassword == null) {
            return true;

        }
        return false;

    }
    //设置支付密码
    @RequestMapping(value = "/wxInsertTransferPW")
    @ResponseBody
    public String  appInsertTransferPW(Users u,String userTransPassword) {
//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);

        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(u.getUserAccount());
        List<Users> users = usersMapper.selectByExample(usersExample1);
        Users users1 = users.get(0);

        users1.setUserTransPassword(Integer.valueOf(userTransPassword));
        int update = usersMapper.updateByPrimaryKeySelective(users1);
        System.out.println(update);
        System.out.println(users1.getUserAccount());
        System.out.println(users1.getUserTransPassword());
        return "sucess";

    }
    //收款列表
    @RequestMapping(value = "/wxQueryTransferGather", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryTransferGather(Users users) {
//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);
        
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo(users.getUserAccount());
        List<Users> users0 = usersMapper.selectByExample(usersExample);
        Users users1 = users0.get(0);

        String userGuid = users1.getUserGuid();
        TransferExample transferExample = new TransferExample();
        transferExample.or().andTransToUserGuidEqualTo(userGuid);
        //判断
        List<Transfer> transfers = transferMapper.selectByExample(transferExample);

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
    @RequestMapping(value = "/wxTranGatherOk")
    @ResponseBody
    public ResultModel appTranGatherOk(Transfer transfer1) {
        int update = 0;
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        String transGuid = transfer1.getTransGuid();
        //当前这条转账
        Transfer transfer = transferMapper.selectByPrimaryKey(transGuid);
        //更新转账处理状态为已完成
        transfer.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-222222222222");
        int update1 = transferMapper.updateByPrimaryKeySelective(transfer);
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
        int update2 = usersMapper.updateByPrimaryKeySelective(transUser);

        //处理汇款人
        String transFromUserId = transfer.getTransFromUserGuid();
        Users transFromUser = usersMapper.selectByPrimaryKey(transFromUserId);
        double initMoney2 = transFromUser.getUserOwnCurrency();
        BigDecimal initMoney22 = new BigDecimal(Double.toString(initMoney2));
        double endMoneyOfReceive = initMoney22.subtract(money1).doubleValue();
        transFromUser.setUserOwnCurrency(endMoneyOfReceive);
        int update3 = usersMapper.updateByPrimaryKeySelective(transFromUser);
        if (update1 == 1 && update2 == 1 && update3 == 1) {
            update = 1;
        }
        return new ResultModel(update, "收款成功");
    }
    //接受者拒绝收款
    @RequestMapping(value = "/wxTranGatherCancel")
    @ResponseBody
    public ResultModel appTranGatherCancel(String transGuid) {
        Subject account = SecurityUtils.getSubject();
        UsersExample usersExample = new UsersExample();
        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
        List<Users> users = usersMapper.selectByExample(usersExample);
        Users users1 = users.get(0);
        //双方钱数不变，只是将进程状态改为 拒绝
        //当前这条转账
        Transfer transfer = transferMapper.selectByPrimaryKey(transGuid);
        //更新转账处理状态为拒绝
        transfer.setTransTypeGuidProcessStatus("66666666-94e3-4eb7-aad3-333333333333");
        int update = transferMapper.updateByPrimaryKeySelective(transfer);
        return new ResultModel(update, "您已拒绝收款");
    }
    //汇款列表请求数据
    @RequestMapping(value = "/wxQueryTransferRemit", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String appQueryTransferRemit(Users users) {
//        Subject account = SecurityUtils.getSubject();
//        UsersExample usersExample = new UsersExample();
//        usersExample.or().andUserAccountEqualTo((String) account.getPrincipal());
//        List<Users> users = usersMapper.selectByExample(usersExample);
//        Users users1 = users.get(0);

        UsersExample usersExample1 = new UsersExample();
        usersExample1.or().andUserAccountEqualTo(users.getUserAccount());
        List<Users> users0 = usersMapper.selectByExample(usersExample1);
        Users users1 = users0.get(0);



        TransferExample transferExample = new TransferExample();
        transferExample.clear();

        //判断TRANS_FROM_USER_GUID字段中包不包含当前登陆者的ID
        String fromUserId = users1.getUserGuid();
        TransferExample transferExample1 = new TransferExample();
        transferExample1.or().andTransFromUserGuidEqualTo(fromUserId);
        List<Transfer> transfers1 = transferMapper.selectByExample(transferExample1);

        List<Transfer> transferRecordList = new ArrayList<>();
        for (int i = 0; i < transfers1.size(); i++) {
            Transfer transfer1 = transfers1.get(i);
            //处理转账接受者的ID，装换为接受者的账号名
            Users users2 = usersMapper.selectByPrimaryKey(transfer1.getTransToUserGuid());
            transfer1.setTransToUserGuid(users2.getUserAccount());
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
             System.out.println(json1);
            return json1;
        } catch (Exception e) {
            return null;
        }
    }
}
