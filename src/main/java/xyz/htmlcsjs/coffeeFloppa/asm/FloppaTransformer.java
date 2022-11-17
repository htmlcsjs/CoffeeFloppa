package xyz.htmlcsjs.coffeeFloppa.asm;

import org.objectweb.asm.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FloppaTransformer implements Opcodes{
    private static final String TRANSFORM_CLASS = "gregtech.api.util.GTUtility";
    private static final List<String> IGNORE_METHODS = List.of("getTierByVoltage@(J)B", "<init>@()V");

    public static byte[] transform(ClassLoader loader, String className, byte[] classfileBuffer) {
        if(!TRANSFORM_CLASS.equals(className)) {
            return classfileBuffer;
        }

        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, 0);

        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (IGNORE_METHODS.contains(name + "@" + desc)) {
                    return super.visitMethod(access, name, desc, signature, exceptions);
                } else if (name.equals("<clinit>") && desc.equals("()V")) {
                    return new GTValCLInit(super.visitMethod(access, name, desc, signature, exceptions));
                }
                return null;
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                if (name.equals("tierByVoltage")) {
                    return super.visitField(access, name, descriptor, signature, value);
                }
                return null;
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {

            }
        }, 0);

        try {
            Files.write(Paths.get(className + ".class").toAbsolutePath(), cw.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FloppaLauncher.logger.info(String.format("Transformed %s", className));
        return cw.toByteArray();
    }

    private static class GTValCLInit extends MethodVisitor {
        private final MethodVisitor mv;

        protected GTValCLInit(MethodVisitor mv) {
            super(ASM9, null);
            this.mv = mv;
        }

        @Override
        public void visitCode() {
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/util/TreeMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/TreeMap", "<init>", "()V", false);
            mv.visitFieldInsn(PUTSTATIC, "gregtech/api/util/GTUtility", "tierByVoltage", "Ljava/util/NavigableMap;");
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, 0);
            Label label2 = new Label();
            mv.visitLabel(label2);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{Opcodes.INTEGER}, 0, null);
            mv.visitVarInsn(ILOAD, 0);
            mv.visitFieldInsn(GETSTATIC, "gregtech/api/GTValues", "V", "[J");
            mv.visitInsn(ARRAYLENGTH);
            Label label3 = new Label();
            mv.visitJumpInsn(IF_ICMPGE, label3);
            mv.visitFieldInsn(GETSTATIC, "gregtech/api/util/GTUtility", "tierByVoltage", "Ljava/util/NavigableMap;");
            mv.visitFieldInsn(GETSTATIC, "gregtech/api/GTValues", "V", "[J");
            mv.visitVarInsn(ILOAD, 0);
            mv.visitInsn(LALOAD);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            mv.visitVarInsn(ILOAD, 0);
            mv.visitInsn(I2B);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/NavigableMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(POP);
            mv.visitIincInsn(0, 1);
            mv.visitJumpInsn(GOTO, label2);
            mv.visitLabel(label3);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitInsn(RETURN);
            mv.visitLocalVariable("i", "I", null, label2, label3, 0);
            mv.visitMaxs(3, 1);
            mv.visitEnd();
        }
    }
}
