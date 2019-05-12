package cn.sheratan.jvm;

import org.freeinternals.format.FileFormatException;
import org.freeinternals.format.classfile.*;

import java.util.logging.Logger;

/**
 * @Description: Class entity
 * @Author aries
 * @Data 2019-03-08 11:54
 */
public class Class {
    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
    private final ClassFile CF;
    private final ClassLoader CL;
    private final String CLASS_NAME;
    /**
     * @Description: 是否已经进行初始化
     */
    private boolean isStaticInited;

    /**
     * @Description: 方法实体
     */
    public static class Symbol {
        public Class clazz;
        public MethodInfo method;
        public int argCount;

        public Symbol(Class clazz, MethodInfo method, int argCount) {
            this.clazz = clazz;
            this.method = method;
            this.argCount = argCount;
        }
    }

    Class(ClassFile CF, ClassLoader CL, String className) {
        this.CF = CF;
        this.CL = CL;
        this.CLASS_NAME = className;
    }

    /**
     * @Description: 获取类方法
     * @Param name 类名称
     * @Param descriptor 类描述符
     * @return: org.freeinternals.format.classfile.MethodInfo
     */
    public MethodInfo findMethod(String name, String descriptor) {
        for (MethodInfo methodInfo : CF.getMethods()) {
            String methodName = getStringFromConstantPool(methodInfo.getNameIndex());
            String methodDescriptor = getStringFromConstantPool(methodInfo.getDescriptorIndex());
            if (methodName.equals(name) && methodDescriptor.equals(methodDescriptor)) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * @Description: 获取类属性
     */
    public FieldInfo findField(String name, String descriptor) {
        for (FieldInfo fieldInfo : CF.getFields()) {
            String fieldName = getStringFromConstantPool(fieldInfo.getNameIndex());
            if (fieldName.equals(name)) {
                return fieldInfo;
            }
        }
        return null;
    }

    /**
     * @Description: 通过常量池下标获取栈帧中调用的方法的全称（包括描述符），并生成方法实体Symbol
     * @Param constantIndex 常量池下标
     * @return: cn.sheratan.jvm.Class.Symbol
     */
    public Symbol resloveMethodByRef(int constantIndex) {
        //获取常量池
        AbstractCPInfo[] pool = CF.getConstantPool();

        //获取方法表结构
        AbstractCPInfo info = pool[constantIndex];
        assert (info.getTag() == AbstractCPInfo.CONSTANT_Methodref);
        ConstantMethodrefInfo methodrefInfo = (ConstantMethodrefInfo) info;

        //由方法表结构获取声明该方法的类表结构
        ConstantClassInfo classInfo = (ConstantClassInfo) pool[methodrefInfo.getClassIndex()];
        String className = getStringFromConstantPool(classInfo.getNameIndex());
        Class clazz = CL.loadClass(className);
        if (clazz == null) {
            //出现null的情况是clazz为Object对象，暂无实现
            return null;
        }

        ConstantNameAndTypeInfo nameAndTypeInfo = (ConstantNameAndTypeInfo) pool[methodrefInfo.getNameAndTypeIndex()];
        String methodName = getStringFromConstantPool(nameAndTypeInfo.getNameIndex());
        String methodDescription = getStringFromConstantPool(nameAndTypeInfo.getDescriptorIndex());
        MethodInfo method = clazz.findMethod(methodName, methodDescription);

        return new Symbol(clazz, method, getMethodArgCount(method, methodDescription));
    }

    /**
     * @Description: 从常量池中获取字符串值
     * @Param idx 常量池中索引值
     * @return: java.lang.String
     */
    public String getStringFromConstantPool(int idx) {
        try {
            return CF.getConstantUtf8Value(idx);
        } catch (FileFormatException e) {
            throw new RuntimeException("could not found utf8 constant value at " + idx);
        }
    }

    /**
     * @Description: 获取方法的参数数量，需要注意的是：非静态方法隐含传入“this”参数，故最少参数数量为1
     * @Param method 目标解析方法，由该参数判断是否静态方法
     * @Param methodDescription 方法描述符，主要通过分割该参数获取数量
     * @return: int
     */
    private int getMethodArgCount(MethodInfo method, String methodDescription) {
        int argCount;
        int left = methodDescription.indexOf("(") + 1;
        int right = methodDescription.indexOf(")");
        argCount = ((method.getAccessFlags() & MethodInfo.ACC_STATIC) == 0) ? 1 : 0;
        for (int i = left; i < right; i++, argCount++) {
            if (methodDescription.charAt(i) == 'L') {
                i = methodDescription.indexOf(';', i);
            }
        }
        return argCount;
    }

    public String getClassName() {
        return CLASS_NAME;
    }

    public boolean isStaticInited() {
        return isStaticInited;
    }

    public void setStaticInited() {
        isStaticInited = true;
    }

    public ClassFile getClassFile() {
        return CF;
    }

    public ClassLoader getClassLoader() {
        return CL;
    }

}
