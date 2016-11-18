public class SortTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        int[] a = {62, 54, 68, 73, 99, 22, 46, 83, 22, 70};
        long t1 = System.currentTimeMillis();
        sortMethod1(a);
        long t2 = System.currentTimeMillis();
        sortMethod2(a);
        long t3 = System.currentTimeMillis();
        System.out.println("第一次:"+(t2-t1)+"\t,第二次:"+(t3-t2));
    }

    public static void sortMethod1(int[] s) {
        int temp;
        for (int i = 0; i < s.length - 1; i++) {
            for (int j = i + 1; j < s.length; j++) {
                if (s[i] > s[j]) {
                    temp = s[i];
                    s[i] = s[j];
                    s[j] = temp;
                }
//                for (int k = 0; k < s.length; k++) {//输出动态
//                    System.out.print(s[k] + ", ");
//                }
//                System.out.println(" ");
            }
//            System.out.println(" ");
        }
        for(int k = 0;k<s.length;k++){
//            System.out.println("直接排序:");
            System.out.print(s[k] + ", ");
        }
    }

    public static void sortMethod2(int[] s) {
        int temp;
        for (int i = 1; i <= s.length; i++) {
            for (int j = 0; j < s.length - 1; j++) {
                if (s[j] > s[j + 1]) {
                    temp = s[j];
                    s[j] = s[j + 1];
                    s[j + 1] = temp;
                }
//                for (int k = 0; k < s.length; k++) {//输出动态
//                    System.out.print(s[k] + ", ");
//                }
//                System.out.println(" ");
            }
//            System.out.println(" ");
        }
        for(int k = 0;k<s.length;k++){
//            System.out.println("冒泡排序:");
            System.out.print(s[k] + ", ");
        }
    }
}
