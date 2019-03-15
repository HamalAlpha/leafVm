package cn.sheratan.jvm;

import org.freeinternals.format.classfile.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Description: 栈帧
 * @Author aries
 * @Data 2019-03-11 17:32
 */
public class Frame {
    private static final Logger LOGGER = Logger.getLogger(Frame.class.getName());

    private final Thread fThread;
    private final Class fClass;
    private final MethodInfo fMethod;

    /**
     * @Description: 字节码指令偏移值
     */
    private int fPC;

    /**
     * @Description: 局部变量表
     */
    private Slot[] fLocals;

    /**
     * @Description: 操作数栈
     */
    private Slot[] fOperStacks;

    /**
     * @Description: 操作数栈深度指针
     */
    private int fStackPos;

    /**
     * @Description: Code属性输入流，通过该对象解析方法字节码指令
     */
    private PosDataInputStream fCodeStream;

    public Frame(Thread thread, Class clazz, MethodInfo method) {
        this.fThread = thread;
        this.fClass = clazz;
        this.fMethod = method;
        initialize();
    }

    public void run() {

    }

    private void initialize() {
        fPC = 0;
        AttributeCode attr = getAttributeCode();
        if (attr == null) {
            throw new RuntimeException("could not found the code attribute");
        }
        fLocals = new Slot[attr.getMaxLocals()];
        fOperStacks = new Slot[attr.getMaxStack()];
        fStackPos = 0;
        fCodeStream = new PosDataInputStream(new PosByteArrayInputStream(attr.getCode()));
        try {
            //标记Code属性字节开始位置，使得可反复读取指令，传入参数意为若一次读取超过参数大小的字节，mark标记将失效
            fCodeStream.mark(fCodeStream.available());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    /**
     * @Description: 获取Code属性值
     */
    private AttributeCode getAttributeCode() {
        return (AttributeCode) getAttributeInfo(AttributeInfo.TypeCode);
    }

    /**
     * @Description: 获取属性信息
     * @Param name 属性名称
     * @return: org.freeinternals.format.classfile.AttributeInfo
     */
    private AttributeInfo getAttributeInfo(String name) {
        for (int i = 0; i < fMethod.getAttributesCount(); i++) {
            AttributeInfo attr = fMethod.getAttribute(i);
            if (name.equals(attr.getName())) {
                return attr;
            }
        }
        return null;
    }
}
