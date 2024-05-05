package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 枚举类名带上 Enum 后缀，枚举成员名称需要全大写，单词间用下划线隔开。
 *
 * @author liuzhihang
 * @version EnumNamingInspection.java, v 0.1 2024/5/4 liuzhihang
 */
public class EnumNamingInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 日志
     */
    private final static Logger LOG = Logger.getInstance(EnumNamingInspection.class);

    /**
     * 枚举类名带上 Enum 后缀，枚举成员名称需要全大写，单词间用下划线隔开。
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.enum.naming.message");

    /**
     * 修复
     */
    private static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.name.enum.naming.fix");


    /**
     * 构建一个访问者对象，用于检查Java代码中的枚举类型和枚举常量是否符合命名规范。
     *
     * @param holder     用于存储检查问题的对象。
     * @param isOnTheFly 表示检查是否在飞行模式下进行，即实时编码时。
     * @return 返回一个Java元素访问者对象，该对象会访问类元素并检查枚举命名。
     */
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        // 创建一个Java元素访问者，专门检查类元素是否为枚举，并对枚举及其常量进行命名规范检查
        return new JavaElementVisitor() {
            @Override
            public void visitClass(@NotNull PsiClass psiClass) {
                // 检查当前访问的类是否为枚举
                if (psiClass.isEnum()) {
                    // 检查枚举类名是否以"Enum"结尾
                    if (!psiClass.getName().endsWith("Enum")) {
                        holder.registerProblem(psiClass, MESSAGE, ProblemHighlightType.ERROR, new AppendEnumSuffixFix());
                    }
                    // 遍历枚举中的所有字段，即枚举常量
                    for (PsiField field : psiClass.getFields()) {
                        // 仅处理枚举常量
                        if (field instanceof PsiEnumConstant) {
                            String fieldName = field.getName();
                            // 检查枚举常量名是否符合大写字母和下划线的模式
                            if (!fieldName.matches("^[A-Z_]+$")) {
                                holder.registerProblem(field, MESSAGE, ProblemHighlightType.ERROR, new FixEnumConstantNameFix(fieldName));
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * 用于修正枚举类名缺少"Enum"后缀的问题的本地快速修复类。
     */
    private static class AppendEnumSuffixFix implements LocalQuickFix {
        /**
         * 获取快速修复的家族名称。
         *
         * @return 返回该修复操作的家族名称。
         */
        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 应用修复操作，即将枚举类名加上缺少的"Enum"后缀。
         *
         * @param project    当前项目对象。
         * @param descriptor 描述问题的对象。
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            // 获取问题对应的元素，若为枚举类，则修改其名称
            PsiElement element = descriptor.getPsiElement();
            if (element instanceof PsiClass psiClass) {
                // 准备新类名
                String newName = psiClass.getName() + "Enum";

                // 在写入操作中执行类名和文件重命名
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    // 修改类名
                    psiClass.setName(newName);
                });
            }
        }

    }

    /**
     * 用于修正枚举常量名不符合规范的本地快速修复类。
     */
    private static class FixEnumConstantNameFix implements LocalQuickFix {

        /**
         * 需要修正为的正确枚举常量名。
         */
        private final String fieldName;

        /**
         * 构造函数，初始化需要修正的正确枚举常量名。
         *
         * @param fieldName 需要修正为的正确枚举常量名。
         */
        public FixEnumConstantNameFix(String fieldName) {
            this.fieldName = fieldName;
        }

        /**
         * 获取该快速修复操作的家族名称。
         *
         * @return 返回该修复操作的家族名称。
         */
        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 应用修复操作，即将枚举常量名修改为正确的名称。
         *
         * @param project    当前项目对象。
         * @param descriptor 描述问题的对象。
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            // 获取问题对应的元素，若为枚举常量，则修改其名称
            PsiElement element = descriptor.getPsiElement();
            if (element instanceof PsiField field) {
                PsiElement identifier = field.getNameIdentifier();
                PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

                PsiElement newIdentifier = factory.createIdentifier(fieldName.toUpperCase());

                identifier.replace(newIdentifier);
            }
        }

    }
}
