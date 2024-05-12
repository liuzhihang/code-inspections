package com.code.inspections.bundle;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * 此类用于代码检查的本地化资源包，继承自AbstractBundle类，提供针对各种检查键的本地化消息。
 */
public class CodeInspectionsBundle extends AbstractBundle {

    /**
     * 用于加载消息的资源包键。
     */
    @NonNls
    public static final String BUNDLE = "bundle.CodeInspectionsBundle";

    /**
     * 用于访问本地化消息的CodeInspectionsBundle单例实例。
     */
    private static final CodeInspectionsBundle INSTANCE = new CodeInspectionsBundle();

    /**
     * 私有构造函数，使用指定的资源包键初始化资源包。
     */
    public CodeInspectionsBundle() {
        super(BUNDLE);
    }

    /**
     * 根据给定的键获取本地化消息，可选的参数用于消息格式化。
     *
     * @param key    要获取的本地化消息的键，注解确保引用正确的资源包。
     * @param params 格式化消息时可选的参数。
     * @return 格式化的本地化消息字符串。
     */
    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}

