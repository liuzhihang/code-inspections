package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】类型与中括号紧挨相连来定义数组。
 *
 * @author liuzhihang
 * @version ArrayDefinitionInspection.java, v 0.1 2024/5/1 liuzhihang
 */
public class ArrayDefinitionInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 错误提示
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.array.definition.message");

    /**
     * 快速修复
     */
    private static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.name.array.definition.fix");


    /**
     * 扫描的元素
     *
     * @param holder     ProblemsHolder
     * @param isOnTheFly 是否是 on the fly
     * @return PsiElementVisitor
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {


            @Override
            public void visitVariable(@NotNull PsiVariable variable) {
                super.visitVariable(variable);
                PsiType type = variable.getType();
                if (type instanceof PsiArrayType && variable.getNameIdentifier() != null) {
                    @NotNull PsiElement[] children = variable.getChildren();
                    for (PsiElement child : children) {
                        if (child instanceof PsiJavaToken && ((PsiJavaToken) child).getTokenType() == JavaTokenType.LBRACKET) {
                            holder.registerProblem(variable.getNameIdentifier(), MESSAGE, new FixArrayDefinitionFix());
                            break;
                        }
                    }
                }
            }

        };

    }

    /**
     * 快速修复
     */
    private static class FixArrayDefinitionFix implements LocalQuickFix {

        /**
         * 快速修复名称
         *
         * @return 快速修复名称
         */
        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 快速修复
         *
         * @param project    项目
         * @param descriptor 问题描述
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement().getParent();
            if (element instanceof PsiVariable) {
                PsiVariable variable = (PsiVariable) element;
                PsiType type = variable.getType();
                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

                // 提取所有的唯独并构造修复后的类型字符串
                StringBuilder typeString = new StringBuilder();
                while (type instanceof PsiArrayType) {
                    typeString.append("[]");
                    type = ((PsiArrayType) type).getComponentType();
                }
                typeString.insert(0, type.getCanonicalText());

                // 保留修饰符
                StringBuilder variableBuilder = new StringBuilder();
                for (PsiElement modifierOrAnnotation : variable.getModifierList().getChildren()) {
                    if (modifierOrAnnotation instanceof PsiAnnotation || modifierOrAnnotation instanceof PsiModifier || modifierOrAnnotation instanceof PsiKeyword) {
                        variableBuilder.append(modifierOrAnnotation.getText()).append(" ");
                    }
                }

                // 生成新的声明代码
                variableBuilder.append(typeString).append(" ").append(variable.getName());
                if (variable.getInitializer() != null) {
                    variableBuilder.append(" = ").append(variable.getInitializer().getText());
                }
                variableBuilder.append(";");
                PsiElement newElement;
                if (variable instanceof PsiField) {
                    newElement = factory.createFieldFromText(variableBuilder.toString(), null);
                } else {
                    newElement = factory.createStatementFromText(variableBuilder.toString(), null);

                }
                variable.replace(newElement);

            }

        }

    }
}
