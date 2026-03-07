# 随机系统集成说明

## 更新内容

根据项目规范，事件系统和效果系统已更新为使用项目统一的随机选择系统。

## 项目随机系统架构

### 两级随机选择
项目使用两级随机选择架构：

1. **第一级 - 分类选择** (`CategorySelector`)
   - 从启用的分类中选择一个（ENTITY, ITEM, EFFECT, EVENT）
   - 基于每个分类的权重进行加权随机选择

2. **第二级 - 产物选择** (`ProductSelector`)
   - 在选中的分类内选择具体产物
   - 支持为每个产物设置权重
   - 使用 `WeightedRandom` 进行加权随机选择

### 核心组件

#### WeightedRandomSystem
- 主入口点，协调两级随机选择
- 提供统一的 API 用于选择随机产物
- 支持自定义种子提供器（用于确定性结果）

#### ProductSelector<T>
- 泛型选择器，支持任意类型的产物
- 使用权重映射进行选择
- 过滤无效产物（null 或权重 ≤ 0）

#### WeightedRandom<T>
- 底层加权随机算法实现
- 支持种子设置（确定性）
- 支持单选和多选

## 集成实现

### 事件系统集成

**EventGenerator.java**
```java
private static String selectRandomEvent(List<String> enabledEvents) {
    // 构建权重映射（所有事件等权重）
    Map<String, Double> eventWeights = new HashMap<>();
    for (String eventId : enabledEvents) {
        eventWeights.put(eventId, 1.0);
    }

    // 使用 ProductSelector 进行加权随机选择
    ProductSelector<String> selector = new ProductSelector<>();
    String selected = selector.selectProduct(eventWeights);
    return selected != null ? selected : "LIGHTNING";
}
```

**优势：**
- ✅ 与项目其他部分保持一致
- ✅ 支持未来为不同事件设置不同权重
- ✅ 可以使用种子实现确定性选择
- ✅ 统一的日志记录

### 效果系统集成

**EffectGenerator.java**
```java
private static RegistryEntry<StatusEffect> selectRandomEffect() {
    // 构建效果权重映射（所有效果等权重）
    Map<RegistryEntry<StatusEffect>, Double> effectWeights = new HashMap<>();

    for (StatusEffect effect : Registries.STATUS_EFFECT) {
        RegistryEntry<StatusEffect> entry = Registries.STATUS_EFFECT.getEntry(effect);
        if (isValidEffect(entry)) {
            effectWeights.put(entry, 1.0);
        }
    }

    // 使用 ProductSelector 进行选择
    ProductSelector<RegistryEntry<StatusEffect>> selector = new ProductSelector<>();
    return selector.selectProduct(effectWeights);
}
```

**优势：**
- ✅ 与 ENTITY 和 ITEM 分类使用相同的选择机制
- ✅ 支持未来为不同效果设置不同权重
- ✅ 过滤无效效果
- ✅ 统一的错误处理

## 与其他分类的一致性

### ENTITY 分类
```java
// EggHitHandler.java
RegistryEntry<EntityType<?>> selectedType = randomSystem.selectEntity(config, entityWeights);
```

### ITEM 分类
```java
// EggHitHandler.java
ItemStack selectedItem = randomSystem.selectItem(config, itemWeights);
```

### EFFECT 分类
```java
// EffectGenerator.java
ProductSelector<RegistryEntry<StatusEffect>> selector = new ProductSelector<>();
RegistryEntry<StatusEffect> selected = selector.selectProduct(effectWeights);
```

### EVENT 分类
```java
// EventGenerator.java
ProductSelector<String> selector = new ProductSelector<>();
String selected = selector.selectProduct(eventWeights);
```

## 未来扩展

### 支持权重配置

可以在配置文件中为不同的事件或效果设置权重：

```json
"EVENT": {
  "specificSettings": {
    "events": ["LIGHTNING", "EXPLOSION"],
    "eventWeights": {
      "LIGHTNING": 2.0,
      "EXPLOSION": 1.0
    }
  }
}
```

然后在代码中读取：
```java
Map<String, Double> eventWeights = new HashMap<>();
Map<String, Object> weights = getEventWeights(specificSettings);
for (String eventId : enabledEvents) {
    double weight = weights.getOrDefault(eventId, 1.0);
    eventWeights.put(eventId, weight);
}
```

### 支持确定性选择

使用种子可以实现可重现的随机选择：
```java
long seed = generateSeed(position, worldSeed, playerUUID);
ProductSelector<String> selector = new ProductSelector<>(seed);
```

## 验证

### 编译状态
✅ **编译成功** - 所有更改通过编译

```bash
./gradlew compileJava
BUILD SUCCESSFUL
```

### 代码一致性
- ✅ 使用项目统一的 `ProductSelector`
- ✅ 移除了独立的 `Random` 实例
- ✅ 与 ENTITY 和 ITEM 分类保持一致的选择机制
- ✅ 保持了相同的日志记录风格

## 总结

事件系统和效果系统现在完全集成了项目的统一随机选择系统：
- 使用 `ProductSelector` 进行产物选择
- 支持权重配置（当前为等权重）
- 与其他分类保持一致的架构
- 为未来扩展预留了空间

这确保了整个项目的随机选择机制统一、可维护且易于扩展。
