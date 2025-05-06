package com.code.inspections.ali.concurrent;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * [Recommended] When using CountDownLatch to convert asynchronous operations to synchronous ones,
 * each thread must call countdown method before quitting.
 * Make sure to catch any exception during thread running, to let countdown method be executed.
 * If main thread cannot reach await method, program will return until timeout.
 * <p>
 * Note: Be careful, exception thrown by sub-thread cannot be caught by main thread.
 * <p>
 * [推荐] 使用 CountDownLatch 将异步操作转换为同步操作时，
 * 每个线程在退出前必须调用 countdown 方法。
 * 确保在线程运行过程中捕获任何异常，以便 countdown 方法能够被执行。
 * 如果主线程无法到达 await 方法，程序将返回，直到超时。
 * <p>
 * 注意：子线程抛出的异常无法被主线程捕获。
 *
 * @author liuzhihang
 * @version CountDownShouldInFinallyInspectionTool.java, v 0.1 2025/5/6 22:20 liuzhihang
 */
public class CountDownShouldInFinallyInspectionTool extends AbstractBaseJavaLocalInspectionTool {
    /**
     * 检查方法：查找 CountDownLatch 的 countDown() 调用是否位于 try-finally 的 finally 块中
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                // 获取方法名和类类型
                PsiReferenceExpression methodExpression = expression.getMethodExpression();
                String methodName = methodExpression.getReferenceName();

                if (!"countDown".equals(methodName)) {
                    return;
                }

                PsiMethod method = expression.resolveMethod();
                if (method == null) {
                    return;
                }

                PsiClass calledClass = method.getContainingClass();
                if (calledClass == null || !"java.util.concurrent.CountDownLatch".equals(calledClass.getQualifiedName())) {
                    return;
                }

                // 检查是否在 finally 块中
                PsiElement parentBlock = findParentBlock(expression);
                if (parentBlock != null && !isInFinallyBlock(parentBlock)) {
                    holder.registerProblem(
                            expression,
                            CodeInspectionsBundle.message("ali.p3c.concurrent.CountDownShouldInFinallyInspectionTool.message", "CountDownLatch"),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    );
                }
            }

            /**
             * 查找最近的代码块（可能是 try、catch 或 finally）
             */
            private PsiElement findParentBlock(PsiElement element) {
                PsiElement parent = element.getParent();
                while (parent != null && !(parent instanceof PsiCodeBlock)) {
                    parent = parent.getParent();
                }
                return parent;
            }

            /**
             * 判断给定的代码块是否属于 finally 块
             */
            private boolean isInFinallyBlock(PsiElement block) {
                PsiElement parent = block.getParent();
                while (parent != null) {
                    if (parent instanceof PsiTryStatement tryStatement) {
                        return tryStatement.getFinallyBlock() == block;
                    }
                    parent = parent.getParent();
                }
                return false;
            }
        };
    }
}
