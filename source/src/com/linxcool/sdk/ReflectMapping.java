package com.linxcool.sdk;

import java.lang.reflect.Method;

public class ReflectMapping {
	
	protected Object obj;
	protected Class<?> cls;
	
	public ReflectMapping(Class<?> cls){
		try {
			this.cls = cls;
			this.obj = cls.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void invoke(Object... args) throws Exception{
		StackTraceElement element = Thread.currentThread().getStackTrace()[2];
		String name = element.getMethodName();
		Method[] methods = cls.getMethods();
		for (Method method : methods) {
			if(name.equals(method.getName())){
				method.invoke(obj, args);
				break;
			}
		}
	}
}
