package com.catchingnow.robfuscate

import org.gradle.api.Project
import org.objectweb.asm.*
import org.objectweb.asm.commons.Method

class RoInject {

    private static final int MAGIC = 0xc91e8d1e;

    public static void injectDir(Project project, String path) {
        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                //确保当前文件是 R.class 文件
                if (file.absolutePath.matches(".*([/\\\\])R(\\\$[a-z]*)?\\.class")) {
                    // 第一次扫描，记录下所有的 id
                    def scanResult = scanFirst(file)
                    // 第二次扫描，删方法并生成 static 代码块
                    scanSecond(file, scanResult)
                }
            }
        }
    }

    private static Set<SavedIntField> scanFirst(File file) {
        def is = file.newInputStream()

        ClassReader cr = new ClassReader(is);
        ReadClassVisitor reader = new ReadClassVisitor();
        cr.accept(reader, ClassReader.SKIP_FRAMES);

        is.close()
        return reader.getFieldSet()
    }

    private static void scanSecond(File file, Set<SavedIntField> fieldSet) {
        def file1 = new File(file.absolutePath + 1)
        def is = file.newInputStream()
        def os = file1.newOutputStream()

        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new WriteClassVisitor(fieldSet, cw);
        cr.accept(cv, ClassReader.SKIP_FRAMES);

        def bytes = cw.toByteArray()
        os.write(bytes)
        is.close()
        os.close()

        if (file.exists()) {
            file.delete()
            file1.renameTo(file)
        }

    }

    static class ReadClassVisitor extends ClassVisitor {
        String className;
        Set<SavedIntField> mFieldSet = new HashSet<>()

        ReadClassVisitor() {
            super(Opcodes.ASM5, null)
        }

        public Set<SavedIntField> getFieldSet() {
            return mFieldSet
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.className = name;
        }

        @Override
        FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if ("I".equals(desc) && value != null) {
                def field = new SavedIntField()
                field.className = this.className
                field.access = access
                field.name = name
                field.desc = desc
                field.signature = signature
                field.value = Integer.valueOf(value)
                mFieldSet.add(field)
            }
            return super.visitField(access, name, desc, signature, value);
        }

    }

    static class WriteClassVisitor extends ClassVisitor {
        private static STATIC_INITIALIZER_METHOD = new Method("<clinit>", Type.VOID_TYPE, new Type[0])

        String className;
        ClassWriter cw;
        Set<SavedIntField> mFieldSet = new HashSet<>()
        private boolean hasCalledInitializerMethod = false

        WriteClassVisitor(Set<SavedIntField> fieldSet, ClassWriter cw) {
            super(Opcodes.ASM5, cw)
            this.mFieldSet = fieldSet
            this.cw = cw
            hasCalledInitializerMethod = mFieldSet.size() == 0
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.className = name;
        }

        @Override
        FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if ("I".equals(desc) && value != null) {
                return super.visitField(access, name, desc, null, null);
            } else {
                return super.visitField(access, name, desc, signature, value);
            }
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            def mv = super.visitMethod(access, name, desc, signature, exceptions)
            if (STATIC_INITIALIZER_METHOD.name.equals(name)) {
                if (hasCalledInitializerMethod) return
                hasCalledInitializerMethod = true
                return new StaticBlockMethodVisitor(mv)
            }
            return mv
        }

        @Override
        void visitEnd() {
            if (!hasCalledInitializerMethod) {
                hasCalledInitializerMethod = true
                MethodVisitor mv = super.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv = new StaticBlockMethodVisitor(mv);
                mv.visitCode();
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
            super.visitEnd()
        }


        class StaticBlockMethodVisitor extends MethodVisitor {
            StaticBlockMethodVisitor(MethodVisitor mv) {
                super(Opcodes.ASM5, mv);
            }

            @Override
            public void visitCode() {
                super.visitCode();

                for (def f in mFieldSet) {
                    def mixVal = f.value ^ MAGIC

                    Label l0 = new Label();
                    mv.visitLabel(l0);
                    mv.visitLdcInsn(new Integer(mixVal));
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/catchingnow/base/util/RobfuscateUtil", "convert", "(I)I", false);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, f.name, "I");
                }
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                super.visitMaxs(Math.max(1, maxStack), maxLocals)
            }
        }
    }

}
