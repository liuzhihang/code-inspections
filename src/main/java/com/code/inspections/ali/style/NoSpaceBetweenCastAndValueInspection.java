package com.code.inspections.ali.style;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiTypeCastExpression;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】在进行类型强制转换时，右括号与强制转换值之间不需要任何空格隔开。
 *
 * @author liuzhihang
 * @version NoSpaceBetweenCastAndValueInspection.java, v 0.1 2024/5/6 liuzhihang
 */
public class NoSpaceBetweenCastAndValueInspection extends LocalInspectionTool {

    /**
     * 提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.style.cast.spaces.message");

    /**
     * 构建一个访问者对象，用于访问Java代码中的二元表达式，并检查特定的格式规范问题。
     * <p>
     *
     * @param holder     用于收集和报告问题的问题持有者对象。
     * @param isOnTheFly 表示是否在飞行模式下进行检查，即是否是在实时编码过程中进行的检查。
     * @return 返回一个实现了Java元素访问者接口的对象，该对象会访问二元表达式并检查格式问题。
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);

                // 检查当前元素是否为类型转换表达式
                if (element instanceof PsiTypeCastExpression typeCastExpression) {
                    String text = typeCastExpression.getText();

                    // 查找最后一个右括号的位置
                    int lastParenthesisIndex = text.lastIndexOf(')');

                    // 检查右括号后面是否有空格
                    if (lastParenthesisIndex >= 0 && text.charAt(lastParenthesisIndex + 1) == ' ') {
                        // 报告问题，假设holder是可用的ProblemsHolder实例
                        holder.registerProblem(typeCastExpression, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }

}
