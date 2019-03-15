package cn.sheratan.jvm;

import org.freeinternals.format.FileFormatException;
import org.freeinternals.format.classfile.ClassFile;
import org.freeinternals.format.classfile.MethodInfo;

import java.util.logging.Logger;

/**
 * @Description: Class entity
 * @Author aries
 * @Data 2019-03-08 11:54
 */
public class Class {
    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
    private ClassFile cf;
    private ClassLoader cl;

    public Class(ClassFile cf, ClassLoader cl){
        this.cf = cf;
        this.cl = cl;
    }

    /**
     * @Description: 获取类方法
     * @Param name 类名称
	 * @Param descriptor 类描述符
     * @return: org.freeinternals.format.classfile.MethodInfo
     */
    public MethodInfo findMethod(String name, String descriptor){
        for(MethodInfo methodInfo : cf.getMethods()){
            String methodName = getStringFromConstantPool(methodInfo.getNameIndex());
            String methodDescriptor = getStringFromConstantPool(methodInfo.getDescriptorIndex());
            if(methodName.equals(name) && methodDescriptor.equals(methodDescriptor)){
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * @Description: 从常量池中获取字符串值
     * @Param idx 常量池中索引值
     * @return: java.lang.String
     */
    private String getStringFromConstantPool(int idx){
        try {
            return cf.getConstantUtf8Value(idx);
        } catch (FileFormatException e) {
            throw new RuntimeException("could not found utf8 constant value at " + idx);
        }
    }

}
