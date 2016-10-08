/**
 * Created by wangshikai on 2016/9/19.
 */
public class PrintHelloWorld implements MyPrint{

    public PrintHelloWorld(){

    }
    @Override
    public void print() {
        System.out.println("打印:"+"helloWord");
    }

    @Override
    public void write(String name, int age) {
        System.out.println("打印:"+"name is :"+name+",\tage is :"+age);
    }
}
