package com.code.inspections.ali.concurrent;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * [Mandatory] Threads should be provided by thread pools. Explicitly creating threads is not allowed.
 * Note: Using thread pool can reduce the time of creating and destroying thread and save system resource.
 * If we do not use thread pools, lots of similar threads will be created which lead to
 * "running out of memory" or over-switching problems.
 * <p>
 * Detection rule
 * New Thread can only be created in ThreadFactory.newThread method,as Runtime.getRuntime().addShutdownHook() parameter,
 * or in static block
 * <p>
 * 【强制】线程应由线程池提供。不允许显式创建线程。
 * 注意：使用线程池可以减少创建和销毁线程的时间，节省系统资源。
 * 如果不使用线程池，则会创建大量类似的线程，从而导致
 * “内存溢出”或过度切换问题。
 * <p>
 * 检测规则
 * 新线程只能在 ThreadFactory.newThread 方法中创建，作为 Runtime.getRuntime().addShutdownHook() 的参数，
 * 或在静态块中创建。
 *
 * @author liuzhihang
 * @version AvoidManuallyCreateThreadInspectionTool.java, v 0.1 2025年05月01日 21:00 liuzhihang
 */
public class AvoidManuallyCreateThreadInspectionTool extends AbstractBaseJavaLocalInspectionTool {

    /**
     * Thread 类
     */
    private static final String THREAD_CLASS = "java.lang.Thread";

    /**
     * ThreadFactory 类
     */
    private static final String THREAD_FACTORY_CLASS = "java.util.concurrent.ThreadFactory";

    /**
     * newThread 方法
     */
    private static final String METHOD_NEW_THREAD = "newThread";

    /**
     * 创建 PsiElementVisitor
     *
     * @param holder     ProblemsHolder
     * @param isOnTheFly 是否在编辑时
     * @return PsiElementVisitor
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitNewExpression(@NotNull PsiNewExpression expression) {
                super.visitNewExpression(expression);

                // 检查是否是 new Thread(...)
                if (!isCreatingThread(expression)) {
                    return;
                }

                // 排除允许的情况
                if (isInAddShutdownHook(expression) || isInStaticInitializer(expression) || isInNewThreadMethod(expression)
                        || isImplementingThreadFactory(expression)) {
                    return;
                }

                // 注册问题
                holder.registerProblem(
                        expression,
                        CodeInspectionsBundle.message("ali.p3c.concurrent.AvoidManuallyCreateThreadInspectionTool.message")
                );
            }
        };
    }

    /**
     * 判断表达式是否是 new Thread(...)
     */
    private boolean isCreatingThread(PsiNewExpression expression) {
        PsiJavaCodeReferenceElement reference = expression.getClassOrAnonymousClassReference();
        if (reference == null) {
            return false;
        }

        PsiElement resolved = reference.resolve();
        return resolved instanceof PsiClass psiClass && THREAD_CLASS.equals(psiClass.getQualifiedName());
    }

    /**
     * 检查是否是 addShutdownHook(new Thread(...))
     */
    private boolean isInAddShutdownHook(PsiNewExpression expression) {
        PsiMethodCallExpression methodCall = PsiTreeUtil.getParentOfType(expression, PsiMethodCallExpression.class);
        if (methodCall == null) {
            return false;
        }

        PsiReferenceExpression methodExpr = methodCall.getMethodExpression();
        String methodName = methodExpr.getReferenceName();

        if ("addShutdownHook".equals(methodName)) {
            PsiExpression qualifier = methodExpr.getQualifierExpression();
            if (qualifier instanceof PsiReferenceExpression refExpr) {
                PsiElement resolved = refExpr.resolve();
                return resolved instanceof PsiClass psiClass && "java.lang.Runtime".equals(psiClass.getQualifiedName());
            }
        }

        return false;
    }

    /**
     * 检查是否在静态初始化块中
     */
    private boolean isInStaticInitializer(PsiNewExpression expression) {
        PsiClassInitializer initializer = PsiTreeUtil.getParentOfType(expression, PsiClassInitializer.class);
        return initializer != null && initializer.hasModifierProperty(PsiModifier.STATIC);
    }

    /**
     * 检查是否在 ThreadFactory.newThread(...) 方法中
     */
    private boolean isInNewThreadMethod(PsiNewExpression expression) {
        PsiMethod method = PsiTreeUtil.getParentOfType(expression, PsiMethod.class);
        if (method == null) {
            return false;
        }

        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null || !THREAD_FACTORY_CLASS.equals(containingClass.getQualifiedName())) {
            return false;
        }

        return METHOD_NEW_THREAD.equals(method.getName()) && returnsThread(method) && hasRunnableParameter(method);
    }

    /**
     * 检查返回类型是否是 Thread
     */
    private boolean returnsThread(PsiMethod method) {
        PsiType returnType = method.getReturnType();
        return returnType instanceof PsiClassType && THREAD_CLASS.equals(returnType.getCanonicalText());
    }

    /**
     * 检查是否有 Runnable 参数
     */
    private boolean hasRunnableParameter(PsiMethod method) {
        PsiParameter[] parameters = method.getParameterList().getParameters();
        return parameters.length == 1 && "java.lang.Runnable".equals(parameters[0].getType().getCanonicalText());
    }

    /**
     * 检查是否是 ThreadFactory 的实现类中的 new Thread(...)
     */
    private boolean isImplementingThreadFactory(PsiNewExpression expression) {
        PsiClass aClass = PsiTreeUtil.getParentOfType(expression, PsiClass.class);
        if (aClass == null) {
            return false;
        }

        // 查找 implements ThreadFactory
        PsiReferenceList list = aClass.getImplementsList();
        if (list == null) {
            return false;
        }

        for (PsiJavaCodeReferenceElement ref : list.getReferenceElements()) {
            if (THREAD_FACTORY_CLASS.equals(ref.getQualifiedName())) {
                return true;
            }
        }

        // 查看是否是 ThreadFactory 子类
        return InheritanceUtil.isInheritor(aClass, THREAD_FACTORY_CLASS);
    }
}