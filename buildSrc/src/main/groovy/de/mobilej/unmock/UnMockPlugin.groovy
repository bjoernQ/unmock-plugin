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

        project.task('unMock') << {

            ProcessRealAndroidJar.process(project.unMock.allAndroid, project.unMock.keep,
                    project.unMock.rename,
                    "$project.buildDir/intermediates/unmocked-android.jar",
                    "$project.buildDir/intermediates/",
                    project.buildFile,
                    project.logger)

        }

        project.afterEvaluate {
            project.tasks.each {
                task ->
                    if (task.name ==~ /compile.*UnitTestJava/) {
                        task.dependsOn('unMock')
                    }
            }
        }


    }
}

class UnMockExtension {

    String allAndroid

    String[] keep

    String[] rename
}