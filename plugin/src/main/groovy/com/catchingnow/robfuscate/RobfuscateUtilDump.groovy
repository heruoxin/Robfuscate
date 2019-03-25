package com.catchingnow.robfuscate

import java.util.*;

import org.objectweb.asm.*;

public class RobfuscateUtilDump implements Opcodes {
    public static final String NAME = "com/catchingnow/base/util/RobfuscateUtil";

    public static byte[] dump() throws Exception {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, NAME, null, "java/lang/Object", null);

        cw.visitSource("RobfuscateUtil.java", null);

            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "MAGIC", "I", null, null);
            fv.visitEnd();
        
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label ll0 = new Label();
            mv.visitLabel(ll0);
            mv.visitLineNumber(7, ll0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            Label ll1 = new Label();
            mv.visitLabel(ll1);
            mv.visitLocalVariable("this", "L"+NAME+";", null, ll0, ll1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "convert", "(I)I", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(12, l0);
            mv.visitVarInsn(ILOAD, 0);
            mv.visitFieldInsn(GETSTATIC, NAME, "MAGIC", "I");
            mv.visitInsn(IXOR);
            mv.visitInsn(IRETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("val", "I", null, l0, l1, 0);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        
                    mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
                    mv.visitCode();
                    Label la0 = new Label();
                    mv.visitLabel(la0);
                    mv.visitLineNumber(9, la0);
                    mv.visitLdcInsn("f86r4y");
                    mv.visitIntInsn(BIPUSH, 36);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;I)I", false);
                    mv.visitInsn(INEG);
                    mv.visitFieldInsn(PUTSTATIC, NAME, "MAGIC", "I");
                    mv.visitInsn(RETURN);
                    mv.visitMaxs(2, 0);
                    mv.visitEnd();
        
        cw.visitEnd();

        return cw.toByteArray();
    }
}
