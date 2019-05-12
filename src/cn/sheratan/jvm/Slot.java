package cn.sheratan.jvm;

/**
 * @Description: 局部变量存储单位
 * @Author aries
 * @Data 2019-03-12 11:16
 */
public class Slot {

    /**
     * @Description: 定义Slot存储类型，目前只支持引用类型和int类型（包括short，byte）
     */
    public enum Type{ERF, NUM}

    /**
     * @Description: 存储对象
     */
    private Object obj;

    private Type type;

    public Slot(int n){
        this.obj = n;
        this.type = Type.NUM;
    }

    public Slot(Object obj){
        this.obj = obj;
        this.type = Type.ERF;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    @Override
    public String toString() {
        return type == Type.NUM ? String.valueOf(obj) : obj.toString();
    }
}
