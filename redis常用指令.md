# Redis 常用命令速查手册

> Redis 不使用 SQL，而是使用独立的命令体系。本文档整理了常用的 Redis 命令。

---

## 一、基础 Key 操作

| 命令 | 作用 | 示例 |
|------|------|------|
| `SET key value` | 设置键值对 | `SET token:access:1 "abc123"` |
| `GET key` | 获取值 | `GET token:access:1` |
| `DEL key` | 删除键 | `DEL token:access:1` |
| `KEYS pattern` | 查找匹配的键 | `KEYS token:*` |
| `EXISTS key` | 检查键是否存在 | `EXISTS token:access:1` |
| `EXPIRE key seconds` | 设置过期时间（秒） | `EXPIRE token:access:1 3600` |
| `TTL key` | 查看剩余过期时间 | `TTL token:access:1` |
| `TYPE key` | 查看数据类型 | `TYPE token:access:1` |

---

## 二、String 类型操作（最常用）

| 命令 | 作用 | 示例 |
|------|------|------|
| `SET key value` | 设置值 | `SET name "forum"` |
| `SET key value EX seconds` | 设置值 + 过期时间（秒） | `SET token "abc" EX 3600` |
| `SET key value PX milliseconds` | 设置值 + 过期时间（毫秒） | `SET token "abc" PX 3600000` |
| `GET key` | 获取值 | `GET name` |
| `INCR key` | 自增（原子操作） | `INCR view_count` |
| `DECR key` | 自减 | `DECR stock` |
| `APPEND key value` | 追加字符串 | `APPEND name "-new"` |

**实战示例**：
```bash
# 存储用户 Token
SET token:access:1 "eyJhbGciOiJIUzI1NiJ9..." EX 7200

# 获取 Token
GET token:access:1

# 查看剩余时间
TTL token:access:1
# 返回剩余秒数，-1 表示永不过期，-2 表示已过期/不存在
```

---

## 三、Hash 类型操作（对象存储）

Hash 适合存储对象，比 String 更节省内存。

| 命令 | 作用 | 示例 |
|------|------|------|
| `HSET key field value` | 设置哈希字段 | `HSET user:1 name "张三"` |
| `HGET key field` | 获取单个字段 | `HGET user:1 name` |
| `HGETALL key` | 获取所有字段 | `HGETALL user:1` |
| `HMSET key field1 v1 field2 v2` | 设置多个字段 | `HMSET user:1 name "张三" age 25` |
| `HDEL key field` | 删除字段 | `HDEL user:1 age` |
| `HEXISTS key field` | 检查字段是否存在 | `HEXISTS user:1 name` |
| `HKEYS key` | 获取所有字段名 | `HKEYS user:1` |
| `HVALS key` | 获取所有字段值 | `HVALS user:1` |

**实战示例**：
```bash
# 存储用户信息
HSET user:1 id 1 name "张三" email "zhangsan@example.com"

# 获取所有信息
HGETALL user:1
# 返回：
# 1) "id"
# 2) "1"
# 3) "name"
# 4) "张三"
# 5) "email"
# 6) "zhangsan@example.com"
```

---

## 四、List 类型操作（列表/队列）

| 命令 | 作用 | 示例 |
|------|------|------|
| `LPUSH key value` | 左侧插入 | `LPUSH messages "hello"` |
| `RPUSH key value` | 右侧插入 | `RPUSH messages "world"` |
| `LPOP key` | 左侧弹出 | `LPOP messages` |
| `RPOP key` | 右侧弹出 | `RPOP messages` |
| `LRANGE key start stop` | 获取范围 | `LRANGE messages 0 -1` |
| `LLEN key` | 获取长度 | `LLEN messages` |

**实战示例**：
```bash
# 消息队列场景
LPUSH queue:email "send:to:user1"
LPUSH queue:email "send:to:user2"

# 消费消息
RPOP queue:email
```

---

