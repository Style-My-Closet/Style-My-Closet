local key    = KEYS[1]                   -- 스트림 키 = notification:{userId}
local id     = ARGV[1]                   -- minId

local deletedCount = redis.call('XTRIM', key, 'MINID', id)
local length = redis.call('XLEN', key)
if length == 0 then redis.call('DEL', key) end
return deletedCount