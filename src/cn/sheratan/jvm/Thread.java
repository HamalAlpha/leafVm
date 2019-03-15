package cn.sheratan.jvm;

import org.freeinternals.format.classfile.MethodInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @Description: 模拟执行线程，利用栈结构依次压入弹出栈帧
 * @Author aries
 * @Data 2019-03-11 17:00
 */
public class Thread {
    private static final Logger LOGGER = Logger.getLogger(Thread.class.getName());

    /**
     * @Description: 维护一个栈帧队列
     */
    private List<Frame> frameList;

    public Thread() {
        this.frameList = new LinkedList<>();
    }

    public void run(Class clazz, MethodInfo method) {
        Frame frame = new Frame(this, clazz, method);
        pushFrame(frame);
        while (!frameList.isEmpty()) {
            frame.run();
        }
    }

    private void pushFrame(Frame f) {
        LOGGER.info("push the " + f.toString());
        frameList.add(f);
    }

    private void popFrame() {
        Frame f = frameList.remove(frameList.size() - 1);
        LOGGER.info("pop the " + f.toString());
    }

    private Frame topFrame() {
        return frameList == null || frameList.isEmpty() ? null : frameList.get(frameList.size() - 1);
    }
}
