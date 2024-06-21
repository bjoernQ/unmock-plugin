/*
   Copyright (C) 2015 Björn Quentin

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package de.mobilej;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.transform.CacheableTransform;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;

import static java.util.Objects.requireNonNull;

/**
 * Gradle transform to handle unmocking.
 * <p>
 * Actual work is delegated to {@link ProcessRealAndroidJar} but this task handles defining all inputs
 * and outputs so Gradle can do proper up-to-date checking and only run the task when necessary.
 * <p>
 * Note that all File properties specify {@link PathSensitivity} NONE. This is because the task does not care
 * about the names or paths of any of these files. All that matters to this task is that the contents of the
 * input jar can be read and that data can be written to the output directory and output jar file.
 */
@CacheableTransform
public abstract class UnMockTransform implements TransformAction<UnmockParameters> {
    @InputArtifact
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        File allAndroidFile = getInputArtifact().get().getAsFile();

        UnmockParameters parameters = getParameters();

        try {
            ProcessRealAndroidJar.process(
                    allAndroidFile,
                    parameters.getTmpDir(),
                    outputs.file(allAndroidFile.getName()),
                    parameters.getKeepClasses().toArray(new String[0]),
                    parameters.getRenameClasses().toArray(new String[0]),
                    parameters.getDelegateClasses().toArray(new String[0]),
                    LoggerFactory.getLogger("UnmockTransform")
            );
        } catch (Exception e) {
            throw new GradleException("Exception while unmocking", e);
        }
    }
}
