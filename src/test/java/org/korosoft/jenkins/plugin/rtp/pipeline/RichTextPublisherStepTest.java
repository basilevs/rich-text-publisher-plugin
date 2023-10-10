/*

The New BSD License

Copyright (c) 2023-2023, Spirent Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright notice, this
  list of conditions and the following disclaimer in the documentation and/or
  other materials provided with the distribution.

- Neither the name of the Jenkins RuSalad Plugin nor the names of its
  contributors may be used to endorse or promote products derived from this
  software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
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
        List<String> lines = Arrays.asList("pipeline {", 
                "agent any", // 
                "stages {",// 
                "  stage('Example') {",// 
                "    steps {", //
                "        echo 'Ensure pipeline basics are injected'",
                "    }",
                "    post {",//
                "      always {",//
                "        script { ",//
                "        rtp stableText: '<a href=http://artifactory/rtp-test1><b>http://artifactory/rtp-test1</b>Artifact 1</a>'", //
                "        rtp stableText: '<a href=http://artifactory/rtp-test1><b>http://artifactory/rtp-test1</b>Artifact 2</a>'", //
                "}}}}}}");

        String pipelineText = lines.stream().collect(Collectors.joining("\n"));
        foo.setDefinition(new CpsFlowDefinition(pipelineText, true));

        WorkflowRun w = j.assertBuildStatusSuccess(foo.scheduleBuild2(0));
        j.assertLogContains("Ensure pipeline basics are injected", w);

        String status = w.getActions(BuildRichTextAction.class).stream().map(BuildRichTextAction::getRichText)
                .collect(Collectors.joining("\n"));
        try {
            assertTrue("Should contain Artifact 2: " + status, status.contains("Artifact 2"));
            // Disabled until https://issues.jenkins.io/browse/JENKINS-72140 is fixed 
            // assertTrue("Should contain Artifact 1: " + status, status.contains("Artifact 1"));
        } catch (AssertionError e) {
            throw new AssertionError(JenkinsRule.getLog(w), e);
        }
    }

}
