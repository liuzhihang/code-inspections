package com.code.inspections.ali.constant;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】不允许任何魔法值（即未经预先定义的常量）直接出现在代码中
 *
 * @author liuzhihang
 * @version MagicValueInspection.java, v 0.1 2024/5/4 liuzhihang
 */
public class MagicValueInspection extends AbstractBaseJavaLocalInspectionTool {

    static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.constant.magic.value.message");
    static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.constant.magic.value.fix");

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitLiteralExpression(@NotNull PsiLiteralExpression expression) {
                PsiType type = expression.getType();
                if (type instanceof PsiPrimitiveType || "java.lang.String".equals(type.getCanonicalText())) {
                    PsiElement parent = expression.getParent();
                    // 首先检查是否直接在局部变量赋值中
                    if (parent instanceof PsiVariable variable) {
                        if (variable instanceof PsiLocalVariable) {
                            holder.registerProblem(expression, MESSAGE, new ReplaceWithStaticConstantQuickFix(expression));
                        }
                    } else {
                        // 如果不是直接赋值给局部变量，继续向上查找
                        PsiElement codeBlock = PsiTreeUtil.getParentOfType(expression, PsiCodeBlock.class);
                        if (codeBlock != null) {
                            PsiElement methodOrLambda = PsiTreeUtil.getParentOfType(codeBlock, PsiMethod.class, PsiLambdaExpression.class);
                            if (methodOrLambda != null) {
                                // 确保赋值表达式处于方法或lambda表达式内
                                if (methodOrLambda instanceof PsiMethod method) {
                                    // 检查是否在方法参数列表中的赋值（这种情况应排除）
                                    if (!PsiTreeUtil.isAncestor(method.getParameterList(), expression, true)) {
                                        holder.registerProblem(expression, MESSAGE, new ReplaceWithStaticConstantQuickFix(expression));
                                    }
                                } else if (methodOrLambda instanceof PsiLambdaExpression) {
                                    holder.registerProblem(expression, MESSAGE, new ReplaceWithStaticConstantQuickFix(expression));
                                }
                            }
                        }
                    }
                }
            }
        };
    }


    private static class ReplaceWithStaticConstantQuickFix implements LocalQuickFix {
        private final PsiLiteralExpression literalExpression;

        ReplaceWithStaticConstantQuickFix(PsiLiteralExpression literalExpression) {
            this.literalExpression = literalExpression;
        }

        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiLiteralExpression expr = (PsiLiteralExpression) descriptor.getPsiElement();
            String fieldType = expr.getType().getCanonicalText();
            PsiClass containingClass = PsiTreeUtil.getParentOfType(expr, PsiClass.class, true);
            if (containingClass != null) {
                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
                // 移除字符串字面量的引号
                String value = expr.getText().replaceAll("\"", "");
                String constantName = "CONST_" + value.replaceAll("\\W", "_").toUpperCase();

                PsiElement firstMember = containingClass.getLBrace();
                PsiElement current = firstMember.getNextSibling();
                while (current != null && !(current instanceof PsiField || current instanceof PsiMethod)) {
                    current = current.getNextSibling();
                }
                if (current == null) {
                    // 如果没有找到其他字段或方法，则添加到大括号后
                    current = containingClass.getRBrace();
                }

                String constRefText = containingClass.getQualifiedName() + "." + constantName;
                PsiField newField = factory.createFieldFromText("private static final " + fieldType + " " + constantName + " = " + expr.getText() + ";", containingClass);

                // 在第一个字段或方法之前插入新字段
                containingClass.addBefore(newField, current);

                expr.replace(factory.createExpressionFromText(constRefText, expr));
            }
        }
    }
}
