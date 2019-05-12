package cn.sheratan.jvm.Instruction;

import cn.sheratan.jvm.Class;
import cn.sheratan.jvm.Frame;
import cn.sheratan.jvm.Slot;
import cn.sheratan.jvm.Thread;
import org.freeinternals.format.classfile.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @Description: 指令工厂
 * @Author aries
 * @Data 2019-03-12 15:35
 */
public class InstructionFactory {
    private static final Logger LOGGER = Logger.getLogger(InstructionFactory.class.getName());

    /**
     * @Description: 保存指令集
     */
    private static final Map<Integer, Instruction> instructionMap = new HashMap<>();

    /**
     * @Description: 指令对象
     */
    public interface Instruction {
        void exec(DataInputStream codeStream, Frame frame);
    }

    static {
        initialize();
    }

    /**
     * @Description: 初始化指令
     * @return: void
     */
    private static void initialize() {
        register(Opcode.op_nop, (codeStream, frame) -> {
            //nothing to do of 'nop' instruction
        });

        register(Opcode.op_aconst_null, (codeStream, frame) -> {
            frame.pushRef(null);
        });

        register(Opcode.op_iconst_m1, createIConst(-1));
        register(Opcode.op_iconst_0, createIConst(0));
        register(Opcode.op_iconst_1, createIConst(1));
        register(Opcode.op_iconst_2, createIConst(2));
        register(Opcode.op_iconst_3, createIConst(3));
        register(Opcode.op_iconst_4, createIConst(4));
        register(Opcode.op_iconst_5, createIConst(5));

        register(Opcode.op_lconst_0, createLConst(0));

        register(Opcode.op_bipush, (codeStream, frame) -> {
            try {
                int c1 = codeStream.readByte();
                frame.pushInt(c1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_sipush, (codeStream, frame) -> {
            try {
                frame.pushInt(codeStream.readShort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        /**
         * @Description: Ldc is only usable for constant pool indexes up to 255. When in doubt, use ldc_w, since it always works
         */
        register(Opcode.op_ldc, (codeStream, frame) -> {
            try {
                int idx = codeStream.readByte();
                AbstractCPInfo info = frame.getfClass().getClassFile().getConstantPool()[idx];
                int tag = info.getTag();
                switch (tag) {
                    case AbstractCPInfo.CONSTANT_Integer:
                        int val = ((ConstantIntegerInfo) info).getValue();
                        frame.pushInt(val);
                        break;
                    case AbstractCPInfo.CONSTANT_Float:
                        //TODO
                        break;
                    case AbstractCPInfo.CONSTANT_String:
                        String str = frame.getfClass().getStringFromConstantPool(((ConstantStringInfo) info).getStringIndex());
                        frame.pushRef(str);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_ldc_w, (codeStream, frame) -> {
            //TODO 宽索引暂未确定占用多少个字节，需考证
        });

        register(Opcode.op_ldc2_w, (codeStream, frame) -> {
            try {
//                int c1 = codeStream.readByte() & 0xff;
//                int c2 = codeStream.readByte() & 0xff;
//                int idx = (c1 << 8) + c2;
                int idx = codeStream.readShort();
                //long数据类型需要占用8个字节，拆开存储到两个Slot（Slot为4个字节），用字符串存储（可行性待商榷）
                //这里采取存储原数值对应的补码，正负数都保留符号位
                String value = frame.getfClass().getStringFromConstantPool(idx);
                if ("l".equalsIgnoreCase(value.substring(value.length() - 1))) {
                    value = value.substring(0, value.length() - 1);
                }
                StringBuilder v = new StringBuilder(Long.toBinaryString(Long.valueOf(value)));
                if (value.length() < 64) {
                    for (int i = value.length(); i < 64; i++) {
                        v = v.insert(0, "0");
                    }
                }
                //低32位先进栈
                frame.pushRef(v.substring(32, 64));
                frame.pushRef(v.substring(0, 32));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_iload, (codeStream, frame) -> {
            try {
                int v = codeStream.readByte();
                frame.pushInt(v);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_aload, (codeStream, frame) -> {
            try {
                int idx = codeStream.readByte() & 0xff;
                Object obj = frame.loadRef(idx);
                frame.pushRef(obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_iload_0, createILoad(0));
        register(Opcode.op_iload_1, createILoad(1));
        register(Opcode.op_iload_2, createILoad(2));
        register(Opcode.op_iload_3, createILoad(3));

        register(Opcode.op_aload_0, createALoad(0));
        register(Opcode.op_aload_1, createALoad(1));
        register(Opcode.op_aload_2, createALoad(2));
        register(Opcode.op_aload_3, createALoad(3));

        register(Opcode.op_istore, (codeStream, frame) -> {
            try {
                int idx = codeStream.readByte() & 0xff;
                int v = frame.popInt();
                frame.storeInt(idx, v);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_astore, (codeStream, frame) -> {
            try {
                int idx = codeStream.readByte() & 0xff;
                Object obj = frame.popRef();
                frame.storeRef(idx, obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_istore_0, createIStore(0));
        register(Opcode.op_istore_1, createIStore(1));
        register(Opcode.op_istore_2, createIStore(2));
        register(Opcode.op_istore_3, createIStore(3));

        register(Opcode.op_astore_0, createAStore(0));
        register(Opcode.op_astore_1, createAStore(1));
        register(Opcode.op_astore_2, createAStore(2));
        register(Opcode.op_astore_3, createAStore(3));

        register(Opcode.op_pop, (codeStream, frame) -> {
            frame.popSlot();
        });

        /**
         * @Description: Pop the top one or two values from the operand stack.
         * For example:invoke the method 'System.currentTimeMillis()',and do not use the result of this call.
         * Then the bytecode looks like
         *
         * INVOKESTATIC java/lang/System.currentTimeMillis()J
         * POP2
         *
         */
        register(Opcode.op_pop2, (codeStream, frame) -> {
            frame.popSlot();
            frame.popSlot();
        });

        /**
         * @Description: 复制栈顶第一个值，并push到栈顶（栈顶第一个值必须为类型1）
         */
        register(Opcode.op_dup, createDup(1, 0));

        /**
         * @Description: 复制一份操作数栈顶值，并插入到栈顶第二个值之后（即插入到距栈顶第三个位置，该指令保证栈的前两个值都为虚拟机规范中的值类型1，即只占用一个slot）。
         */
        register(Opcode.op_dup_x1, createDup(1, 1));

        /**
         * @Description: 复制一份操作数栈顶值，并插入到栈顶第二个或第三个栈值之后
         * 分为两种情况：
         * 1.前三个值都为类型1，即各占用一个slot，此时插入到第四个位置
         * 2.第一个值为类型1，第二个值为类型2（long和double类型，占用两个slot），此时插入到第三个位置。
         */
        register(Opcode.op_dup_x2, createDup(1, 2));

        /**
         * @Description: 复制一份操作数栈值，并push到栈顶。与dup区别在于：dup2复制两个slot（两个值类型为1，或一个值类型为2）
         */
        register(Opcode.op_dup2, createDup(2, 0));

        /**
         * @Description: 复制栈顶一个或两个操作数栈值，并插入到第两个或三个栈值之后（具体取决于数值类型）
         * 分两种情况：
         * 1.栈顶前三个值均为类型1，这时复制栈顶前两个值并插入到第三个值之后
         * 2.栈顶第一个值为类型2，第二个值为类型1，这时复制栈顶第一个值并插入到第二个值之后
         */
        register(Opcode.op_dup2_x1, createDup(2, 1));

        /**
         * @Description: 复制栈顶一个或两个操作数栈值，并插入到第两个、三个栈值或四个栈值之后（具体取决于数值类型）
         * 分四种情况：
         * 1.栈顶前四个之均为类型1，这时复制栈顶前两个值并插入到第四个值之后
         * 2.栈顶第一个值为类型2，后两个值为类型1，这时复制栈顶第一个值并插入到第三个值之后
         * 3.栈顶前两个值为类型1，后一个值为类型2，这时复制栈顶前两个值并插入到第三个值之后
         * 4.栈顶前两个值均为类型2，这时复制栈顶第一个值并插入到第二个值之后
         */
        register(Opcode.op_dup2_x2, createDup(2, 2));

        register(Opcode.op_swap, (codeStream, frame) -> {
            //貌似jvm目前不支持swap opCode
        });

        register(Opcode.op_iadd, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            frame.pushInt(i2 + i1);
        });

        register(Opcode.op_isub, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            frame.pushInt(i2 - i1);
        });

        register(Opcode.op_imul, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            frame.pushInt(i2 * i1);
        });

        register(Opcode.op_idiv, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            frame.pushInt(i2 / i1);
        });

        register(Opcode.op_irem, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            frame.pushInt(i2 % i1);
        });

        register(Opcode.op_ineg, (codeStream, frame) -> {
            int i1 = frame.popInt();
            frame.pushInt(-i1);
        });

        /**
         * @Description: 将int型数值左移指定位数并将结果压入栈顶，注意最大只支持移动31位，溢出的取模（x%32，若x为负数的取模后再+32）
         * 若移动位数位long型，会利用l2i指令强转为int，强转策略为抛弃高32位（若为负数补码也相同直接抛弃补码前32位）
         */
        register(Opcode.op_ishl, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            i1 = i1 < 0 ? (i1 % 32) + 32 : i1 % 32;
            frame.pushInt(i2 << i1);
        });

        /**
         * @Description: 带符号右移，负数左边补1，正数补0
         */
        register(Opcode.op_ishr, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            i1 = i1 < 0 ? (i1 % 32) + 32 : i1 % 32;
            frame.pushInt(i2 >> i1);
        });

        /**
         * @Description: 无符号右移，正负数左边均补1
         */
        register(Opcode.op_iushr, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            i1 = i1 < 0 ? (i1 % 32) + 32 : i1 % 32;
            frame.pushInt(i2 >>> i1);
        });

        register(Opcode.op_iand, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            frame.pushInt(i2 & i1);
        });

        register(Opcode.op_ior, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            frame.pushInt(i2 | i1);
        });

        register(Opcode.op_ixor, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            frame.pushInt(i2 ^ i1);
        });

        /**
         * @Description: 该指令含需要两个参数：目标在局部变量表中的下标，常量值
         * 下标值范围：0~256
         * 常量值范围：-128~127
         * 该操作指令可以与wide指令结合，javap中指令显示为：iinc_w。将下标值拓展为两字节无符号位（0~65535），将常量值拓展为两字节符号位（-32768~32767）
         */
        register(Opcode.op_iinc, (codeStream, frame) -> {
            try {
                int index = codeStream.readByte();
                int value = codeStream.readByte();
                frame.storeInt(index, frame.loadInt(index) + value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        /**
         * @Description: int型转byte型，截取低8位，且符号扩展为32位
         */
        register(Opcode.op_i2b, createIntConverted(8, true));

        register(Opcode.op_i2c, createIntConverted(16, false));

        register(Opcode.op_i2s, createIntConverted(16, true));

        /**
         * @Description: 当栈顶数值等于0时跳转
         */
        register(Opcode.op_ifeq, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v = frame.popInt();
                if (v == 0) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_ifne, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v = frame.popInt();
                if (v != 0) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_iflt, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v = frame.popInt();
                if (v < 0) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_ifge, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v = frame.popInt();
                if (v >= 0) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_ifgt, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v = frame.popInt();
                if (v > 0) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_ifle, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v = frame.popInt();
                if (v <= 0) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_if_icmpeq, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v1 = frame.popInt();
                int v2 = frame.popInt();
                if (v2 == v1) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_if_icmpne, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v1 = frame.popInt();
                int v2 = frame.popInt();
                if (v2 != v1) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_if_icmplt, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v1 = frame.popInt();
                int v2 = frame.popInt();
                if (v2 - v1 < 0) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_if_icmpge, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v1 = frame.popInt();
                int v2 = frame.popInt();
                if (v2 - v1 >= 0) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_if_icmpgt, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v1 = frame.popInt();
                int v2 = frame.popInt();
                if (v2 - v1 > 0) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_if_icmple, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                int v1 = frame.popInt();
                int v2 = frame.popInt();
                if (v2 - v1 <= 0) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_if_acmpeq, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                Object o1 = frame.popRef();
                Object o2 = frame.popRef();
                if (o1 == o2) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_if_acmpne, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                Object o1 = frame.popRef();
                Object o2 = frame.popRef();
                if (o1 != o2) {
                    frame.offsetPC(offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_goto, (codeStream, frame) -> {
            try {
                int offset = codeStream.readUnsignedShort();
                frame.offsetPC(offset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        register(Opcode.op_jsr, (codeStream, frame) -> {
            //jsr指令貌似被抛弃。具体用法虚拟机规范中写道：
            //the jsr instruction was used with the ret instruction in the implementation of the finally clause.
            //To implement the try-finally construct, a compiler for the Java programming language that generates class files with
            //version number 50.0 or below may use the exception-handling facilities together with two special instructions: jsr ("jump to subroutine") and ret ("return from subroutine").
        });

        register(Opcode.op_ret, (codeStream, frame) -> {
            //通常与jsr配合使用
        });

        Function<Frame, Void> paddingSkip = (frame) -> {
            PosDataInputStream codeStream = frame.getfCodeStream();
            //计算相对tableswitch指令需要偏移的padding offset值
            int current = codeStream.getPos();
            if (current % 4 != 0) {
                //4 - (current % 4)计算得到的是相对codeStream.pos的偏移值
                //而我们目标是获得相对tableswitch指令（比codeStream.pos落后1字节）的偏移值，故需要+1
                int offset = 4 - (current % 4) + 1;
                frame.offsetPC(offset);
            }
            return null;
        };

        /**
         * @Description: 当jump table比较紧致时(例如：1,2,4,5)，采取tableswitch指令。jump table中会插入若干对指向default的映射，使得table连续且升序排序。
         * The length of tableswitch is equal to 1 for the opcode
         * + up to 0~3 bytes for padding(保证4字节对齐，即defaultbyte的起始地址与该方法第一个指令的地址相对偏移量为4的倍数)
         * + 4 bytes for the default jump offset
         * + 4 bytes for low + 4 bytes for high
         * + 4 bytes for each jump offset (there are high-low+1 of them).
         */
        register(Opcode.op_tableswitch, (codeStream, frame) -> {
            paddingSkip.apply(frame);
            try {
                Map<Integer, Integer> offsetMap = new HashMap<>();
                int defaultOffset = codeStream.readInt();
                int start = codeStream.readInt();
                int end = codeStream.readInt();
                for (int i = start; i <= end; i++) {
                    offsetMap.put(i, codeStream.readInt());
                }
                int target = frame.popInt();
                if (offsetMap.containsKey(target)) {
                    frame.offsetPC(offsetMap.get(target));
                } else {
                    frame.offsetPC(defaultOffset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        /**
         * @Description: 当jump table key比较疏松时(间隔较大：例如1,100,1000)采取lookupswitch指令，并采用二分法查找key
         * 与tableswitch不同在于采用npairs记录每个映射
         * The length of tableswitch is equal to 1 for the opcode
         * + up to 0~3 bytes for padding
         * + 4 bytes for the default jump offset
         * + 4 bytes for npairs pairs number
         * + each pair (consists of an int match and a signed 32-bit offset).
         */
        register(Opcode.op_lookupswitch, (codeStream, frame) -> {
            paddingSkip.apply(frame);
            try {
                Map<Integer, Integer> offsetMap = new HashMap<>();
                int defaultOffset = codeStream.readInt();
                int tatal = codeStream.readInt();
                for (int i = 0; i < tatal; i++) {
                    int key = codeStream.readInt();
                    int offset = codeStream.readInt();
                    offsetMap.put(key, offset);
                }
                int target = frame.popInt();
                if (offsetMap.containsKey(target)) {
                    frame.offsetPC(offsetMap.get(target));
                } else {
                    frame.offsetPC(defaultOffset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        register(Opcode.op_ireturn, (codeStream, frame) -> {
            int i = frame.popInt();
            frame.getfThread().popFrame();
            frame.getfThread().topFrame().pushInt(i);
        });

        register(Opcode.op_areturn, (codeStream, frame) -> {
            Object obj = frame.popRef();
            frame.getfThread().popFrame();
            frame.getfThread().topFrame().pushRef(obj);
        });

        register(Opcode.op_return, (codeStream, frame) -> {
            frame.getfThread().popFrame();
        });

        register(Opcode.op_getstatic, (codeStream, frame) -> {

        });

        register(Opcode.op_putstatic, (codeStream, frame) -> {

        });

        Function<Boolean, Instruction> createInvokeInst = (isStatic) -> {
            return (codeStream, frame) -> {
                try {
                    //获取指向常量池的下标
//                    int c1 = codeStream.readByte() & 0xff;
//                    int c2 = codeStream.readByte() & 0xff;
//                    int idx = (c1 << 8) + c2;
                    int idx = codeStream.readShort();

                    Class.Symbol symbol = frame.getfClass().resloveMethodByRef(idx);
                    if (symbol != null) {
                        //初始化目标方法所属类
                        initClass(frame.getfThread(), symbol.clazz);
                    }
                    //执行方法
                    invokeMethod(symbol, frame, isStatic);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        };
        register(Opcode.op_invokevirtual, createInvokeInst.apply(false));
        register(Opcode.op_invokespecial, createInvokeInst.apply(false));
        register(Opcode.op_invokestatic, createInvokeInst.apply(true));

    }


    /**
     * @Description: 注册指令集
     * @Param bytecode 字节码
     * @Param inst 指令对象
     * @return: void
     */
    private static void register(int bytecode, Instruction inst) {
        //这里对传入的inst参数再次封装成Instruction对象，目的在于减少代码冗余（抽取日志记录代码），有点类似代理模式
        instructionMap.put(bytecode, (codeStream, frame) -> {
            LOGGER.info("register instruction: " + bytecode);
            inst.exec(codeStream, frame);
        });
    }

    public static Instruction createInstruction(DataInputStream codeStream) {
        int codeByte = 0;
        try {
            codeByte = codeStream.readByte() & 0xff;
            Instruction inst = instructionMap.get(codeByte);
            if (inst == null) {
                throw new RuntimeException("could not found the instruction: " + codeByte);
            }
            return inst;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void initClass(Thread thread, Class clazz) {
        if (clazz.isStaticInited()) {
            //类已经初始化
            return;
        }
        //初始化阶段主要工作：为静态变量赋初始值。即执行<clinit>:()V方法，只在该类首次初始化时执行
        clazz.setStaticInited();
        MethodInfo clinitMethod = clazz.findMethod("<clinit>", "()V");
        if (clinitMethod != null) {
            Frame f = new Frame(thread, clazz, clinitMethod);
            //必须等初始化方法完成后，才能继续执行指令
            thread.runToEnd(f);
            LOGGER.info("OnInitComplete the " + clazz.getClassName());
        }
    }

    private static void invokeMethod(Class.Symbol symbol, Frame frame, boolean isStatic) {
        //如symbol为null，表示调用的是java.lang.Object类方法，暂时不支持,作特殊处理
        if (symbol == null) {
            if (!isStatic) {
                //如果调用的是非静态方法，应该从操作数栈中将存储参数的局部变量弹出
                //TODO 这里处理不当，假如调用java.lang.Object类中的wait(long timeout)方法，应该从操作数栈中弹出两个slot，分别存储this和timeout局部变量
                frame.popSlot();
            }
            return;
        }

        if (callNative(symbol, frame)) {
            return;
        }

        //将参数从当前frame的栈帧中弹出，并存入到新的frame的局部变量表中（注意存储顺序）
        Frame newFrame = new Frame(frame.getfThread(), symbol.clazz, symbol.method);
        if (symbol.argCount > 0) {
            for (int i = symbol.argCount - 1; i >= 0; i--) {
                Slot s = frame.popSlot();
                newFrame.storeSlot(i, s);
            }
        }
        frame.getfThread().pushFrame(newFrame);
    }

    private static boolean callNative(Class.Symbol symbol, Frame frame) {
        MethodInfo method = symbol.method;
        Class clazz = symbol.clazz;
        if ((method.getAccessFlags() & MethodInfo.ACC_NATIVE) != 0) {
            String clazzName = clazz.getClassName();
            String methodName = clazz.getStringFromConstantPool(method.getNameIndex());
            String methodDescription = clazz.getStringFromConstantPool(method.getDescriptorIndex());
            NativeInstructionFactory.invoke(clazzName, methodName, methodDescription, frame);
            return true;
        }
        return false;
    }

    private static Instruction createIConst(int v) {
        return (codeStream, frame) -> {
            frame.pushInt(v);
        };
    }

    private static Instruction createLConst(int v) {
        return (codeStream, frame) -> {

        };
    }

    private static Instruction createALoad(int idx) {
        return (codeStream, frame) -> {
            Object obj = frame.loadRef(idx);
            frame.pushRef(obj);
        };
    }

    private static Instruction createIStore(int idx) {
        return (codeStream, frame) -> {
            int v = frame.popInt();
            frame.storeInt(idx, v);
        };
    }

    private static Instruction createAStore(int idx) {
        return (codeStream, frame) -> {
            Object obj = frame.popRef();
            frame.storeRef(idx, obj);
        };
    }

    private static Instruction createILoad(int idx) {
        return (codeStream, frame) -> {
            int v = frame.loadInt(idx);
            frame.pushInt(v);
        };
    }

    private static Instruction createDup(int copyAmount, int maxDepth) {
        List<Slot> tempLst = new ArrayList<>();
        return (codeStream, frame) -> {
            for (int i = 0; i < copyAmount; i++) {
                tempLst.add(frame.popSlot());
            }
            for (int i = 0; i < maxDepth; i++) {
                tempLst.add(frame.popSlot());
            }
            for (int i = 0; i < copyAmount; i++) {
                tempLst.add(tempLst.get(i));
            }
            for (int i = tempLst.size() - 1; i >= 0; i--) {
                frame.pushSlot(tempLst.get(i));
            }
        };
    }

    private static Instruction createIntConverted(int digitNum, boolean signExtended) {
        return (codeStream, frame) -> {
            int target = frame.popInt();
            int skew = 1 << digitNum - 1;
            int low = target & (1 << digitNum) - 1;
            if (signExtended & (low & skew) != 0) {
                low = (((1 << 32 - digitNum) - 1) << digitNum) + low;
            }
            frame.pushInt(low);
        };
    }
}
