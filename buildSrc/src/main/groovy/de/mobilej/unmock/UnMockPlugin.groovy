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

package de.mobilej.unmock

import de.mobilej.ProcessRealAndroidJar
import org.gradle.api.Plugin
import org.gradle.api.Project

class UnMockPlugin implements Plugin<Project> {

    void apply(Project project) {

        try {
            project.dependencies.
                    add("testCompile",
                            project.files("$project.buildDir/intermediates/unmocked-android.jar"))
        } catch (Exception e) {
            project.logger.warn("Make sure to use Android Gradle plugin version 1.1.0 (or newer)")
            return
        }

        project.extensions.create("unMock", UnMockExtension)

        project.task('unMock') {

            outputs.upToDateWhen {
                return ProcessRealAndroidJar.isUpToDate(
                        project.unMock.allAndroid,
                        project.unMock.downloadTo,
                        project.unMock.keep.toArray(new String[project.unMock.keep.size()]),
                        project.unMock.rename.toArray(new String[project.unMock.rename.size()]),
                        "$project.buildDir/intermediates/unmocked-android.jar",
                        "$project.buildDir/intermediates/",
                        project.buildFile,
                        project.logger)
            }

            doLast {
                ProcessRealAndroidJar.process(
                        project.unMock.allAndroid,
                        project.unMock.downloadTo,
                        project.unMock.keep.toArray(new String[project.unMock.keep.size()]),
                        project.unMock.rename.toArray(new String[project.unMock.rename.size()]),
                        "$project.buildDir/intermediates/unmocked-android.jar",
                        "$project.buildDir/intermediates/",
                        project.buildFile,
                        project.logger)
            }

        }

        project.afterEvaluate {
            project.tasks.each {
                task ->
                    if (task.name ==~ /compile.*UnitTestJava.*/) {
                        task.dependsOn('unMock')
                    }
            }
        }


    }
}

class UnMockExtension {

    String allAndroid

    String downloadTo

    ArrayList<String> keep = new ArrayList<>()

    ArrayList<String> rename = new ArrayList<>()

    boolean usingDefaults = false

    public UnMockExtension() {
        downloadFrom 'https://oss.sonatype.org/content/groups/public/org/robolectric/android-all/4.3_r2-robolectric-0/android-all-4.3_r2-robolectric-0.jar'

        keep "android.widget.BaseAdapter"
        keep "android.widget.ArrayAdapter"
        keep "android.os.Bundle"
        keepStartingWith "android.database.MatrixCursor"
        keep "android.database.AbstractCursor"
        keep "android.database.CrossProcessCursor"
        keepStartingWith "android.text.TextUtils"
        keepStartingWith "android.util."
        keepStartingWith "android.text."
        keepStartingWith "android.content.ContentValues"
        keepStartingWith "android.content.ComponentName"
        keepStartingWith "android.content.ContentUris"
        keepStartingWith "android.content.ContentProviderOperation"
        keepStartingWith "android.content.ContentProviderResult"
        keepStartingWith "android.content.UriMatcher"
        keepStartingWith "android.content.Intent"
        keep "android.location.Location"
        keepStartingWith "android.content.res.Configuration"
        keepStartingWith "org."
        keepStartingWith "libcore."
        keepStartingWith "com.android.internal.R"
        keepStartingWith "com.android.internal.util."
        keep "android.net.Uri"

        keepAndRename "java.nio.charset.Charsets" to "xjava.nio.charset.Charsets"

        usingDefaults = true
    }

    DownloadTo downloadFrom(final String allAndroidUrl) {
        allAndroid = allAndroidUrl
        return new DownloadTo(this)
    }

    void keep(final String clazz) {
        clearDefaultIfNecessary()
        keep.add("-" + clazz)
    }


    void keepStartingWith(final String clazz) {
        clearDefaultIfNecessary()
        keep.add(clazz)
    }

    KeepMapping keepAndRename(final String clazzToKeep) {
        clearDefaultIfNecessary()
        return new KeepMapping(clazzToKeep, this)
    }

    private void clearDefaultIfNecessary() {
        if (usingDefaults) {
            usingDefaults = false
            keep.clear()
            rename.clear()
        }
    }
}

class KeepMapping {
    String keep
    UnMockExtension extension

    KeepMapping(final String whatToKeep, UnMockExtension extension) {
        keep = whatToKeep
        this.extension = extension
    }

    void to(final String renameTo) {
        extension.rename.add(keep + "=" + renameTo)
    }
}

class DownloadTo {
    String to
    UnMockExtension extension

    DownloadTo(UnMockExtension extension) {
        this.extension = extension
    }

    void to(final String where) {
        extension.downloadTo = where
    }
}
