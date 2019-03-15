package cn.sheratan.jvm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * @Description: test
 * @Author aries
 * @Data 2019-03-12 10:06
 */
public class test {
    public interface I{
        int exec();
    }

    static I i;

    public static void main(String[] args) throws Exception{
//        File file = new File("D:\\test.txt");
//        FileInputStream fis = new FileInputStream(file);
//        BufferedInputStream bis = new BufferedInputStream(fis);
//        byte[] tb = new byte[2];
//        bis.read(tb);
//        bis.mark(10);
//        tb = new byte[6];
//        bis.read(tb);
//        System.out.println(new String(tb));
//        bis.reset();
//        tb = new byte[12];
//        bis.read(tb);
//        System.out.println(new String(tb));
        tt(1,2);
        System.out.println(i.exec());
    }


    public static void tt(int a, int b){
        i = () -> {
            return a+b;
        };
    }
}
