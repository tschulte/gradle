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

package org.gradle.nativeplatform.internal.prebuilt

import org.gradle.nativeplatform.BuildType
import org.gradle.nativeplatform.Flavor
import org.gradle.nativeplatform.PrebuiltLibrary
import org.gradle.nativeplatform.platform.Platform
import spock.lang.Specification

class DefaultPrebuiltStaticLibraryBinaryTest extends Specification {
    def binary = new DefaultPrebuiltStaticLibraryBinary("name", Stub(PrebuiltLibrary), Stub(BuildType), Stub(Platform), Stub(Flavor))

    def "has useful string representation"() {
        expect:
        binary.toString() == "static library 'name'"
        binary.displayName == "static library 'name'"
    }

    def "can set static library file"() {
        given:
        def file = createFile()

        when:
        binary.staticLibraryFile = file

        then:
        binary.staticLibraryFile == file
        binary.linkFiles.files == [file] as Set

        and:
        binary.runtimeFiles.empty
    }

    def createFile() {
        def file = Stub(File) {
            exists() >> true
            isFile() >> true
        }
    }

}
