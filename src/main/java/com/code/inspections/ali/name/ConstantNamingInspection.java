package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 常量命名检查工具类，用于检查常量是否遵循大写字母和下划线的命名规范
 */
public class ConstantNamingInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 检查不合规常量的提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.constant.name.upper.case.message");

    /**
     * 提供快速修复方案的提示信息
     */
    private static final String QUICK_FIX_MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.constant.name.upper.case.fix");

    /**
     * 构建检查访问者，针对字段进行检查
     *
     * @param holder 问题持有者
     * @param isOnTheFly 是否为实时检查
     * @return 返回字段检查访问者
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            /**
             * 访问字段，检查是否为静态最终常量并遵循命名规范
             *
             * @param field 被访问的字段
             */
            @Override
            public void visitField(@NotNull PsiField field) {
                // 检查字段是否为静态最终常量
                if (field.hasModifierProperty(PsiModifier.FINAL) && field.hasModifierProperty(PsiModifier.STATIC)) {
                    checkConstantNamingConvention(holder, field);
                }
            }
        };
    }

    /**
     * 检查常量命名是否符合预期规范
     *
     * @param holder 问题持有者
     * @param field 需要检查的字段
     */
    private void checkConstantNamingConvention(ProblemsHolder holder, PsiField field) {
        // 获取字段名并检查是否符合"^[A-Z_]+$"的正则表达式，即全大写字母和下划线
        String fieldName = field.getName();
        if (!fieldName.matches("^[A-Z_]+$")) {
            // 如果不符合，则注册问题，并提供快速修复方案
            holder.registerProblem(field.getNameIdentifier(), MESSAGE, ProblemHighlightType.ERROR, new ConstantNamingQuickFix(fieldName));
        }
    }

    /**
     * 快速修复类，用于将常量名转换为合规的命名形式
     */
    private static class ConstantNamingQuickFix implements LocalQuickFix {
        private final String currentName;

        /**
         * 构造函数
         *
         * @param currentName 当前不合规的常量名
         */
        ConstantNamingQuickFix(String currentName) {
            this.currentName = currentName;
        }

        /**
         * 获取快速修复的展示名称
         *
         * @return 修复操作的展示名称
         */
        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX_MESSAGE;
        }

        /**
         * 应用快速修复，将字段名转换为合规的常量命名形式
         *
         * @param project 当前项目
         * @param descriptor 问题描述
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            // 获取问题字段，并尝试进行替换
            PsiField field = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), PsiField.class);
            if (field != null) {
                // 计算新的常量名，并进行替换
                String newName = currentName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
                PsiType fieldType = field.getType();
                String initializer = field.getInitializer() != null ? " = " + field.getInitializer().getText() : "";
                String newFieldDeclaration = fieldType.getCanonicalText() + " " + newName + initializer + ";";
                PsiElementFactory factory = PsiElementFactory.getInstance(project);
                PsiField newField = factory.createFieldFromText(newFieldDeclaration, field);
                field.replace(newField);
            }
        }
    }
}
