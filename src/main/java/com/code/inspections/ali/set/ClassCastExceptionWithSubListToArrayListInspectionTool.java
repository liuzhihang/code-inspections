package com.code.inspections.ali.set;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * [Mandatory] Do not cast subList in class ArrayList, otherwise ClassCastException will be
 * thrown：java.util.RandomAccessSubList
 * cannot be cast to java.util.ArrayList ;
 * <p>
 * 【强制】ArrayList 类中的 subList 不能强制类型转换，否则会抛出 ClassCastException
 * 抛出：java.util.RandomAccessSubList
 * 不能强制类型转换为 java.util.ArrayList ；
 *
 * @author liuzhihang
 * @version ClassCastExceptionWithSubListToArrayListInspectionTool.java, v 0.1 2025/5/6 23:11 liuzhihang
 */
public class ClassCastExceptionWithSubListToArrayListInspectionTool extends AbstractBaseJavaLocalInspectionTool {
    /**
     * ArrayList 全限定类名
     */
    private static final String ARRAY_LIST_CLASS = "java.util.ArrayList";

    /**
     * 构建 PSI 访问器以检查非法的类型转换
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitTypeCastExpression(@NotNull PsiTypeCastExpression expression) {
                super.visitTypeCastExpression(expression);

                // 1. 获取目标类型（即强制转换的目标类）
                PsiTypeElement castTypeElement = expression.getCastType();
                if (castTypeElement == null) {
                    return;
                }

                // 使用 PsiUtil 提取 raw type
                PsiType castType = castTypeElement.getType();
                PsiClass targetClass = com.intellij.psi.util.PsiUtil.resolveClassInType(castType);
                if (targetClass == null || !ARRAY_LIST_CLASS.equals(targetClass.getQualifiedName())) {
                    return;
                }

                // 2. 获取被强制转换的表达式
                PsiExpression operand = expression.getOperand();
                if (!(operand instanceof PsiMethodCallExpression methodCall)) {
                    return;
                }

                String methodName = methodCall.getMethodExpression().getReferenceName();
                if (!"subList".equals(methodName)) {
                    return;
                }
                
                // 4. 注册问题
                holder.registerProblem(
                        expression,
                        CodeInspectionsBundle.message("ali.p3c.set.ClassCastExceptionWithSubListToArrayListInspectionTool.message", expression.getText()),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                );

            }

        };
    }

}
