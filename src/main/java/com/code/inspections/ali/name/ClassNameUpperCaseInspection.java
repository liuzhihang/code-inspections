package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 【强制】类名使用 UpperCamelCase 风格，以下情形例外：DO / PO / DTO / BO / VO / UID 等。
 *
 * @author liuzhihang
 * @version ClassNameUpperCaseInspection.java, v 0.1 2024/5/1 liuzhihang
 */
public class ClassNameUpperCaseInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 自定义后缀
     */
    private List<String> customSuffixes = new ArrayList<>() {{
        add("DO");
        add("PO");
        add("DTO");
        add("BO");
        add("VO");
        add("UID");
    }};
    /**
     * 错误提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.class.name.upper.case.message");

    /**
     * 错误修复提示信息
     */
    private static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.name.class.name.upper.case.fix");

    /**
     * 创建检查访问者
     *
     * @param holder     问题持有者
     * @param isOnTheFly 是否为实时检查
     * @return 返回问题检查访问者
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            /**
             * 访问类
             *
             * @param psiClass  当前正在检查的类
             */
            @Override
            public void visitClass(@NotNull PsiClass psiClass) {
                String className = psiClass.getName();
                // 检查是否为例外类名，如DO_、PO_等开头的类名不需要遵循UpperCamelCase规范
                if (!isExceptionClassName(className)) {
                    // 使用正则表达式检查类名是否以大写字母开头，且仅包含字母和数字
                    if (!className.matches("^[A-Z][a-zA-Z0-9]*$")) {
                        holder.registerProblem(psiClass.getNameIdentifier(), MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new RenameToUpperCaseFix(psiClass));
                    }
                }
            }

            /**
             * 判断类名是否为例外类名（不需要遵循UpperCamelCase规范）
             *
             * @param className  类名
             * @return 如果是异常类名，返回true；否则返回false
             */
            private boolean isExceptionClassName(String className) {
                for (String customSuffix : customSuffixes) {
                    if (className.endsWith(customSuffix.trim())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * 创建选项面板
     *
     * @return 选项面板
     */
    @Override
    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 使用FlowLayout以左对齐方式布局

        JLabel label = new JLabel("例外后缀：");
        panel.add(label);

        JTextField inputField = new JTextField(customSuffixes.stream().collect(Collectors.joining(",")), 20);
        inputField.setToolTipText("Enter comma-separated class name suffixes.");
        panel.add(inputField);

        return panel;
    }


    /**
     * 提供一个快速修正，将类名改为UpperCamelCase
     */
    private static class RenameToUpperCaseFix extends LocalQuickFixOnPsiElement {

        /**
         * 需要修正的类
         */
        private final PsiClass psiClass;

        /**
         * 构造函数
         *
         * @param psiClass 需要修正的类
         */
        public RenameToUpperCaseFix(PsiClass psiClass) {
            super(psiClass.getNameIdentifier());
            this.psiClass = psiClass;
        }

        /**
         * 获取修正操作的展示文本
         *
         * @return 修正操作的文本
         */
        @Override
        public @IntentionName @NotNull String getText() {
            return getFamilyName();
        }

        /**
         * 获取修正操作的家族名称
         *
         * @return 修正操作的家族名称
         */
        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 执行修正操作
         *
         * @param project      当前项目
         * @param file         包含类的文件
         * @param startElement 修正操作的起始元素
         * @param endElement   修正操作的结束元素
         */
        @Override
        public void invoke(@NotNull Project project, @NotNull PsiFile file, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
            // 生成新的类名
            String newName = toUpperCamelCase(psiClass.getName());
            try {
                // 在写入操作中执行类名修改
                WriteCommandAction.writeCommandAction(project, file).run(() -> psiClass.setName(newName));
            } catch (Exception e) {
                // 修改失败的处理
                System.err.println("Failed to rename class: " + e.getMessage());
            }
        }

        /**
         * 将给定字符串转换为UpperCamelCase格式
         *
         * @param className 待转换的类名
         * @return 转换后的类名
         */
        @Nullable
        private static String toUpperCamelCase(String className) {
            if (className == null || className.isEmpty()) {
                return className;
            }
            StringBuilder camelCaseName = new StringBuilder();
            String[] parts = className.split("_");
            for (String part : parts) {
                if (part.isEmpty()) continue; // 跳过空部分
                camelCaseName.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) camelCaseName.append(part.substring(1));
            }
            return camelCaseName.toString();
        }
    }
}
