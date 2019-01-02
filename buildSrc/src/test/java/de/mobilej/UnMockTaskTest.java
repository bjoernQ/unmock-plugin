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

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.util.GFileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.gradle.util.GFileUtils.writeFile;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class UnMockTaskTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private File buildGradle;

    private static final String BUILD_GRADLE_CONTENTS = ""
        + "plugins { id 'de.mobilej.unmock' apply false }\n"
        + "\n"
        + "repositories {\n"
        + "    jcenter()\n"
        + "}"
        + "\n"
        // Since we're not adding AGP as a dependency, we need to create this configuration manually for things to work
        + "configurations { testImplementation }"
        + "\n"
        + "apply plugin: 'de.mobilej.unmock'"
        + "\n"
        + "dependencies {\n"
        + "    unmock 'org.robolectric:android-all:7.1.0_r7-robolectric-0'\n"
        + "}\n"
        + "\n"
        + "unMock {\n"
        + "    keep \"android.widget.BaseAdapter\"\n"
        + "    keep \"android.widget.ArrayAdapter\"\n"
        + "    keep \"android.os.Bundle\"\n"
        + "    keepStartingWith \"android.database.MatrixCursor\"\n"
        + "    keep \"android.database.AbstractCursor\"\n"
        + "    keep \"android.database.CrossProcessCursor\"\n"
        + "    keepStartingWith \"android.text.TextUtils\"\n"
        + "    keepStartingWith \"android.util.\"\n"
        + "    keepStartingWith \"android.text.\"\n"
        + "    keepStartingWith \"android.content.ContentValues\"\n"
        + "    keepStartingWith \"android.content.ComponentName\"\n"
        + "    keepStartingWith \"android.content.ContentUris\"\n"
        + "    keepStartingWith \"android.content.ContentProviderOperation\"\n"
        + "    keepStartingWith \"android.content.ContentProviderResult\"\n"
        + "    keepStartingWith \"android.content.UriMatcher\"\n"
        + "    keepStartingWith \"android.content.Intent\"\n"
        + "    keep \"android.location.Location\"\n"
        + "    keepStartingWith \"android.content.res.Configuration\"\n"
        + "    keepStartingWith \"org.\"\n"
        + "    keepStartingWith \"libcore.\"\n"
        + "    keepStartingWith \"com.android.internal.R\"\n"
        + "    keepStartingWith \"com.android.internal.util.\"\n"
        + "    keep \"android.net.Uri\"\n"
        + "\n"
        + "    keepAndRename \"java.nio.charset.Charsets\" to \"xjava.nio.charset.Charsets\"\n"
        + "\n"
        + "    keepStartingWith \"android.icu.\"\n"
        + "}\n";

    @Before
    public void setup() throws Exception {
        buildGradle = testProjectDir.newFile("build.gradle");
        writeFile(BUILD_GRADLE_CONTENTS, buildGradle);

        File localBuildCacheDirectory = testProjectDir.newFolder("local-cache");
        File settingsGradle = testProjectDir.newFile("settings.gradle");
        writeFile("buildCache {\n"
                      + "    local {\n"
                      + "        directory '" + localBuildCacheDirectory.toURI() + "'\n"
                      + "    }\n"
                      + "}\n", settingsGradle);
    }

    @Test
    public void canAddTaskToProject() {
        Project project = ProjectBuilder.builder().build();
        Task task = project.getTasks().create("unMock", UnMockTask.class);
        assertNotNull(task);
    }

    @Test
    public void unMockTaskPassesOnce() {
        BuildResult result = newGradleRunner().build();

        assertSame(result.task(":unMock").getOutcome(), TaskOutcome.SUCCESS);
    }

    @Test
    public void unMockTaskIsUpToDateOnSecondRun() {
        BuildResult firstResult = newGradleRunner().build();
        BuildResult secondResult = newGradleRunner().build();

        assertSame(firstResult.task(":unMock").getOutcome(), TaskOutcome.SUCCESS);
        assertSame(secondResult.task(":unMock").getOutcome(), TaskOutcome.UP_TO_DATE);
    }

    @Test
    public void unMockTaskIsNotUpToDateIfNewKeepClassAdded() {
        BuildResult firstResult = newGradleRunner().build();

        assertSame(firstResult.task(":unMock").getOutcome(), TaskOutcome.SUCCESS);

        writeFile(BUILD_GRADLE_CONTENTS
                      + "unMock {\n"
                      + "    keep \"android.net.Uri\"\n"
                      + "}",
                  buildGradle);

        BuildResult secondResult = newGradleRunner().build();

        assertSame(secondResult.task(":unMock").getOutcome(), TaskOutcome.SUCCESS);
    }

    @Test
    public void unMockTaskIsNotUpToDateIfOutputDirectoryIsDeleted() {
        BuildResult firstResult = newGradleRunner().build();

        assertSame(firstResult.task(":unMock").getOutcome(), TaskOutcome.SUCCESS);

        GFileUtils.deleteDirectory(new File(testProjectDir.getRoot(), "build/intermediates/unmock_work"));

        BuildResult secondResult = newGradleRunner().build();

        assertSame(secondResult.task(":unMock").getOutcome(), TaskOutcome.SUCCESS);
    }

    @Test
    public void unMockTaskIsNotUpToDateIfOutputJarIsDeleted() {
        BuildResult firstResult = newGradleRunner().build();

        assertSame(firstResult.task(":unMock").getOutcome(), TaskOutcome.SUCCESS);

        GFileUtils.forceDelete(new File(testProjectDir.getRoot(),
                                        "build/intermediates/unmocked-android" + testProjectDir.getRoot()
                                                                                               .getName() + ".jar"));

        BuildResult secondResult = newGradleRunner().build();

        assertSame(secondResult.task(":unMock").getOutcome(), TaskOutcome.SUCCESS);
    }

    @Test
    public void unMockTaskIsLoadedFromCacheWhenUsingBuildCache() {
        BuildResult firstResult = newGradleRunner().withArguments("--build-cache", "unMock").build();

        assertSame(firstResult.task(":unMock").getOutcome(), TaskOutcome.SUCCESS);

        GFileUtils.deleteDirectory(new File(testProjectDir.getRoot(), "build"));

        BuildResult secondResult = newGradleRunner().withArguments("--build-cache", "unMock").build();

        assertSame(secondResult.task(":unMock").getOutcome(), TaskOutcome.FROM_CACHE);
    }

    private GradleRunner newGradleRunner() {
        return GradleRunner.create()
                           .withProjectDir(testProjectDir.getRoot())
                           .withPluginClasspath()
                           .withArguments("unMock");
    }
}
