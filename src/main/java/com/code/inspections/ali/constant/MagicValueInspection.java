package com.code.inspections.ali.constant;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】不允许任何魔法值（即未经预先定义的常量）直接出现在代码中
 *
 * @author liuzhihang
 * @version MagicValueInspection.java, v 0.1 2024/5/4 liuzhihang
 */
public class MagicValueInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.constant.magic.value.message");

    /**
     * 快速修复
     */
    private static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.constant.magic.value.fix");

    /**
     * 构建并返回一个PsiElementVisitor，用于访问Java元素并检测字面量表达式是否可替换为静态常量。
     *
     * @param holder     用于报告问题的ProblemsHolder对象。
     * @param isOnTheFly 表示是否在飞行模式下执行检查。
     * @return 返回一个Java元素访问者，它将访问字面量表达式并检查是否应该被替换为静态常量。
     */
    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            /**
             * 访问字面量表达式，检查是否符合条件被替换为静态常量。
             *
             * @param expression 被访问的字面量表达式。
             */
            @Override
            public void visitLiteralExpression(@NotNull PsiLiteralExpression expression) {
                PsiType type = expression.getType();
                // 仅处理原始类型或字符串类型的字面量
                if (type instanceof PsiPrimitiveType || "java.lang.String".equals(type.getCanonicalText())) {
                    PsiElement parent = expression.getParent();

                    // 检查字面量是否在比较表达式中，若是则忽略
                    if (isInComparisonExpression(expression)) {
                        return;
                    }

                    // 检查字面量是否直接赋值给局部变量
                    if (parent instanceof PsiVariable variable) {
                        if (variable instanceof PsiLocalVariable) {
                            holder.registerProblem(expression, MESSAGE, new ReplaceWithStaticConstantQuickFix(expression));
                        }
                    } else {
                        // 若不是直接赋值给局部变量，尝试在方法或lambda表达式内查找合适的静态常量定义位置
                        PsiElement codeBlock = PsiTreeUtil.getParentOfType(expression, PsiCodeBlock.class);
                        if (codeBlock != null) {
                            PsiElement methodOrLambda = PsiTreeUtil.getParentOfType(codeBlock, PsiMethod.class, PsiLambdaExpression.class);
                            if (methodOrLambda != null) {
                                // 在方法或lambda表达式内，但排除在方法参数列表中的赋值情况
                                if (methodOrLambda instanceof PsiMethod method) {
                                    if (!PsiTreeUtil.isAncestor(method.getParameterList(), expression, true)) {
                                        holder.registerProblem(expression, MESSAGE, new ReplaceWithStaticConstantQuickFix(expression));
                                    }
                                } else if (methodOrLambda instanceof PsiLambdaExpression) {
                                    holder.registerProblem(expression, MESSAGE, new ReplaceWithStaticConstantQuickFix(expression));
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * 检查给定的字面量表达式是否在比较表达式或条件表达式中。
     *
     * @param expression 要检查的字面量表达式。
     * @return 如果字面量表达式在比较或条件表达式中，则返回true；否则返回false。
     */
    private boolean isInComparisonExpression(PsiLiteralExpression expression) {
        PsiElement current = expression.getParent();
        while (current != null) {
            if (current instanceof PsiBinaryExpression || current instanceof PsiConditionalExpression) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * 提供一个快速修复，将字面量替换为静态常量。
     */
    private static class ReplaceWithStaticConstantQuickFix implements LocalQuickFix {
        private final PsiLiteralExpression literalExpression;

        ReplaceWithStaticConstantQuickFix(PsiLiteralExpression literalExpression) {
            this.literalExpression = literalExpression;
        }

        /**
         * 返回快速修复的家族名称。
         *
         * @return 快速修复的家族名称。
         */
        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 应用快速修复，将字面量替换为静态常量。
         *
         * @param project 当前项目。
         * @param descriptor 问题描述符。
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiLiteralExpression expr = (PsiLiteralExpression) descriptor.getPsiElement();
            String fieldType = expr.getType().getCanonicalText();
            PsiClass containingClass = PsiTreeUtil.getParentOfType(expr, PsiClass.class, true);
            if (containingClass != null) {
                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
                // 创建新的静态常量字段并插入到类中
                String value = expr.getText().replaceAll("\"", "");
                String constantName = "CONST_" + value.replaceAll("\\W", "_").toUpperCase();
                PsiElement firstMember = containingClass.getLBrace();
                PsiElement current = firstMember.getNextSibling();
                String constRefText = containingClass.getQualifiedName() + "." + constantName;
                PsiField newField = factory.createFieldFromText("private static final " + fieldType + " " + constantName + " = " + expr.getText() + ";", containingClass);

                // 在第一个成员之前插入新字段
                containingClass.addBefore(newField, current == null ? containingClass.getRBrace() : current);

                // 替换原始表达式为静态常量引用
                expr.replace(factory.createExpressionFromText(constRefText, expr));
            }
        }
    }

}
