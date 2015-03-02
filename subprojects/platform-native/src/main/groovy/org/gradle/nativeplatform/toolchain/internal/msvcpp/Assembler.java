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

package org.gradle.nativeplatform.toolchain.internal.msvcpp;

import com.google.common.collect.Iterables;
import org.gradle.api.Transformer;
import org.gradle.internal.operations.BuildOperationProcessor;
import org.gradle.nativeplatform.toolchain.internal.ArgsTransformer;
import org.gradle.nativeplatform.toolchain.internal.ArgsTransformerFactory;
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolInvocationWorker;
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolContext;
import org.gradle.nativeplatform.toolchain.internal.ObjectFileExtensionCalculator;
import org.gradle.nativeplatform.toolchain.internal.compilespec.AssembleSpec;

import java.util.List;

class Assembler extends VisualCppNativeCompiler<AssembleSpec> {

    Assembler(BuildOperationProcessor buildOperationProcessor, CommandLineToolInvocationWorker commandLineTool, CommandLineToolContext invocationContext, Transformer<AssembleSpec, AssembleSpec> specTransformer, ObjectFileExtensionCalculator objectFileExtensionCalculator, boolean useCommandFile) {
        super(buildOperationProcessor, commandLineTool, invocationContext, getArgsTransformerFactory(), specTransformer, objectFileExtensionCalculator, useCommandFile);
    }

    protected Iterable<String> buildPerFileArgs(List<String> genericArgs, List<String> sourceArgs, List<String> outputArgs) {
        // ml/ml64 have position sensitive arguments,
        // e.g., /Fo must appear before /c and /c must appear before the source file.

        return Iterables.concat(outputArgs, genericArgs, sourceArgs);
    }

    private static class AssemblerArgsTransformer extends VisualCppCompilerArgsTransformer<AssembleSpec> {
    }

    private static ArgsTransformerFactory<AssembleSpec> getArgsTransformerFactory() {
        return new ArgsTransformerFactory<AssembleSpec>() {
            @Override
            public ArgsTransformer<AssembleSpec> create(AssembleSpec spec) {
                return new AssemblerArgsTransformer();
            }
        };
    }
}
