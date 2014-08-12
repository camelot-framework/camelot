package ru.yandex.qatools.camelot.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import static ru.yandex.qatools.camelot.maven.service.CamelotRunner.camelot;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Mojo(name = "stop", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class StopMojo extends AbstractMojo {

    @Parameter(defaultValue = "8080")
    protected int jettyPort;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            camelot().shutdown(jettyPort);
        } catch (Exception e) {
            getLog().warn("Failed to shutdown runner", e);
        }
    }
}
