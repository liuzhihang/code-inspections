package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;


/**
 * 检查Java代码中是否存在子父类成员变量或不同代码块局部变量之间完全相同的命名，
 * 以提升代码的可理解性。
 *
 * @author liuzhihang
 * @version DuplicateVariableNamingInspection.java, v 0.1 2024/5/4 liuzhihang
 */
public class DuplicateVariableNamingInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 错误提示信息。
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.duplicate.variable.naming.message");

    /**
     * 创建一个快速修复，用于解决命名冲突的问题。
     */
    private static final String QUICK_FIX = CodeInspectionsBundle.message("ali.p3c.name.duplicate.variable.naming.fix");

    /**
     * 创建一个访问者来检查字段是否与超类中的字段同名。
     *
     * @param holder     用于存储问题的容器。
     * @param isOnTheFly 表示检查是否在飞行模式下进行。
     * @return 返回一个Java元素遍历访问者。
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            /**
             * 访问字段，检查该字段是否在超类中已定义。
             *
             * @param field 当前正在访问的字段。
             */
            @Override
            public void visitField(@NotNull PsiField field) {
                super.visitField(field);
                PsiClass containingClass = field.getContainingClass();
                if (containingClass != null) {
                    PsiClass superClass = containingClass.getSuperClass();
                    if (superClass != null) {
                        PsiField superField = superClass.findFieldByName(field.getName(), true);
                        if (superField != null) {
                            // 如果找到同名字段，则注册一个问题，并提供一个快速修复方案。
                            holder.registerProblem(field, MESSAGE, ProblemHighlightType.ERROR, new RemoveFieldAndAccessorsFix(field));
                        }
                    }
                }
            }

            /**
             * 访问类，检查类中是否存在字段与超类中的字段重名。
             *
             * @param psiClass 当前正在访问的类。
             */
            @Override
            public void visitClass(@NotNull PsiClass psiClass) {
                super.visitClass(psiClass);
                if (psiClass.isEnum() || psiClass.isInterface()) {
                    return; // Enums and interfaces do not have getters/setters to check.
                }

                PsiClass superClass = psiClass.getSuperClass();
                if (superClass != null) {
                    for (PsiField superField : superClass.getFields()) {
                        PsiField subclassField = psiClass.findFieldByName(superField.getName(), true);
                        if (subclassField != null) {
                            // 已经在visitField中处理了字段重复的情况，这里不再处理。
                            continue;
                        }
                        PsiMethod getter = findGetter(superField, psiClass);
                        PsiMethod setter = findSetter(superField, psiClass);
                        if (getter != null || setter != null) {
                            holder.registerProblem(psiClass, MESSAGE, ProblemHighlightType.ERROR, new RemoveRedundantAccessorsFix(superField, getter, setter));
                        }
                    }
                }
            }
        };
    }

    /**
     * 查找给定字段的getter方法。
     *
     * @param field  查找getter方法的目标字段。
     * @param aClass 字段所属的类。
     * @return 找到的getter方法，如果未找到则返回null。
     */
    private static PsiMethod findGetter(PsiField field, PsiClass aClass) {
        String getterName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        for (PsiMethod method : aClass.findMethodsByName(getterName, true)) {
            if (method.getParameterList().getParametersCount() == 0 && method.getReturnType().equals(field.getType())) {
                return method;
            }
        }
        return null;
    }

    /**
     * 查找给定字段的setter方法。
     *
     * @param field  查找setter方法的目标字段。
     * @param aClass 字段所属的类。
     * @return 找到的setter方法，如果未找到则返回null。
     */
    private static PsiMethod findSetter(PsiField field, PsiClass aClass) {
        String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        for (PsiMethod method : aClass.findMethodsByName(setterName, true)) {
            if (method.getParameterList().getParametersCount() == 1 && method.getParameterList().getParameters()[0].getType().equals(field.getType())) {
                return method;
            }
        }
        return null;
    }

    /**
     * 提供一个快速修复方案，用于删除具有相同名称的字段及其访问器方法。
     */
    private static class RemoveFieldAndAccessorsFix implements LocalQuickFix {
        private final PsiField field;

        /**
         * 构造函数。
         *
         * @param field 需要被删除的字段。
         */
        RemoveFieldAndAccessorsFix(@NotNull PsiField field) {
            this.field = field;
        }

        /**
         * 获取快速修复的显示名称。
         *
         * @return 修复的显示名称。
         */
        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 应用快速修复，删除字段及其对应的getter和setter方法。
         *
         * @param project    当前项目。
         * @param descriptor 问题描述符。
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiMethod getter = findGetter(field, field.getContainingClass());
            PsiMethod setter = findSetter(field, field.getContainingClass());

            // 删除getter、setter和字段本身。
            if (getter != null) {
                getter.delete();
            }
            if (setter != null) {
                setter.delete();
            }
            field.delete();
        }
    }

    /**
     * 提供一个快速修复方案，用于删除继承自父类的字段及其对应的getter和setter方法。
     */
    private static class RemoveRedundantAccessorsFix implements LocalQuickFix {
        private final PsiField superField;
        private final PsiMethod getter;
        private final PsiMethod setter;

        RemoveRedundantAccessorsFix(@NotNull PsiField superField, PsiMethod getter, PsiMethod setter) {
            this.superField = superField;
            this.getter = getter;
            this.setter = setter;
        }

        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return QUICK_FIX;
        }

        /**
         * 应用快速修复，删除继承自父类的字段及其对应的getter和setter方法。
         *
         * @param project    当前项目。
         * @param descriptor 问题描述符。
         */
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            if (getter != null) {
                getter.delete();
            }
            if (setter != null) {
                setter.delete();
            }
        }
    }

}
