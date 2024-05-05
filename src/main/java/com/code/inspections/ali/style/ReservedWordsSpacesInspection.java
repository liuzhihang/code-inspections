package com.code.inspections.ali.style;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 【强制】if / for / while / switch / do 等保留字与左右括号之间都必须加空格。
 *
 * @author liuzhihang
 * @version ReservedWordsSpacesInspection.java, v 0.1 2024/5/5 liuzhihang
 */
public class ReservedWordsSpacesInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 错误提示
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.style.reserved.words.spaces.message");

    /**
     * 保留字
     */
    private Set<String> reservedWords = new HashSet<>() {{
        add("if");
        add("for");
        add("while");
        add("do");
        add("switch");
    }};


    /**
     * 创建选项面板，用于展示和输入保留字。
     *
     * @return JComponent 返回一个包含保留字输入字段的面板。
     */
    @Nullable
    @Override
    public JComponent createOptionsPanel() {
        // 创建一个使用左对齐的FlowLayout的JPanel
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 添加一个标签显示"保留字：" 
        JLabel label = new JLabel("保留字：");
        panel.add(label);

        // 创建一个JTextField用于输入保留字，初始值为已定义的保留字列表，长度为20个字符
        JTextField inputField = new JTextField(reservedWords.stream().collect(Collectors.joining(",")), 20);
        inputField.setToolTipText("请输入保留字！");
        panel.add(inputField);

        return panel;
    }

    /**
     * 构建一个访问者对象，用于检查代码中的括号使用是否符合规范。
     *
     * @param holder     问题持有者，用于报告检查中发现的问题。
     * @param isOnTheFly 表示检查是否在飞行模式下进行，影响问题的报告方式。
     * @param session    局部检查会话，提供检查过程中的上下文信息。
     * @return 返回一个Java元素访问者对象，用于遍历和检查Java代码元素。
     */
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new JavaElementVisitor() {
            // 检查if、for、while、do、switch等保留字与左括号之间是否有空格
            private void checkReservedWordSpacing(PsiElement element) {
                if (element instanceof PsiKeyword keyword && reservedWords.contains(keyword.getText())) {
                    PsiElement nextSibling = keyword.getNextSibling();
                    if (nextSibling != null && nextSibling.getNode().getElementType() != JavaTokenType.WHITE_SPACE) {
                        holder.registerProblem(nextSibling, MESSAGE);
                    }
                }
            }

            /**
             * 访问if语句，并检查'if'关键字与左括号之间的空格。
             * @param statement 表示if语句的PsiIfStatement对象。
             */
            @Override
            public void visitIfStatement(@NotNull PsiIfStatement statement) {
                checkReservedWordSpacing(statement.getFirstChild()); // 检查'if'与左括号
            }

            /**
             * 访问for语句，并检查'for'关键字与左括号之间的空格。
             * @param statement 表示for语句的PsiForStatement对象。
             */
            @Override
            public void visitForStatement(@NotNull PsiForStatement statement) {
                checkReservedWordSpacing(statement.getFirstChild()); // 检查'for'与左括号
            }

            /**
             * 访问while语句，并检查'while'关键字与左括号之间的空格。
             * @param statement 表示while语句的PsiWhileStatement对象。
             */
            @Override
            public void visitWhileStatement(@NotNull PsiWhileStatement statement) {
                checkReservedWordSpacing(statement.getFirstChild()); // 检查'while'与左括号
            }

            /**
             * 访问do-while语句，并检查'do'关键字与左括号之间的空格。
             * 注意：do-while的条件在while关键字后，此处逻辑需针对特殊结构进行调整。
             * @param statement 表示do-while语句的PsiDoWhileStatement对象。
             */
            @Override
            public void visitDoWhileStatement(@NotNull PsiDoWhileStatement statement) {
                checkReservedWordSpacing(statement.getFirstChild()); // 检查'do'与左括号
            }

            /**
             * 访问switch语句，并检查'switch'关键字与左括号之间的空格。
             * @param statement 表示switch语句的PsiSwitchStatement对象。
             */
            @Override
            public void visitSwitchStatement(PsiSwitchStatement statement) {
                checkReservedWordSpacing(statement.getFirstChild()); // 检查'switch'与左括号
            }

            // 注意：对于switch标签（PsiSwitchLabelStatement），直接检查default情况下的case值逻辑需要更正或移除，因为它不符合新逻辑
        };
    }
}
