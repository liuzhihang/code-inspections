package com.code.inspections.ali.concurrent;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * [Mandatory] Customized ThreadLocal variables must be recycled, especially when using thread pools in which threads
 * are often reused. Otherwise, it may affect subsequent business logic and cause unexpected problems such as memory
 * leak.
 * <p>
 * 【强制】自定义的 ThreadLocal 变量必须回收，尤其是在使用线程池时，因为线程经常被复用。否则可能会影响后续业务逻辑，并导致内存泄漏等意外问题。
 *
 * @author liuzhihang
 * @version ThreadLocalShouldRemoveInspectionTool.java, v 0.1 2025/4/29 21:37 liuzhihang
 */
public class ThreadLocalShouldRemoveInspectionTool extends AbstractBaseJavaLocalInspectionTool {

    /**
     * ThreadLocal 类名
     */
    private static final String THREAD_LOCAL_CLASS_NAME = "java.lang.ThreadLocal";

    /**
     * 创建一个PsiElementVisitor，用于遍历Java代码中的元素
     *
     * @param holder     ProblemsHolder对象，用于报告问题
     * @param isOnTheFly 是否在运行时进行检查
     * @return PsiElementVisitor对象
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitField(@NotNull PsiField field) {
                super.visitField(field);
                checkThreadLocalField(field, holder);
            }
        };
    }

    /**
     * 核心检测逻辑（按流程图实现）
     * <p>
     * 1. 检查字段是否为ThreadLocal类型
     * 2. 验证是否使用withInitial初始化
     * 3. 未使用withInitial时检查remove调用
     */
    private void checkThreadLocalField(PsiField field, ProblemsHolder holder) {
        // 检查字段类型是否为ThreadLocal
        if (!isThreadLocalType(field)) {
            return;
        }

        // 检查是否通过withInitial初始化
        if (isInitializedWithWithInitial(field)) {
            return;
        }

        // 检查是否存在remove调用
        if (!hasRemoveCall(field)) {
            holder.registerProblem(
                    field,
                    CodeInspectionsBundle.message("ali.p3c.concurrent.ThreadLocalShouldRemoveInspectionTool", field.getName()),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING

            );
        }
    }

    /**
     * 判断字段类型是否为ThreadLocal
     */
    private boolean isThreadLocalType(PsiField field) {
        PsiType type = field.getType();
        if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) type).resolve();
            return psiClass != null && THREAD_LOCAL_CLASS_NAME.equals(psiClass.getQualifiedName());
        }
        return false;
    }

    /**
     * 检查是否通过withInitial初始化
     */
    private boolean isInitializedWithWithInitial(PsiField field) {
        PsiExpression initializer = field.getInitializer();
        if (initializer instanceof PsiMethodCallExpression methodCall) {
            PsiMethod method = methodCall.resolveMethod();
            return method != null &&
                    "withInitial".equals(method.getName()) &&
                    THREAD_LOCAL_CLASS_NAME.equals(Objects.requireNonNull(method.getContainingClass()).getQualifiedName());
        }
        return false;
    }


    /**
     * 检查是否存在remove调用
     */
    private boolean hasRemoveCall(PsiField field) {
        // 确保获取最新的引用信息
        return ReferencesSearch.search(field)
                .allowParallelProcessing()
                .anyMatch(ref -> {

                    PsiElement element = ref.getElement();
                    if (!(element instanceof PsiReferenceExpression refExpr)) {
                        return false;
                    }

                    PsiMethodCallExpression parent = PsiTreeUtil.getParentOfType(refExpr, PsiMethodCallExpression.class);
                    if (parent == null) {
                        return false;
                    }

                    String referenceName = parent.getMethodExpression().getReferenceName();
                    return "remove".equals(referenceName);
                });
    }


}
