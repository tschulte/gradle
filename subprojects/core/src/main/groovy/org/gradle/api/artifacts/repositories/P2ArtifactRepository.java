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
package org.gradle.api.artifacts.repositories;

import org.gradle.api.Incubating;

import java.net.URI;

/**
 * <p>An artifact repository which uses a P2 format to store artifacts and meta-data.</p>
 *
 */
@Incubating
public interface P2ArtifactRepository extends ArtifactRepository, AuthenticationSupported {

    /**
     * The base URL of this repository.
     *
     * @return The URL.
     */
    URI getUrl();

    /**
     * Sets the base URL of this repository. The provided value is evaluated as per {@link org.gradle.api.Project#uri(Object)}. This means,
     * for example, you can pass in a File object or a relative path which is evaluated relative to the project directory.
     *
     * File are resolved based on the supplied URL for this repository.
     *
     * @param url The base URL.
     */
    void setUrl(Object url);

}
