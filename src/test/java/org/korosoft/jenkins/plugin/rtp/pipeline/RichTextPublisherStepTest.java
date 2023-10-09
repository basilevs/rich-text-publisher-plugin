package org.korosoft.jenkins.plugin.rtp.pipeline;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class RichTextPublisherStepTest extends Assert {
	
	@Rule
	public JenkinsRule j = new JenkinsRule();
	
	/** https://issues.jenkins.io/browse/JENKINS-72140 **/
	@Test
	public void accumulateStatus() throws Exception {
		
		WorkflowJob foo = j.jenkins.createProject(WorkflowJob.class, "foo");
		List<String> lines = Arrays.asList(
                "   rtp stableText: '<a href=http://artifactory/rtp-test1><b>http://artifactory/rtp-test1</b>Artifact 1</a>'",
                "   rtp stableText: '<a href=http://artifactory/rtp-test1><b>http://artifactory/rtp-test1</b>Artifact 2</a>'"
                );
//		lines = Arrays.asList("node {}");
				
		String pipelineText = lines.stream().collect(Collectors.joining("\n"));
		foo.setDefinition(new CpsFlowDefinition(pipelineText, true));
		
		// raises error "No such DSL method 'rtp' found among [build, checkout, input, load, node, parallel, stage, stash, unstash, ws]"
		WorkflowRun w = j.assertBuildStatusSuccess(foo.scheduleBuild2(0).get());
		
		assertEquals(true, true);
	}

}
