package cn.sheratan.jvm;

import org.freeinternals.format.FileFormatException;
import org.freeinternals.format.classfile.ClassFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @Description: classpath entity
 * @Author aries
 * @Data 2019-03-08 11:33
 */
public class ClassPath {
    private static final Logger LOGGER = Logger.getLogger(ClassPath.class.getName());

    /**
     * @Description: 保存类文件路径列表
     */
    private String[] cpLst;

    public ClassPath(String[] classpathLst) {

    }

    public ClassFile loadClassFile(String fullName) {
        for (String cp : cpLst) {
            ClassFile cf = loadClassFile(cp, fullName);
            if (cf != null) {
                LOGGER.info("success loaded classfile:" + fullName + " from " + cp);
                return cf;
            }
        }
        return null;
    }

    private ClassFile loadClassFile(String path, String fullName) {
        String className = path + "/" + fullName + ".class";
        File file = new File(className);
        if(file == null){
            LOGGER.fine("could not found the file " + className);
            return null;
        }
        FileInputStream fis = null;
        byte[] data = new byte[(int)file.length()];

        try {
            fis = new FileInputStream(file);
            fis.read(data);
            return new ClassFile(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileFormatException e) {
            e.printStackTrace();
        }finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
