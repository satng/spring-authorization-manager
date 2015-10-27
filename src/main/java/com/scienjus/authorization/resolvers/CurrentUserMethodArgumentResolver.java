package com.scienjus.authorization.resolvers;

import com.scienjus.authorization.annotation.CurrentUser;
import com.scienjus.authorization.interceptor.AuthorizationInterceptor;
import com.scienjus.authorization.repository.UserModelRepository;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * 增加方法注入，将含有CurrentUser注解的方法参数注入当前登录用户
 * @see com.scienjus.authorization.annotation.CurrentUser
 * @author ScienJus
 * @date 2015/7/31.
 */
public class CurrentUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private String userModelClass;
    private UserModelRepository userModelRepository;

    public void setUserModelClass(String userModelClass) {
        this.userModelClass = userModelClass;
    }

    public void setUserModelRepository(UserModelRepository userModelRepository) {
        this.userModelRepository = userModelRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class clazz;
        try {
            clazz = Class.forName(userModelClass);
        } catch (ClassNotFoundException e) {
            return false;
        }
        //如果参数类型是User并且有CurrentUser注解则支持
        if (parameter.getParameterType().isAssignableFrom(clazz) &&
                parameter.hasParameterAnnotation(CurrentUser.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        //取出鉴权时存入的登录用户Id
        Object object = webRequest.getAttribute(AuthorizationInterceptor.REQUEST_CURRENT_KEY, RequestAttributes.SCOPE_REQUEST);
        if (object != null) {
            String key = String.valueOf(object);
            //从数据库中查询并返回
            Object userModel = userModelRepository.getCurrentUser(key);
            if (userModel != null) {
                return userModel;
            }
            //有key但是得不到用户，抛出异常
            throw new MissingServletRequestPartException(AuthorizationInterceptor.REQUEST_CURRENT_KEY);
        }
        //没有key就直接返回null
        return null;
    }
}
