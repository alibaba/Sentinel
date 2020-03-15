local redisKey = "sentinel:token:"..KEYS[1];
local configKey = "sentinel:config:"..KEYS[1];
local acq = tonumber(string.sub(KEYS[2], 0, #KEYS[2] - #KEYS[1]));

redis.replicate_commands();
-- get config from redis
local config = redis.call("HGETALL", configKey);
if(next(config) == nil)
then
    return -2;
end;

local sampleCount,windowLengthInMs,thresholdCount,intervalInMs;
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
    elseif("intervalInMs" == config[i])
    then
        intervalInMs = tonumber(config[i + 1]);
    end
end

--calculate current bucket
local times=redis.call('TIME');
local bucketIdx = (times[1]*1000+times[2]/1000)/windowLengthInMs;

local bucketIdxStr = string.format("%d",bucketIdx);

local isExistBucket = redis.call('HGET',redisKey,bucketIdxStr);

if(isExistBucket == false)
then
    -- delete expired bucket
    local allKeys = redis.call('HKEYS', redisKey);
    local delKeys = {};
    local dekIdx = 1;
    local lastIndex = (bucketIdx - sampleCount);
    for key, value in pairs(allKeys) do
        if(tonumber(value) <= lastIndex)
        then
            delKeys[dekIdx] = value;
            dekIdx = dekIdx + 1;
        end
    end


    if(dekIdx > 1)
    then
        redis.call("HDEL", redisKey, unpack(delKeys));
    end

    -- add new bucket
    redis.call('HSET',redisKey, bucketIdxStr, 0);
end;


-- count bucket sum
local datas = redis.call('HVALS', redisKey);
local sum = 0;
for key, value in pairs(datas) do
    sum = sum + tonumber(value);
end

-- check qps
local qps = sum / (intervalInMs / 1000);
if(thresholdCount - qps < acq)
then
    return -1;
end

-- add acq num
redis.call('HINCRBYFLOAT',redisKey,bucketIdxStr,acq);

return 1;