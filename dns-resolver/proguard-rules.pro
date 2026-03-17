# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 1. 保留 CustomDns 类本身及其公共构造函数、公共方法和公共字段
# 'CustomDns' 类是整个库的入口，外部会直接实例化和调用。
-keep class com.hezd.dnsresolver.CustomDns {
    public <init>(...); # 保留所有公共构造函数
    public <methods>;   # 保留所有公共方法
    public <fields>;    # 保留所有公共字段
}

# 新增：保留 CustomDns 的伴生对象 (Companion object) 及其所有公共方法和字段
# clearCache() 方法是静态方法，位于伴生对象中，需要显式保留。
-keep class com.hezd.dnsresolver.CustomDns$Companion {
    public <methods>; # 保留伴生对象中的所有公共方法，包括 clearCache()
    public <fields>;  # 保留伴生对象中的所有公共字段（如果有的话，例如常量）
}

# 2. 保留 CustomDns.Builder 类及其公共构造函数、公共方法和公共字段
# 'Builder' 类是用于构建 CustomDns 实例的公共 API。
-keep class com.hezd.dnsresolver.CustomDns$Builder {
    public <init>(...); # 保留所有公共构造函数
    public <methods>;   # 保留所有公共方法
    public <fields>;    # 保留所有公共字段
}

# 2. 保留 CustomDnsNetworkRegister.Builder 类及其公共构造函数、公共方法和公共字段
-keep class com.hezd.dnsresolver.CustomDnsNetworkRegister {
    public <init>(...); # 保留所有公共构造函数
    public <methods>;   # 保留所有公共方法
    public <fields>;    # 保留所有公共字段
}

# 2. 保留 DnsExecutionStrategy 枚举类及其所有字段和方法
# 枚举类通常需要完整保留，因为其常量名可能会被外部代码引用。
-keep enum com.hezd.dnsresolver.DnsExecutionStrategy {
    *;
}

# =====================================================================
# 第三方库 dnsjava (org.xbill.DNS) 混淆规则
# = =====================================================================
# 保留 dnsjava 整个包下的所有类和成员，不混淆
-keep class org.xbill.DNS.** { *; }

## 1. 保留 CustomDns 类本身及其公共构造函数、公共方法和公共字段
## 'CustomDns' 类是整个库的入口，外部会直接实例化和调用。
## -keep 作用：保留指定的类和类的成员，不进行混淆、优化和删除。
## -dontshrink：不进行代码收缩，但对于您保留的类，通常不需要显式写这个。
## -dontoptimize：不进行代码优化。
## -dontobfuscate：不进行混淆。
#-keep class com.hezd.dnsresolver.CustomDns {
#    public <init>(...); # 保留所有公共构造函数
#    public <methods>;   # 保留所有公共方法
#    public <fields>;    # 保留所有公共字段
#}
#
## 2. 保留 CustomDns.Builder 类及其公共构造函数、公共方法和公共字段
## 'Builder' 类是用于构建 CustomDns 实例的公共 API。
#-keep class com.hezd.dnsresolver.CustomDns$Builder {
#    public <init>(...); # 保留所有公共构造函数
#    public <methods>;   # 保留所有公共方法
#    public <fields>;    # 保留所有公共字段
#}
#
## 2. 保留 CustomDnsNetworkRegister.Builder 类及其公共构造函数、公共方法和公共字段
#-keep class com.hezd.dnsresolver.CustomDnsNetworkRegister {
#    public <init>(...); # 保留所有公共构造函数
#    public <methods>;   # 保留所有公共方法
#    public <fields>;    # 保留所有公共字段
#}
#
## 3. 保留 DnsExecutionStrategy 枚举类及其所有字段和方法
## 枚举类通常需要完整保留，因为其常量名可能会被外部代码引用。
#-keep enum com.hezd.dnsresolver.DnsExecutionStrategy {
#    *;
#}
#
## 4. 保留 CacheEntry 数据类（如果它被直接用于公共API或需要保留字段名）
## 尽管 CacheEntry 是 private static，但为了安全起见，
## 如果您在日志或其他地方意外地将其字段名暴露出来，或者未来可能改为public，
## 建议保留其数据字段，至少是其 getter/setter (如果Kotlin生成的话)。
## 但由于它是数据类，通常它的属性就是字段。
## 考虑到它是 private static，通常可以不保留，让其自由混淆。
## 如果您不希望它的字段被混淆（例如为了在调试时更容易查看内存中的缓存对象），可以保留。
## 此处以“不保留”为默认，因为它不属于公共API。如果您有需求可以放开此段注释。
## -keep class com.hezd.practice.dns.CustomDns$Companion$CacheEntry {
##     <fields>;
## }
#
## 5. 保留 com.hezd.practice.dns 包路径（可选，但通常推荐）
## 这可以避免整个包名被混淆，使得日志或堆栈跟踪更易读。
##-keepnames class com.hezd.dnsresolver.** { *; } # 保留包名下的所有类名
#
## =====================================================================
## 第三方库 dnsjava (org.xbill.DNS) 混淆规则
## = =====================================================================
## OkHttp 已经有其默认的混淆规则，通常不需要手动添加。
## dnsjava 库通常也不需要特殊处理，因为您只是在内部使用它。
## 但为了确保反射或其他潜在的内部机制正常工作，可以保守地保留其主要入口点。
## 如果在使用过程中出现 dnsjava 相关的问题，再来审查和添加更具体的规则。
#
## 假设您只在内部使用了 Lookup, SimpleResolver, Type, ARecord, AAAARecord
## 这些类通常在您的代码中直接引用，如果它们的方法或字段被混淆，可能会出问题。
## 通常，混淆器会自动处理直接引用的情况。
## 如果您遇到问题，可以考虑以下更保守的规则：
# -keep class org.xbill.DNS.** { *; } # 保留 dnsjava 整个包下的所有类和成员，不混淆
