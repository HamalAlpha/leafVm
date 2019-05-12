package cn.sheratan.jvm;

import org.freeinternals.format.classfile.ClassFile;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Description: 这里仅封装一种类加载器，所有类都由该类加载器进行装载。实际中类似hotspot虚拟机的双亲委派机制，存在多种类加载器，由此加载不同类路径下的类
 * 另外，leafVM只保证在单线程下正确加载类，实际中还需要考虑线程安全问题。
 * @Author aries
 * @Data 2019-03-08 11:34
 */
public class ClassLoader {
    private static final Logger LOGGER = Logger.getLogger(ClassLoader.class.getName());

    /**
     * @Description: 指定类所在的类路径
     */
    private ClassPath classPath;

    /**
     * @Description: 存放由该类加载器加载过的类
     */
    private Map<String, Class> classTable;

    public ClassLoader(ClassPath classpath) {
        this.classPath = classpath;
        classTable = new HashMap<>();
    }

    /**
     * @Description: 根据类的完整名称（xx/xx/xx.class）执行类加载过程，无法加载Object类
     * @Param fullName
     * @return: cn.sheratan.jvm.Class
     */
    public Class loadClass(String fullName) {

        if ("java/lang/Object".equals(fullName)) {
            LOGGER.log(Level.WARNING, "refuse load the java/lang/Object");
            return null;
        }

        Class clazz = classTable.get(fullName);
        if(clazz != null){
            return clazz;
        }

        //下面步骤模拟类装载链接（只执行验证、准备两个阶段，解析阶段是无法确定什么时候执行的）过程
        ClassFile cf = classPath.loadClassFile(fullName);
        if(cf == null){
            throw new RuntimeException("could not found the classFile :" + fullName);
        }
        clazz = newClass(cf, fullName);
        resloveSuperClass(clazz);
        resloveInterfaces(clazz);
        link(clazz);

        LOGGER.info("completed load the class:" + fullName);
        classTable.put(fullName, clazz);
        return clazz;
    }

    /**
     * @Description: 处理继承父类
     */
    private void resloveSuperClass(Class clazz){

    }

    /**
     * @Description: 处理实现接口
     */
    private void resloveInterfaces(Class clazz){

    }

    /**
     * @Description: 完成类装载后，执行链接阶段处理
     * @Param clazz
     * @return: void
     */
    private void link(Class clazz){
        //TODO：验证，准备，解析
    }

    /** 
     * @Description: 类装载阶段，依据类文件生成java.lang.Class类
     * @Param cf 
     * @return: cn.sheratan.jvm.Class 
     */ 
    private Class newClass(ClassFile cf, String className){
        return new Class(cf, this, className);
    }
}
