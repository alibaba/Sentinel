package com.alibaba.csp.sentinel.command.handler;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandHandlerProvider;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @author houyi
 **/
@CommandMapping(name = "api",desc = "get all available command handlers")
public class ApiCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        Map<String, CommandHandler> handlers = CommandHandlerProvider.getInstance().namedHandlers();
        JSONArray array = new JSONArray();
        if(handlers.isEmpty()){
            return CommandResponse.ofSuccess(array.toJSONString());
        }
        for(CommandHandler handler : handlers.values()){
            CommandMapping commandMapping = handler.getClass().getAnnotation(CommandMapping.class);
            if(commandMapping==null){
                continue;
            }
            String api = commandMapping.name();
            String desc = commandMapping.desc();
            JSONObject obj = new JSONObject();
            obj.put("url","/"+api);
            obj.put("desc",desc);
            array.add(obj);
        }
        return CommandResponse.ofSuccess(array.toJSONString());
    }

}
