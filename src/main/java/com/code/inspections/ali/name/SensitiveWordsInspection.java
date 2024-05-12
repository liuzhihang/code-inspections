package com.code.inspections.ali.name;

import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.code.inspections.bundle.CodeInspectionsBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 检查Java源代码中的字符串字面量和注释，以识别并标记出潜在的种族歧视性或侮辱性词汇。
 * 提供快速修复功能跳转到需要修改的词汇
 *
 * @author liuzhihang
 * @since 2024-04-30
 */
public class SensitiveWordsInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 错误提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.sensitive.words.message");
    /**
     * 快速修复提示信息
     */
    private static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.name.sensitive.words.fix");
    /**
     * 敏感词汇列表，用于匹配字符串字面量和注释中的词汇。
     */
    private static final List<String> SENSITIVE_PATTERNS = List.of(
            "blackList", "whiteList", "slave", "SB", "WTF"
            // 可根据需要添加更多敏感词汇
    );

    /**
     * 敏感词汇的正则表达式模式，用于匹配字符串字面量和注释中的词汇。
     */
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile("(?i)\\b(" + String.join("|", SENSITIVE_PATTERNS) + ")\\b");

    /**
     * 构建访问者，用于遍历Psi元素并检查敏感词汇。
     *
     * @param holder     问题持有者，用于报告发现的问题。
     * @param isOnTheFly 指示检查是否在“飞行模式”下执行。
     * @return 返回一个PsiElementVisitor实例，用于递归遍历Java元素。
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            private final Pattern pattern = Pattern.compile(String.join("|", SENSITIVE_PATTERNS));


            /**
             * 访问注释，检查是否包含敏感词汇。
             *
             * @param comment 注释元素。
             */
            @Override
            public void visitComment(@NotNull PsiComment comment) {
                super.visitComment(comment);
                String commentText = comment.getText();
                Matcher matcher = SENSITIVE_PATTERN.matcher(commentText);

                while (matcher.find()) {
                    // 精确到敏感词，避免包括注释符号
                    holder.registerProblem(comment, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new JumpToEditLocationQuickFix(comment));
                }
            }

            /**
             * 访问字段，检查其名称是否包含敏感词汇。
             *
             * @param field 字段元素。
             */
            @Override
            public void visitField(@NotNull PsiField field) {
                super.visitField(field);
                String text = field.getName();
                if (pattern.matcher(text).find()) {
                    holder.registerProblem(field, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new JumpToEditLocationQuickFix(field));
                }
            }


        };
    }

    /**
     * 提供快速修复操作，用于替换或移除敏感词汇。
     */
    private static class JumpToEditLocationQuickFix implements LocalQuickFix {

        /**
         * 要修复的Psi元素。
         */
        @SafeFieldForPreview
        private final PsiElement element;

        /**
         * 构造快速修复实例。
         *
         * @param element 要修复的Psi元素。
         */
        JumpToEditLocationQuickFix(PsiElement element) {
            this.element = element;
        }

        /**
         * 返回快速修复操作的名称。
         *
         * @return 快速修复操作的名称。
         */
        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 应用快速修复操作，将敏感词汇替换为更中性的表达。
         *
         * @param project    当前项目。
         * @param descriptor 问题描述符，包含问题的相关信息。
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            openFileInEditor(project, element.getContainingFile());
        }

        /**
         * 打开包含敏感词汇的Psi文件，并将光标定位到敏感词汇的位置。
         *
         * @param project 当前项目。
         * @param psiFile 包含敏感词汇的Psi文件。
         */
        private void openFileInEditor(Project project, PsiFile psiFile) {
            Editor editor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, psiFile.getVirtualFile()), true);
            if (editor != null) {
                int offset = element.getTextRange().getStartOffset();
                editor.getCaretModel().moveToOffset(offset);
                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER_UP);
            }
        }


    }
}
