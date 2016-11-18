import java.util.*;

/**
 * Created by wangshikai on 2016/10/9.
 */
public class TestList {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
//        list.retainAll()
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);


        List<Integer> list2 = new ArrayList<>();

//        list2.add(3);
//        list2.add(4);
//        list2.add(5);

        list2.add(7);
        list2.add(8);
        list2.add(9);
                list2.add(3);
        list2.add(4);
        list2.add(5);
        Set<Integer> set = new HashSet<>();
        set.add(1);
        set.add(2);
        set.add(3);

        System.out.println(set.toString());


//        System.out.println(Collections.disjoint(list,list2));
//        System.out.println(list.retainAll(list2));
//        System.out.println(list);
//        System.out.println(list2);
//
//        Set<Integer> set = Collections.singleton(6);
//        System.out.println(set);
//        int count = Collections.frequency(list,5);
//        System.out.println(count);
    }
}
