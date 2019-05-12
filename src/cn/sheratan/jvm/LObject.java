package cn.sheratan.jvm;

import org.freeinternals.format.classfile.FieldInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description the representation of the Object
 * @Author aries
 * @Data 2019-04-29 16:10
 */
public final class LObject {
    /**
     * @Description: 指向对象所属class
     */
    private final Class lClazz;

    /**
     * @Description: 实例字段
     */
    private final Map<String, Object> insFields;

    LObject(Class clazz) {
        lClazz = clazz;
        insFields = new HashMap<>();
        initialize();
    }

    private void initialize() {
        for (FieldInfo fieldInfo : lClazz.getClassFile().getFields()) {
            //存入非static修饰的成员变量
            if ((fieldInfo.getAccessFlags() & FieldInfo.ACC_STATIC) == 0) {
                insFields.put(lClazz.getStringFromConstantPool(fieldInfo.getNameIndex()), null);
            }
        }
    }

    public void setFieldValue(String key, java.lang.Object val) {
        if (insFields.containsKey(key)) {
            insFields.put(key, val);
        } else {
            throw new RuntimeException("not exist the field name :" + key);
        }
    }

    public Object getFieldValue(String key) {
        if (insFields.containsKey(key)) {
            return insFields.get(key);
        } else {
            throw new RuntimeException("not exist the field name :" + key);
        }
    }

    public Class getlClazz() {
        return lClazz;
    }
}
