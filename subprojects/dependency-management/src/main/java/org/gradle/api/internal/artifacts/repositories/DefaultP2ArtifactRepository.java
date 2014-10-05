/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.artifacts.repositories;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.repositories.P2ArtifactRepository;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.internal.artifacts.ModuleVersionPublisher;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ConfiguredModuleComponentRepository;
import org.gradle.api.internal.artifacts.repositories.resolver.P2Resolver;
import org.gradle.internal.component.external.model.ModuleComponentArtifactMetaData;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransport;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransportFactory;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.internal.resource.local.FileStore;
import org.gradle.internal.resource.local.LocallyAvailableResourceFinder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DefaultP2ArtifactRepository extends AbstractAuthenticationSupportedRepository implements P2ArtifactRepository, ResolutionAwareRepository, PublicationAwareRepository {
    private final FileResolver fileResolver;
    private final RepositoryTransportFactory transportFactory;
    private Object url;
    private List<Object> additionalUrls = new ArrayList<Object>();
    private final LocallyAvailableResourceFinder<ModuleComponentArtifactMetaData> locallyAvailableResourceFinder;
    private final FileStore<ModuleComponentArtifactMetaData> artifactFileStore;

    public DefaultP2ArtifactRepository(FileResolver fileResolver, PasswordCredentials credentials, RepositoryTransportFactory transportFactory,
                                          LocallyAvailableResourceFinder<ModuleComponentArtifactMetaData> locallyAvailableResourceFinder,
                                          FileStore<ModuleComponentArtifactMetaData> artifactFileStore) {
        super(credentials);
        this.fileResolver = fileResolver;
        this.transportFactory = transportFactory;
        this.locallyAvailableResourceFinder = locallyAvailableResourceFinder;
        this.artifactFileStore = artifactFileStore;
    }

    public URI getUrl() {
        return url == null ? null : fileResolver.resolveUri(url);
    }

    public void setUrl(Object url) {
        this.url = url;
    }

    public ModuleVersionPublisher createPublisher() {
        return createRealResolver();
    }

    public ConfiguredModuleComponentRepository createResolver() {
        return createRealResolver();
    }

    protected P2Resolver createRealResolver() {
        URI rootUri = getUrl();
        if (rootUri == null) {
            throw new InvalidUserDataException("You must specify a URL for a P2 repository.");
        }

        P2Resolver resolver = createResolver(rootUri);

        return resolver;
    }

    private P2Resolver createResolver(URI rootUri) {
        RepositoryTransport transport = getTransport(rootUri.getScheme());
        return new P2Resolver(getName(), rootUri, transport, locallyAvailableResourceFinder, artifactFileStore);
    }

    protected FileStore<ModuleComponentArtifactMetaData> getArtifactFileStore() {
        return artifactFileStore;
    }

    protected RepositoryTransport getTransport(String scheme) {
        return transportFactory.createTransport(scheme, getName(), getCredentials());
    }

    protected LocallyAvailableResourceFinder<ModuleComponentArtifactMetaData> getLocallyAvailableResourceFinder() {
        return locallyAvailableResourceFinder;
    }
}
