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
package org.gradle.api.internal.artifacts.repositories

import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.internal.artifacts.repositories.resolver.P2Resolver
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransport
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransportFactory
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.filestore.ivy.ArtifactIdentifierFileStore
import org.gradle.internal.resource.local.LocallyAvailableResourceFinder
import org.gradle.internal.resource.transport.ExternalResourceRepository
import org.gradle.logging.ProgressLoggerFactory
import spock.lang.Specification

class DefaultP2ArtifactRepositoryTest extends Specification {
    final FileResolver fileResolver = Mock()
    final PasswordCredentials credentials = Mock()
    final RepositoryTransportFactory transportFactory = Mock()
    final LocallyAvailableResourceFinder locallyAvailableResourceFinder = Mock()
    final ExternalResourceRepository resourceRepository = Mock()
    final ProgressLoggerFactory progressLoggerFactory = Mock()
    final ArtifactIdentifierFileStore artifactIdentifierFileStore = Stub()

    final DefaultP2ArtifactRepository repository = new DefaultP2ArtifactRepository(
            fileResolver, credentials, transportFactory, locallyAvailableResourceFinder,
            artifactIdentifierFileStore
    )

    def "default values"() {
        expect:
        repository.url == null
    }

    def "creates a resolver for HTTP url"() {
        repository.name = 'name'
        repository.url = 'http://host/'

        given:
        fileResolver.resolveUri('http://host/') >> new URI('http://host/')
        transportFactory.createTransport('http', 'name', credentials) >> transport()


        when:
        def resolver = repository.createResolver()

        then:
        with(resolver) {
            it instanceof P2Resolver
            repository == resourceRepository
            name == 'name'
            uri == new URI('http://host/')
        }
    }

    def "creates a resolver for file patterns"() {
        def file = new File("repo")
        def fileUri = file.toURI()
        repository.name = 'name'
        repository.url = 'repo-dir'

        given:
        fileResolver.resolveUri('repo-dir') >> fileUri
        transportFactory.createTransport('file', 'name', credentials) >> transport()

        when:
        def resolver = repository.createResolver()

        then:
        with(resolver) {
            it instanceof P2Resolver
            repository instanceof ExternalResourceRepository
            name == 'name'
            uri == fileUri
        }
    }

    private RepositoryTransport transport() {
        return Mock(RepositoryTransport) {
            getRepository() >> resourceRepository
        }
    }
}
