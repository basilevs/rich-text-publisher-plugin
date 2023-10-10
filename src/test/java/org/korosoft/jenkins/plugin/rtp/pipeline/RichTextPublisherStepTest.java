package org.korosoft.jenkins.plugin.rtp.pipeline;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.korosoft.jenkins.plugin.rtp.BuildRichTextAction;


public class RichTextPublisherStepTest extends Assert {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /** https://issues.jenkins.io/browse/JENKINS-72140 **/
    @Test
    public void accumulateStatus() throws Exception {

        WorkflowJob foo = j.jenkins.createProject(WorkflowJob.class, "foo");
        List<String> lines = Arrays.asList("pipeline {", "agent any", "stages {", "  stage('Example') {", "    steps {",
        		"      script { ",
        		"      echo 'Hi!' ",
                "      rtp stableText: '<a href=http://artifactory/rtp-test1><b>http://artifactory/rtp-test1</b>Artifact 1</a>'",
                "      rtp stableText: '<a href=http://artifactory/rtp-test1><b>http://artifactory/rtp-test1</b>Artifact 2</a>'",
                "      throw stuff", "}}}}}");
//         lines = Arrays.asList("pipeline {",
//                "      rtp stableText: '<a href=http://artifactory/rtp-test1><b>http://artifactory/rtp-test1</b>Artifact 1</a>'",
//                "      rtp stableText: '<a href=http://artifactory/rtp-test1><b>http://artifactory/rtp-test1</b>Artifact 2</a>'",
//                "}");
//         lines = Arrays.asList("",
//                 "      rtp stableText: '<a href=http://artifactory/rtp-test1><b>http://artifactory/rtp-test1</b>Artifact 1</a>'",
//                 "      rtp stableText: '<a href=http://artifactory/rtp-test1><b>http://artifactory/rtp-test1</b>Artifact 2</a>'",
//                 "");

//                 lines = Arrays.asList("node {}");

        String pipelineText = lines.stream().collect(Collectors.joining("\n"));
        foo.setDefinition(new CpsFlowDefinition(pipelineText, true));

        WorkflowRun w = j.assertBuildStatusSuccess(foo.scheduleBuild2(0));
        j.assertLogContains("Hi!", w);

        String status = w.getActions(BuildRichTextAction.class).stream().map(BuildRichTextAction::getRichText)
                .collect(Collectors.joining("\n"));
        try {
            assertTrue(status, status.contains("Artifact 1"));
            assertTrue(status, status.contains("Artifact 2"));
        } catch (AssertionError e) {
            throw new AssertionError(JenkinsRule.getLog(w), e);
        }
    }

}
