# SPEC-06: 效果系统 (Effect System)

## 1. 需求概述

实现鸡蛋投掷后的效果生成功能。当选中 EFFECT 分类时，从所有可用的状态效果中随机选择一种效果，并以两种方式之一施加给目标：喷溅型（Splash）或直接给予型（Direct）。

## 2. 预期效果

### 2.1 功能预期

- **效果随机选择**：从 Minecraft 效果注册表中随机选择一种可用效果
- **喷溅型效果**：在鸡蛋碰撞位置生成范围效果云（AreaEffectCloud），类似喷溅药水的效果
- **直接给予型**：直接将效果施加到鸡蛋落点附近最近的玩家或实体上
- **配置支持**：通过配置文件控制效果持续时间、等级、作用方式等参数

### 2.2 用户体验

- 喷溅型效果应有明显的视觉反馈（彩色粒子云）
- 效果应在合理范围内生效（参考喷溅药水 8.25 格范围）
- 效果参数可通过配置文件调整

### 2.3 边界情况

- 如果鸡蛋落在玩家无法进入的位置（如虚空），不生成效果
- 如果落点没有可施加效果的实体，喷溅型仍可生效，直接给予型需有后备方案
- 某些效果对特定实体无效（如治疗对亡灵生物无效）

## 3. 系统设计

### 3.1 架构设计

#### 模块职责

| 模块 | 职责 | 位置 |
|------|------|------|
| `EffectGenerator` | 效果生成器，负责协调两种效果施加方式 | `core/generator/` |
| `EffectRegistry` | 效果注册表，管理可用效果列表和权重 | `core/generator/` |
| `SplashEffectApplier` | 喷溅型效果实现，使用 AreaEffectCloud | `core/generator/effect/` |
| `DirectEffectApplier` | 直接给予型效果实现 | `core/generator/effect/` |
| `EffectType` | 效果类型枚举，定义效果施加方式 | `config/` |

#### 设计模式

- **策略模式**：SplashEffectApplier 和 DirectEffectApplier 作为不同策略
- **工厂模式**：根据配置创建对应的效果施加器
- **注册表模式**：EffectRegistry 统一管理所有可用效果

### 3.2 效果施加方式

#### 喷溅型 (Splash)

```
鸡蛋碰撞位置
    ↓
创建 AreaEffectCloud 实体
    ↓
设置效果列表（随机选中的效果 + 配置的参数）
    ↓
设置范围（默认 3 格，可配置）
    ↓
设置持续时间（默认 60 tick，可配置）
    ↓
生成到世界
```

#### 直接给予型 (Direct)

```
鸡蛋碰撞位置
    ↓
查找附近最近的有效实体/玩家
    ↓
使用 Entity#addStatusEffect 添加效果
    ↓
效果参数从配置读取（duration, amplifier）
```

### 3.3 数据结构设计

#### 效果配置扩展 (CategoryConfig.specificSettings)

```json
{
  "effect": {
    "enabled": true,
    "weight": 1.0,
    "specificSettings": {
      "applyMode": "SPLASH",        // 施加方式: SPLASH 或 DIRECT
      "duration": 300,              // 效果持续时间（tick）
      "amplifier": 0,               // 效果等级（0=等级I）
      "splashRadius": 3.0,          // 喷溅半径
      "splashDuration": 60,         // 喷溅云持续时间（tick）
      "targetType": "PLAYER"       // 目标类型: PLAYER, ENTITY, ANY
    }
  }
}
```

#### 效果类型枚举

```java
public enum EffectApplyMode {
    SPLASH,   // 喷溅型
    DIRECT    // 直接给予型
}

public enum EffectTargetType {
    PLAYER,   // 仅玩家
    ENTITY,   // 仅实体
    ANY       // 任意
}
```

### 3.4 可扩展性设计

- 新效果类型：通过在 EffectApplyMode 枚举添加新值并实现对应的 Applier
- 效果组合：可扩展为同时施加多个效果
- 权重系统：可参考 ENTITY 分类，为不同效果设置不同权重

## 4. 技术要点

### 4.1 Minecraft API 引用

- **效果注册表**: `net.minecraft.registry.Registries.STATUS_EFFECT`
- **AreaEffectCloud**: `net.minecraft.entity.AreaEffectCloudEntity`
- **效果实例**: `net.minecraft.entity.effect.StatusEffectInstance`
- **效果类型**: `net.minecraft.entity.effect.StatusEffect`

### 4.2 关键实现细节

#### 获取所有可用效果

```java
// 遍历效果注册表，排除仅对特定生物有效的效果
for (RegistryEntry<StatusEffect> effect : Registries.STATUS_EFFECT) {
    // 过滤不可用的效果
}
```

#### AreaEffectCloud 配置

```java
AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world, x, y, z);
cloud.addEffect(new StatusEffectInstance(effect, duration, amplifier));
cloud.setRadius(radius);
cloud.setDuration(splashDuration);
world.spawnEntity(cloud);
```

#### 直接施加效果

```java
// 查找最近实体
Entity nearest = findNearestEntity(world, pos, maxDistance);
// 施加效果
nearest.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier));
```

### 4.3 需要排除的效果

- 立即生效效果（如 Instant Health, Instant Damage）的特殊处理
- 隐形效果（Visibility）可能需要特殊考虑
- 仅对特定生物有效的效果（如 Dolphin系 ）

## 5. 验收标准

- [ ] 能够从效果注册表中随机选择效果
- [ ] 喷溅型效果正确生成 AreaEffectCloud 并能被周围实体获取
- [ ] 直接给予型效果正确施加到目标实体/玩家
- [ ] 配置参数（duration, amplifier, applyMode）能够正确读取和应用
- [ ] 效果系统可与现有 ENTITY、ITEM 分类共存
- [ ] 添加新的效果施加方式无需修改现有代码（开闭原则）

## 6. 文件结构 (规划)

```
src/main/java/com/alleggrandomizer/
├── core/
│   └── generator/
│       ├── EffectGenerator.java          # 效果生成器入口
│       ├── EffectRegistry.java           # 效果注册表
│       └── effect/
│           ├── EffectApplier.java         # 效果施加器接口
│           ├── SplashEffectApplier.java   # 喷溅型实现
│           └── DirectEffectApplier.java   # 直接给予型实现
├── config/
│   ├── EffectApplyMode.java              # 效果施加方式枚举
│   └── EffectTargetType.java             # 目标类型枚举
```
