package cn.sheratan.jvm;

import org.apache.commons.cli.*;

import java.util.function.Function;

/**
 * @Description: leafVm, 由Java语言编写的JVM，功能比较有限且没有涉及过多底层实现，旨在对JVM研究有个初步认识
 * @Author: Aries
 * @Date: 2019/3/4
 */
public class Main {
    private static String classpath = "classpath";
    private static String help = "help";

    /**
     * @Description: leafVm主入口
     * @Param args 传入命令行参数，类路径
     * @return: void
     */
    public static void main(String[] args) {
        //创建默认命令行解析对象实例
        CommandLineParser parser = new DefaultParser();
        //添加命令行参数
        Options options = new Options();
        options.addOption("cp", classpath, true, "specify the classpath");
        options.addOption("h", help, false, "show the help message");

        //定义帮助信息展示方法
        Function<Void, Integer> leafHelp = (v) -> {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("leafVm", options);
            return -1;
        };

        try {
            //对命令行参数进行解析
            CommandLine line = parser.parse(options, args);
            if(line.getArgList().size() == 0){
                leafHelp.apply(null);
                return;
            }

            //参数中包含classpath
            String cp = null;
            if(line.hasOption(classpath)){
                cp = line.getOptionValue(classpath);
            }

            //获取java源文件
            String mainClass = line.getArgList().get(0);
            run(cp, mainClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** 
     * @Description: vm执行入口,
     * @Param cp 类路径
	 * @Param mainClass java源文件主名称
     * @return: void
     */ 
    private static void run(String cp, String mainClass){
        System.out.println(Main.class.getClassLoader().toString());
    }
}
