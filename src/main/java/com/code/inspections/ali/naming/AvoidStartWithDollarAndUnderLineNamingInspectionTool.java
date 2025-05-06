/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.code.inspections.ali.naming;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.refactoring.rename.RenameProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * [Mandatory] All names should not start or end with an underline or a dollar sign.
 * <p>
 * [强制] 所有名称都不应以下划线或美元符号开头或结尾。
 *
 * @author liuzhihang
 * @version AvoidStartWithDollarAndUnderLineNamingInspectionTool.java, v 0.1 2025年04月29日 11:33 liuzhihang
 */
public class AvoidStartWithDollarAndUnderLineNamingInspectionTool extends AbstractBaseJavaLocalInspectionTool {

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
        // [强制] 所有名称都不应以下划线或美元符号开头或结尾。
        return new JavaElementVisitor() {
            @Override
            public void visitClass(@NotNull PsiClass aClass) {
                super.visitClass(aClass);
                checkNaming(aClass.getName(), aClass, holder);
            }

            @Override
            public void visitMethod(@NotNull PsiMethod method) {
                super.visitMethod(method);
                checkNaming(method.getName(), method, holder);
            }

            @Override
            public void visitVariable(@NotNull PsiVariable variable) {
                super.visitVariable(variable);
                checkNaming(variable.getName(), variable, holder);
            }
        };
    }

    /**
     * 检查标识符是否符合命名规范。
     * 规范要求：标识符不应以下划线或美元符号开头或结尾。
     *
     * @param name    标识符名称
     * @param element 当前 PSI 元素
     * @param holder  用于报告问题的持有者对象
     */
    private void checkNaming(String name, PsiNamedElement element, ProblemsHolder holder) {
        if (name == null || name.isEmpty()) {
            return;
        }

        if (name.startsWith("_") || name.startsWith("$") || name.endsWith("_") || name.endsWith("$")) {
            holder.registerProblem(
                    element,
                    CodeInspectionsBundle.message("ali.p3c.naming.AvoidStartWithDollarAndUnderLineNamingInspectionTool.message", name),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    new QuickFix()
            );
        }
    }

    /**
     * 快速修复类，用于修复命名问题。
     *
     * @author liuzhihang
     * @version AvoidStartWithDollarAndUnderLineNamingInspectionTool.java, v 0.1 2025/4/29 liuzhihang
     */
    private static class QuickFix implements LocalQuickFix {

        /**
         * 获取修复建议的描述信息
         *
         * @return 修复建议的描述信息
         */
        @NotNull
        @Override
        public String getFamilyName() {
            return CodeInspectionsBundle.message("ali.p3c.naming.AvoidStartWithDollarAndUnderLineNamingInspectionTool.fix");
        }

        /**
         * 应用修复建议
         *
         * @param project    当前项目
         * @param descriptor 问题描述符
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            if (!(element instanceof PsiNamedElement namedElement)) {
                return;
            }
            String oldName = namedElement.getName();
            if (oldName == null || oldName.isEmpty()) {
                return;
            }

            String newName = processName(oldName);
            if (newName.isEmpty() || newName.equals(oldName)) {
                return;
            }

            if (!isValidJavaIdentifier(newName)) {
                return;
            }

            // 使用重构处理器重命名元素
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                // 使用写操作锁确保线程安全
                WriteCommandAction.runWriteCommandAction(project, () -> new RenameProcessor(project, element, newName, false, false).run());
            });
        }

        /**
         * 处理原始名称，去除首尾的非法字符
         * 此方法旨在移除给定字符串首尾的下划线(_)和美元符号($)，
         * 直到遇到第一个合法字符为止。如果字符串为空或全部由非法字符组成，
         * 则返回空字符串。
         *
         * @param name 待处理的原始名称字符串
         * @return 处理后的字符串，首尾非法字符被移除
         */
        private String processName(String name) {
            // 从字符串首部开始，查找第一个合法字符的索引
            int start = 0;
            while (start < name.length() && (name.charAt(start) == '_' || name.charAt(start) == '$')) {
                start++;
            }

            // 从字符串尾部开始，查找最后一个合法字符的索引
            int end = name.length() - 1;
            while (end >= start && (name.charAt(end) == '_' || name.charAt(end) == '$')) {
                end--;
            }

            // 根据start和end的值，判断并返回处理后的字符串
            return (start > end) ? "" : name.substring(start, end + 1);
        }

        /**
         * 检查是否为有效的Java标识符
         * <p>
         * 此方法用于判断一个字符串是否可以作为Java中的标识符，例如变量名、方法名或类名
         * 它首先检查字符串是否为空，然后检查字符串的首字符是否是Java标识符的有效起始字符
         * 最后，检查字符串中剩余部分的每个字符是否是Java标识符的有效组成部分
         *
         * @param name 待检查的字符串
         * @return 如果字符串是有效的Java标识符，则返回true；否则返回false
         */
        private boolean isValidJavaIdentifier(String name) {
            // 检查字符串是否为空，空字符串不是有效的Java标识符
            if (name.isEmpty()) {
                return false;
            }
            // 检查字符串的首字符是否是Java标识符的有效起始字符
            if (!Character.isJavaIdentifierStart(name.charAt(0))) {
                return false;
            }
            // 检查字符串中剩余部分的每个字符是否是Java标识符的有效组成部分
            for (int i = 1; i < name.length(); i++) {
                if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                    return false;
                }
            }
            // 如果所有检查都通过，则字符串是有效的Java标识符
            return true;
        }
    }
}