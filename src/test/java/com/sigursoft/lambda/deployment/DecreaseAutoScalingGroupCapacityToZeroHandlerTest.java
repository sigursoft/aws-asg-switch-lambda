package com.sigursoft.lambda.deployment;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

import com.amazonaws.services.lambda.runtime.Context;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class DecreaseAutoScalingGroupCapacityToZeroHandlerTest {

    private static Object input;

    @BeforeClass
    public static void createInput() throws IOException {
        input = null;
    }

    @Test(expected = RuntimeException.class)
    public void testLambdaFunctionHandler() {
        DecreaseAutoScalingGroupCapacityToZeroHandler handler = new DecreaseAutoScalingGroupCapacityToZeroHandler();
        Context ctx = createContext();
        String output = handler.handleRequest(input, ctx);
        assertNotNull(output);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();
        ctx.setFunctionName("Your Function Name");
        return ctx;
    }

}
