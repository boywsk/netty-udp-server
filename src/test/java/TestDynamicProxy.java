/**
 * Created by wangshikai on 2016/9/19.
 */
public class TestDynamicProxy {
    public static void main(String[] args) {
        //正常调用
      /*  MyPrint p = new PrintHelloWorld();
        p.print();

        //代理调用
        MyPrint o = new PrintHelloWorld();
        MyPrint proxyObject = (MyPrint)Proxy.newProxyInstance(o.getClass().getClassLoader(),o.getClass().getInterfaces(),new MyDynamicProxy(o));
        proxyObject.print();
        proxyObject.write("lily",20);
        System.out.println(proxyObject.getClass().getName());
        System.out.println(new Object(){
            public boolean check(){
                System.out.println("ab");
                return false;
            }
        }.check());*/
//        if(("a") -> {return false;}){
//
//        }
//        if(((BooleanSupplier)()->{
//            System.out.println("a");
//            return false;
//        }).getAsBoolean()){
//
//        }
//        String a = "1";
//        String b = "2";
//        String c ="12";
//        System.out.println(a+b == c);
//        System.out.println((a+b).equals(c));
//        System.out.println((a+b).intern() == c );

    }

}
