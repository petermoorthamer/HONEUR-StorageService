package com.jnj.honeur.storage;

import com.jnj.honeur.security.CasAuthorizationGenerator;
import com.jnj.honeur.security.ShiroCasLogoutHandler;
import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.LogoutFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.config.web.autoconfigure.ShiroWebAutoConfiguration;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.subject.Subject;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.config.CasProtocol;
import org.pac4j.core.config.Config;
import org.pac4j.core.matching.Matcher;
import org.pac4j.core.matching.PathMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring configuration for Apache Shiro + CAS
 *
 * @author Peter Moorthamer
 */

@Configuration
public class ShiroCasConfiguration extends ShiroWebAutoConfiguration {

    private static Logger log = LoggerFactory.getLogger(ShiroCasConfiguration.class);

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleException(AuthorizationException e, Model model) {

        // you could return a 404 here instead (this is how github handles 403, so the user does NOT know there is a
        // resource at that location)
        log.warn("AuthorizationException was thrown", e);

        Map<String, Object> map = new HashMap<>();
        map.put("status", HttpStatus.FORBIDDEN.value());
        map.put("message", "No message available");
        model.addAttribute("errors", map);

        return "error";
    }

    @ModelAttribute(name = "subject")
    public Subject subject() {
        return SecurityUtils.getSubject();
    }

    @Bean
    public Realm realm() {
        return new Pac4jRealm();
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager, Config config, ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean filterFactoryBean = new ShiroFilterFactoryBean();

        filterFactoryBean.setLoginUrl("/login");
        filterFactoryBean.setSuccessUrl("/test");
        filterFactoryBean.setUnauthorizedUrl("/error/error401.html");

        filterFactoryBean.setSecurityManager(securityManager);

        filterFactoryBean.getFilters().put("callbackFilter", callbackFilter(config));
        filterFactoryBean.getFilters().put("logout", logoutFilter(config));

        //filterFactoryBean.setFilters(filterMap);
        filterFactoryBean.setFilterChainDefinitionMap(shiroFilterChainDefinition.getFilterChainMap());

        return filterFactoryBean;
    }

    @Bean
    @Override
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        chainDefinition.addPathDefinition("/login", "authc"); // need to accept POSTs from the login form
        chainDefinition.addPathDefinition("/logout", "logout");
        chainDefinition.addPathDefinition("/callback", "callbackFilter");
        chainDefinition.addPathDefinition("/test", "authc");
        chainDefinition.addPathDefinition("/**", "anon");

        return chainDefinition;
    }

    @Bean
    public ShiroCasLogoutHandler casLogoutHandler() {
        return new ShiroCasLogoutHandler();
    }

    @Bean
    public CasConfiguration casConfiguration(@Value("${cas.loginUrl}") String casLoginUrl, ShiroCasLogoutHandler casLogoutHandler) {
        CasConfiguration casConfig = new CasConfiguration();
        casConfig.setLoginUrl(casLoginUrl);
        casConfig.setProtocol(CasProtocol.CAS30);
        casConfig.setLogoutHandler(casLogoutHandler);
        casConfig.setRenew(false);
        return casConfig;
    }

    @Bean
    public CasClient casClient(CasConfiguration casConfiguration) {
        CasClient casClient = new CasClient(casConfiguration);
        casClient.setName("CasClient");
        casClient.setIncludeClientNameInCallbackUrl(true);
        casClient.setAuthorizationGenerator(new CasAuthorizationGenerator<>());
        return casClient;
    }

    @Bean(name="callBackUrl")
    public String callBackUrl(@Value("${cas.callbackUrl}") String callbackUrl) {
        return callbackUrl;
    }

    @Bean
    public Config pack4jConfig(CasClient casClient, String callBackUrl) {
        Config config = new Config(callBackUrl, casClient);
        config.addMatcher("excludedPath", excludedPathMatcher());
        return config;
    }

    private Matcher excludedPathMatcher() {
        return new PathMatcher()
                .excludeRegex("^/js/.*$")
                .excludeRegex("^/css/.*$")
                .excludeRegex("^/img/.*$")
                .excludeRegex("^/fonts/.*$")
                .excludeRegex("^/error/.*$")
                .excludeRegex("^/fragments/.*$")
                .excludeRegex("^/cohort-results/.*$")
                .excludeRegex("^/notebook-results/.*$")
                .excludePath("/")
                //.excludePath("/test")
                .excludePath("/index.html");
    }

    private LogoutFilter logoutFilter(Config pack4jConfig) {
        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setCentralLogout(true);
        logoutFilter.setLocalLogout(true);
        logoutFilter.setConfig(pack4jConfig);
        logoutFilter.setDefaultUrl("/");
        return logoutFilter;
    }

    @Bean
    public SecurityFilter casSecurityFilter(Config pack4jConfig) {
        SecurityFilter securityFilter = new SecurityFilter();
        securityFilter.setConfig(pack4jConfig);
        securityFilter.setClients("CasClient");
        securityFilter.setMatchers("excludedPath");
        return securityFilter;
    }

    private CallbackFilter callbackFilter(Config pack4jConfig) {
        CallbackFilter callbackFilter = new CallbackFilter();
        callbackFilter.setConfig(pack4jConfig);
        callbackFilter.setMultiProfile(false);
        //callbackFilter.setDefaultUrl("/test");
        return callbackFilter;
    }

    @Bean
    protected CacheManager cacheManager() {
        return new MemoryConstrainedCacheManager();
    }
}
