package com.luguosong.ssiach2ex4.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		// 从认证对象中提取用户名
        String username = authentication.getName();
		// 从认证对象中提取密码凭据并转换为字符串
        String password = String.valueOf(authentication.getCredentials());


		//检查用户名和密码是否匹配预设的硬编码值
        if ("john".equals(username) && "12345".equals(password)) {
			// 这里替代了 UserDetailsService 和 PasswordEncoder 的职责
			//认证成功，创建并返回包含用户名、密码和空权限列表的认证令牌
            return new UsernamePasswordAuthenticationToken(username, password, Arrays.asList());
        } else {
			// 认证失败，抛出认证凭据未找到异常
            throw new AuthenticationCredentialsNotFoundException("Error!");
        }
    }

	//指定此认证提供者支持的认证类型，返回true表示支持UsernamePasswordAuthenticationToken类型
    @Override
    public boolean supports(Class<?> authenticationType) {
		// 检查传入的认证类型是否为UsernamePasswordAuthenticationToken或其子类
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authenticationType);
    }
}
