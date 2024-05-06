package com.code.inspections.ali.style;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * 【强制】单行字符数限制不超过 120 个，超出需要换行，换行时遵循如下原则：
 * 1）第二行相对第一行缩进 4 个空格，从第三行开始，不再继续缩进，参考示例。
 * 2）运算符与下文一起换行。
 * 3）方法调用的点符号与下文一起换行。
 * 4）方法调用中的多个参数需要换行时，在逗号后进行。
 * 5）在括号前不要换行
 *
 * @author liuzhihang
 * @version LineLengthLimitInspection.java, v 0.1 2024/5/6 liuzhihang
 */
public class LineLengthLimitInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 单行字符数限制
     */
    private int maxLineLength = 120;

    /**
     * 提示信息
     */
    private static final String MESSAGE1 = CodeInspectionsBundle.message("ali.p3c.style.line.length.message1");
    /**
     * 提示信息
     */
    private static final String MESSAGE2 = CodeInspectionsBundle.message("ali.p3c.style.line.length.message2");
    /**
     * 提示信息
     */
    private static final String MESSAGE3 = CodeInspectionsBundle.message("ali.p3c.style.line.length.message3");
    /**
     * 提示信息
     */
    private static final String MESSAGE4 = CodeInspectionsBundle.message("ali.p3c.style.line.length.message4");

    /**
     * 创建一个选项面板，用于设置和显示单行字符数限制。
     *
     * @return JComponent 返回一个包含标签和文本字段的面板，用户可以在其中输入单行字符数限制。
     */
    @Override
    public JComponent createOptionsPanel() {
        // 创建一个使用FlowLayout左对齐的面板
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 添加"单行字符数："标签
        JLabel label = new JLabel("单行字符数：");
        panel.add(label);

        // 创建一个文本字段，用于输入单行字符数限制，并设置工具提示文本
        JTextField inputField = new JTextField(Integer.toString(maxLineLength));
        inputField.setToolTipText("单行字符数");
        panel.add(inputField);

        // 为文本字段添加文档监听器，以在输入时更新maxLineLength
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            // 当文档内容插入、移除或改变时更新maxLineLength
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateMaxLength();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateMaxLength();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateMaxLength();
            }

            // 更新maxLineLength，并在输入无效时回退到之前的值
            private void updateMaxLength() {
                try {
                    maxLineLength = Integer.parseInt(inputField.getText());
                } catch (NumberFormatException ex) {
                    inputField.setText(Integer.toString(maxLineLength));
                }
            }
        });

        return panel;
    }

    /**
     * 构建一个访问者，用于在代码中执行行长度、运算符位置、字符特殊位置的检查。
     *
     * @param holder     问题持有者，用于报告问题。
     * @param isOnTheFly 是否在飞行模式下执行检查。
     * @param session    局部检查会话，提供上下文信息。
     * @return PsiElementVisitor 返回一个实现了PsiElementVisitor接口的对象，该对象会访问并检查代码元素。
     */
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);
                if (file instanceof PsiJavaFile) {
                    Document document = file.getFileDocument();
                    int lineCount = document.getLineCount();
                    for (int i = 0; i < lineCount; i++) {
                        int startOffset = document.getLineStartOffset(i);
                        int endOffset = document.getLineEndOffset(i);
                        TextRange textRange = new TextRange(startOffset, endOffset);
                        String lineText = document.getText(textRange).trim();

                        // 跳过明显的单行和多行注释
                        if (lineText.startsWith("//") || lineText.startsWith("/*") || lineText.startsWith("*") || lineText.endsWith("*/")) {
                            continue;
                        }

                        int lineLength = lineText.length();
                        if (lineLength > maxLineLength) {
                            holder.registerProblem(file, CodeInspectionsBundle.message("ali.p3c.style.line.length.message0", maxLineLength), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, textRange);
                        }

                        // 检查运算符是否出现在行尾
                        if (endsWithAny(lineText, "+", "-", "*", "/", "%", "<<", ">>", ">>>", "|", "^", "!", "~", "=", "+=", "-=", "*=", "/=", "%=", "<<=", ">>=", ">>>=", "&=", "^=", "|=")) {
                            holder.registerProblem(file, MESSAGE1, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, textRange);
                        }

                        // 检查是否以"."结尾
                        if (lineText.endsWith(".")) {
                            holder.registerProblem(file, MESSAGE2, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, textRange);
                        }

                        // 检查是否以","开头
                        if (lineText.startsWith(",")) {
                            holder.registerProblem(file, MESSAGE3, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, textRange);
                        }

                        // 检查是否以"("或")"开头
                        if (lineText.startsWith("(") || lineText.startsWith(")")) {
                            holder.registerProblem(file, MESSAGE4, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, textRange);
                        }
                    }

                }
            }
        };
    }

    /**
     * 检查字符串是否以指定的任何字符结尾。
     *
     * @param text     源字符串。
     * @param suffixes 后缀字符串数组。
     * @return boolean 如果源字符串以任何给定的后缀结尾，则返回true；否则返回false。
     */
    private boolean endsWithAny(String text, String... suffixes) {
        // 遍历后缀数组，检查是否有匹配
        for (String suffix : suffixes) {
            if (text.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }


}
