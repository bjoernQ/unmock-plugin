# Android Unmock Gradle Plugin

## Purpose

This is a super simple plugin to be used in combination with the new unit testing feature of the Gradle Plugin / Android Studio. ( http://tools.android.com/tech-docs/unit-testing-support )

It tries to solve the problem that you have to mock each and every Android SDK class (even SparseArray, TextUtils etc.) by allowing you to use selected classes from a real Android-Jarfile.

## How to use

Add this to your buildscript dependencies:

```groovy
classpath 'de.mobilej.unmock:UnMockPlugin:0.1.0'
```

And this to the module's build script:

```groovy
apply plugin: 'de.mobilej.unmock'
```

Additionally you have to configure which classes to use and where to get the real Android.jar from. e.g:

```groovy
unMock {
    // URI to download the android-all.jar from. e.g. https://oss.sonatype.org/content/groups/public/org/robolectric/android-all/
    allAndroid =
            'https://oss.sonatype.org/content/groups/public/org/robolectric/android-all/4.3_r2-robolectric-0/android-all-4.3_r2-robolectric-0.jar'

    // classes to keep
    // matched by "startsWith" - you also need to include the dependencies manually
    keep = [
            "android.text.TextUtils",
            "android.util.",
            "android.text.",
            "android.content.ContentValues",
            "android.content.Intent",
            "android.location.Location",
            "android.content.res.Configuration",
            "org.",
            "libcore.",
            "com.android.internal.R",
            "com.android.internal.util."
    ]
}
```

That's it. I use the android-all.jar from the Robolectric project for convenience.

Have a look at the example contained in this repository.

## Versions

|Version|Description|
|-------|-----------|
|0.1.0|initial public release|


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
