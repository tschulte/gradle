This spec covers the consumption of p2 repositories. In the Eclipse eco system it is common to only publish artifacts in an p2 repository.
These artifacts can not be used directly in a project build with gradle.

# Use cases

- Resolve files from a p2 repository

# Out of scope

Creation of P2 repositories is currently planned to be implemented by Wuff (https://github.com/akhikhl/wuff/).

# P2 repository format

P2 seems not to be a specification, just an implementation (http://stackoverflow.com/questions/12601526/where-can-i-find-the-p2-repository-specification). But I don't know
if it is feasible to use the eclipse implementation. Therefore it might be better to re-implement either by reverse engineering or by looking at the source code of the
implementation in eclipse.

## Simple p2 repositories

P2 repositories contain two meta files, content.xml or content.jar and artifacts.xml or artifacts.jar. If the files are as jar instead of xml, the jar contains the xml as only
entry. It seems the eclipse implementation first looks for the jar, and only if not found for the xml.

### artifacts.xml

The file artifacts.xml lists all artifacts with their version, download size and md5 sums. It can also define mappings like

    <rule filter='(&amp; (classifier=osgi.bundle))' output='${repoUrl}/plugins/${id}_${version}.jar'/>

### content.xml

The file content.xml lists all installable units, their name, description. An installable unit might just contain dependencies on other installable units (like a dependency
on a pom). If the installable unit is a jar, the content.xml lists all exported packages and required packages and bundles.

## Composite p2 repositories

A composite p2 repository does not contain any bundles, and also does not have a content.jar or artifacts.jar. Instead it does contain the files compositeContent.jar and
compositeArtifacts.jar (or .xml). These files list other repositories. These other repository locations might be relative or absolute. E.g. for 
http://download.eclipse.org/releases/juno the compositeContent.xml lists following repositories

    <child location='201406250900'/>
    <child location='http://download.eclipse.org/technology/epp/packages/luna/'/>

where the first location is relative, i.e. the resulting url would be http://download.eclipse.org/releases/luna/201402280900

# Implementation plan

Downloading of artifacts from a p2 repository with the exact version can be done using a pattern based ivy repository

    repositories {
        ivy {
            url 'http://download.eclipse.org/technology/epp/packages/luna/'
            layout('pattern') {
                artifact 'plugins/[artifact](.[classifier])_[revision].[ext]'
            }
        }
    }

One problem is that the classifier for source artifacts is 'source' in p2, not sources. The next step would be to parse the meta data (content.jar and artifacts.jar) and use
the information from these files to allow using version ranges and to get information about transitive dependencies.

# Stories

## Allow declaring a p2 repository

    repositories {
        p2 {
            url 'http://download.eclipse.org/releases/luna'
        }
    }

When declaring a p2 repository just using the url, all artifacts in that repository can be used.

## Allow restricting a p2 repository to specific installable units

    repositories {
        p2 {
            url 'http://download.eclipse.org/releases/luna'
            unit id: 'org.eclipse.foo', version: '1.5'
            unit id: 'org.eclipse.bar', version: '2.3'
        }
    }

When declaring a p2 repository using installable units, the artifacts that can be used are restricted by the unit.

## Allow declaring p2 repositories using a target definition file

    repositories {
        p2 {
            from 'luna.target'
        }
    }

A target definition file can be created with Eclipse. In this file one can define multiple repositories with their
installable units.

## Allow resolution dependencies from a p2 repository when using installable units

    dependencies {a
        compile ':org.eclipse.swt:'
    }

When using installable units, the version does not need to be given.

## Allow resolution of fixed dependencies from a p2 repository

    dependencies {
        compile ':org.eclipse.swt:3.102.1.v20140206-1334'
    }

When not using installable units, the version must be given.

## Allow defining dependencies on package level

    dependencies {
        compile package: 'org.eclipse.swt.widgets', version: '3.102.1.v20140206-1334'
    }

P2 repositories contain information about packages of the bundles. Packages in OSGI should be unique. Therefore
it is possible to declare dependencies on package level.

## Allow resolution of version ranges

    dependencies {
        compile ':org.eclipse.swt:3.102.+'
    }

## Resolve transitive dependencies

content.jar/content.xml contains information about every bundle in the repository. Transitive dependencies are
either directly defined by using the module name, or indirect by using the package name. content.xml also contains
the exported packages, using this information the transitive dependencies might be calculated.

## Allow composite p2 repositories

E.g. the luna repository is a composite repository. Instead of content.jar and artifacts.jar, it contains
compositeContent.jar and compositeArtifacts.jar referencing other repositories.
