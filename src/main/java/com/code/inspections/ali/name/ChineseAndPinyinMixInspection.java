package com.code.inspections.ali.name;

import com.code.inspections.bundle.CodeInspectionsBundle;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * 【强制】所有编程相关的命名严禁使用拼音与英文混合的方式，更不允许直接使用中文的方式。
 * 说明：正确的英文拼写和语法可以让阅读者易于理解，避免歧义。注意，即使纯拼音命名方式也要避免采用。
 * 正例：ali / alibaba / taobao / kaikeba / aliyun / youku / hangzhou 等国际通用的名称，可视同英文。
 * 反例：DaZhePromotion【打折】/ getPingfenByName()【评分】 / String fw【福娃】/ int 变量名 = 3
 *
 * @author liuzhihang
 * @version ChineseAndPinyinMixInspection.java, v 0.1 2024/4/30 22:25 zijun
 */
public class ChineseAndPinyinMixInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * 错误提示
     */
    private static final String MESSAGE = CodeInspectionsBundle.message("ali.p3c.name.chinese.pinyin.mix.message");
    /**
     * 匹配拼音与英文混合
     */
    private static final Pattern PINYIN_ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]+[\\u4e00-\\u9fa5]|[\\u4e00-\\u9fa5]+[a-zA-Z]");
    /**
     * 匹配中文字符
     */
    private static final Pattern CHINESE_CHAR_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");

    /**
     * 构建检查器
     *
     * @param holder     ProblemsHolder
     * @param isOnTheFly 是否是 on the fly
     * @return PsiElementVisitor
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitIdentifier(@NotNull PsiIdentifier identifier) {
                super.visitIdentifier(identifier);
                String name = identifier.getText();

                // 检查拼音与英文混合
                if (PINYIN_ENGLISH_PATTERN.matcher(name).find()) {
                    holder.registerProblem(identifier, MESSAGE, ProblemHighlightType.WARNING);
                }

                // 检查是否包含中文字符
                if (CHINESE_CHAR_PATTERN.matcher(name).find()) {
                    holder.registerProblem(identifier, MESSAGE, ProblemHighlightType.WARNING);
                }
            }
        };
    }
}
