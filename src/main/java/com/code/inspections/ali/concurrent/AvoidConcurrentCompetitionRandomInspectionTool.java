package com.code.inspections.ali.concurrent;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * [Recommended] Avoid using Random instance by multiple threads.
 * Although it is safe to share this instance, competition on the same seed will damage performance.
 * Note: Random instance includes instances of java.util.Random and Math.random().
 * <p>
 * [推荐] 避免在多个线程中使用 Random 实例。
 * 虽然共享此实例是安全的，但同一种子上的竞争会损害性能。
 * 注意：Random 实例包含 java.util.Random 和 Math.random() 的实例。
 *
 * @author liuzhihang
 * @version AvoidConcurrentCompetitionRandomInspectionTool.java, v 0.1 2025/5/6 22:37 liuzhihang
 */
public class AvoidConcurrentCompetitionRandomInspectionTool extends AbstractBaseJavaLocalInspectionTool {
    private static final String JAVA_UTIL_RANDOM = "java.util.Random";
    private static final String JAVA_LANG_MATH_RANDOM = "java.lang.Math.random";

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            /**
             * 检查对 Math.random() 的调用
             */
            @Override
            public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                String methodFqn = expression.getMethodExpression().getQualifiedName();
                if (JAVA_LANG_MATH_RANDOM.equals(methodFqn)) {
                    holder.registerProblem(
                            expression,
                            CodeInspectionsBundle.message("ali.p3c.concurrent.AvoidConcurrentCompetitionRandomInspectionTool.message.math.random"),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    );
                }
            }

            /**
             * 检查静态 Random 字段的访问
             */
            @Override
            public void visitReferenceExpression(@NotNull PsiReferenceExpression expression) {
                super.visitReferenceExpression(expression);

                PsiElement resolved = expression.resolve();
                if (!(resolved instanceof PsiField field)) {
                    return;
                }

                if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                    return;
                }

                PsiClass containingClass = field.getContainingClass();
                if (containingClass != null && JAVA_UTIL_RANDOM.equals(containingClass.getQualifiedName())) {
                    holder.registerProblem(
                            expression,
                            CodeInspectionsBundle.message("ali.p3c.concurrent.AvoidConcurrentCompetitionRandomInspectionTool.message.random", field.getName()),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    );
                }
            }
        };
    }
}
