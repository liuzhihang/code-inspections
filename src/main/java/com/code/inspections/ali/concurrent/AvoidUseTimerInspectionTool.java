package com.code.inspections.ali.concurrent;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiReferenceExpression;
import org.jetbrains.annotations.NotNull;

/**
 * [Mandatory] Run multiple TimeTask by using ScheduledExecutorService rather than Timer
 * because Timer will kill all running threads in case of failing to catch exception.
 * <p>
 * * [强制] 使用 ScheduledExecutorService 而不是 Timer 来运行多个 TimeTask
 * * 因为 Timer 在捕获异常失败时会终止所有正在运行的线程。
 *
 * @author liuzhihang
 * @version AvoidUseTimerInspectionTool.java, v 0.1 2025/5/6 22:08 liuzhihang
 */
public class AvoidUseTimerInspectionTool extends AbstractBaseJavaLocalInspectionTool {


    /**
     * Timer 类全限定名
     */
    private static final String TIMER_CLASS = "java.util.Timer";

    /**
     * 构建 PSI 访问器以检查使用 Timer 的位置
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitNewExpression(@NotNull PsiNewExpression expression) {
                super.visitNewExpression(expression);

                // 检查是否是 Timer 实例化
                if (expression.getType() != null && TIMER_CLASS.equals(expression.getType().getCanonicalText())) {
                    holder.registerProblem(
                            expression,
                            CodeInspectionsBundle.message("ali.p3c.concurrent.AvoidUseTimerInspectionTool.message"),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    );
                }
            }

            @Override
            public void visitReferenceExpression(@NotNull PsiReferenceExpression expression) {
                super.visitReferenceExpression(expression);

                // 可选：检查对 Timer 类的直接引用（如静态方法调用等）
                if (TIMER_CLASS.equals(expression.getQualifiedName())) {
                    holder.registerProblem(
                            expression,
                            CodeInspectionsBundle.message("ali.p3c.concurrent.AvoidUseTimerInspectionTool.message"),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    );
                }
            }
        };
    }
}
