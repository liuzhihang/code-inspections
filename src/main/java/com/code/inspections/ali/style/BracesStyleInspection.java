package com.code.inspections.ali.style;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 检查Java代码块的大括号风格是否符合阿里P3C风格指南的规定。
 * 如果大括号内为空，要求写成{}，大括号中间无需换行和空格；
 * 如果是非空代码块，则检查大括号的格式是否符合以下规则：
 * 1）左大括号前不换行。
 * 2）左大括号后换行。
 * 3）右大括号前换行。
 * 4）右大括号后还有 else 等代码则不换行；表示终止的右大括号后必须换行。
 *
 * @author liuzhihang
 * @version BracesStyleInspection.java, v 0.1 2024/5/5 liuzhihang
 */
public class BracesStyleInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final String MESSAGE0 = CodeInspectionsBundle.message("ali.p3c.style.braces.style.message0");
    private static final String MESSAGE1 = CodeInspectionsBundle.message("ali.p3c.style.braces.style.message1");
    private static final String MESSAGE2 = CodeInspectionsBundle.message("ali.p3c.style.braces.style.message2");
    private static final String MESSAGE3 = CodeInspectionsBundle.message("ali.p3c.style.braces.style.message3");
    private static final String MESSAGE4 = CodeInspectionsBundle.message("ali.p3c.style.braces.style.message4");

    /**
     * 创建检查访问者，用于遍历Java代码元素并检查大括号风格。
     *
     * @param holder     用于存储检查问题的对象。
     * @param isOnTheFly 表示检查是否在编辑时进行。
     * @return 返回检查访问者对象。
     */
    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitCodeBlock(PsiCodeBlock block) {
                super.visitCodeBlock(block);
                // 检查空代码块格式
                if (block.getStatementCount() == 0) {
                    checkEmptyBlockFormat(block, holder);
                } else {
                    // 检查非空代码块格式
                    checkNonEmptyBlockFormat(block, holder);
                }
            }
        };
    }

    /**
     * 检查空代码块的格式是否符合规定。
     *
     * @param block  待检查的代码块。
     * @param holder 用于存储检查问题的对象。
     */
    private void checkEmptyBlockFormat(PsiCodeBlock block, ProblemsHolder holder) {
        String text = block.getText();
        // 如果空代码块没有使用{}，则记录问题
        if (!text.equals("{}")) {
            holder.registerProblem(block, MESSAGE0);
        }
    }

    /**
     * 检查非空代码块的格式是否符合规定。
     *
     * @param block  待检查的代码块。
     * @param holder 用于存储检查问题的对象。
     */
    private void checkNonEmptyBlockFormat(PsiCodeBlock block, ProblemsHolder holder) {
        PsiJavaToken lBrace = block.getLBrace();
        PsiJavaToken rBrace = block.getRBrace();
        if (lBrace != null) {
            // 左大括号前不换行
            if (isNewlineBeforeIgnoringSpaces(lBrace)) {
                holder.registerProblem(lBrace, MESSAGE1);
            }

            // 左大括号后换行
            if (!isNewlineAfterIgnoringSpaces(lBrace)) {
                holder.registerProblem(lBrace, MESSAGE2);
            }
        }

        if (rBrace != null) {
            // 右大括号前换行
            if (!isNewlineBeforeIgnoringSpaces(rBrace)) {
                holder.registerProblem(rBrace, MESSAGE3);
            }

            // 右大括号后还有 else 等代码则不换行；表示终止的右大括号后必须换行
            // 查找右大括号之后的第一个非空白、非注释的元素
            PsiElement nextSibling = PsiTreeUtil.nextLeaf(rBrace);
            if (nextSibling != null) {
                // 跳过可能存在的空白和注释，直接寻找下一个实际的代码元素
                while (nextSibling instanceof PsiWhiteSpace || nextSibling instanceof PsiComment) {
                    nextSibling = PsiTreeUtil.nextLeaf(nextSibling);
                    if (nextSibling == null) break; // 防止无限循环
                }
            }
            // 如果找到了下一个元素并且是else关键字，则不检查换行
            if (nextSibling instanceof PsiKeyword && ((PsiKeyword) nextSibling).getTokenType() == JavaTokenType.ELSE_KEYWORD) {
                // 此处无需操作，因为找到了else，符合不换行的条件
                // 在 else 关键字前不允许有换行
                if (isNewlineBeforeIgnoringSpaces(nextSibling)) {
                    // 如果有换行，这里报错
                    holder.registerProblem(nextSibling, MESSAGE4);
                }
            }
            if (nextSibling == null) {
                // 如果 nextSibling 为空或不是 ELSE 关键字，检查换行
                if (!isNewlineAfterIgnoringSpaces(rBrace)) {
                    holder.registerProblem(rBrace, MESSAGE4);
                }
            }
        }
    }

    /**
     * 检查给定元素前是否换行。
     *
     * @param element 待检查的元素。
     * @return 如果元素前有换行，则返回true，否则返回false。
     */
    /**
     * 检查给定元素前是否直接换行（忽略前导空白）。
     *
     * @param element 待检查的元素。
     * @return 如果元素前直接是换行符（忽略前导空白），则返回true，否则返回false。
     */
    private boolean isNewlineBeforeIgnoringSpaces(PsiElement element) {
        TextRange range = element.getTextRange();
        Document document = element.getContainingFile().getViewProvider().getDocument();
        if (document != null) {
            int startOffset = range.getStartOffset();
            if (startOffset <= 0) return false; // 起始位置不合法

            // 从startOffset开始向前遍历，忽略空白字符直到遇到非空白字符或文档开始
            for (int i = startOffset - 1; i >= 0; i--) {
                char currentChar = document.getCharsSequence().charAt(i);
                if (currentChar == '\n') {
                    // 遇到换行符，且之前是空白字符，满足条件
                    return true;
                } else if (!Character.isWhitespace(currentChar)) {
                    // 遇到非空白字符，不满足条件
                    break;
                }
            }
        }
        return false;
    }

    /**
     * 检查给定元素后是否直接换行（忽略尾随空白）。
     *
     * @param element 待检查的元素。
     * @return 如果元素后直接是换行符（忽略尾随空白），则返回true，否则返回false。
     */
    private boolean isNewlineAfterIgnoringSpaces(PsiElement element) {
        TextRange range = element.getTextRange();
        Document document = element.getContainingFile().getViewProvider().getDocument();
        if (document != null) {
            int endOffset = range.getEndOffset();
            if (endOffset >= document.getTextLength()) return false; // 超出文档长度

            // 从endOffset开始向后遍历，忽略空白字符直到遇到非空白字符或文档结束
            for (int i = endOffset; i < document.getTextLength(); i++) {
                char currentChar = document.getCharsSequence().charAt(i);
                if (currentChar == '\n') {
                    // 遇到换行符，且之后是空白字符，满足条件
                    return true;
                } else if (!Character.isWhitespace(currentChar)) {
                    // 遇到非空白字符，不满足条件
                    break;
                }
            }
        }
        return false;
    }


}
