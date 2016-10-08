import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangshikai on 2016/9/22.
 */
public class TestDinning {
    private static AtomicInteger integer = new AtomicInteger(2);

    static class Person implements Runnable {
        private int id;

        public Person(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                //1.吃饭
                int count = getChoise();
                if (count > 0) {
                    System.out.println("id:" + this.id + "吃饭......." + "\t 当前可用数量:" + count);
                    try {
                        int time = new Random().nextInt(1000);
                        System.out.println(time);
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    integer.getAndIncrement();
                } else {
                    try {
                        int time = new Random().nextInt(500);
                        System.out.println("500:"+time);
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("id:" + this.id + "思考.......");
                }
            }
        }
    }

    public static synchronized int getChoise() {
        if (integer.get() > 0) {
            return integer.getAndDecrement();
        } else {
            return 0;
        }
    }

    public static void main(String[] args) {
        for (int i = 1; i < 6; i++) {
            Person person = new Person(i);
            Thread t = new Thread(person);
            t.start();
        }
    }

}
