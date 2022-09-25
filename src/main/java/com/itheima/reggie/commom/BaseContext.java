package com.itheima.reggie.commom;

public class BaseContext {
    private  static  ThreadLocal<Long> threadLocal=new ThreadLocal<>();
    public static void setThreadLocal(Long id){
        threadLocal.set(id);
    }
    public  static  Long get(){
       return threadLocal.get();
    }
}
