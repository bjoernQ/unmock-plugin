package de.mobilej;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

interface UnmockParameters extends TransformParameters {
    @Input
    List<String> getKeepClasses();
    void setKeepClasses(List<String> newValue);
    @Input
    List<String> getRenameClasses();
    void setRenameClasses(List<String> newValue);
    @Input
    List<String> getDelegateClasses();
    void setDelegateClasses(List<String> newValue);
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    File getTmpDir();
    void setTmpDir(File newValue);
}
