package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】抽象类命名使用 Abstract 或 Base 开头；异常类命名使用 Exception 结尾，测试类命名以它要
 * 测试的类的名称开始，以 Test 结尾。
 *
 * @author liuzhihang
 * @date 2021/10/13 20:06
 */
public class ClassNamingInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * LOG
     */
    private static final Logger LOG = Logger.getInstance(ClassNamingInspection.class); 
    
    /**
     * 提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.class.name.message");

    /**
     * 创建用于检查Java元素的访问者。
     *
     * @param holder     用于报告问题的对象。
     * @param isOnTheFly 标记检查是否即时进行。
     * @return 一个Java元素访问者。
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitClass(@NotNull PsiClass psiClass) {
                super.visitClass(psiClass);

                // 跳过接口和注解类型
                if (psiClass.isInterface() || psiClass.isAnnotationType()) return;

                String className = psiClass.getName();

                // 检查抽象类是否以"Abstract"或"Base"开头
                boolean isAbstract = psiClass.hasModifierProperty(PsiModifier.ABSTRACT);
                if (isAbstract && !className.startsWith("Abstract") && !className.startsWith("Base")) {
                    String newName = className.startsWith("Base") ? "Base" + className : "Abstract" + className;
                    holder.registerProblem(psiClass, MESSAGE, new NamingConventionFix(newName));
                }

                // 检查异常类是否以"Exception"结尾
                if (isExceptionClass(psiClass) && !className.endsWith("Exception")) {
                    String newName = className + "Exception";
                    holder.registerProblem(psiClass, MESSAGE, new NamingConventionFix(newName));
                }

                // 检查位于测试源根目录下的类是否以"Test"结尾
                if (isPsiClassInTestSourceRoot(psiClass) && !className.endsWith("Test")) {
                    String newName = className + "Test";
                    holder.registerProblem(psiClass, MESSAGE, new NamingConventionFix(newName));
                }

            }
        };
    }


    /**
     * 检查给定的类是否位于测试源根目录下。
     *
     * @param psiClass 要检查的类。
     * @return 类在测试源根目录下则返回true，否则返回false。
     */
    private boolean isPsiClassInTestSourceRoot(PsiClass psiClass) {
        VirtualFile virtualFile = psiClass.getContainingFile().getVirtualFile();
        if (virtualFile == null) return false;

        Module module = ModuleUtilCore.findModuleForPsiElement(psiClass);
        if (module == null) return false;

        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        for (ContentEntry contentEntry : moduleRootManager.getContentEntries()) {
            for (SourceFolder sourceFolder : contentEntry.getSourceFolders()) {
                if (sourceFolder.getFile() != null && sourceFolder.getFile().equals(virtualFile.getParent())) {
                    // 直接判断是否为测试源根
                    return sourceFolder.isTestSource();
                }
            }
        }

        return false;
    }

    /**
     * 检查给定的类是否是异常类。
     *
     * @param psiClass 要检查的类。
     * @return 如果类是异常类则返回true，否则返回false。
     */
    private boolean isExceptionClass(PsiClass psiClass) {
        // Example: Check if the class extends java.lang.Exception or java.lang.RuntimeException
        PsiClassType[] supers = psiClass.getSuperTypes();
        for (PsiClassType superType : supers) {
            PsiClass superClass = superType.resolve();
            if (superClass != null && "java.lang.Exception".equals(superClass.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 一个用于修复命名规范问题的本地快速修复类。
     */
    private static class NamingConventionFix implements LocalQuickFix {
        /**
         * 新的命名
         */
        private final String newName;

        /**
         * 构造函数。
         *
         * @param newName 新的命名。
         */
        NamingConventionFix(String newName) {
            this.newName = newName;
        }

        /**
         * 获取该快速修复的家族名称。
         *
         * @return 快速修复的家族名称字符串。
         */
        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            // 返回该修复操作的展示名称
            return CodeInspectionsBundle.message("ali.p3c.name.class.name.fix", newName);
        }

        /**
         * 应用该快速修复操作。
         *
         * @param project    当前项目实例。
         * @param descriptor 描述问题的指标。
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            // 检查问题元素是否为类，如果是，则重命名
            if (element instanceof PsiClass psiClass) {
                // 在写入操作中执行类名和文件重命名
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    // 修改类名
                    psiClass.setName(newName);
                });
            }
        }
    }

}
