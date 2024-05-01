package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 【强制】方法名、参数名都统一使用 lowerCamelCase 风格的检查工具类。
 *
 * @author zijun.lzh
 * @version MethodAndParameterNamingInspection.java, v 0.1 2024/5/1 zijun.lzh
 */
public class MethodAndParameterNamingInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 提示信息
     */
    public static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.method.name.lower.case.message");

    /**
     * 快速修复
     */
    public static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.name.method.name.lower.case.fix");

    // 是否忽略Javadoc中的命名
    public boolean ignoreInJavadoc = false;

    /**
     * 创建设置面板
     *
     * @return 设置面板的组件
     */
    @NotNull
    @Override
    public JComponent createOptionsPanel() {
        return new SingleCheckboxOptionsPanel(
                "Ignore names in Javadoc",
                this, "ignoreInJavadoc"
        );
    }

    /**
     * 构建检查访问者
     *
     * @param holder     问题持有者
     * @param isOnTheFly 是否为实时检查
     * @return 检查访问者对象
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            /**
             * 检查方法及其参数名
             *
             * @param method 方法元素
             */
            @Override
            public void visitMethod(@NotNull PsiMethod method) {
                checkName(holder, method.getNameIdentifier(), method);
                for (PsiParameter parameter : method.getParameterList().getParameters()) {
                    checkName(holder, parameter.getNameIdentifier(), parameter);
                }
            }

            /**
             * 检查名称是否符合lowerCamelCase风格
             *
             * @param pair 参数对元素
             */
            @Override
            public void visitNameValuePair(@NotNull PsiNameValuePair pair) {
                checkName(holder, pair.getNameIdentifier(), pair);
            }

            /**
             * 校验名称
             *
             * @param holder 问题持有者
             * @param identifier 名称标识符
             * @param element 相关元素
             */
            private void checkName(ProblemsHolder holder, PsiIdentifier identifier, PsiElement element) {
                if (identifier != null) {
                    String name = identifier.getText();
                    if (!isValidName(name, element)) {
                        holder.registerProblem(identifier, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new LowerCamelCaseFix());
                    }
                }
            }
        };
    }

    /**
     * 判断名称是否有效
     *
     * @param name 名称
     * @return 是否符合lowerCamelCase风格
     */
    private boolean isValidName(String name, PsiElement element) {
        // 如果配置了忽略Javadoc中的名称，则进行忽略判断
        if (ignoreInJavadoc && isInsideJavadoc(element)) {
            return true;
        }
        // 使用正则表达式检查名称是否符合lowerCamelCase风格
        return name.matches("^[a-z][a-zA-Z0-9]*$");
    }

    /**
     * 判断元素是否位于Javadoc中
     *
     * @param element 目标元素
     * @return 元素是否在Javadoc内
     */
    private boolean isInsideJavadoc(PsiElement element) {
        PsiDocComment docComment = PsiTreeUtil.getParentOfType(element, PsiDocComment.class);
        return docComment != null && docComment.getTextRange().contains(element.getTextRange());
    }

    /**
     * lowerCamelCase快速修复实现
     */
    private static class LowerCamelCaseFix implements LocalQuickFix {
        /**
         * 获取修复的家族名称
         *
         * @return 修复操作的名称
         */
        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 应用修复
         *
         * @param project    项目
         * @param descriptor 问题描述符
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement identifierElement = descriptor.getPsiElement();
            if (identifierElement instanceof PsiIdentifier) {
                String newName = NamingHelper.toLowerCamelCase(identifierElement.getText());
                PsiElementFactory factory = PsiElementFactory.getInstance(project);
                PsiIdentifier newIdentifier = factory.createIdentifier(newName);
                identifierElement.replace(newIdentifier);
            }
        }
    }

    /**
     * 命名辅助类
     */
    private static class NamingHelper {
        /**
         * 将下划线分隔的字符串转换为lowerCamelCase风格
         *
         * @param name 输入字符串
         * @return 转换后的字符串
         */
        static String toLowerCamelCase(String name) {
            String[] parts = name.split("_");
            StringBuilder camelCaseStr = new StringBuilder(parts[0].toLowerCase());
            for (int i = 1; i < parts.length; i++) {
                camelCaseStr.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1).toLowerCase());
            }
            return camelCaseStr.toString();
        }
    }
}
