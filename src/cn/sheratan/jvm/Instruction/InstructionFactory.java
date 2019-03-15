package cn.sheratan.jvm.Instruction;

import cn.sheratan.jvm.Frame;
import org.freeinternals.format.classfile.Opcode;
import org.freeinternals.format.classfile.PosDataInputStream;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @Description: 指令工厂
 * @Author aries
 * @Data 2019-03-12 15:35
 */
public class InstructionFactory {
    static {
        initializion();
    }

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

    /**
     * @Description: 初始化指令
     * @return: void
     */
    private static void initializion() {
        register(Opcode.op_iconst_m1, createIConst(-1));
        register(Opcode.op_iconst_0, createIConst(0));
        register(Opcode.op_iconst_1, createIConst(1));
        register(Opcode.op_iconst_2, createIConst(2));
        register(Opcode.op_iconst_3, createIConst(3));
        register(Opcode.op_iconst_4, createIConst(4));
        register(Opcode.op_iconst_5, createIConst(5));

        register(Opcode.op_istore_0, createIStore(0));
        register(Opcode.op_istore_1, createIStore(1));
        register(Opcode.op_istore_2, createIStore(2));
        register(Opcode.op_istore_3, createIStore(3));

        register(Opcode.op_iload_0, createILoad(0));
        register(Opcode.op_iload_1, createILoad(1));
        register(Opcode.op_iload_2, createILoad(2));
        register(Opcode.op_iload_3, createILoad(3));

        register(Opcode.op_iadd, (codeStream, frame) -> {
            int i1 = frame.popInt();
            int i2 = frame.popInt();
            frame.pushInt(i1 + i2);
        });

        register(Opcode.op_return, (codeStream, frame) -> {
            frame.getfThread().popFrame();
        });
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

    private static Instruction createIConst(int v) {
        return (codeStream, frame) -> {
            frame.pushInt(v);
        };
    }

    private static Instruction createIStore(int idx) {
        return (codeStream, frame) -> {
            int v = frame.popInt();
            frame.storeInt(v, idx);
        };
    }

    private static Instruction createILoad(int idx) {
        return (codeStream, frame) -> {
            frame.loadInt(idx);
        };
    }
}
