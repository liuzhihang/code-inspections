## 开发指南

本工程全程使用 AI 进行开发，推荐使用[通义灵码插件](https://tongyi.aliyun.com/lingma/download)进行开发，如果你能使用 GPT4 也可以使用 GPT4 进行开发。

### 1. 开发代码
开发中直接通过窗口进行对话即可，下面是对话术语，自己可以使用其他术语进行开发。

```text
请实现 AbstractBaseJavaLocalInspectionTool，完成 #{xxx}
对于检查出的错误请创建内部类实现 LocalQuickFix，从而快速修复问题。
```
其中 `#{xxx}` 为占位符，直接复制开发手册上的标题即可。

- 举例

```text
请实现 AbstractBaseJavaLocalInspectionTool，完成【强制】所有编程相关的命名均不能以下划线或美元符号开始，也不能以下划线或美元符号结束。
对于检查出的错误请创建内部类实现 LocalQuickFix，从而快速修复问题。
```

注意最后补全相关注释，可以全选，然后`生成代码注释`。

包名要求
```text
# 命名风格
com.code.inspections.ali.name
# 常量定义
com.code.inspections.ali.constant
# 代码格式
com.code.inspections.ali.style
# OOP 规约
com.code.inspections.ali.oop
# 日期时间
com.code.inspections.ali.datetime
# 集合处理
com.code.inspections.ali.collections
# 并发处理
com.code.inspections.ali.concurrent
# 控制语句
com.code.inspections.ali.control
# 注释规约
com.code.inspections.ali.comment
# 前后端规约
com.code.inspections.ali.frontend
# 其他
com.code.inspections.ali.other
# 日志规约
com.code.inspections.ali.logging
# 异常处理
com.code.inspections.ali.exception
```

### 2. 配置 bundle
bundle 中为配置文件，主要作用是国际化使用，配置文件使用 `.properties` 格式。

- 举例
```properties
ali.p3c.name.convention.key=【强制】所有编程相关的命名均不能以下划线或美元符号开始，也不能以下划线或美元符号结束。
ali.p3c.name.convention.message=【强制】所有编程相关的命名均不能以下划线或美元符号开始，也不能以下划线或美元符号结束。
ali.p3c.name.convention.fix=移除下划线或美元符号
```
其中 `key` 在 plugin.xml 中使用，`message` 为提示信息，`fix` 为快速修复信息，是在代码中使用。

### 3. 配置 plugin.xml
plugin.xml 主要是注册到设置中，并且可以配置一些默认的配置。

- 举例
```xml
<!-- 【强制】所有编程相关的命名均不能以下划线或美元符号开始，也不能以下划线或美元符号结束。 -->
<localInspection language="JAVA" groupPath="Ali-P3C" groupName="Name" enabledByDefault="true" level="ERROR"
                 bundle="bundle.CodeInspectionsBundle" key="ali.p3c.name.convention.key"
                 implementationClass="com.code.inspections.ali.name.NamingConventionInspection"/>
```

通过以上三个部分，基本就可以完成开发，测试过程如下：

## 测试自测
打开 Gradle 菜单，选择 `intellij` -> `runlde`，即可本地打开工程测试。