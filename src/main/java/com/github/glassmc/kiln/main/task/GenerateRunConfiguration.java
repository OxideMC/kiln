package com.github.glassmc.kiln.main.task;

import com.github.glassmc.kiln.main.KilnMainPlugin;
import com.github.glassmc.kiln.common.Util;
import com.github.glassmc.kiln.standard.mappings.ObfuscatedMappingsProvider;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public abstract class GenerateRunConfiguration extends DefaultTask {

    private final String runConfigurationTemplate =
            "<component name=\"ProjectRunConfigurationManager\">\n" +
            "  <configuration default=\"false\" name=\"%s\" type=\"Application\" factoryName=\"Application\">\n" +
            "    <option name=\"MAIN_CLASS_NAME\" value=\"%s\" />\n" +
            "    <module name=\"%s\" />\n" +
            "    <option name=\"PROGRAM_PARAMETERS\" value=\"%s\" />\n" +
            "    <option name=\"VM_PARAMETERS\" value=\"%s\" />\n" +
            "    <option name=\"WORKING_DIRECTORY\" value=\"run\" />\n" +
            "    <method v=\"2\">\n" +
            "      <option name=\"Gradle.BeforeRunTask\" enabled=\"true\" tasks=\"shadowJar\" externalProjectPath=\"$PROJECT_DIR$\" vmOptions=\"\" scriptParameters=\"\" />\n" +
            "    </method>\n" +
            "  </configuration>\n" +
            "</component>";

    @TaskAction
    public void run() {
        String environment = (String) this.getProject().getProperties().get("minecraftEnvironment");
        String version = (String) this.getProject().getProperties().get("minecraftVersion");

        File runConfigurationsFile = new File(".idea/runConfigurations");
        runConfigurationsFile.mkdirs();

        File pluginCache = KilnMainPlugin.getInstance().getCache();
        File jar = Util.downloadMinecraft(environment, version, pluginCache, new ObfuscatedMappingsProvider());
        File dependencies = new File(jar.getParentFile(), "libraries");
        File natives = new File(jar.getParentFile(), "natives");

        StringBuilder vmArgsBuilder = new StringBuilder();
        vmArgsBuilder.append("-Xbootclasspath/a:").append(jar.getAbsolutePath()).append(File.pathSeparator);
        for(File dependency : Objects.requireNonNull(dependencies.listFiles())) {
            vmArgsBuilder.append(dependency.getAbsolutePath()).append(File.pathSeparator);
        }
        vmArgsBuilder.append(" -Djava.library.path=").append(natives.getAbsolutePath());

        String name = environment.substring(0, 1).toUpperCase(Locale.ROOT) + environment.substring(1) + " " + version;
        String mainClass = environment.equals("client") ? "com.github.glassmc.loader.client.GlassClientMain" : "com.github.glassmc.loader.server.GlassServerMain";
        String module = getProject().getRootProject().getName() + ".main";
        String programArguments = "--accessToken 0 --version " + version + " --userProperties {}";
        String vmArguments = vmArgsBuilder.toString();

        String runConfigurationData = String.format(this.runConfigurationTemplate, name, mainClass, module, programArguments, vmArguments);

        try {
            FileWriter fileWriter = new FileWriter(new File(runConfigurationsFile, name.replace(" ", "_").replace(".", "_") + ".xml"));
            fileWriter.write(runConfigurationData);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new File("run").mkdirs();
    }

}
