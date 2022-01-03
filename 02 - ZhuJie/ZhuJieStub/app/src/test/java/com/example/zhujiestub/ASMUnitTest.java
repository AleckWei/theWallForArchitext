package com.example.zhujiestub;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ASM单元测试
 */
public class ASMUnitTest {

    private static final String FilePathInput = "src/test/java/com/example/zhujiestub/InjectTest.class";

    private static final String FilePathOutput = "src/test/java2/com/example/zhujiestub/InjectTest.class";

    @Test
    public void Test() throws IOException {
        File injectTestClassFile = new File(FilePathInput);
        FileInputStream fis = new FileInputStream(injectTestClassFile);

        // 将文件当中的数据，通过文件输入流，读取到内存当中
        // 构造一个byte数组
        // ClassReader内部会将FileInputStream转成byte数组
        // 所以这里就省略将文件转成byte数组的操作
        ClassReader cr = new ClassReader(fis);

        // 有读就有写
        // COMPUTE_FRAMES : 栈帧，自动计算栈帧和局部变量表的大小
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        // 执行分析，过程当中就要对class数据进行修改
        // 传一个ClassVisitor对象，访问者设计模式
        // 要去补一下这个设计模式了。。。
        // EXPAND_FRAMES android 中使用插桩，就用这个参数吧，记牢了
        cr.accept(new MyClassVisitor(Opcodes.ASM7, cw), ClassReader.EXPAND_FRAMES);

        // 执行了插桩之后的字节码数据
        byte[] bytes = cw.toByteArray();
        // 通过FileOutputStream写回到原本的文件
        // 这里为了学习对比，就放到另一个文件夹中
        FileOutputStream fos = new FileOutputStream(FilePathOutput);
        fos.write(bytes);

        // 关闭读写流
        fis.close();
        fos.close();
    }

    /**
     * 处理.class文件，将文件中的分析结果回调到调用的地方
     * 有点像OnClickListener一样
     */
    static class MyClassVisitor extends ClassVisitor {
        public MyClassVisitor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        /**
         * 针对方法的字节码修改，需要在这个方法当中执行
         */
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new MyMethodVisitor(api, methodVisitor, access, name, descriptor);
        }
    }

    /**
     * 这里才能对method的方法体进行插桩
     */
    static class MyMethodVisitor extends AdviceAdapter {
        protected MyMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
            super(api, methodVisitor, access, name, descriptor);
        }

        /**
         * 进入方法时，会回调这个
         * 刚刚进入这个方法
         */
        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            // 插入 long startTime = System.currentTimeMillis();
        }

        /**
         * 要退出这个方法时，会回调这个
         */
        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode);
            // 插入 long endTime = System.currentTimeMillis();
            // System.out.println("execute: " + (endTime - startTime) + "ms.");
        }
    }
}
