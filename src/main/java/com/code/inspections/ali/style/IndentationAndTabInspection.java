package com.code.inspections.ali.style;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.annotations.NotNull;

/**
 * 【强制】采用 4 个空格缩进，禁止使用 Tab 字符。
 *
 * @author liuzhihang
 * @version IndentationAndTabInspection.java, v 0.1 2024/5/6 liuzhihang
 */
public class IndentationAndTabInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 提示信息
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.style.indent.spaces.message");

    /**
     * 构建并返回一个访问者对象，用于遍历Java代码元素并检查其格式规范。
     *
     * @param holder     用于存储检查过程中发现的问题的容器。
     * @param isOnTheFly 表示检查是否在飞行模式下进行，即实时编码时的检查。
     * @return 返回一个实现了JavaRecursiveElementWalkingVisitor的匿名类对象，该对象会遍历代码中的每一个元素并执行特定的检查逻辑。
     */
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            /**
             * 访问空白字符元素，检查是否符合缩进规则。
             *
             * @param space 表示一个空白字符元素，可能是空格或换行符等。
             */
            @Override
            public void visitWhiteSpace(@NotNull PsiWhiteSpace space) {
                super.visitWhiteSpace(space);
                String text = space.getText();
                if (text.contains("\t")) {
                    // 检查是否使用了Tab字符进行缩进，如果是，则注册一个错误。
                    holder.registerProblem(space, MESSAGE, ProblemHighlightType.ERROR);
                }
            }


        };
    }

}
