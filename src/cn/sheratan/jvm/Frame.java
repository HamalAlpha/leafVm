package cn.sheratan.jvm;

import cn.sheratan.jvm.Instruction.InstructionFactory;
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
        InstructionFactory.Instruction inst = InstructionFactory.createInstruction(fCodeStream);
        fPC = fCodeStream.getPos();
        inst.exec(fCodeStream, this);
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
        //以字节流形式读取code属性
        fCodeStream = new PosDataInputStream(new PosByteArrayInputStream(attr.getCode()));
        try {
            //标记Code属性字节开始位置，用于需要跳转的指令，传入参数意为若一次读取超过参数大小的字节，mark标记将失效
            fCodeStream.mark(fCodeStream.available());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    /** 
     * @Description: 操控偏移PC，若offset为负数，需要reset fCodeStream
     * @Param offset 偏移值
     * @return: void 
     */ 
    public void offsetPC(int offset){
        int pc = fPC - 1;
        if(offset > 0){
            //举个栗子：读取到字节指令偏移值为5的ifeq指令，紧接着读取到两字节的目标指令偏移值9，即跳至偏移值为14的指令
            //此时，fPC=6,fCodeStream.getPos=8,offset=9
            //所以实际上fCodeStream需要skip的字节数为6，跳至14。之所以需要减1，偏移值9是相对ifeq指令来说，而此时fPC的下标实际上比ifeq指令偏移值多1字节
            offset = offset - (fCodeStream.getPos() - fPC) - 1;
        }else{
            try {
                fCodeStream.reset();
                offset = pc + offset;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fCodeStream.skip(offset);
        } catch (IOException e) {
            e.printStackTrace();
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

    public void pushInt(int i) {
        fOperStacks[fStackPos++] = new Slot(i);
    }

    public int popInt() {
        return (Integer) fOperStacks[--fStackPos].getObj();
    }

    public void storeInt(int idx, int v) {
        fLocals[idx] = new Slot(v);
    }

    public int loadInt(int idx) {
        return (Integer) fLocals[idx].getObj();
    }

    public void pushSlot(Slot s) {
        fOperStacks[fStackPos++] = s;
    }

    public Slot popSlot() {
        return fOperStacks[--fStackPos];
    }

    public void storeSlot(int idx, Slot s) {
        fLocals[idx] = s;
    }

    public Slot loadSlot(int idx) {
        return fLocals[idx];
    }

    public void pushRef(Object obj) {
        fOperStacks[fStackPos++] = new Slot(obj);
    }

    public Object popRef() {
        return fOperStacks[--fStackPos].getObj();
    }

    public void storeRef(int idx, Object obj) {
        fLocals[idx] = new Slot(obj);
    }

    public Object loadRef(int idx) {
        return fLocals[idx].getObj();
    }

    public Thread getfThread() {
        return fThread;
    }

    public Class getfClass() {
        return fClass;
    }

    public MethodInfo getfMethod() {
        return fMethod;
    }

    public PosDataInputStream getfCodeStream() {
        return fCodeStream;
    }
}
