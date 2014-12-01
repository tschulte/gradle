/*
 * Copyright 2013 the original author or authors.
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
package org.gradle.api.internal.component;

import org.gradle.util.GUtil;

public enum ArtifactType {
    SOURCES, JAVADOC, IVY_DESCRIPTOR, MAVEN_POM, P2_ARTIFACTS, P2_CONTENT;

    public String toString() {
        return String.format("'%s' artifacts", GUtil.toWords(name()));
    }
}
