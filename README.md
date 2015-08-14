# Android Unmock Gradle Plugin

## Purpose

This is a super simple plugin to be used in combination with the new unit testing feature of the Gradle Plugin / Android Studio. ( http://tools.android.com/tech-docs/unit-testing-support )

It tries to solve the problem that you have to mock each and every Android SDK class (even SparseArray, TextUtils etc.) by allowing you to use selected classes from a real Android-Jarfile.

## How to use

Available on jcenter: [ ![Download](https://api.bintray.com/packages/bjoernq/maven/de.mobilej.unmock/images/download.svg) ](https://bintray.com/bjoernq/maven/de.mobilej.unmock/_latestVersion)

Add the plugin to your buildscript dependencies and make sure to use the jcenter repository:

```groovy
buildscript {
    repositories {
        jcenter()
    }
    
    dependencies {
        classpath 'de.mobilej.unmock:UnMockPlugin:0.3.3'
    }
}
```

And this to the module's build script:

```groovy
apply plugin: 'de.mobilej.unmock'
```

Additionally you have to configure which classes to use and where to get the real Android.jar from. e.g:

```groovy
unMock {
    // URI to download the android-all.jar from. e.g. https://oss.sonatype.org/content/groups/public/org/robolectric/android-all/
    downloadFrom 'https://oss.sonatype.org/content/groups/public/org/robolectric/android-all/4.3_r2-robolectric-0/android-all-4.3_r2-robolectric-0.jar'

    keepStartingWith "android.text.TextUtils"
    keepStartingWith "android.util."
    keepStartingWith "android.text."
    keepStartingWith "android.content.ContentValues"
    keepStartingWith "android.content.Intent"
    keep "android.location.Location"
    keepStartingWith "android.content.res.Configuration"
    keepStartingWith "org."
    keepStartingWith "libcore."
    keepStartingWith "com.android.internal.R"
    keepStartingWith "com.android.internal.util."
    keep "android.net.Uri"

    keepAndRename "java.nio.charset.Charsets" to "xjava.nio.charset.Charsets"
}
```

keep keeps the specified class (and it's possible present inner classes)
keepStartingWith keeps every class which FQN starts with the given string
keepAndRename let you keep a class while renaming it (e.g. needed for classes in the "java" top-level package since these are only allowed to be loaded from the boot classpath)

That's it. I use the android-all.jar from the Robolectric project for convenience.

Have a look at the example contained in this repository for more details.

## Versions

|Version|Description|
|-------|-----------|
|0.1.0|initial public release|
|0.1.1|added the '-' matching|
|0.1.2|fixed a bug preventing the plugin to work correctly on Windows systems|
|0.1.3|the binary is targeting Java 1.7, again|
|0.2.0|support class renaming, rebuild jar if build file changed|
|0.3.0|use Gradle way of upToDate check, have a DSL for the configuration|
|0.3.1|compile with Gradle 2.4|
|0.3.2|lib-sample and some bugfixes (frozen class problem)|
|0.3.3|Android Gradle Plugin 1.3.0 compatibility|

## License

```
Copyright 2015 Björn Quentin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
