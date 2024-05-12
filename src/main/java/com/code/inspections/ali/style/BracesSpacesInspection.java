package com.code.inspections.ali.style;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】左小括号和右边相邻字符之间不需要空格；右小括号和左边相邻字符之间也不需要空格；而左大括号前需要加空格。
 *
 * @author liuzhihang
 * @version BracesSpacesInspection.java, v 0.1 2024/5/5 liuzhihang
 */
public class BracesSpacesInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 括号空格检查
     */
    private static final String MESSAGE1 = CodeInspectionsBundle.message("ali.p3c.style.braces.spaces.message1");
    /**
     * 括号空格检查
     */
    private static final String MESSAGE2 = CodeInspectionsBundle.message("ali.p3c.style.braces.spaces.message2");

    /**
     * 括号空格检查
     */
    private static final String MESSAGE3 = CodeInspectionsBundle.message("ali.p3c.style.braces.spaces.message3");

    /**
     * 构建一个访问者对象，用于检查Java代码中的括号和左花括号是否符合特定格式。
     *
     * @param holder     用于存储检查过程中发现的问题。
     * @param isOnTheFly 指示检查是否在飞行模式下进行，影响问题的报告方式。
     * @param session    本地检查工具会话，提供上下文信息。
     * @return 返回一个Java元素访问者对象，该对象会访问代码中的每个元素并执行特定的检查逻辑。
     */
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new JavaElementVisitor() {
            /**
             * 访问一个Java元素，检查是否为括号或左花括号，并执行相应的检查逻辑。
             *
             * @param element 当前正在访问的Java元素。
             */
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                if (element instanceof PsiJavaToken) {
                    IElementType elementType = element.getNode().getElementType();
                    PsiElement prevSibling = element.getPrevSibling();
                    PsiElement nextSibling = element.getNextSibling();

                    // 检查左大括号 "{" 前是否有一个空格
                    if (elementType == JavaTokenType.LBRACE) {
                        if (prevSibling != null && prevSibling.getNode().getElementType() != TokenType.WHITE_SPACE) {
                            holder.registerProblem(element, new TextRange(0, 0), MESSAGE3);
                        }
                    }
                    // 检查左小括号 "(" 后是否有空格
                    if (elementType == JavaTokenType.LPARENTH) {
                        if (nextSibling != null && nextSibling.getNode().getElementType() == TokenType.WHITE_SPACE) {
                            holder.registerProblem(element, new TextRange(element.getTextLength(), element.getTextLength()), MESSAGE1);
                        }
                    }
                    // 检查右小括号 ")"，如果后面不是右大括号 "}"，则检查前后是否有空格
                    if (elementType == JavaTokenType.RPARENTH) {
                        if (nextSibling != null && nextSibling.getNode().getElementType() != JavaTokenType.RBRACE) {
                            if (prevSibling != null && prevSibling.getNode().getElementType() == TokenType.WHITE_SPACE) {
                                holder.registerProblem(prevSibling, new TextRange(0, prevSibling.getTextLength()), MESSAGE2);
                            }
                        }
                    }
                }
            }

        };
    }


}
