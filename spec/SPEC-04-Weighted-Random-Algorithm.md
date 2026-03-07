# SPEC-04: 加权随机算法系统

## 1. 需求概述

实现伪随机算法系统，根据配置的权重从启用的产物分类中随机选择产物类型，确保各分类的选中概率与权重成正比。

## 2. 预期效果

### 2.1 功能预期
- 伪随机算法确保每次选择是确定的（基于种子）
- 权重越高的分类，被选中的概率越大
- 仅从已启用的分类中进行选择
- 分类内具体产物的选择同样使用加权随机

### 2.2 算法预期
- 权重相等时，各分类概率相等
- 权重为 0 的分类不会被选中
- 算法时间复杂度为 O(n)

## 3. 算法设计

### 3.1 加权随机选择算法

#### 算法描述
使用"累加权重区间"算法：

1. 计算所有启用分类的总权重 W
2. 生成 [0, W) 范围内的随机数 R
3. 遍历所有分类，累加权重，当累加和 > R 时选中该分类

#### 数学表达
```
P(选中分类i) = weight(i) / Σ(weight(j))  (j为所有启用分类)
```

### 3.2 伪随机数生成

#### 种子来源
- 使用投掷位置坐标 + 时间戳 + 玩家UUID作为种子
- 确保相同条件下结果可重现（可选）

#### Random 实例化
```java
// 使用确定性种子
long seed = calculateSeed(player, position, time);
Random random = new Random(seed);
```

### 3.3 两级随机选择

```
第一级: 分类选择
├── 根据分类权重选择产物分类
└── 输出: CategoryType

第二级: 产物选择
├── 在选中分类内根据各产物权重选择具体产物
└── 输出: 具体产物 (EntityType / ItemStack / EffectType / EventType)
```

## 4. 系统设计

### 4.1 架构设计

#### 模块职责
- **WeightedRandom**: 加权随机选择器核心类
- **RandomSeedProvider**: 种子生成器接口
- **CategorySelector**: 分类选择器
- **ProductSelector**: 产物选择器

#### 设计模式
- **策略模式**: 不同的产物选择策略
- **模板方法模式**: 随机选择流程模板

### 4.2 产物权重配置

每个分类内部可配置各产物的权重：

```json
{
  "ENTITY": {
    "enabled": true,
    "weight": 1.0,
    "products": {
      "minecraft:cow": 1.0,
      "minecraft:pig": 1.0,
      "minecraft:sheep": 1.0,
      "minecraft:zombie": 0.5
    }
  }
}
```

### 4.3 权重边界情况

| 场景 | 处理 |
|-----|------|
| 所有分类权重为0 | 无产物生成 |
| 只有一个分类启用 | 100%选中该分类 |
| 权重为0.001 | 最小权重，防止除零 |

## 5. 核心算法实现

### 5.1 分类选择

```java
public CategoryType selectCategory(List<CategoryConfig> enabledCategories) {
    double totalWeight = enabledCategories.stream()
        .mapToDouble(CategoryConfig::getWeight)
        .sum();
    
    double randomValue = random.nextDouble() * totalWeight;
    double cumulative = 0.0;
    
    for (CategoryConfig config : enabledCategories) {
        cumulative += config.getWeight();
        if (randomValue < cumulative) {
            return config.getType();
        }
    }
    
    // 兜底：返回最后一个
    return enabledCategories.get(enabledCategories.size() - 1).getType();
}
```

### 5.2 产物选择

```java
public <T> T selectProduct(Map<T, Double> products, Random random) {
    double totalWeight = products.values().stream()
        .mapToDouble(Double::doubleValue)
        .sum();
    
    double randomValue = random.nextDouble() * totalWeight;
    double cumulative = 0.0;
    
    for (Map.Entry<T, Double> entry : products.entrySet()) {
        cumulative += entry.getValue();
        if (randomValue < cumulative) {
            return entry.getKey();
        }
    }
    
    return products.keySet().iterator().next();
}
```

## 6. 技术要点

### 6.1 性能优化
- 权重计算结果可缓存
- 避免每次选择时重复计算总权重

### 6.2 精度处理
- 权重使用 double 类型
- 避免浮点数精度问题（添加 epsilon）

### 6.3 可配置性
- 产物权重可通过配置文件调整
- 默认所有产物权重相等

## 7. 验收标准

- [ ] 权重相等时，各分类概率相等
- [ ] 权重差异明显时，概率差异正确反映
- [ ] 禁用的分类不会被选中
- [ ] 伪随机种子生成正确
- [ ] 分类内产物选择正常工作

## 8. 文件结构 (规划)

```
src/main/java/com/alleggrandomizer/
├── random/
│   ├── WeightedRandom.java          # 加权随机选择器
│   ├── RandomSeedProvider.java      # 种子提供者接口
│   ├── CategorySelector.java        # 分类选择器
│   ├── ProductSelector.java         # 产物选择器
│   └── DefaultSeedProvider.java     # 默认种子实现
```
