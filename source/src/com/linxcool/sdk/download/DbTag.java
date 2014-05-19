package com.linxcool.sdk.download;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户数据库的注解标识
 * @author: linxcool.hu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DbTag {
	
	public String name();
	
	public String type() default "varchar(50)";
	
	public String constraint() default "";
	
	public boolean ignore() default false;
}
