package com.code.inspections.ali.style;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】任何二目、三目运算符的左右两边都需要加一个空格。
 *
 * @author liuzhihang
 * @version OperatorSpacingInspection.java, v 0.1 2024/5/5 liuzhihang
 */
public class OperatorSpacingInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.style.operator.spaces.message");

    /**
     * 构建并返回一个访问者对象，用于检查二元、赋值和三元表达式中的操作符是否符合特定要求。
     *
     * @param holder     用于报告问题的问题持有者对象。
     * @param isOnTheFly 表示检查是否在飞行模式下进行，影响问题的报告方式。
     * @param session    局部检查工具会话对象，提供检查过程中的上下文信息。
     * @return 返回一个实现了JavaElementVisitor接口的匿名类对象，用于访问和检查代码中的元素。
     */
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new JavaElementVisitor() {
            /**
             * 访问二元表达式，检查操作符。
             */
            @Override
            public void visitBinaryExpression(@NotNull PsiBinaryExpression binaryExpression) {
                checkOperator(binaryExpression.getOperationSign());
            }

            /**
             * 访问赋值表达式，检查操作符。
             */
            @Override
            public void visitAssignmentExpression(@NotNull PsiAssignmentExpression assignmentExpression) {
                checkOperator(assignmentExpression.getOperationSign());
            }

            /**
             * 访问三元条件表达式，检查条件操作符。
             */
            @Override
            public void visitConditionalExpression(@NotNull PsiConditionalExpression ternaryExpression) {
                PsiElement[] children = ternaryExpression.getChildren();
                for (PsiElement child : children) {
                    if ("?".equals(child.getText()) || ":".equals(child.getText())) {
                        checkOperator(child);
                    }
                }
            }

            /**
             * 检查给定的操作符是否符合特定的格式要求。
             * 如果操作符前后不满足要求的空格，则记录一个问题。
             */
            private void checkOperator(PsiElement operator) {

                PsiElement prevSibling = operator.getPrevSibling();
                PsiElement nextSibling = operator.getNextSibling();
                if ((prevSibling != null && !prevSibling.getText().equals(" ")) ||
                        (nextSibling != null && !nextSibling.getText().equals(" "))) {
                    holder.registerProblem(operator, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }

}
