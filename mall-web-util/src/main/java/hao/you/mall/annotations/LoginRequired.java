package hao.you.mall.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//源注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
    //true:该方法必须被拦截，登录成功才可以访问
    //false：登录失败也可以访问
    boolean loginSuccess() default true;

}
