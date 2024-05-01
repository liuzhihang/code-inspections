package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】所有编程相关的命名均不能以下划线或美元符号开始，也不能以下划线或美元符号结束。
 * 反例：_name / __name / $Object / name_ / name$ / Object$
 *
 * @author liuzhihang
 * @version NamingConventionInspection.java, v 0.1 2024年04月30日 20:16 liuzhihang
 */
public class NamingConventionInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 命名不符合规范的问题提示信息
     */
    public static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.convention.message");
    /**
     * 命名不符合规范的问题修复提示信息
     */
    public static final String QUICK_FIX_NAME = CodeInspectionsBundle.message("ali.p3c.name.convention.fix");

    /**
     * 构建并返回一个访问者对象，用于检查Java标识符是否符合命名规范。
     * <p>
     *
     * @param holder     用于收集和报告问题的问题持有者对象。
     * @param isOnTheFly 表示是否是在飞行模式下进行检查，即编辑器中实时进行的检查。
     * @return 返回一个实现了Java元素访问者接口的对象，用于访问和检查Java标识符。
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        // 创建一个Java元素访问者，重访标识符以检查其是否符合命名约定
        return new JavaElementVisitor() {
            /**
             * 访问一个标识符元素，检查其名称是否以下划线或美元符号开头或结尾，
             * 如果是，则登记一个命名不符合规范的问题。
             * <p>
             * @param identifier 需要被访问的标识符元素。
             */
            @Override
            public void visitIdentifier(@NotNull PsiIdentifier identifier) {
                super.visitIdentifier(identifier);
                String name = identifier.getText();
                // 检查标识符名称是否以特定字符开头或结尾，若是，则登记一个错误
                if (name.startsWith("_") || name.endsWith("_") || name.startsWith("$") || name.endsWith("$")) {
                    holder.registerProblem(identifier, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new NamingConventionFix());
                }
            }
        };
    }

    /**
     * 单一内部类实现LocalQuickFix，尝试同时移除标识符的首尾下划线或美元符号。
     * 注意：此实现可能不是最高效或最精确的，因为每次应用都会尝试两端的修正。
     */
    private static class NamingConventionFix implements LocalQuickFix {

        /**
         * 返回一个字符串，表示此快速修复的名称。
         *
         * @return 返回一个字符串，表示此快速修复的名称。
         */
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX_NAME;
        }

        /**
         * 应用此快速修复，尝试移除标识符的首尾下划线或美元符号。
         *
         * @param project      当前项目的实例。
         * @param descriptor   问题描述符，包含问题的相关信息。
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            if (element instanceof PsiIdentifier identifier) {
                String originalName = identifier.getText();
                String correctedName = originalName.replaceAll("^[_$]", "").replaceAll("[_$]$", "");
                if (!originalName.equals(correctedName)) {
                    PsiElementFactory factory = PsiElementFactory.getInstance(project);
                    PsiIdentifier newIdentifier = factory.createIdentifier(correctedName);
                    identifier.replace(newIdentifier);
                }
            }
        }
    }

}
