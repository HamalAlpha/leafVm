package cn.sheratan.jvm.Instruction;

import cn.sheratan.jvm.Frame;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @Description: 本地方法指令处理
 * @Author aries
 * @Data 2019-03-18 16:27
 */
public class NativeInstructionFactory {
    private static final Logger LOGGER = Logger.getLogger(NativeInstructionFactory.class.getName());
    private static final String SYSTEM_CLASS = "java/lang/System";
    private static final String STRING_CLASS = "java/lang/String";

    private interface NativeMethod{
        void exec(Frame frame);
    }

    /**
     * @Description: 存储本地方法
     */
    private static final HashMap<String, NativeMethod> nativeMethodMap;

    static{
        nativeMethodMap = new HashMap<>();
        registerNativeMethod();
    }

    private static void registerNativeMethod() {
        nativeMethodMap.put(keyProduce(SYSTEM_CLASS, "println", "(Ljava/lang/String;)V"), (frame) ->{
            String s = (String)frame.popRef();
            System.out.println(s);
        });

        nativeMethodMap.put(keyProduce(STRING_CLASS, "valueOf", "(I)Ljava/lang/String;"), (frame) ->{
            int v = frame.popInt();
            String str = String.valueOf(v);
            frame.pushRef(str);
        });
    }

    public static void invoke(String clazzName, String methodName, String methodDescription, Frame frame){
        String key = keyProduce(clazzName, methodName, methodDescription);
        NativeMethod nativeMethod = nativeMethodMap.get(key);
        if(nativeMethod == null){
            throw new RuntimeException("not found the native method : " + key);
        }
        LOGGER.info("call the native method : " + key);
        nativeMethod.exec(frame);
    }

    private static String keyProduce(String clazzName, String methodName, String methodDescription){
        return clazzName + "@" + methodName + "@" + methodDescription;
    }
}
