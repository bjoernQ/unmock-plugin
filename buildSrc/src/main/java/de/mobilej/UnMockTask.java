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

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Gradle task to handle unmocking.
 *
 * Actual work is delegated to {@link ProcessRealAndroidJar} but this task handles defining all inputs
 * and outputs so Gradle can do proper up-to-date checking and only run the task when necessary.
 */
@CacheableTask
public class UnMockTask extends DefaultTask {

    private File allAndroid;
    private File outputDir;
    private File unmockedOutputJar;
    private List<String> keepClasses;
    private List<String> renameClasses;
    private List<String> delegateClasses;

    @InputFile
    public File getAllAndroid() {
        return allAndroid;
    }

    public void setAllAndroid(File allAndroid) {
        this.allAndroid = allAndroid;
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    @OutputFile
    public File getUnmockedOutputJar() {
        return unmockedOutputJar;
    }

    public void setUnmockedOutputJar(File unmockedOutputJar) {
        this.unmockedOutputJar = unmockedOutputJar;
    }

    @Input
    public List<String> getKeepClasses() {
        return keepClasses;
    }

    public void setKeepClasses(List<String> keepClasses) {
        this.keepClasses = keepClasses;
    }

    @Input
    public List<String> getRenameClasses() {
        return renameClasses;
    }

    public void setRenameClasses(List<String> renameClasses) {
        this.renameClasses = renameClasses;
    }

    @Input
    public List<String> getDelegateClasses() {
        return delegateClasses;
    }

    public void setDelegateClasses(List<String> delegateClasses) {
        this.delegateClasses = delegateClasses;
    }

    @TaskAction
    public void unmock() {
        requireNonNull(allAndroid, "Missing android-all.jar file required for unmocking");
        requireNonNull(outputDir, "No output directory provided for UnMockTask");
        requireNonNull(unmockedOutputJar, "No output file name provided for UnMockTask");
        requireNonNull(keepClasses, "UnMock keep class list cannot be null");
        requireNonNull(renameClasses, "UnMock rename class list cannot be null");
        requireNonNull(delegateClasses, "UnMock delegate class list cannot be null");

        try {
            ProcessRealAndroidJar.process(
                    allAndroid,
                    outputDir,
                    unmockedOutputJar,
                    keepClasses.toArray(new String[0]),
                    renameClasses.toArray(new String[0]),
                    delegateClasses.toArray(new String[0]),
                    getProject().getLogger()
            );
        } catch (Exception e) {
            throw new GradleException("Exception while unmocking", e);
        }
    }
}
