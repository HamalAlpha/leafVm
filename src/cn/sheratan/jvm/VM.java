package cn.sheratan.jvm;

import org.freeinternals.format.classfile.MethodInfo;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @Description: VM body
 * @Author aries
 * @Data 2019-03-08 11:24
 */
public class VM {
    /**
     * @Description: VM 配置类
     */
    public static class Config {
        /**
         * @Description: 是否需要JIT处理
         */
        private boolean isProcessByJIT = false;

        public void setProcessByJIT(boolean processByJIT) {
            isProcessByJIT = processByJIT;
        }
    }

    /**
     * @Description: 类路径
     */
    private ClassPath classPath;

    /**
     * @Description: 类加载器
     */
    private ClassLoader classLoader;

    /**
     * @Description: vm配置
     */
    private Config config;

    public VM(String[] classPathLst, Config config) {
        classPath = new ClassPath(classPathLst);
        classLoader = new ClassLoader(classPath);
        this.config = config;
    }

    /**
     * @Description: 虚拟机启动从main方法主入口开始执行
     * @Param mainClass main方法所在类的全称
     * @return: void
     */
    public void run(String mainClass) {
        //加载主类
        Class clazz = classLoader.loadClass(mainClass);
        //获取并执行主类main方法
        MethodInfo methodInfo = clazz.findMethod("main", "([Ljava/lang/String;)V");
        //验证访问标志
        Function<Integer, Boolean> flagCheck = (f) -> {
            return (f & methodInfo.getAccessFlags()) != 0;
        };
        if(methodInfo == null){
            throw new RuntimeException("not found the main function!");
        }else if(!flagCheck.apply(MethodInfo.ACC_PUBLIC) || !flagCheck.apply(MethodInfo.ACC_STATIC)) {
            throw new RuntimeException("main method is not static&public");
        }
        //启动主线程
        Thread thread = new Thread();
        thread.run(clazz, methodInfo);
    }
}
