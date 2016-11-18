/**
 * Created by wangshikai on 2016/10/13.
 */
public class TestGcd {
    public static void main(String[] args) {
        System.out.println(gcd(15,45));
    }

    public static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        } else {
            return gcd(b, a % b);
        }
    }


}
