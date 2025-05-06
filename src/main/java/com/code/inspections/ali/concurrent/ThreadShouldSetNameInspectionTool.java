/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.code.inspections.ali.concurrent;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import org.jetbrains.annotations.NotNull;

/**
 * [Mandatory] A meaningful thread name is helpful to trace the error information,
 * so assign a name when creating threads or thread pools.
 * <p>
 * Detection rule
 * 1. Use specific constructor while create thread pool
 * 2. Use Executors.defaultThreadFactory() is not allowed
 * <p>
 * 【强制】有意义的线程名称有助于追踪错误信息，因此，在创建线程或线程池时，请指定一个名称。
 * <p>
 * 检测规则
 * 1. 创建线程池时使用特定的构造函数
 * 2. 不允许使用 Executors.defaultThreadFactory()
 *
 * @author liuzhihang
 * @version ThreadShouldSetNameInspectionTool.java, v 0.1 2025年05月01日 21:00 liuzhihang
 */
public class ThreadShouldSetNameInspectionTool extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 线程池类名
     */
    private static final String THREAD_POOL_EXECUTOR = "java.util.concurrent.ThreadPoolExecutor";

    /**
     * 线程池类名
     */
    private static final String SCHEDULED_THREAD_POOL_EXECUTOR = "java.util.concurrent.ScheduledThreadPoolExecutor";

    /**
     * 线程池类名
     */
    private static final String EXECUTORS_DEFAULT_THREAD_FACTORY = "java.util.concurrent.Executors.defaultThreadFactory";

    /**
     * 线程池构造函数参数个数
     */
    private static final int THREAD_POOL_EXECUTOR_EXPECTED_ARGS = 6;

    /**
     * 线程池类名
     */
    private static final int SCHEDULED_THREAD_POOL_EXECUTOR_EXPECTED_ARGS = 2;

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitNewExpression(@NotNull PsiNewExpression expression) {
                super.visitNewExpression(expression);

                if (expression.getType() == null) {
                    return;
                }

                if (InheritanceUtil.isInheritor(expression.getType(), THREAD_POOL_EXECUTOR)) {
                    checkThreadPoolExecutor(expression, holder);
                } else if (InheritanceUtil.isInheritor(expression.getType(), SCHEDULED_THREAD_POOL_EXECUTOR)) {
                    checkScheduledThreadPoolExecutor(expression, holder);
                }

            }
        };
    }

    /**
     * 检查 ThreadPoolExecutor 的构造函数参数
     *
     * @param expression 构造函数表达式
     * @param holder     问题处理器
     */
    private void checkThreadPoolExecutor(PsiNewExpression expression, ProblemsHolder holder) {
        PsiExpressionList argumentList = expression.getArgumentList();
        if (argumentList == null || argumentList.getExpressions().length < THREAD_POOL_EXECUTOR_EXPECTED_ARGS) {
            return;
        }

        PsiExpression lastArg = argumentList.getExpressions()[THREAD_POOL_EXECUTOR_EXPECTED_ARGS - 1];
        if (!isThreadFactory(lastArg)) {
            holder.registerProblem(expression, CodeInspectionsBundle.message("ali.p3c.concurrent.ThreadShouldSetNameInspectionTool.message.ThreadPoolExecutor"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
    }

    /**
     * 检查 ScheduledThreadPoolExecutor 的构造函数参数
     *
     * @param expression 构造函数表达式
     * @param holder     问题处理器
     */
    private void checkScheduledThreadPoolExecutor(PsiNewExpression expression, ProblemsHolder holder) {
        PsiExpressionList argumentList = expression.getArgumentList();
        if (argumentList == null || argumentList.getExpressions().length < SCHEDULED_THREAD_POOL_EXECUTOR_EXPECTED_ARGS) {
            return;
        }

        PsiExpression lastArg = argumentList.getExpressions()[SCHEDULED_THREAD_POOL_EXECUTOR_EXPECTED_ARGS - 1];
        if (!isThreadFactory(lastArg)) {
            holder.registerProblem(expression, CodeInspectionsBundle.message("ali.p3c.concurrent.ThreadShouldSetNameInspectionTool.message.ScheduledThreadPoolExecutor"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
    }

    /**
     * 检查是否是 ThreadFactory
     *
     * @param expression 表达式
     * @return 是否是 ThreadFactory
     */
    private boolean isThreadFactory(PsiExpression expression) {
        if (expression.getType() != null && InheritanceUtil.isInheritor(expression.getType(), "java.util.concurrent.ThreadFactory")) {
            return true;
        }

        // 检查是否是 Executors.defaultThreadFactory()
        if (expression instanceof PsiMethodCallExpression methodCall) {
            String methodName = methodCall.getMethodExpression().getQualifiedName();
            if (EXECUTORS_DEFAULT_THREAD_FACTORY.equals(methodName)) {
                return false;
            }
        }

        // Lambda 表达式处理（可选扩展）
        if (expression instanceof PsiLambdaExpression lambda) {
            // 可以进一步判断 lambda 是否返回 ThreadFactory
            return false;
        }

        return false;
    }

}