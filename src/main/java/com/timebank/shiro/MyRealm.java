package com.timebank.shiro;

import com.timebank.domain.Users;
import com.timebank.domain.UsersExample;
import com.timebank.mapper.UsersMapper;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @Description: 认证和授权具体实现
 */
public class MyRealm extends AuthorizingRealm {
    @Autowired
    private UsersMapper usersMapper;
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String message = (String) getAvailablePrincipal(principalCollection);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        System.out.println("message+"+message);
        String role;
        String em = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        String ph = "^[1][34578]\\d{9}$";
        if(message.matches(em)){
            UsersExample usersExample = new UsersExample();
            usersExample.or().andUserMailEqualTo(message);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            Users users = usersList.get(0);
            role=users.getUserRole();

        } else if(message.matches(ph)){
            UsersExample usersExample = new UsersExample();
            usersExample.or().andUserPhoneEqualTo(message);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            Users users = usersList.get(0);
            role=users.getUserRole();

        }else {
            UsersExample usersExample = new UsersExample();
            usersExample.or().andUserAccountEqualTo(message);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            Users users = usersList.get(0);
            role=users.getUserRole();
        }
        Set<String> r = new HashSet<String>();
        r.add(role);
        info.setRoles(r);

        return info;
    }

    /**
     * 认证登陆subject身份
     * @param authenticationToken
     * @return AuthenticationInfo
     * @throws AuthenticationException
     */
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
//        String salt="";
        String salt=null;
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String password = new String((char[])token.getCredentials());//输入的密码
        //根据用户名查询密码  修改成先判断用户名 手机号 邮箱 再查询
        String message=(String) authenticationToken.getPrincipal();
        //正则表达式校验
        String passWord;
        String em = "^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$";
        String ph = "^[1][34578]\\d{9}$";
        if(message.matches(em)){
            System.out.println("验证邮箱");
            UsersExample usersExample = new UsersExample();
            usersExample.or().andUserMailEqualTo(message);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            Users users = usersList.get(0);
            passWord = users.getUserPassword();
            salt=users.getUserSalt();
        } else if(message.matches(ph)){
            System.out.println("手机号登录");
            UsersExample usersExample = new UsersExample();
            usersExample.or().andUserPhoneEqualTo(message);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            Users users = usersList.get(0);
            passWord = users.getUserPassword();
            salt=users.getUserSalt();
        }else{
            System.out.println("账号登录");
            UsersExample usersExample = new UsersExample();
            usersExample.or().andUserAccountEqualTo(message);
            List<Users> usersList = usersMapper.selectByExample(usersExample);
            Users users = usersList.get(0);
            passWord = users.getUserPassword();
            System.out.println(passWord);
           salt=users.getUserSalt();
        }

//            return new SimpleAuthenticationInfo(authenticationToken.getPrincipal(), passWord,getName());
        return new SimpleAuthenticationInfo(authenticationToken.getPrincipal(), passWord,ByteSource.Util.bytes(salt),getName());
//        return new SimpleAuthenticationInfo(authenticationToken.getPrincipal(), passWord,ByteSource.Util.bytes(""),getName());
    }

}
