


以下是对您提供的信息进行的重要信息提取（按逻辑分类，无遗漏）：

### 1. 核心变更（API 移除）
* **涉及版本**：Minecraft 1.21.11（Yarn mappings 1.21.11+build.4）。
* **移除内容**：旧版 API `source.hasPermissionLevel(int)` 已被**彻底移除**。
* **现状提示**：旧版 Fabric Wiki 中的示例已过时，不能再使用旧写法。

### 2. 变更原因及底层逻辑
* **原因**：Mojang 重构了权限系统，以支持更灵活的权限集成（如未来模组、LuckPerms 等）。
* **新架构**：
  * `ServerCommandSource` 现在实现了 `PermissionSource` 接口。
  * 权限获取方式变更为通过 `source.getPermissions()` 返回 `PermissionPredicate` 对象来处理。

### 3. 核心替代方案（代码实现）
* **涉及的新类**：
  * `PermissionPredicate`
  * `LeveledPermissionPredicate`
* **新版判断逻辑**：
  1. 调用 `source.getPermissions()` 获取权限对象。
  2. 判断对象是否为 `LeveledPermissionPredicate` 实例（控制台等非 leveled 情况通常默认返回 false）。
  3. 获取权限等级（Enum类型），并通过 `.ordinal()` 方法将其转换为数字进行 `>=` 或 `==` 的对比。

### 4. 权限等级常量对照（基于 `LeveledPermissionPredicate`）
新版推荐直接使用内置的静态常量替代原来的硬编码数字：
* `MODERATORS` → 对应**原版等级 1**
* `GAMEMASTERS` → 对应**原版等级 2**（包含 Game Master 和 命令方块）
* `ADMINS` → 对应**原版等级 3**
* `OWNERS` → 对应**原版等级 4**（纯 OP，不包含命令方块）

### 5. 最佳实践（官方/推荐封装）
为保证代码简洁且兼容新版，推荐在项目中封装一个全局工具方法替代原有的 `hasPermissionLevel`：
```java
public static boolean hasPermissionLevel(ServerCommandSource source, int level) {
    if (!(source.getPermissions() instanceof LeveledPermissionPredicate leveled)) {
        return false;
    }
    return leveled.getLevel().ordinal() >= level;
}
```
**调用方式**：在命令注册的 `.requires()` 中直接调用该工具方法（如 `.requires(source -> hasPermissionLevel(source, 2))`）。