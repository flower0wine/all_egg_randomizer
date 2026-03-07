# SPEC-03: 命令控制系统

## 1. 需求概述

实现游戏内命令系统，允许玩家/管理员通过命令控制鸡蛋随机产物的分类启用/禁用状态，以及调整各分类的权重。

## 2. 预期效果

### 2.1 功能预期
- 提供主命令 `/allegg` 作为入口
- 子命令用于控制分类和权重
- 命令反馈清晰明了

### 2.2 用户体验
- 命令语法简洁直观
- 参数错误时提供正确用法提示
- 操作成功后有明确反馈

## 3. 命令设计

### 3.1 命令结构

```
/allegg <subcommand> [arguments]
```

### 3.2 子命令列表

| 子命令 | 权限 | 功能 |
|--------|------|------|
| help | 所有人 | 显示命令帮助 |
| list | 所有人 | 列出所有分类及其状态 |
| enable \<category\> | OP | 启用指定分类 |
| disable \<category\> | OP | 禁用指定分类 |
| setweight \<category\> \<weight\> | OP | 设置分类权重 |
| reload | OP | 重载配置文件 |
| gui | 所有人 | 打开配置GUI面板 |

### 3.3 使用示例

```bash
# 查看帮助
/allegg help

# 查看所有分类状态
/allegg list

# 启用效果分类
/allegg enable EFFECT

# 禁用物品分类
/allegg disable ITEM

# 设置生物权重为 5
/allegg setweight ENTITY 5

# 设置物品权重为 3
/allegg setweight ITEM 3

# 重载配置
/allegg reload

# 打开GUI面板
/allegg gui
```

### 3.4 输出格式

#### list 命令输出示例
```
=== All Egg Randomizer 配置 ===
生物 (ENTITY): 启用 | 权重: 1.0
物品 (ITEM): 启用 | 权重: 1.0
效果 (EFFECT): 禁用 | 权重: 1.0
事件 (EVENT): 禁用 | 权重: 1.0
```

#### 操作成功反馈
- 启用: "已启用 [分类名称]"
- 禁用: "已禁用 [分类名称]"
- 设置权重: "已将 [分类名称] 权重设置为 [值]"
- 重载: "配置已重载"

## 4. 系统设计

### 4.1 架构设计

#### 模块职责
- **EggCommand**: 主命令类，注册所有子命令
- **ListCommand**: list 子命令实现
- **EnableCommand**: enable 子命令实现
- **DisableCommand**: disable 子命令实现
- **SetWeightCommand**: setweight 子命令实现
- **ReloadCommand**: reload 子命令实现
- **GuiCommand**: gui 子命令实现

#### 设计模式
- **命令模式**: 每个子命令作为独立命令类实现
- **策略模式**: 根据子命令名称分发到对应处理类

### 4.2 权限控制

- 基础子命令 (help, list, gui): 所有玩家可用
- 配置修改子命令 (enable, disable, setweight, reload): 仅 OP 可用

### 4.3 参数验证

- 分类名称不区分大小写，自动转换为大写
- 权重值必须为非负浮点数
- 无效分类名称时提示可用选项

## 5. 技术要点

### 5.1 命令注册方式
- 使用 Fabric API 的 `CommandRegistrationCallback`
- 或直接使用 Minecraft 的命令注册 API

### 5.2 依赖模块
- 配置系统 (SPEC-02): 读取和修改配置

### 5.3 国际化支持
- 命令反馈信息支持多语言（可选）

## 6. 验收标准

- [ ] 所有子命令能够正确注册
- [ ] 各子命令功能正确执行
- [ ] 权限控制生效
- [ ] 参数验证正确
- [ ] 命令反馈清晰

## 7. 文件结构 (规划)

```
src/main/java/com/alleggrandomizer/
├── command/
│   ├── EggCommand.java              # 主命令入口
│   ├── ListCommand.java             # 列表子命令
│   ├── EnableCommand.java           # 启用子命令
│   ├── DisableCommand.java          # 禁用子命令
│   ├── SetWeightCommand.java        # 权重设置子命令
│   ├── ReloadCommand.java           # 重载子命令
│   └── GuiCommand.java              # GUI子命令
```
