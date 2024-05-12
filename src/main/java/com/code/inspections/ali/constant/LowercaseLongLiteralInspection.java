package com.code.inspections.ali.constant;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * 检查 long 或 Long 赋值时是否使用了大写 L。如果使用了小写 l，会被视为潜在的代码质量问题，
 * 因为小写 l 容易与数字 1 混淆，从而造成误解。
 *
 * @author liuzhihang
 * @version LowercaseLongLiteralInspection.java, v 0.1 2024/5/4 liuzhihang
 */
public class LowercaseLongLiteralInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 错误提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.constant.lowercase.long.literal.message");
    /**
     * 快速修复提示信息
     */
    private static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.constant.lowercase.long.literal.fix");

    /**
     * 创建一个访问者，用于访问 Java 代码元素并检测潜在的问题。
     *
     * @param holder     用于存储检测到的问题的容器。
     * @param isOnTheFly 表示是否在实时（飞行）模式下进行检查。
     * @param session    本地检查会话，提供与检查相关的上下文信息。
     * @return 返回一个访问者对象，该对象将访问 Java 源代码中的字面量表达式并检查 long 类型字面量的小写 'l' 是否被使用。
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new BaseInspectionVisitor() {
            /**
             * 访问字面量表达式，检查是否为 long 类型且以小写 'l' 结尾。
             *
             * @param literalExpression 被访问的字面量表达式。
             */
            @Override
            public void visitLiteralExpression(@NotNull PsiLiteralExpression literalExpression) {
                PsiType type = literalExpression.getType();
                if (type == PsiType.LONG) { // 检查类型是否为 long
                    String text = literalExpression.getText();
                    if (text.endsWith("l") || text.endsWith("L")) { // 检查文本末尾是否是 'l' 或 'L'
                        if (text.endsWith("l")) { // 如果是小写 'l'，则注册一个问题并提供快速修复
                            holder.registerProblem(literalExpression,
                                    MESSAGE,
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    new FixLongLiteralCaseQuickFix(literalExpression));
                        }
                    }
                }
            }
        };
    }

    /**
     * 为 long 字面量的小写 'l' 提供快速修复的类。
     */
    private static class FixLongLiteralCaseQuickFix implements LocalQuickFix {
        private final PsiLiteralExpression literalExpression;

        /**
         * 构造函数。
         *
         * @param literalExpression 需要进行修复的字面量表达式。
         */
        FixLongLiteralCaseQuickFix(PsiLiteralExpression literalExpression) {
            this.literalExpression = literalExpression;
        }

        /**
         * 返回快速修复的显示名称。
         *
         * @return 快速修复的名称。
         */
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 应用快速修复，将 long 字面量的小写 'l' 替换为大写 'L'。
         *
         * @param project    当前项目。
         * @param descriptor 描述问题的描述符。
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement elementToReplace = descriptor.getPsiElement();
            if (elementToReplace instanceof PsiLiteralExpression longLiteral) { // 确认需要替换的元素是字面量表达式
                String newText = longLiteral.getText().replace('l', 'L'); // 将小写 'l' 替换为大写 'L'
                PsiElement replacement = JavaPsiFacade.getElementFactory(project).createExpressionFromText(newText, longLiteral);
                longLiteral.replace(replacement); // 替换原始表达式
            }
        }
    }
}