## 五、Set 类型操作（无序集合）

| 命令 | 作用 | 示例 |
|------|------|------|
| `SADD key member` | 添加元素 | `SADD tags "java" "redis"` |
| `SREM key member` | 删除元素 | `SREM tags "java"` |
| `SMEMBERS key` | 获取所有元素 | `SMEMBERS tags` |
| `SISMEMBER key member` | 检查是否成员 | `SISMEMBER tags "redis"` |
| `SINTER key1 key2` | 交集 | `SINTER set1 set2` |
| `SUNION key1 key2` | 并集 | `SUNION set1 set2` |

---

## 六、Sorted Set 类型操作（有序集合）

适合排行榜、评分等场景。

| 命令 | 作用 | 示例 |
|------|------|------|
| `ZADD key score member` | 添加元素（带分数） | `ZADD rank 100 "user1"` |
| `ZRANGE key start stop` | 按分数升序获取 | `ZRANGE rank 0 -1` |
| `ZREVRANGE key start stop` | 按分数降序获取 | `ZREVRANGE rank 0 9` |
| `ZSCORE key member` | 获取元素分数 | `ZSCORE rank "user1"` |
| `ZINCRBY key increment member` | 增加分数 | `ZINCRBY rank 10 "user1"` |

**实战示例**：
```bash
# 排行榜
ZADD leaderboard 100 "player1"
ZADD leaderboard 200 "player2"
ZADD leaderboard 150 "player3"

# 获取 Top 10
ZREVRANGE leaderboard 0 9 WITHSCORES
```

---

## 七、数据库管理命令

| 命令 | 作用 | 示例 |
|------|------|------|
| `SELECT n` | 切换数据库（0-15） | `SELECT 1` |
| `DBSIZE` | 当前数据库 key 数量 | `DBSIZE` |
| `FLUSHDB` | 清空当前数据库 | `FLUSHDB` |
| `FLUSHALL` | 清空所有数据库 | `FLUSHALL`（慎用！） |
| `INFO` | 查看服务器信息 | `INFO` |

---

## 八、本项目常用查询（forum-auth 模块）

本项目 Redis 配置使用 **database: 1**，连接后需要先切换数据库。

```bash
# 1. 切换到 1 号数据库
SELECT 1

# 2. 查看所有 Token
KEYS token:*

# 3. 查看特定用户的 Access Token
GET token:access:1

# 4. 查看特定用户的 Refresh Token
GET token:refresh:1

# 5. 查看 Token 剩余过期时间
TTL token:access:1

# 6. 手动删除 Token（强制下线）
DEL token:access:1
DEL token:refresh:1

# 7. 查看当前数据库有多少 key
DBSIZE
```

---

## 九、SQL vs Redis 命令对比

| 操作 | SQL | Redis |
|------|-----|-------|
| 插入 | `INSERT INTO table VALUES (...)` | `SET key value` |
| 查询 | `SELECT * FROM table WHERE id=1` | `GET key` |
| 删除 | `DELETE FROM table WHERE id=1` | `DEL key` |
| 更新 | `UPDATE table SET col=val WHERE id=1` | `SET key newvalue` |
| 条件查询 | `SELECT * FROM table WHERE col LIKE '%x%'` | `KEYS pattern`（不推荐生产使用） |

---

## 十、连接 Redis

### 命令行连接

```bash
# 默认连接本地 6379 端口，0 号数据库
redis-cli

# 指定数据库编号
redis-cli -n 1

# 指定主机和端口
redis-cli -h localhost -p 6379

# 带密码连接
redis-cli -a your_password
```

### IDEA 连接

1. 打开 `View` → `Tool Windows` → `Database`
2. 点击 `+` → `Data Source` → `Redis`
3. 配置连接信息：
   - Host: `localhost`
   - Port: `6379`
   - Database: `1`（根据项目配置）
4. 点击 `Test Connection` 测试连接
