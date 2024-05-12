package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * POJO 类中的任何布尔类型的变量，都不要加 is 前缀，否则部分框架解析会引起序列化错误。
 *
 * @author liuzhihang
 * @version BooleanVariableNamingInspection.java, v 0.1 2024/5/3 liuzhihang
 */
public class BooleanVariableNamingInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 错误提示
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.boolean.variable.message");
    /**
     * 快速修复
     */
    private static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.name.boolean.variable.fix");

    /**
     * 创建一个访客对象，用于遍历Java文件中的元素并执行检查。
     *
     * @param holder     用于存储问题描述的对象。
     * @param isOnTheFly 表示检查是否在飞行模式下进行。
     * @return 返回一个PsiElementVisitor对象，该对象会访问代码中的字段并执行命名检查。
     */
    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            /**
             * 访问字段，并检查是否为布尔类型、非静态且非常量，
             * 如果是以"is"开头，会注册一个问题。
             *
             * @param field 当前访问的字段。
             */
            @Override
            public void visitField(@NotNull PsiField field) {
                super.visitField(field);
                // 忽略静态字段和常量
                if (field.hasModifierProperty(PsiModifier.STATIC) || field.hasModifierProperty(PsiModifier.FINAL)) return;
                PsiType fieldType = field.getType();
                // 检查字段类型是否为布尔类型
                if ("boolean".equals(fieldType.getCanonicalText()) || fieldType.equalsToText("java.lang.Boolean")) {
                    String fieldName = field.getName();
                    // 检查字段名是否以"is"开头，并且长度大于2，且第三个字符是大写字母
                    if (fieldName.startsWith("is") && fieldName.length() > 2 && Character.isUpperCase(fieldName.charAt(2))) {
                        holder.registerProblem(field, MESSAGE, new RemoveIsPrefixQuickFix());
                    }
                }
            }
        };
    }

    /**
     * 提供一个快速修复方案，用于移除字段名前的"is"前缀。
     */
    private static class RemoveIsPrefixQuickFix implements LocalQuickFix {
        /**
         * 返回该快速修复的显示名称。
         *
         * @return 修复操作的名称。
         */
        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 应用快速修复，即通过移除字段名中的"is"前缀来修复问题。
         *
         * @param project    当前项目对象。
         * @param descriptor 描述问题的对象。
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiField field = (PsiField) descriptor.getPsiElement();
            String currentName = field.getName();
            String newName = currentName.startsWith("is") ? "has" + currentName.substring(2) : currentName;

            field.setName(newName);
        }
    }
}
