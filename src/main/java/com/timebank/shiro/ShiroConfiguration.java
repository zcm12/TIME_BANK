package com.timebank.shiro;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.remoting.SecureRemoteInvocationExecutor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.shiro.realm.jdbc.JdbcRealm.SaltStyle.COLUMN;

@Component
@PropertySource("classpath:application.properties")
//@ConfigurationProperties(prefix = "spring.datasource")
//这个注解不能丢啊亲
@Configuration
public class ShiroConfiguration {
    //@Value("${spring.datasource.url}")
    public String url="jdbc:sqlserver://localhost\\DESKTOP-8F5KG5D:1433;databaseName=TIME_BANK";
  // @Value("${spring.datasource.username}")
    public String username="sa";
   //@Value("${spring.datasource.password}")
    public String password="sa";
   //@Value("${spring.datasource.driver-class-name}")
    public String driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();

    @Bean(name = "pooledDataSource")
    public DriverManagerDataSource getDriverManagerDataSource(){
        DriverManagerDataSource driverManagerDataSource=new DriverManagerDataSource();
        driverManagerDataSource.setUrl(url);
        driverManagerDataSource.setUsername(username);
        driverManagerDataSource.setPassword(password);
        driverManagerDataSource.setDriverClassName(driverName);
        return driverManagerDataSource;
    }

    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }
    @Bean
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator(){
        return new DefaultAdvisorAutoProxyCreator();
    }
    @Bean
    public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(){
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor =new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(getWebDefaultSecurityManager());
        return authorizationAttributeSourceAdvisor;
    }
    @Bean(name = "secureRemoteInvocationExecutor")
    public SecureRemoteInvocationExecutor getSecureRemoteInvocationExecutor(){
        SecureRemoteInvocationExecutor secureRemoteInvocationExecutor=new SecureRemoteInvocationExecutor();
        secureRemoteInvocationExecutor.setSecurityManager(getWebDefaultSecurityManager());
        return secureRemoteInvocationExecutor;
    }
    @Bean(name = "sessionDAO")
    public MemorySessionDAO getMemorySessionDAO(){
        return new MemorySessionDAO();
    }
    @Bean(name = "sessionManager")
    public DefaultWebSessionManager getDefaultWebSessionManager(){
        DefaultWebSessionManager defaultWebSessionManager=new DefaultWebSessionManager();
        defaultWebSessionManager.setSessionDAO(getMemorySessionDAO());
        return defaultWebSessionManager;
    }
    @Bean(name = "cachaManager")
    public EhCacheManager getEhCacheManager(){
       return new EhCacheManager();
    }
    @Bean(name ="hashedCredentialsMatcher" )
    public HashedCredentialsMatcher getHashedCredentialsMatcher(){
        HashedCredentialsMatcher hashedCredentialsMatcher=new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("MD5");
        hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
        hashedCredentialsMatcher.setHashIterations(10);


        return hashedCredentialsMatcher;
    }

    @Bean(name = "myRealm")
    public JdbcSaltRealm getJdbcSaltRealm(){
        JdbcSaltRealm jdbcSaltRealm=new JdbcSaltRealm();
        jdbcSaltRealm.setDataSource(getDriverManagerDataSource());
        jdbcSaltRealm.setAuthenticationQuery("select user_password, user_salt from users where user_account = ?");
        jdbcSaltRealm.setUserRolesQuery("select user_role from users where user_account = ?");
        jdbcSaltRealm.setPermissionsQuery("select user_salt from users where user_account = ?");
        jdbcSaltRealm.setPermissionsLookupEnabled(true);
        jdbcSaltRealm.setSaltStyle(COLUMN);
        jdbcSaltRealm.setCredentialsMatcher(getHashedCredentialsMatcher());

        return jdbcSaltRealm;
    }


    @Bean(name = "securityManager")
    public DefaultWebSecurityManager getWebDefaultSecurityManager(){
        DefaultWebSecurityManager defaultWebSecurityManager=new DefaultWebSecurityManager();
        defaultWebSecurityManager.setRealm(getJdbcSaltRealm());
        defaultWebSecurityManager.setCacheManager(getEhCacheManager());
        defaultWebSecurityManager.setSessionManager(getDefaultWebSessionManager());
        return defaultWebSecurityManager;
    }



    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(){
        ShiroFilterFactoryBean shiroFilterFactoryBean= new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(getWebDefaultSecurityManager());
        shiroFilterFactoryBean.setLoginUrl("/");
        shiroFilterFactoryBean.setUnauthorizedUrl("/fail.html");
        filterChainDefinitionMap.put("/css/**","anon");
        filterChainDefinitionMap.put("/fonts/**","anon");
        filterChainDefinitionMap.put("/jquery/**","anon");
        filterChainDefinitionMap.put("/js/**","anon");
        filterChainDefinitionMap.put("/img/**","anon");
        filterChainDefinitionMap.put("/loginUser","anon");
        filterChainDefinitionMap.put("/appLoginUser","anon");
        filterChainDefinitionMap.put("/","anon");
        filterChainDefinitionMap.put("/register","anon");
        filterChainDefinitionMap.put("/registerUser","anon");
        filterChainDefinitionMap.put("/**","authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

}
