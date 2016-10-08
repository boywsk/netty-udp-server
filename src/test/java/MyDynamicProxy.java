import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by wangshikai on 2016/9/19.
 */
class MyDynamicProxy implements InvocationHandler {

    private Object proxy;

    public MyDynamicProxy(Object proxy){
        this.proxy = proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         /*
         * before ：doSomething();
         */
        System.out.println("调用的方法名称:"+method.getName());
        long a = System.currentTimeMillis();
        Object result = method.invoke(this.proxy, args);

        /*
         * after : doSomething();
         */
        long b = System.currentTimeMillis();
        System.out.println("方法执行时间:"+(b-a));
        return result;
    }
}
