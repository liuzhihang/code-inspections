/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.code.inspections.ali.concurrent;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * [Mandatory] SimpleDataFormat is unsafe, do not define it as a static variable.
 * If have to, lock or DateUtils class must be used.
 * <p>
 * 【强制】SimpleDataFormat 不安全，请勿将其定义为静态变量。
 * 若必须定义，请使用 lock 或 DateUtils 类。
 *
 * @author liuzhihang
 * @version AvoidCallStaticSimpleDateFormatInspectionTool.java, v 0.1 2025年05月01日 21:00 liuzhihang
 */
public class AvoidCallStaticSimpleDateFormatInspectionTool extends AbstractBaseJavaLocalInspectionTool {

    /**
     * SimpleDateFormat 类名
     */
    private static final String SIMPLE_DATE_FORMAT_CLASS = "java.text.SimpleDateFormat";

    /**
     * format 方法名
     */
    private static final String FORMAT_METHOD_NAME = "format";

    /**
     * 创建 PsiElementVisitor 来查找问题
     *
     * @param holder     ProblemsHolder
     * @param isOnTheFly 是否在运行时检查
     * @return PsiElementVisitor
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                // 获取方法名和调用对象
                PsiReferenceExpression methodExpression = expression.getMethodExpression();
                String methodName = methodExpression.getReferenceName();

                if (!FORMAT_METHOD_NAME.equals(methodName)) {
                    return;
                }

                PsiExpression qualifier = methodExpression.getQualifierExpression();
                if (qualifier == null) {
                    return;
                }

                // 判断是否是 SimpleDateFormat 类型
                PsiType type = qualifier.getType();
                if (!(type instanceof PsiClassType) || !SIMPLE_DATE_FORMAT_CLASS.equals(type.getCanonicalText())) {
                    return;
                }

                // 判断是否是静态变量
                if (!isStaticFieldAccess(qualifier)) {
                    return;
                }

                // 排除 synchronized 块中的访问
                if (isInSynchronizedBlock(expression)) {
                    return;
                }

                // 注册问题
                holder.registerProblem(
                        expression,
                        CodeInspectionsBundle.message("ali.p3c.concurrent.AvoidCallStaticSimpleDateFormatInspectionTool.message", expression.getText())
                );
            }
        };
    }

    /**
     * 判断表达式是否是对静态变量的访问
     */
    private boolean isStaticFieldAccess(PsiExpression expression) {
        if (expression instanceof PsiReferenceExpression refExpr) {
            PsiElement resolved = refExpr.resolve();
            if (resolved instanceof PsiVariable variable && variable.hasModifierProperty(PsiModifier.STATIC)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前表达式是否在 synchronized 块中
     */
    private boolean isInSynchronizedBlock(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, PsiSynchronizedStatement.class) != null;
    }

}