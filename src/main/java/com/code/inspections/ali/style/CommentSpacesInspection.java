package com.code.inspections.ali.style;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】注释的双斜线与注释内容之间有且仅有一个空格。
 *
 * @author liuzhihang
 * @version SingleSpaceBetweenCommentSlashesAndTextInspection.java, v 0.1 2024/5/6 liuzhihang
 */
public class CommentSpacesInspection extends LocalInspectionTool {

    /**
     * 提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.style.comment.spaces.message");


    /**
     * 创建并返回一个访问者对象，该对象会访问Java代码中的注释元素。
     * 这是用于语法检查或类似工具的钩子方法。
     *
     * @param holder     用于报告问题的容器。当找到潜在问题时，会使用此容器来注册问题。
     * @param isOnTheFly 表示是否在飞行模式下执行检查。true表示是在用户输入时实时进行的检查，false表示是在完整项目分析时进行的检查。
     * @return 返回一个实现了PsiElementVisitor接口的对象，该对象在访问Java源代码元素时会执行特定的逻辑。
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        // 创建并返回一个Java元素访问者，特别关注注释元素
        return new JavaElementVisitor() {
            /**
             * 当访问到一个注释元素时，会执行此方法。
             * 主要检查注释是否以特定方式开始，若是，则认为是不规范的注释格式并报告问题。
             *
             * @param comment 代表被访问的注释元素。
             */
            @Override
            public void visitComment(@NotNull PsiComment comment) {
                // 获取注释的文本内容
                String commentText = comment.getText();
                if (!commentText.startsWith("//")) {
                    return;
                }
                // 寻找双斜杠后的第一个字符
                int start = commentText.indexOf("//") + 2;
                if (start < commentText.length()) {
                    // 检查双斜杠后的第一个字符是否不为空格，或紧随其后的字符也是空格
                    char firstCharAfterSlashes = commentText.charAt(start);
                    if (firstCharAfterSlashes != ' ' || start + 1 < commentText.length() && commentText.charAt(start + 1) == ' ') {
                        // 如果检查失败，即发现不规范的注释，则注册一个问题
                        holder.registerProblem(comment, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }

}
