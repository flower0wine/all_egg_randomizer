# SPEC-07: 事件系统 (Event System)

## 1. 需求概述

实现鸡蛋投掷后触发世界事件的功能。当选中 EVENT 分类时，从配置的事件列表中随机选择一种事件并在指定位置触发。

初始阶段实现雷击（Lightning）事件，后续可扩展支持更多事件类型。

## 2. 预期效果

### 2.1 功能预期

- **事件随机选择**：从已注册的事件类型中随机选择
- **雷击事件**：在指定位置生成闪电，可选择对实体造成伤害或仅视觉/装饰效果
- **事件位置**：可配置为玩家位置或鸡蛋碰撞位置
- **可扩展架构**：支持未来添加更多事件类型（爆炸、降雨、天气变化等）

### 2.2 用户体验

- 雷击应有明显的视觉和音效反馈
- 可配置闪电是否造成伤害（避免恶意破坏）
- 闪电可引燃火焰（可配置）

### 2.3 边界情况

- 雷击在虚空或无效位置时不生成
- 需要考虑世界天气（晴天仍可生成闪电）
- 需要考虑难度设置（和平模式下闪电不造成伤害）

## 3. 系统设计

### 3.1 架构设计

#### 模块职责

| 模块 | 职责 | 位置 |
|------|------|------|
| `EventGenerator` | 事件生成器入口，协调事件选择和执行 | `core/generator/` |
| `EventRegistry` | 事件注册表，管理所有可用事件类型 | `core/generator/` |
| `EventType` | 事件类型定义 | `config/` |
| `IEffectEvent` | 事件接口，定义事件执行的抽象行为 | `core/generator/event/` |
| `LightningEvent` | 雷击事件实现 | `core/generator/event/` |

#### 设计模式

- **策略模式**：每种事件类型作为独立策略实现 IEffectEvent 接口
- **注册表模式**：EventRegistry 统一管理所有事件类型，支持运行时注册
- **工厂模式**：根据事件类型创建对应的事件处理器

### 3.2 事件流

```
选中 EVENT 分类
    ↓
EventGenerator 获取可用事件列表
    ↓
随机选择事件类型
    ↓
根据配置确定事件参数
    ↓
执行事件（实现 IEffectEvent 接口）
    ↓
记录日志
```

### 3.3 事件类型定义

#### 初始事件类型

| 事件ID | 说明 | 参数 |
|--------|------|------|
| LIGHTNING | 雷击 | damage, fire, cosmetic |

#### 未来可扩展事件

| 事件ID | 说明 | 备注 |
|--------|------|------|
| EXPLOSION | 爆炸 | 类似 TNT |
| RAIN | 降雨 | 改变天气 |
| THUNDER | 雷暴 | 长时间雷雨 |
| FIREWORK | 烟花 | 视觉效果 |
| SOUND | 播放音效 | 自定义音效 |

### 3.4 数据结构设计

#### 事件配置扩展 (CategoryConfig.specificSettings)

```json
{
  "event": {
    "enabled": true,
    "weight": 1.0,
    "specificSettings": {
      "targetPosition": "EGG",           // 位置: PLAYER 或 EGG（鸡蛋落点）
      "events": ["LIGHTNING"],            // 启用的事件列表
      "lightning": {
        "damage": false,                 // 是否造成伤害
        "fire": false,                   // 是否生成火焰
        "cosmetic": false                // 是否为装饰性闪电（不造成任何效果）
      }
    }
  }
}
```

#### 事件接口定义

```java
public interface IEffectEvent {
    /**
     * 获取事件类型ID
     */
    String getEventId();
    
    /**
     * 执行事件
     * @param world 目标世界
     * @param position 事件触发位置
     * @param config 事件参数配置
     * @return 是否执行成功
     */
    boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config);
    
    /**
     * 获取事件描述
     */
    String getDescription();
}
```

### 3.5 可扩展性设计

