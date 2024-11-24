package com.example.emos.wx.config.shiro;

import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OAuth2Realm extends AuthorizingRealm {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    //判断当前 Realm 是否支持特定类型的 AuthenticationToken
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /*
    * 授权，验证权限时调用
    * */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection Collection) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        //查询用户权限列表
        TbUser user = (TbUser) Collection.getPrimaryPrincipal();
        Set<String> permissions = userService.searchUserPermissions(user.getId());
        //把用户权限加入到info当中
        info.setStringPermissions(permissions);
        return info;
    }

    /*
    * 认证，登录时调用
    * */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken Token) throws AuthenticationException {
        //从令牌中获取用户userId,检测用户是否被冻结
        String accessToken = (String) Token.getPrincipal();
        int userId = jwtUtil.getUserId(accessToken);
        TbUser tbUser = userService.selectById(userId);
        if (tbUser == null) {
            throw new LockedAccountException("账号已被锁定，请联系管理员");
        }
        // 将token信息，用户信息加入到info当中
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(tbUser, accessToken, this.getName());
        return info;
    }
}
