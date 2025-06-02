package com.cy.ns.gateway.util;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

/**
 * Redis 工具类，封装了常用的 Redis 操作方法
 * 包括字符串、哈希、列表、集合、有序集合等数据类型的操作
 * 
 * @author Haechi
 * @date 2025/4/5
 */
@Component
public class RedisUtil {
    @Resource
    private StringRedisTemplate redisTemplate;


    /**
     * 获取 RedisTemplate 实例
     * @return RedisTemplate 实例
     */
    public StringRedisTemplate getRedisTemplate() {
        return this.redisTemplate;
    }

    /* -------------------key相关操作--------------------- */

    /**
     * 删除指定的 key
     * @param key 要删除的键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除指定的 key
     * @param keys 要删除的键集合
     */
    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 序列化指定的 key
     * @param key 要序列化的键
     * @return 序列化后的字节数组
     */
    public byte[] dump(String key) {
        return redisTemplate.dump(key);
    }

    /**
     * 检查指定的 key 是否存在
     * @param key 要检查的键
     * @return 如果键存在返回 true，否则返回 false
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置 key 的过期时间
     * @param key 要设置过期时间的键
     * @param timeout 过期时间长度
     * @param unit 时间单位
     * @return 设置成功返回 true，否则返回 false
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 设置 key 在指定时间过期
     * @param key 要设置过期时间的键
     * @param date 过期时间点
     * @return 设置成功返回 true，否则返回 false
     */
    public Boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(key, date);
    }

    /**
     * 查找匹配指定模式的 key
     * @param pattern 匹配模式，支持通配符
     * @return 匹配的 key 集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 将 key 移动到指定的数据库
     * @param key 要移动的键
     * @param dbIndex 目标数据库索引
     * @return 移动成功返回 true，否则返回 false
     */
    public Boolean move(String key, int dbIndex) {
        return redisTemplate.move(key, dbIndex);
    }

    /**
     * 移除 key 的过期时间，使 key 永久有效
     * @param key 要移除过期时间的键
     * @return 移除成功返回 true，否则返回 false
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    /**
     * 获取 key 的剩余过期时间
     * @param key 要查询的键
     * @param unit 时间单位
     * @return 剩余过期时间，如果 key 不存在返回 -2，如果 key 没有过期时间返回 -1
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 获取 key 的剩余过期时间（以秒为单位）
     * @param key 要查询的键
     * @return 剩余过期时间（秒），如果 key 不存在返回 -2，如果 key 没有过期时间返回 -1
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 从当前数据库中随机返回一个 key
     * @return 随机返回的 key，如果数据库为空返回 null
     */
    public String randomKey() {
        return redisTemplate.randomKey();
    }

    /**
     * 修改 key 的名称
     * @param oldKey 原键名
     * @param newKey 新键名
     */
    public void rename(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * 仅当 newKey 不存在时，将 oldKey 改名为 newKey
     * @param oldKey 原键名
     * @param newKey 新键名
     * @return 重命名成功返回 true，否则返回 false
     */
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    /**
     * 返回 key 所储存的值的类型
     * @param key 要查询的键
     * @return 值的类型，可能的值有：NONE, STRING, LIST, SET, Z_SET, HASH
     */
    public DataType type(String key) {
        return redisTemplate.type(key);
    }

    /* -------------------string相关操作--------------------- */

    /**
     * 设置指定 key 的值
     * @param key 键
     * @param value 值
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取指定 key 的值
     * @param key 键
     * @return 值，如果 key 不存在返回 null
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 返回 key 中字符串值的子字符
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     * @return 子字符串，如果 key 不存在返回 null
     */
    public String getRange(String key, long start, long end) {
        return redisTemplate.opsForValue().get(key, start, end);
    }

    /**
     * 将给定 key 的值设为 value，并返回 key 的旧值
     * @param key 键
     * @param value 新值
     * @return 旧值，如果 key 不存在返回 null
     */
    public String getAndSet(String key, String value) {
        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    /**
     * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)
     * @param key 键
     * @param offset 偏移量
     * @return 指定偏移量上的位值，如果 key 不存在返回 false
     */
    public Boolean getBit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    /**
     * 批量获取多个 key 的值
     * @param keys 键集合
     * @return 值集合，如果某个 key 不存在，对应的位置为 null
     */
    public List<String> multiGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 设置字符串的指定位
     * @param key 键
     * @param offset 偏移量
     * @param value 要设置的值，true 为 1，false 为 0
     * @return 设置前的位值
     */
    public boolean setBit(String key, long offset, boolean value) {
        return Boolean.TRUE.equals( redisTemplate.opsForValue().setBit( key, offset, value ) );
    }

    /**
     * 设置 key 的值和过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void setEx(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 只有在 key 不存在时设置 key 的值
     * @param key 键
     * @param value 值
     * @return 设置成功返回 true，如果 key 已存在返回 false
     */
    public boolean setIfAbsent(String key, String value) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value));
    }

    /**
     * 用 value 参数覆写给定 key 所储存的字符串值，从偏移量 offset 开始
     * @param key 键
     * @param value 新值
     * @param offset 开始覆写的偏移量
     */
    public void setRange(String key, String value, long offset) {
        redisTemplate.opsForValue().set(key, value, offset);
    }

    /**
     * 获取字符串的长度
     * @param key 键
     * @return 字符串长度，如果 key 不存在返回 0
     */
    public Long size(String key) {
        return redisTemplate.opsForValue().size(key);
    }

    /**
     * 批量设置多个 key-value 对
     * @param maps key-value 集合
     */
    public void multiSet(Map<String, String> maps) {
        redisTemplate.opsForValue().multiSet(maps);
    }

    /**
     * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在
     * @param maps key-value 集合
     * @return 设置成功返回 true，如果任何一个 key 已存在返回 false
     */
    public boolean multiSetIfAbsent(Map<String, String> maps) {
        return Boolean.TRUE.equals( redisTemplate.opsForValue().multiSetIfAbsent( maps ) );
    }

    /**
     * 将 key 中储存的数字值增一
     * @param key 键
     * @param increment 增量
     * @return 增加后的值
     */
    public Long incrBy(String key, long increment) {
        return redisTemplate.opsForValue().increment(key, increment);
    }

    /**
     * 将 key 中储存的数字值增加指定的浮点数
     * @param key 键
     * @param increment 增量
     * @return 增加后的值
     */
    public Double incrByFloat(String key, double increment) {
        return redisTemplate.opsForValue().increment(key, increment);
    }

    /**
     * 将 value 追加到 key 原来的值的末尾
     * @param key 键
     * @param value 要追加的值
     * @return 追加后字符串的长度
     */
    public Integer append(String key, String value) {
        return redisTemplate.opsForValue().append(key, value);
    }

    /* -------------------hash相关操作------------------------- */

    /**
     * 获取存储在哈希表中指定字段的值
     * @param key 键
     * @param field 字段
     * @return 字段对应的值，如果字段不存在返回 null
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取哈希表中所有字段和值
     * @param key 键
     * @return 包含所有字段和值的 Map
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取哈希表中多个字段的值
     * @param key 键
     * @param fields 字段集合
     * @return 字段值列表，如果某个字段不存在，对应的位置为 null
     */
    public List<Object> hMultiGet(String key, Collection<Object> fields) {
        return redisTemplate.opsForHash().multiGet(key, fields);
    }

    /**
     * 设置哈希表中字段的值
     * @param key 键
     * @param hashKey 字段
     * @param value 值
     */
    public void hPut(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 批量设置哈希表中的字段和值
     * @param key 键
     * @param maps 字段和值的映射
     */
    public void hPutAll(String key, Map<String, String> maps) {
        redisTemplate.opsForHash().putAll(key, maps);
    }

    /**
     * 仅当字段不存在时，设置哈希表中字段的值
     * @param key 键
     * @param hashKey 字段
     * @param value 值
     * @return 设置成功返回 true，如果字段已存在返回 false
     */
    public Boolean hPutIfAbsent(String key, String hashKey, String value) {
        return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
    }

    /**
     * 删除哈希表中的一个或多个字段
     * @param key 键
     * @param fields 要删除的字段
     * @return 被删除字段的数量
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 查看哈希表中指定的字段是否存在
     * @param key 键
     * @param field 字段
     * @return 字段存在返回 true，否则返回 false
     */
    public boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 为哈希表中指定字段的整数值加上增量
     * @param key 键
     * @param field 字段
     * @param increment 增量
     * @return 增加后的值
     */
    public Long hIncrBy(String key, Object field, long increment) {
        return redisTemplate.opsForHash().increment(key, field, increment);
    }

    /**
     * 为哈希表中指定字段的浮点数值加上增量
     * @param key 键
     * @param field 字段
     * @param delta 增量
     * @return 增加后的值
     */
    public Double hIncrByFloat(String key, Object field, double delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    /**
     * 获取哈希表中的所有字段
     * @param key 键
     * @return 字段集合
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取哈希表中字段的数量
     * @param key 键
     * @return 字段数量
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 获取哈希表中所有值
     * @param key 键
     * @return 值列表
     */
    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 迭代哈希表中的键值对
     * @param key 键
     * @param options 扫描选项
     * @return 键值对迭代器
     */
    public Cursor<Entry<Object, Object>> hScan(String key, ScanOptions options) {
        return redisTemplate.opsForHash().scan(key, options);
    }

    /* ------------------------list相关操作---------------------------- */

    /**
     * 通过索引获取列表中的元素
     * @param key 键
     * @param index 索引位置，0 表示第一个元素，-1 表示最后一个元素
     * @return 指定索引位置的元素，如果索引超出范围返回 null
     */
    public String lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 获取列表指定范围内的元素
     * @param key 键
     * @param start 开始位置，0 表示第一个元素
     * @param end 结束位置，-1 表示最后一个元素
     * @return 元素列表
     */
    public List<String> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 将元素插入到列表头部
     * @param key 键
     * @param value 要插入的值
     * @return 插入后列表的长度
     */
    public Long lLeftPush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 将多个元素插入到列表头部
     * @param key 键
     * @param value 要插入的值数组
     * @return 插入后列表的长度
     */
    public Long lLeftPushAll(String key, String... value) {
        return redisTemplate.opsForList().leftPushAll(key, value);
    }

    /**
     * 将多个元素插入到列表头部
     * @param key 键
     * @param value 要插入的值集合
     * @return 插入后列表的长度
     */
    public Long lLeftPushAll(String key, Collection<String> value) {
        return redisTemplate.opsForList().leftPushAll(key, value);
    }

    /**
     * 当列表存在时才将元素插入到列表头部
     * @param key 键
     * @param value 要插入的值
     * @return 插入后列表的长度，如果列表不存在返回 0
     */
    public Long lLeftPushIfPresent(String key, String value) {
        return redisTemplate.opsForList().leftPushIfPresent(key, value);
    }

    /**
     * 在列表中的指定元素前插入新元素
     * @param key 键
     * @param pivot 基准元素
     * @param value 要插入的值
     * @return 插入后列表的长度，如果基准元素不存在返回 -1
     */
    public Long lLeftPush(String key, String pivot, String value) {
        return redisTemplate.opsForList().leftPush(key, pivot, value);
    }

    /**
     * 将元素插入到列表尾部
     * @param key 键
     * @param value 要插入的值
     * @return 插入后列表的长度
     */
    public Long lRightPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 将多个元素插入到列表尾部
     * @param key 键
     * @param value 要插入的值数组
     * @return 插入后列表的长度
     */
    public Long lRightPushAll(String key, String... value) {
        return redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * 将多个元素插入到列表尾部
     * @param key 键
     * @param value 要插入的值集合
     * @return 插入后列表的长度
     */
    public Long lRightPushAll(String key, Collection<String> value) {
        return redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * 当列表存在时才将元素插入到列表尾部
     * @param key 键
     * @param value 要插入的值
     * @return 插入后列表的长度，如果列表不存在返回 0
     */
    public Long lRightPushIfPresent(String key, String value) {
        return redisTemplate.opsForList().rightPushIfPresent(key, value);
    }

    /**
     * 在列表中的指定元素后插入新元素
     * @param key 键
     * @param pivot 基准元素
     * @param value 要插入的值
     * @return 插入后列表的长度，如果基准元素不存在返回 -1
     */
    public Long lRightPush(String key, String pivot, String value) {
        return redisTemplate.opsForList().rightPush(key, pivot, value);
    }

    /**
     * 通过索引设置列表元素的值
     * @param key 键
     * @param index 索引位置
     * @param value 新值
     */
    public void lSet(String key, long index, String value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 移出并获取列表的第一个元素
     * @param key 键
     * @return 被移除的元素，如果列表为空返回 null
     */
    public String lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 移出并获取列表的第一个元素，如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     * @param key 键
     * @param timeout 等待时间
     * @param unit 时间单位
     * @return 被移除的元素，如果超时返回 null
     */
    public String lBLeftPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().leftPop(key, timeout, unit);
    }

    /**
     * 移出并获取列表的最后一个元素
     * @param key 键
     * @return 被移除的元素，如果列表为空返回 null
     */
    public String lRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 移出并获取列表的最后一个元素，如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     * @param key 键
     * @param timeout 等待时间
     * @param unit 时间单位
     * @return 被移除的元素，如果超时返回 null
     */
    public String lBRightPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPop(key, timeout, unit);
    }

    /**
     * 移除列表的最后一个元素，并将该元素添加到另一个列表并返回
     * @param sourceKey 源列表键
     * @param destinationKey 目标列表键
     * @return 被移除的元素，如果源列表为空返回 null
     */
    public String lRightPopAndLeftPush(String sourceKey, String destinationKey) {
        return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
    }

    /**
     * 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它
     * 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     * @param sourceKey 源列表键
     * @param destinationKey 目标列表键
     * @param timeout 等待时间
     * @param unit 时间单位
     * @return 被移除的元素，如果超时返回 null
     */
    public String lBRightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey, timeout, unit);
    }

    /**
     * 删除集合中值等于 value 的元素
     * @param key 键
     * @param index index=0, 删除所有值等于 value 的元素
     *             index>0, 从头部开始删除第一个值等于 value 的元素
     *             index<0, 从尾部开始删除第一个值等于 value 的元素
     * @param value 要删除的值
     * @return 被移除元素的数量
     */
    public Long lRemove(String key, long index, String value) {
        return redisTemplate.opsForList().remove(key, index, value);
    }

    /**
     * 裁剪列表，只保留指定区间内的元素
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     */
    public void lTrim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    /**
     * 获取列表长度
     * @param key 键
     * @return 列表长度
     */
    public Long lLen(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /* --------------------set相关操作-------------------------- */

    /**
     * 向集合添加一个或多个元素
     * @param key 键
     * @param values 要添加的值
     * @return 被添加到集合中的新元素的数量，不包括已存在的元素
     */
    public Long sAdd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 移除集合中一个或多个元素
     * @param key 键
     * @param values 要移除的值
     * @return 被移除元素的数量
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 移除并返回集合中的一个随机元素
     * @param key 键
     * @return 被移除的随机元素，如果集合为空返回 null
     */
    public String sPop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    /**
     * 将元素从一个集合移动到另一个集合
     * @param key 源集合键
     * @param value 要移动的元素
     * @param destKey 目标集合键
     * @return 移动成功返回 true，如果元素不存在返回 false
     */
    public Boolean sMove(String key, String value, String destKey) {
        return redisTemplate.opsForSet().move(key, value, destKey);
    }

    /**
     * 获取集合的大小
     * @param key 键
     * @return 集合中元素的数量
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 判断元素是否是集合的成员
     * @param key 键
     * @param value 要判断的元素
     * @return 如果元素是集合的成员返回 true，否则返回 false
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取两个集合的交集
     * @param key 第一个集合的键
     * @param otherKey 第二个集合的键
     * @return 包含交集元素的集合
     */
    public Set<String> sIntersect(String key, String otherKey) {
        return redisTemplate.opsForSet().intersect(key, otherKey);
    }

    /**
     * 获取一个集合与多个集合的交集
     * @param key 第一个集合的键
     * @param otherKeys 其他集合的键集合
     * @return 包含交集元素的集合
     */
    public Set<String> sIntersect(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().intersect(key, otherKeys);
    }

    /**
     * 获取两个集合的交集并存储到目标集合
     * @param key 第一个集合的键
     * @param otherKey 第二个集合的键
     * @param destKey 目标集合的键
     * @return 存储到目标集合的元素数量
     */
    public Long sIntersectAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().intersectAndStore(key, otherKey, destKey);
    }

    /**
     * 获取一个集合与多个集合的交集并存储到目标集合
     * @param key 第一个集合的键
     * @param otherKeys 其他集合的键集合
     * @param destKey 目标集合的键
     * @return 存储到目标集合的元素数量
     */
    public Long sIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate.opsForSet().intersectAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取两个集合的并集
     * @param key 第一个集合的键
     * @param otherKeys 第二个集合的键
     * @return 包含并集元素的集合
     */
    public Set<String> sUnion(String key, String otherKeys) {
        return redisTemplate.opsForSet().union(key, otherKeys);
    }

    /**
     * 获取一个集合与多个集合的并集
     * @param key 第一个集合的键
     * @param otherKeys 其他集合的键集合
     * @return 包含并集元素的集合
     */
    public Set<String> sUnion(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().union(key, otherKeys);
    }

    /**
     * 获取两个集合的并集并存储到目标集合
     * @param key 第一个集合的键
     * @param otherKey 第二个集合的键
     * @param destKey 目标集合的键
     * @return 存储到目标集合的元素数量
     */
    public Long sUnionAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
    }

    /**
     * 获取一个集合与多个集合的并集并存储到目标集合
     * @param key 第一个集合的键
     * @param otherKeys 其他集合的键集合
     * @param destKey 目标集合的键
     * @return 存储到目标集合的元素数量
     */
    public Long sUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取两个集合的差集
     * @param key 第一个集合的键
     * @param otherKey 第二个集合的键
     * @return 包含差集元素的集合
     */
    public Set<String> sDifference(String key, String otherKey) {
        return redisTemplate.opsForSet().difference(key, otherKey);
    }

    /**
     * 获取一个集合与多个集合的差集
     * @param key 第一个集合的键
     * @param otherKeys 其他集合的键集合
     * @return 包含差集元素的集合
     */
    public Set<String> sDifference(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().difference(key, otherKeys);
    }

    /**
     * 获取两个集合的差集并存储到目标集合
     * @param key 第一个集合的键
     * @param otherKey 第二个集合的键
     * @param destKey 目标集合的键
     * @return 存储到目标集合的元素数量
     */
    public Long sDifference(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().differenceAndStore(key, otherKey, destKey);
    }

    /**
     * 获取一个集合与多个集合的差集并存储到目标集合
     * @param key 第一个集合的键
     * @param otherKeys 其他集合的键集合
     * @param destKey 目标集合的键
     * @return 存储到目标集合的元素数量
     */
    public Long sDifference(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate.opsForSet().differenceAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取集合中的所有元素
     * @param key 键
     * @return 包含所有元素的集合
     */
    public Set<String> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 随机获取集合中的一个元素
     * @param key 键
     * @return 随机元素，如果集合为空返回 null
     */
    public String sRandomMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    /**
     * 随机获取集合中指定数量的元素
     * @param key 键
     * @param count 要获取的元素数量
     * @return 随机元素列表，如果集合为空返回空列表
     */
    public List<String> sRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().randomMembers(key, count);
    }

    /**
     * 随机获取集合中指定数量的不重复元素
     * @param key 键
     * @param count 要获取的元素数量
     * @return 随机元素集合，如果集合为空返回空集合
     */
    public Set<String> sDistinctRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().distinctRandomMembers(key, count);
    }

    /**
     * 迭代集合中的元素
     * @param key 键
     * @param options 扫描选项
     * @return 元素迭代器
     */
    public Cursor<String> sScan(String key, ScanOptions options) {
        return redisTemplate.opsForSet().scan(key, options);
    }

    /* -------------------zSet相关操作--------------------------------*/

    /**
     * 向有序集合添加一个元素，或者更新已存在元素的分数
     * @param key 键
     * @param value 元素值
     * @param score 分数
     * @return 添加成功返回 true，更新已存在元素返回 false
     */
    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 向有序集合添加多个元素
     * @param key 键
     * @param values 元素集合，包含元素值和分数
     * @return 被成功添加的新元素的数量，不包括已存在的元素
     */
    public Long zAdd(String key, Set<TypedTuple<String>> values) {
        return redisTemplate.opsForZSet().add(key, values);
    }

    /**
     * 移除有序集合中的一个或多个元素
     * @param key 键
     * @param values 要移除的元素
     * @return 被移除元素的数量
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 增加元素的分数值，并返回增加后的值
     * @param key 键
     * @param value 元素值
     * @param delta 增量
     * @return 增加后的分数值
     */
    public Double zIncrementScore(String key, String value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    /**
     * 返回元素在集合中的排名，按分数值递增排序
     * @param key 键
     * @param value 元素值
     * @return 排名，0 表示第一位，如果元素不存在返回 null
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 返回元素在集合中的排名，按分数值递减排序
     * @param key 键
     * @param value 元素值
     * @return 排名，0 表示第一位，如果元素不存在返回 null
     */
    public Long zReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 获取有序集合中指定范围内的元素，按分数值递增排序
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置，-1 表示最后一个元素
     * @return 元素集合
     */
    public Set<String> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取有序集合中指定范围内的元素和分数，按分数值递增排序
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置，-1 表示最后一个元素
     * @return 元素和分数的集合
     */
    public Set<TypedTuple<String>> zRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * 获取有序集合中指定分数范围内的元素
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素集合
     */
    public Set<String> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 获取有序集合中指定分数范围内的元素和分数
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素和分数的集合
     */
    public Set<TypedTuple<String>> zRangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }

    /**
     * 获取有序集合中指定分数范围内的元素和分数，并限制返回数量
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @param start 开始位置
     * @param end 结束位置
     * @return 元素和分数的集合
     */
    public Set<TypedTuple<String>> zRangeByScoreWithScores(String key, double min, double max, long start, long end) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, start, end);
    }

    /**
     * 获取有序集合中指定范围内的元素，按分数值递减排序
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置，-1 表示最后一个元素
     * @return 元素集合
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 获取有序集合中指定范围内的元素和分数，按分数值递减排序
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置，-1 表示最后一个元素
     * @return 元素和分数的集合
     */
    public Set<TypedTuple<String>> zReverseRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    /**
     * 获取有序集合中指定分数范围内的元素，按分数值递减排序
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素集合
     */
    public Set<String> zReverseRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
    }

    /**
     * 获取有序集合中指定分数范围内的元素和分数，按分数值递减排序
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素和分数的集合
     */
    public Set<TypedTuple<String>> zReverseRangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
    }

    /**
     * 获取有序集合中指定分数范围内的元素，按分数值递减排序，并限制返回数量
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @param start 开始位置
     * @param end 结束位置
     * @return 元素集合
     */
    public Set<String> zReverseRangeByScore(String key, double min, double max, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, start, end);
    }

    /**
     * 获取有序集合中指定分数范围内的元素数量
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素数量
     */
    public Long zCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * 获取有序集合的大小
     * @param key 键
     * @return 集合大小
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 获取有序集合的大小
     * @param key 键
     * @return 集合大小
     */
    public Long zZCard(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 获取有序集合中指定元素的分数
     * @param key 键
     * @param value 元素值
     * @return 元素分数，如果元素不存在返回 null
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 移除有序集合中指定排名范围内的元素
     * @param key 键
     * @param start 开始排名
     * @param end 结束排名
     * @return 被移除元素的数量
     */
    public Long zRemoveRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    /**
     * 移除有序集合中指定分数范围内的元素
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 被移除元素的数量
     */
    public Long zRemoveRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    /**
     * 计算两个有序集合的并集并存储到目标集合
     * @param key 第一个集合的键
     * @param otherKey 第二个集合的键
     * @param destKey 目标集合的键
     * @return 存储到目标集合的元素数量
     */
    public Long zUnionAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
    }

    /**
     * 计算一个有序集合与多个有序集合的并集并存储到目标集合
     * @param key 第一个集合的键
     * @param otherKeys 其他集合的键集合
     * @param destKey 目标集合的键
     * @return 存储到目标集合的元素数量
     */
    public Long zUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 计算两个有序集合的交集并存储到目标集合
     * @param key 第一个集合的键
     * @param otherKey 第二个集合的键
     * @param destKey 目标集合的键
     * @return 存储到目标集合的元素数量
     */
    public Long zIntersectAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForZSet().intersectAndStore(key, otherKey, destKey);
    }

    /**
     * 计算一个有序集合与多个有序集合的交集并存储到目标集合
     * @param key 第一个集合的键
     * @param otherKeys 其他集合的键集合
     * @param destKey 目标集合的键
     * @return 存储到目标集合的元素数量
     */
    public Long zIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey);
    }

    /**
     * 迭代有序集合中的元素
     * @param key 键
     * @param options 扫描选项
     * @return 元素迭代器
     */
    public Cursor<TypedTuple<String>> zScan(String key, ScanOptions options) {
        return redisTemplate.opsForZSet().scan(key, options);
    }
}