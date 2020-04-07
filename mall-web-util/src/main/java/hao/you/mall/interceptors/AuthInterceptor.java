package hao.you.mall.interceptors;

import com.alibaba.fastjson.JSON;
import hao.you.mall.annotations.LoginRequired;
import hao.you.mall.common.HttpclientUtil;
import hao.you.mall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

//拦截器
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    // 预处理
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断被拦截的请求的访问的方法的注解(是否时需要拦截的)
        System.out.println("进入拦截器");
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);
        //是否拦截annotations
        if (methodAnnotation == null) {
            return true;
        }

        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        //是否必须登录
        boolean b = methodAnnotation.loginSuccess();
        // 调用认证中心进行验证
        String success = "fail";
        Map<String, String> successMap = new HashMap<>();

        if (StringUtils.isNotBlank(token)) {
            String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();// 从request中获取ip
                if (StringUtils.isBlank(ip)) {
                    ip = "127.0.0.1";
                }
            }

            String successJson = HttpclientUtil.doGet("http://passport.mall.com:8085/verify?token=" + token + "&currentIp=" + ip);
            successMap = JSON.parseObject(successJson, Map.class);
            success = successMap.get("status");
        }

        if (b) {
            //必须登录成功才可以进入
            if (!success.equals("success")) {
                //重定向passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.mall.com:8085/index?ReturnUrl=" + requestURL.toString());
                return false;
            }
            //需要将token携带的用户信息写入
            request.setAttribute("memberId", successMap.get("memberId"));
            request.setAttribute("nickname", successMap.get("nickname"));
            //验证通过，覆盖cookie中的token
            if (StringUtils.isNotBlank(token)) {
                CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
            }
        } else {
            //没有登录也能用，但是必须验证
            if (success.equals("success")) {
                //需要将token携带的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
                //验证通过，覆盖cookie中的token
                if (StringUtils.isNotBlank(token)) {
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
                }
            }
        }

        return true;
    }

    // 后处理
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("后处理");
    }

    // 返回处理
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("返回处理");
    }


}
