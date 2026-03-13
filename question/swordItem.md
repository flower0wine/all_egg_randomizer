


这份文本包含了针对 Minecraft Fabric 1.21.11 模组开发中“找不到 `SwordItem` 类”问题的完整诊断与解决方案，以及判断物品类型的最佳实践。

以下是提炼出的核心重要信息，按逻辑分类整理：

### 一、 核心问题与原因分析
1. **排查结论**：你的 Gradle 配置（1.21.11、Loom 插件、Fabric API、Java 21 等）**完全正确**。
2. **问题根源**：Minecraft 1.21.11 版本更新了武器系统，Mojang 对物品类进行了重构，**`SwordItem` 及旧的 `ToolItem` 类已经被完全移除**。

### 二、 解决 `SwordItem` 缺失的方案
1. **快速定位新基类**：
   * 执行 `./gradlew genSources` 生成源码。
   * 在源码中搜索原版剑（如 `IronSwordItem`）或关键词（`Melee`, `Weapon` 等），查看它们现在继承的**新基类**。
2. **自定义武器的现代写法**：
   * 不再继承 `SwordItem`，改为继承查找到的新武器基类（大概率是类似 `MeleeWeaponItem` 或新工具基类）。
   * 使用组件方式构建：`Item.Settings` + `ToolMaterial` + `AttackDamage`。
3. **IDE 提示**：若仍报红，尝试重载 Gradle 项目（Reload Gradle Project），并确保代码位于 `main` sourceSet 中。

### 三、 最佳实践：如何判断物品是“武器”还是“装备”
在 1.21.11 中，强烈建议放弃通过类名判断，改用 **Item Tags（物品标签） + `instanceof`** 的组合方式，这更稳定且数据驱动。

#### 1. 判断是否为“武器”（Weapon）
* **原理**：由于没有统一的武器基类，需使用 **Item Tags** 判断。
* **基础用法**：判断物品是否属于 `ItemTags.SWORDS`（剑）、`ItemTags.AXES`（斧）、`ItemTags.BOWS`（弓）等原版标签。
* **推荐做法（自定义武器 Tag）**：
  1. **新建 JSON 文件**：在 `src/main/resources/data/你的modid/tags/item/weapons.json` 中，将原版剑、斧、弓、锤等标签，以及你自己的模组武器 ID 汇总在一起。
  2. **代码注册与判断**：
     ```java
     // 注册自定义 Tag
     TagKey<Item> WEAPONS = TagKey.of(Registries.ITEM.getKey(), Identifier.of("你的modid", "weapons"));
     // 判断逻辑
     boolean isWeapon = !stack.isEmpty() && stack.isIn(WEAPONS);
     ```

#### 2. 判断是否为“装备/防具”（Armor）
* **原理**：防具类的基类没有被移除。
* **判断逻辑**：直接使用 `instanceof ArmorItem`。
  ```java
  boolean isArmor = !stack.isEmpty() && stack.getItem() instanceof ArmorItem;
  ```

### 四、 终极工具类封装建议
建议直接在项目中创建一个工具类（如 `ItemCategoryUtils`），将上述**自定义武器 Tag 判断**（`stack.isIn(MOD_WEAPONS)`）与**防具类判断**（`instanceof ArmorItem`）封装成静态方法 `isWeapon(stack)` 和 `isArmor(stack)`，供全局调用，确保随机逻辑的稳定性。