- **新增事件**：实现 IEffectEvent 接口并在 EventRegistry 注册
- **事件权重**：可参考产物分类，为不同事件设置不同权重
- **事件组合**：可配置一次触发多个事件

## 4. 技术要点

### 4.1 Minecraft API 引用

- **闪电实体**: `net.minecraft.entity.LightningEntity`
- **实体类型**: `net.minecraft.entity.EntityType`
- **世界天气**: `net.minecraft.world.World#setWeather`

### 4.2 关键实现细节

#### 生成闪电

```java
// 创建闪电实体
LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
if (lightning != null) {
    lightning.refreshPositionAndAngles(x, y, z, 0, 0);
    // 设置是否为装饰性闪电（不引燃火焰）
    lightning.setCosmetic(cosmetic);
    world.spawnEntity(lightning);
}
```

#### 装饰性闪电 vs 真实闪电

- `lightning.setCosmetic(true)`: 装饰性闪电，仅有视觉效果，不造成伤害，不引燃火焰
- `lightning.setCosmetic(false)`: 真实闪电，会造成伤害和火焰（取决于难度）

#### 事件位置选择

```java
// 鸡蛋落点
Vec3d eggPosition = hitResult.getPos();

// 投掷者位置（玩家位置）
Vec3d playerPosition = egg.getOwner() != null 
    ? egg.getOwner().getPos() 
    : eggPosition;
```

### 4.3 注意事项

- 闪电生成需要考虑世界边界
- 需要处理 `EntityType.LIGHTNING_BOLT.create()` 返回 null 的情况
- 真实闪电会造成村民变女巫、猪变僵尸猪人等转换

## 5. 验收标准

- [ ] 能够在鸡蛋落点生成闪电
- [ ] 能够根据配置选择在玩家位置或鸡蛋落点生成闪电
- [ ] 装饰性闪电和真实闪电可正确区分
- [ ] 事件系统架构支持添加新事件类型（参考现有接口设计）
- [ ] 配置参数能够正确读取和应用
- [ ] 事件系统可与现有 ENTITY、ITEM、EFFECT 分类共存

## 6. 文件结构 (规划)

```
src/main/java/com/alleggrandomizer/
├── core/
│   └── generator/
│       ├── EventGenerator.java          # 事件生成器入口
│       ├── EventRegistry.java            # 事件注册表
│       └── event/
│           ├── IEffectEvent.java         # 事件接口
│           ├── LightningEvent.java       # 雷击事件实现
│           └── EventConfig.java          # 事件配置模型
├── config/
│   └── EventType.java                    # 事件类型枚举
```

## 7. 与效果系统的对比

| 特性 | 效果系统 (EFFECT) | 事件系统 (EVENT) |
|------|------------------|------------------|
| 复杂度 | 中等 | 简单 |
| 随机性 | 从所有效果中随机 | 从所有事件中随机 |
| 目标 | 实体/玩家 | 位置/区域 |
| 初始实现 | 喷溅+直接两种方式 | 单一雷击事件 |
| 扩展方向 | 多种施加方式 | 多种事件类型 |
| 配置参数 | duration, amplifier, radius | damage, fire, position |

## 8. 实现建议

### 实现顺序建议

1. **第一阶段**：先实现事件系统（简单）
   - 实现 EventGenerator 和 EventRegistry
   - 实现 LightningEvent
   - 与现有 EggHitHandler 集成

2. **第二阶段**：实现效果系统（中等）
   - 实现 EffectRegistry
   - 实现 SplashEffectApplier
   - 实现 DirectEffectApplier
   - 与现有 EggHitHandler 集成

### 集成方式

在 `EggHitHandler.java` 中替换空实现：

```java
// 原有代码
case EFFECT -> applyEffect(serverWorld, pos, config);
case EVENT -> triggerEvent(serverWorld, pos, config);

// 修改后
case EFFECT -> EffectGenerator.generate(serverWorld, pos, config);
case EVENT -> EventGenerator.generate(serverWorld, pos, config, egg);
```
