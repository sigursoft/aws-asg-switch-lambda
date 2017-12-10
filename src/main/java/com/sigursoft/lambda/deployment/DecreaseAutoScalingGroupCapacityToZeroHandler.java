package com.sigursoft.lambda.deployment;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * 
 * This Lambda will decrease the number of running instances to 0. Use REGION
 * environment variable to set region. Use AUTO_SCALING_GROUP_NAME environment
 * variable to set target auto scaling group.
 * 
 * @author anton.kozik
 *
 */
public class DecreaseAutoScalingGroupCapacityToZeroHandler extends AbstractAutoScalingGroupCapacityHandler
		implements RequestHandler<Object, String> {

	@Override
	public String handleRequest(Object input, Context context) {
		context.getLogger().log("Input: " + input);
		return changeTargetASGInstanceCapacity().toString();
	}

	@Override
	int getMinimum() {
		return 0;
	}

	@Override
	int getDesired() {
		return 0;
	}

}
