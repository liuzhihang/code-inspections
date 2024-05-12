package com.code.inspections.ali.oop;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】所有的覆写方法，必须加 @Override 注解。
 *
 * @author liuzhihang
 * @version MissingOverrideAnnotationInspection.java, v 0.1 2024/5/12 liuzhihang
 */
public class OverrideAnnotationInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.oop.override.annotation.message");

    /**
     * 快速修复
     */
    private static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.oop.override.annotation.fix");

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethod(@NotNull PsiMethod method) {
                super.visitMethod(method);
                if (method.findSuperMethods().length > 0 && !AnnotationUtil.isAnnotated(method, "java.lang.Override", 0)) {
                    holder.registerProblem(method, MESSAGE);
                }
            }
        };
    }
}
