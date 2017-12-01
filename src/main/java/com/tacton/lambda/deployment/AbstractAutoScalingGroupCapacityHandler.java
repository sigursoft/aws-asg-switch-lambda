package com.tacton.lambda.deployment;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsyncClientBuilder;
import com.amazonaws.services.autoscaling.model.SetDesiredCapacityRequest;
import com.amazonaws.services.autoscaling.model.SetDesiredCapacityResult;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;

/**
 * 
 * Abstract class used in descending Lambda's to set capacity of auto scaling
 * groups to desired level.
 * 
 * @author anton.kozik
 *
 */
public abstract class AbstractAutoScalingGroupCapacityHandler {

	abstract int getMinimum();

	abstract int getDesired();

	public SetDesiredCapacityResult changeTargetASGInstanceCapacity() {
		// configure environment variables
		String region = System.getenv("REGION");
		String autoScalingGroupName = System.getenv("AUTO_SCALING_GROUP_NAME");
		if (region == null || autoScalingGroupName == null) {
			throw new RuntimeException("Please provide REGION and AUTO_SCALING_GROUP_NAME environment variables");
		}
		return executeAutoScaling(region, autoScalingGroupName);
	}

	public SetDesiredCapacityResult changeTargetASGInstanceCapacity(String autoScalingGroupName) {
		// configure environment variables
		String region = System.getenv("REGION");
		if (region == null || autoScalingGroupName == null) {
			throw new RuntimeException("Please provide REGION and AUTO_SCALING_GROUP_NAME environment variables");
		}
		return executeAutoScaling(region, autoScalingGroupName);
	}
	
	private SetDesiredCapacityResult executeAutoScaling(String region, String autoScalingGroupName) {
		// prepare asynchronous client
		AmazonAutoScalingAsyncClientBuilder builder = AmazonAutoScalingAsyncClientBuilder.standard();
		builder.setCredentials(new DefaultAWSCredentialsProviderChain());
		builder.setRegion(region);
		AmazonAutoScaling asg = builder.build();

		// set minimum capacity to 0
		UpdateAutoScalingGroupRequest updateAutoScalingGroup = new UpdateAutoScalingGroupRequest();
		updateAutoScalingGroup.withAutoScalingGroupName(autoScalingGroupName).setMinSize(getMinimum());
		asg.updateAutoScalingGroup(updateAutoScalingGroup);

		// set desired capacity to 0
		SetDesiredCapacityRequest capacity = new SetDesiredCapacityRequest();
		capacity.withAutoScalingGroupName(autoScalingGroupName);
		capacity.setDesiredCapacity(getDesired());
		SetDesiredCapacityResult result = asg.setDesiredCapacity(capacity);
		return result;
	}

}
