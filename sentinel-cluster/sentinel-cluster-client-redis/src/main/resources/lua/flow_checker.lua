local redisKey = "sentinel:cluster:token:{"..KEYS[1].."}";
local configKey = "sentinel:cluster:config:{"..KEYS[1].."}";
local acq = tonumber(string.sub(KEYS[2], 0, #KEYS[2] - #KEYS[1] - 2));

local config = redis.call("HGETALL", configKey);
if(config == false or config == nil)
then
    return -2;
end;



local sampleCount,windowLengthInMs,thresholdCount;
for i = 1, #config, 2 do
    if("sampleCount" == config[i])
    then
        sampleCount = tonumber(config[i + 1]);
    elseif("windowLengthInMs" == config[i])
    then
        windowLengthInMs = tonumber(config[i + 1]);
    elseif("thresholdCount" == config[i])
    then
        thresholdCount = tonumber(config[i + 1]);
    end
end

local times=redis.call('TIME');
local index = (times[1]*1000+times[2]/1000)/windowLengthInMs;

local indexStr = string.format("%d",index);

local count = redis.call('HGET',redisKey,indexStr);

if(count == false or count == nil)
then
    -- 清除过期数据
    local allKeys = redis.call('HKEYS', redisKey);
    local delKeys = {};
    local dekIdx = 1;
    local lastIndex = (index - sampleCount);
    for key, value in pairs(allKeys) do
        if(tonumber(value) <= lastIndex)
        then
            delKeys[dekIdx] = value;
            dekIdx = dekIdx + 1;
        end
    end

    redis.replicate_commands()
    if(dekIdx > 1)
    then
        redis.call("HDEL", redisKey, unpack(delKeys));
    end

    -- 添加新的key
    redis.call('HSET',redisKey, indexStr, 0);
end;


-- 统计总数
local datas = redis.call('HVALS', redisKey);
local sum = 0;
for key, value in pairs(datas) do
    sum = sum + tonumber(value);
end

-- 限流判断
count = sum / sampleCount + acq;
if(count > thresholdCount)
then
    return -1;
end

-- 增加次数
redis.replicate_commands();
redis.call('HINCRBYFLOAT',redisKey,indexStr,acq);

return count;