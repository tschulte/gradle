/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.sonar.runner;

import org.gradle.api.Action;
import org.gradle.process.JavaForkOptions;

/**
 * An extension for configuring the analysis process options.
 * <p>
 * The extension is added to the plugin's target project.
 * <p>
 * Example usage:
 * <pre>
 * sonarRunner {
 *   toolVersion = '2.3' // default
 *
 *   forkOptions {
 *     maxHeapSize = '1024m'
 *     jvmArgs '-XX:MaxPermSize=128m'
 *   }
 * }
 * </pre>
 */
public class SonarRunnerRootExtension extends SonarRunnerExtension {

    /**
     * {@value}
     */
    public static final String DEFAULT_SONAR_RUNNER_VERSION = "2.3";

    private String toolVersion = DEFAULT_SONAR_RUNNER_VERSION;
    private JavaForkOptions forkOptions;

    /**
     * Configure the {@link #forkOptions}.
     *
     * @param action the action to use to configure {@link #forkOptions}
     */
    public void forkOptions(Action<? super JavaForkOptions> action) {
        action.execute(forkOptions);
    }

    /**
     * Version of Sonar Runner JARs to use.
     * <p>
     * Defaults to {@link #DEFAULT_SONAR_RUNNER_VERSION}.
     */
    public String getToolVersion() {
        return toolVersion;
    }

    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

    /**
     * Options for the Sonar Runner process.
     */
    public JavaForkOptions getForkOptions() {
        return forkOptions;
    }

    public void setForkOptions(JavaForkOptions forkOptions) {
        this.forkOptions = forkOptions;
    }

}
