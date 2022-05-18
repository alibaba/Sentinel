package com.alibaba.csp.sentinel.command.handler;

import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModifyParamDegradeRulesCommandHandlerTest {

    ModifyParamDegradeRulesCommandHandler handler = new ModifyParamDegradeRulesCommandHandler();

    @Test
    public void testModify() {
        CommandRequest request = new CommandRequest();
        CommandResponse response = handler.handle(request);
        assertFalse(response.isSuccess());

        request.addParam("data", "%%");
        response = handler.handle(request);
        assertFalse(response.isSuccess());

        request.addParam("data", "%5B%7B%22resource%22%3A%22resA%22%2C%22limitApp%22%3A%22default%22%2C%22grade%22%3A0%2C%22count%22%3A2.0%2C%22timeWindow%22%3A20%2C%22minRequestAmount%22%3A5%2C%22slowRatioThreshold%22%3A0.9%2C%22statIntervalMs%22%3A20000%2C%22paramIdx%22%3A0%2C%22paramDegradeItemList%22%3A%5B%5D%7D%5D");
        response = handler.handle(request);
        assertTrue(response.isSuccess());
    }


}
