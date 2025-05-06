/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.code.inspections.ali.concurrent;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * [Mandatory] A thread pool should be created by ThreadPoolExecutor rather than Executors.
 * These would make the parameters of the thread pool understandable.
 * It would also reduce the risk of running out of system resource.
 * <p>
 * 【强制】线程池应该使用 ThreadPoolExecutor 而不是 Executors 来创建。
 * 这样可以使线程池的参数更易于理解。
 * 还可以降低系统资源耗尽的风险。
 *
 * @author liuzhihang
 * @version ThreadPoolCreationInspectionTool.java, v 0.1 2025年05月01日 21:00 liuzhihang
 */
public class ThreadPoolCreationInspectionTool extends AbstractBaseJavaLocalInspectionTool {

    /**
     * Executors 类的全限定名
     */
    private static final String EXECUTORS_CLASS = "java.util.concurrent.Executors";

    /**
     * 允许的方法
     */
    private static final Set<String> ALLOWED_EXECUTORS_METHODS = Set.of(
            "newScheduledThreadPool",
            "newSingleThreadScheduledExecutor"
    );


    /**
     * 创建线程池时，检查是否使用了 Executors 类的方法。
     * 如果使用了，则提示用户使用 ThreadPoolExecutor 替代 Executors。
     *
     * @param holder     ProblemsHolder
     * @param isOnTheFly boolean
     * @return PsiElementVisitor
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                String methodName = expression.getMethodExpression().getReferenceName();
                if (methodName == null || ALLOWED_EXECUTORS_METHODS.contains(methodName)) {
                    return;
                }

                PsiMethod resolvedMethod = expression.resolveMethod();
                if (resolvedMethod == null) {
                    return;
                }

                PsiClass containingClass = resolvedMethod.getContainingClass();
                if (containingClass != null
                        && EXECUTORS_CLASS.equals(containingClass.getQualifiedName())
                        && isThreadPoolCreatingMethod(resolvedMethod)) {

                    holder.registerProblem(
                            expression,
                            CodeInspectionsBundle.message("ali.p3c.concurrent.ThreadPoolCreationInspectionTool.message")
                    );
                }
            }
        };
    }

    /**
     * 判断是否是一个创建线程池的方法调用。
     *
     * @param method 方法对象
     * @return 如果是线程池创建方法返回 true，否则 false
     */
    private static boolean isThreadPoolCreatingMethod(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null || !EXECUTORS_CLASS.equals(containingClass.getQualifiedName())) {
            return false;
        }

        // 线程池创建方法通常返回 ExecutorService 或其子类
        PsiType returnType = method.getReturnType();
        if (returnType == null) {
            return false;
        }

        return "java.util.concurrent.ExecutorService".equals(returnType.getCanonicalText())
                || InheritanceUtil.isInheritor(returnType, "java.util.concurrent.ExecutorService");
    }
}