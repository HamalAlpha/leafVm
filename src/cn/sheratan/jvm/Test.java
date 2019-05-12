package cn.sheratan.jvm;

import org.freeinternals.format.classfile.MethodInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
class TT{
    public int k = 10;
}

/**
 * @Description: Test
 * @Author aries
 * @Data 2019-03-12 10:06
 */
public class Test {

    private int k = 10;
    private static int d = 5;
    private static final Map<String, Integer> map;
    private static long l = 200L;
    static {
        map = new HashMap<>();
        System.out.println("static");
        System.out.println(map == null);
        initial();
    }

    Test(){
        k = 1;
        System.out.println(k);
        l = k + 2000L;
    }

    private static void initial() {
        map.put("1", 1);
    }

    public static void test() {
        System.out.println("test");
        System.out.println(map == null);
        System.out.println(map.get("1"));
    }

    public void adc(int a, int b, int c, int d, int key) {
        String str = "";
        for (int i = 0; i < 100; i++) {
            str += i;
        }

        String st = "";
        for (int k = 0; k < 100; k++) {
            str += k;
        }
    }

    private static Object internal(){
        try {
            return System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
//        long lg = -9223372036854775808L;
//        System.out.println(Long.toBinaryString(lg));
//
//        long l1 = -1;
//        StringBuilder s = new StringBuilder(Long.toBinaryString(l1));
//        System.out.println(s);
//        System.out.println(s.substring(0, 32));
//        System.out.println(s.substring(32, 64));
//        System.out.println(Long.valueOf("100"));

//        System.out.println(System.getProperties().getProperty("sun.boot.library.path"));
//        int a = 1;
//        int b = -0x80000000;
//        System.out.println(Integer.toBinaryString(a));
//        System.out.println(Integer.toBinaryString(b));
//        System.out.println(Integer.toBinaryString(a | b));

//        int a = 1;
//        int b = 0;
//        b++;
//        switch (a) {
//            case -100:
//                a++;
//                break;
//            case -1:
//                a--;
//                break;
//            case 1:
//                a++;
//                break;
//            case 100:
//                a--;
//                break;
//            default:
//                a += a;
//                break;
//        }

        Test t = new Test();
        int s = t.d;
        internal();
    }

}
