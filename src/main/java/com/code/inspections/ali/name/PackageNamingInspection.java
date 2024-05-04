package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】包名统一使用小写，点分隔符之间有且仅有一个自然语义的英语单词。包名统一使用单数形
 * 式，但是类名如果有复数含义，类名可以使用复数形式。
 * <p>
 * 只检查是否小写
 *
 * @author liuzhihang
 * @version PackageNamingInspection.java, v 0.1 2024/5/4 liuzhihang
 */
public class PackageNamingInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 错误提示信息
     */
    public static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.package.naming.message");

    /**
     * 构建并返回一个PsiElementVisitor对象，该对象会遍历Java文件中的元素。
     *
     * @param holder     用于存储检查过程中发现的问题。
     * @param isOnTheFly 表示检查是否是在编辑过程中实时进行的。
     * @return 返回一个PsiElementVisitor实例，该实例在访问文件时会检查包命名是否全部小写。
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            /**
             * 访问一个PsiFile文件时触发的方法。
             *
             * @param file 当前正在访问的文件。
             */
            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);
                // 获取文件所在的目录
                PsiDirectory directory = file.getContainingDirectory();
                if (directory != null) {
                    // 将目录路径转换为包名，且将路径分隔符替换为点号
                    String packageName = directory.getVirtualFile().getCanonicalPath().replace('/', '.');
                    // 检查转换后的包名是否全为小写，若不是，则注册一个错误问题
                    if (!packageName.equals(packageName.toLowerCase())) {
                        holder.registerProblem(directory, MESSAGE, ProblemHighlightType.ERROR);
                    }
                }
            }
        };
    }

}
