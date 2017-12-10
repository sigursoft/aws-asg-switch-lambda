package com.sigursoft.lambda.deployment;

import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.autoscaling.model.AttachLoadBalancerTargetGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeLoadBalancerTargetGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeLoadBalancerTargetGroupsResult;
import com.amazonaws.services.autoscaling.model.DetachLoadBalancerTargetGroupsRequest;
import com.amazonaws.services.autoscaling.model.LoadBalancerTargetGroupState;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * 
 * Example input JSON: { "from" : "AutoScalingGrouop-Blue", "to" :
 * "AutoScalingGrouop-Green" }
 * 
 * @author anton.kozik
 *
 */
public class SwitchAutoScalingGroupHandler extends AbstractAutoScalingGroupCapacityHandler
		implements RequestHandler<Switch, String> {

	@Override
	public String handleRequest(Switch input, Context context) {
		String region = System.getenv("REGION");
		if (region == null) {
			throw new RuntimeException("Please provide REGION environment variable");
		}
		// take auto scaling group which is attached to ALB (1)
		String fromAutoScalingGroup = input.getFrom();
		if (fromAutoScalingGroup == null || "".equals(fromAutoScalingGroup)) {
			throw new RuntimeException("Online Auto Scaling Group Name is absent (from field)");
		}
		// take auto scaling group which is not attached to ALB (2)
		String toAutoScalingGroup = input.getTo();
		if (toAutoScalingGroup == null || "".equals(toAutoScalingGroup)) {
			throw new RuntimeException("Offline Auto Scaling Group Name is absent (to field)");
		}
		// bring new instances up in target auto scaling group
		changeTargetASGInstanceCapacity(toAutoScalingGroup);
		AmazonAutoScaling asg = prepareAutoScalingGroupClient(region);
		// get all attached target groups
		List<String> targetGroupsArns = collectAllTargetGroupARNs(asg, fromAutoScalingGroup);
		if (targetGroupsArns != null && targetGroupsArns.size() > 0) {
			// detach from 1
			detachTargetGroups(asg, fromAutoScalingGroup, targetGroupsArns);
			// attach to 2
			attachTargetGroups(asg, toAutoScalingGroup, targetGroupsArns);
			return "Successfully switched target groups from " + fromAutoScalingGroup + " to " + toAutoScalingGroup + ".";
		} else {
			return "No Target Groups attached to " + fromAutoScalingGroup + ". Nothing to do.";
		}
	}

	private AmazonAutoScaling prepareAutoScalingGroupClient(String region) {
		AmazonAutoScalingClientBuilder builder = AmazonAutoScalingClientBuilder.standard();
		builder.setCredentials(new DefaultAWSCredentialsProviderChain());
		builder.setRegion(region);
		return builder.build();
	}

	private List<String> collectAllTargetGroupARNs(AmazonAutoScaling asg, String autoScalingGroup) {
		DescribeLoadBalancerTargetGroupsRequest request = new DescribeLoadBalancerTargetGroupsRequest();
		request.setAutoScalingGroupName(autoScalingGroup);
		DescribeLoadBalancerTargetGroupsResult attachedTargetGroups = asg.describeLoadBalancerTargetGroups(request);
		List<LoadBalancerTargetGroupState> targetGroups = attachedTargetGroups.getLoadBalancerTargetGroups();
		System.out.println("Number of target groups in " + autoScalingGroup + " : " + targetGroups.size());
		return targetGroups.stream().map((entry) -> entry.getLoadBalancerTargetGroupARN()).collect(Collectors.toList());
	}

	private void detachTargetGroups(AmazonAutoScaling asg, String fromAutoScalingGroup, List<String> targetGroups) {
		DetachLoadBalancerTargetGroupsRequest detachRequest = new DetachLoadBalancerTargetGroupsRequest();
		detachRequest.setAutoScalingGroupName(fromAutoScalingGroup);
		detachRequest.setTargetGroupARNs(targetGroups);
		asg.detachLoadBalancerTargetGroups(detachRequest);
	}

	private void attachTargetGroups(AmazonAutoScaling asg, String toAutoScalingGroup, List<String> targetGroups) {
		AttachLoadBalancerTargetGroupsRequest attachRequest = new AttachLoadBalancerTargetGroupsRequest();
		attachRequest.setAutoScalingGroupName(toAutoScalingGroup);
		attachRequest.setTargetGroupARNs(targetGroups);
		asg.attachLoadBalancerTargetGroups(attachRequest);
	}

	@Override
	int getMinimum() {
		return 1;
	}

	@Override
	int getDesired() {
		return 1;
	}
}
