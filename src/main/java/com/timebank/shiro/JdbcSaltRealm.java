package com.timebank.shiro;

import com.timebank.domain.Users;
import com.timebank.mapper.UsersMapper;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class JdbcSaltRealm extends AuthorizingRealm{
//    @Autowired
//    private UsersMapper usersMapper;
//
//    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
//        Map<String, Object> params = new HashMap<>();
//        params.put("userCode", (String) super.getAvailablePrincipal(principalCollection));
////        List<Users> userRoleInfos = usersMapper.get(params);
////        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
////        if(!userRoleInfos.isEmpty()) {
////            for(UserRoleInfo role : userRoleInfos) {
////                info.addRole(role.getRoleCode());
////            }
////        }
//        return info;
//    }
//
//}








































//import org.apache.shiro.realm.jdbc.JdbcRealm;
//
//
//public class JdbcSaltRealm extends JdbcRealm{
//
//    public JdbcSaltRealm() {
//
//    setSaltStyle(SaltStyle.COLUMN);
//}
//
//}
