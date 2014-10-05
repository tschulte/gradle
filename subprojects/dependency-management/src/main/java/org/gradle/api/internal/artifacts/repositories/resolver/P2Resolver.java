/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.api.internal.artifacts.repositories.resolver;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ModuleComponentRepositoryAccess;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.DescriptorParseContext;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransport;
import org.gradle.api.internal.component.ArtifactType;
import org.gradle.internal.Transformers;
import org.gradle.internal.component.external.model.*;
import org.gradle.internal.component.model.*;
import org.gradle.internal.resolve.result.BuildableArtifactSetResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult;
import org.gradle.internal.resolve.result.DefaultResourceAwareResolveResult;
import org.gradle.internal.resource.LocallyAvailableExternalResource;
import org.gradle.internal.resource.local.FileStore;
import org.gradle.internal.resource.local.LocallyAvailableResourceFinder;

import java.net.URI;
import java.util.*;

public class P2Resolver extends ExternalResourceResolver {
    private final URI uri;

    public P2Resolver(String name, URI rootUri, RepositoryTransport transport,
                         LocallyAvailableResourceFinder<ModuleComponentArtifactMetaData> locallyAvailableResourceFinder,
                         FileStore<ModuleComponentArtifactMetaData> artifactFileStore) {
        super(name, transport.isLocal(),
                transport.getRepository(),
                transport.getResourceAccessor(),
                new ChainedVersionLister(new MavenVersionLister(transport.getRepository()), new ResourceVersionLister(transport.getRepository())),
                locallyAvailableResourceFinder,
                artifactFileStore);
        this.uri = rootUri;
    }

    @Override
    public String toString() {
        return String.format("P2 repository '%s'", getName());
    }

    public URI getUri() {
        return uri;
    }

    protected void doResolveComponentMetaData(DependencyMetaData dependency, ModuleComponentIdentifier moduleComponentIdentifier, BuildableModuleComponentMetaDataResolveResult result) {
        resolveStaticDependency(dependency, moduleComponentIdentifier, result, super.createArtifactResolver());
    }

    protected boolean isMetaDataArtifact(ArtifactType artifactType) {
        return artifactType == ArtifactType.MAVEN_POM;
    }

    @Override
    protected MutableModuleComponentResolveMetaData processMetaData(MutableModuleComponentResolveMetaData metaData) {
        if (metaData.getId().getVersion().endsWith("-SNAPSHOT")) {
            metaData.setChanging(true);
        }
        return metaData;
    }

    private void resolveUniqueSnapshotDependency(DependencyMetaData dependency, ModuleComponentIdentifier module, BuildableModuleComponentMetaDataResolveResult result, MavenUniqueSnapshotModuleSource snapshotSource) {
        resolveStaticDependency(dependency, module, result, createArtifactResolver(snapshotSource));
        if (result.getState() == BuildableModuleComponentMetaDataResolveResult.State.Resolved) {
            result.getMetaData().setSource(snapshotSource);
        }
    }

    private boolean isSnapshotVersion(ModuleComponentIdentifier module) {
        return module.getVersion().endsWith("-SNAPSHOT");
    }

    @Override
    protected ExternalResourceArtifactResolver createArtifactResolver(ModuleSource moduleSource) {

        if (moduleSource instanceof MavenUniqueSnapshotModuleSource) {
            final String timestamp = ((MavenUniqueSnapshotModuleSource) moduleSource).getTimestamp();
            return new MavenUniqueSnapshotExternalResourceArtifactResolver(super.createArtifactResolver(moduleSource), timestamp);
        }

        return super.createArtifactResolver(moduleSource);
    }

    @Override
    protected IvyArtifactName getMetaDataArtifactName(String moduleName) {
        return new DefaultIvyArtifactName(moduleName, "pom", "pom");
    }

    @Override
    public boolean isM2compatible() {
        return false;
    }

    public ModuleComponentRepositoryAccess getLocalAccess() {
        return new P2LocalRepositoryAccess();
    }

    public ModuleComponentRepositoryAccess getRemoteAccess() {
        return new P2RemoteRepositoryAccess();
    }

    @Override
    protected MutableModuleComponentResolveMetaData createMetaDataForDependency(DependencyMetaData dependency) {
        return new DefaultMavenModuleResolveMetaData(dependency);
    }

    protected MutableModuleComponentResolveMetaData parseMetaDataFromResource(LocallyAvailableExternalResource cachedResource, DescriptorParseContext context) {
        return null;
    }

    protected static MavenModuleResolveMetaData mavenMetaData(ModuleComponentResolveMetaData metaData) {
        return Transformers.cast(MavenModuleResolveMetaData.class).transform(metaData);
    }

    private class P2LocalRepositoryAccess extends LocalRepositoryAccess {
        @Override
        protected void resolveConfigurationArtifacts(ModuleComponentResolveMetaData module, ConfigurationMetaData configuration, BuildableArtifactSetResolveResult result) {
            if (mavenMetaData(module).isKnownJarPackaging()) {
                ModuleComponentArtifactMetaData artifact = module.artifact("jar", "jar", null);
                result.resolved(ImmutableSet.of(artifact));
            }
        }

        @Override
        protected void resolveJavadocArtifacts(ModuleComponentResolveMetaData module, BuildableArtifactSetResolveResult result) {
            // Javadoc artifacts are optional, so we need to probe for them remotely
        }

        @Override
        protected void resolveSourceArtifacts(ModuleComponentResolveMetaData module, BuildableArtifactSetResolveResult result) {
            // Javadoc artifacts are optional, so we need to probe for them remotely
        }
    }

    private class P2RemoteRepositoryAccess extends RemoteRepositoryAccess {
        @Override
        protected void resolveConfigurationArtifacts(ModuleComponentResolveMetaData module, ConfigurationMetaData configuration, BuildableArtifactSetResolveResult result) {
            MavenModuleResolveMetaData mavenMetaData = mavenMetaData(module);
            if (mavenMetaData.isPomPackaging()) {
                Set<ComponentArtifactMetaData> artifacts = new LinkedHashSet<ComponentArtifactMetaData>();
                artifacts.addAll(findOptionalArtifacts(module, "jar", null));
                result.resolved(artifacts);
            } else {
                ModuleComponentArtifactMetaData artifactMetaData = module.artifact(mavenMetaData.getPackaging(), mavenMetaData.getPackaging(), null);

                if (createArtifactResolver(module.getSource()).artifactExists(artifactMetaData, new DefaultResourceAwareResolveResult())) {
                    result.resolved(ImmutableSet.of(artifactMetaData));
                } else {
                    ModuleComponentArtifactMetaData artifact = module.artifact("jar", "jar", null);
                    result.resolved(ImmutableSet.of(artifact));
                }
            }
        }

        @Override
        protected void resolveJavadocArtifacts(ModuleComponentResolveMetaData module, BuildableArtifactSetResolveResult result) {
            result.resolved(findOptionalArtifacts(module, "javadoc", "javadoc"));
        }

        @Override
        protected void resolveSourceArtifacts(ModuleComponentResolveMetaData module, BuildableArtifactSetResolveResult result) {
            result.resolved(findOptionalArtifacts(module, "source", "sources"));
        }
    }
}
