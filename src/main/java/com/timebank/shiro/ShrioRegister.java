package com.timebank.shiro;


import com.sun.org.apache.bcel.internal.generic.RETURN;
import com.timebank.domain.Users;
import com.timebank.domain.UsersExample;
import com.timebank.mapper.UsersMapper;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

@Component
//标志本类为一个bean
@PropertySource("classpath:application.properties")
//指定绑定哪个资源文件，【如果要绑定自定义的资源文件中的值的话，是可以用上的】
// 这里的application.properties文件是springboot默认的资源文件，是可以不用指定的，这里绑定的话，会去加载绑定两次。
public class ShrioRegister {
    @Autowired
    UsersMapper usersMapper;
    @Autowired
     DefaultWebSecurityManager securityManager;
    //散列算法类型为MD5
    private    String algorithmName ="MD5";
    //hash的次数
    private   int hashIterations=10;
    private boolean flag=true;

    public boolean register(Users users) {

        System.out.println("密码加密开始");
        //干扰数据 盐 防破解
        String salt = "";
        System.out.println("原始密码为：" + users.getUserPassword());//注册密码
        SimpleHash hash = new SimpleHash(algorithmName, users.getUserPassword(), salt, hashIterations);
        System.out.println("密码加密结束：" + hash);
        String encodedPassword = hash.toHex();
        users.setUserPassword(encodedPassword);
        UUID userGuid = randomUUID();
        users.setUserGuid(userGuid.toString());
        usersMapper.insertSelective(users);
        return  true;
    }
}
