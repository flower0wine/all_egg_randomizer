请你阅读这个项目，用户在做一个MC模组，目前已经有很多的实现了，下面是新的需求，下面是用户需求和任务等。

# 任务

- 分析用户需求，编写易读、具有可维护性的代码来实现用户需求。你有充足的时间去实现用户需求，在实现之前进行充分的分析，避免立即开始工作，应进行规划。

# 用户需求

- 阅读 src\main\java\com\alleggrandomizer\core\EggHitHandler.java，这个包含了目前的砸鸡蛋的逻辑。
- 阅读生成物品的相关逻辑，现在需要实现判断一个物品是否为桶，如果为桶，则设置生成数量为 1 个。
- 如果物品为挽具、船、潜影盒或者是矿车，均设置为 1 个。
- 分析生成的物品是否可能包含玩家的头颅？预期是有概率生成。
- 潜影盒当中也需要和收纳袋一样，添加随机的物品，相关逻辑相同。

# 代码规范

- 代码应易于维护、模块化且具有易读性，模块、文件等应遵守功能的单一职责原则，避免在同一个文件当中堆砌功能无关的代码。
- 项目文件结构设计需要有架构设计，目录命名具有前瞻性以及功能命名意义，避免无意义，含糊的文件或文件夹命名。
- 在适当的实现时使用设计模式来进行合理的设计，以提高代码的可扩展性，但同时避免过度设计。

# 约束

- 不要猜测 API，如果你不确定 API 是否存在，或者编译时使用了不存在的 API，应告知我或者是使用已有的工具查找相关的方法或者是找到文档。

# 额外信息

- 检测是否为桶
```java
public static boolean isBucket(ItemStack stack) {
    // 检查是否在 c:buckets 标签中
    return stack.isIn(ItemTags.create(Identifier.of("c", "buckets")));
}
```
- 检测是否为挽具
```java
public static boolean isHarness(ItemStack stack) {
    return stack.isIn(ItemTags.create(Identifier.of("harnesses")));
}
```
- 检测是否为船
```java
// 检查是否是任何船物品
public static boolean isAnyBoatItem(ItemStack stack) {
    return stack.getItem() instanceof BoatItem;
}
```
- 检测是否为潜影盒
```java
public static boolean isShulkerBox(ItemStack stack) {
    // 方法1: 检查物品是否在 shulker_boxes 标签中
    return stack.isIn(ItemTags.create(Identifier.of("minecraft", "shulker_boxes")));
}
```
- 检测是否为矿车
```java
public static boolean isMinecart(ItemStack stack) {
    return stack.getItem() instanceof MinecartItem;
}
```