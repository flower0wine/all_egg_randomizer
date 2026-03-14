


这份文档提取了在 Minecraft Fabric 1.21.11 中实现定时与调度功能的核心规范，已按照开发者参考手册的标准进行了整理，方便直接查阅与实操。

---

# Fabric 1.21.11 定时任务与延迟调度开发参考

## 1. 核心设计理念
Fabric **没有**内置类似 Bukkit Scheduler 的高级调度器（保持轻量级和事件驱动）。
**官方唯一推荐且现行标准方案**：基于 **Tick 事件系统** 手动维护计数器或任务队列。
> **基准换算**：20 Ticks ≈ 1 秒

## 2. 核心 API 参考
- **依赖**：仅需 `fabric-api`（内含 `fabric-lifecycle-events-v1` 模块）。
- **包路径**：`net.fabricmc.fabric.api.event.lifecycle.v1`

### 可用事件接口
| 作用域 | 事件类 | 常用回调方法 | 适用场景 |
| :--- | :--- | :--- | :--- |
| **服务器全局** | `ServerTickEvents` | `END_SERVER_TICK` / `START_SERVER_TICK` | 大部分通用服务器逻辑、跨世界任务 |
| **单世界(Server)** | `ServerTickEvents` | `END_WORLD_TICK` / `START_WORLD_TICK` | 仅针对特定世界的定时逻辑（推荐多世界） |
| **客户端全局** | `ClientTickEvents` | `END_CLIENT_TICK` / `START_CLIENT_TICK` | 纯客户端 UI 刷新、本地逻辑定时 |
| **单世界(Client)** | `ClientTickEvents` | `END_WORLD_TICK` / `START_WORLD_TICK` | 客户端本地世界定时 |

---

## 3. 标准实现范式 (Code Snippets)

### 范式 A：周期性任务 (Periodic Task)
适用于需要每隔固定时间（如每 5 秒）执行一次的逻辑。

```java
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class MyMod implements ModInitializer {
    // 维护一个静态计数器 (若需持久化，需存入自定义 World/Player 数据)
    private static int tickCounter = 0; 
    private static final int TICKS_PER_PERIOD = 100; // 5秒 = 5 * 20 ticks

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= TICKS_PER_PERIOD) {
                // 执行你的定时任务
                doPeriodicTask();
                tickCounter = 0; // 重置计数器
            }
        });
    }

    private void doPeriodicTask() {
        // TODO: 任务逻辑
    }
}
```

### 范式 B：延迟任务队列 (Delayed Task Queue)
适用于实现 `scheduleTask(Runnable task, int delayTicks)` 的一次性延迟执行。

```java
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import java.util.ArrayList;
import java.util.List;

public class ModScheduler implements ModInitializer {
    // 使用普通类而非 record，以便修改 remainingTicks
    private static class ScheduledTask {
        Runnable task;
        int remainingTicks;

        ScheduledTask(Runnable task, int remainingTicks) {
            this.task = task;
            this.remainingTicks = remainingTicks;
        }
    }

    private static final List<ScheduledTask> tasks = new ArrayList<>();

    // 暴露给外部的调度 API
    public static void schedule(int delayTicks, Runnable task) {
        tasks.add(new ScheduledTask(task, delayTicks));
    }

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (tasks.isEmpty()) return;

            // 倒数并执行任务，执行后移除
            tasks.removeIf(scheduledTask -> {
                scheduledTask.remainingTicks--;
                if (scheduledTask.remainingTicks <= 0) {
                    scheduledTask.task.run();
                    return true; // 触发 remove
                }
                return false;
            });
        });
    }
}
```

---

## 4. 最佳实践与避坑指南 (Dos & Don'ts)

### ✅ 推荐做法 (Dos)
- **轻量化执行**：Tick 事件每秒执行 20 次（主线程），请保持逻辑足够轻量，复杂计算需异步处理，但**操作游戏世界（如放置方块、修改实体）必须回到 Tick 主线程**。
- **精确作用域**：如果是针对某个维度的任务（如刷新主世界的僵尸），优先使用 `END_WORLD_TICK` 并判断 `world.getRegistryKey()`，而非全局 Server Tick。

### ❌ 严禁使用 (Don'ts)
- 🚫 `Thread.sleep()`：绝对不要在逻辑中使用，这会直接卡死 Minecraft 主线程导致服务器无响应 (Watchdog 崩溃)。
- 🚫 `WorldTickCallback`：这是旧版本的过时 API，已被 `ServerTickEvents.END_WORLD_TICK` 彻底替代。
- 🚫 Vanilla `BasicTickScheduler`：这是原版游戏内部用于**方块状态更新**和**流体流动**的调度器，不适用于通用 Mod 逻辑（强行使用可能导致存档破坏或逻辑错乱）。