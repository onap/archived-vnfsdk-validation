/*
 * Copyright © 2019 iconectiv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.validation.heat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.onap.cli.fw.cmd.OnapCommand;
import org.onap.cli.fw.cmd.cmd.OpenCommandShellCmd;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cli.fw.input.OnapCommandParameter;
import org.onap.cli.fw.input.OnapCommandParameterType;
import org.onap.cli.fw.output.OnapCommandResultType;
import org.onap.cli.fw.registrar.OnapCommandRegistrar;
import org.onap.cli.fw.schema.OnapCommandSchema;
import org.onap.cvc.results.StandardTestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run VVP.
 */

@OnapCommandSchema(type = "heat")
public class HeatTestingVvpPythonInvoker extends OnapCommand {

  private Logger logger = LoggerFactory.getLogger(HeatTestingVvpPythonInvoker.class);
  private boolean preserveOutput = false;

  public HeatTestingVvpPythonInvoker() {
    String home = System.getenv("ONAP_VVP_HOME");
    if (home == null) {
      logger.error("ONAP_VVP_HOME environment variable is not set");
    }

  }

  @Override
  protected void run() throws OnapCommandException {
    logger.debug("run {}.{}.{}", getInfo().getProduct(), getInfo().getService(), this.getName());

    if (logger.isDebugEnabled()) {
      getParameters().forEach(p -> logger.debug("got {} with value {}.", p.getName(), p.getValue()));
    }

    // determine the name of the incoming file
    String filename = null;
    for (OnapCommandParameter p : getParameters().stream().filter(p -> !p.isDefaultParam())
        .collect(Collectors.toList())) {
      if (OnapCommandParameterType.BINARY.equals(p.getParameterType())) {
        filename = p.getValue().toString();
        if ("".equals(filename) && !p.isOptional()) {
          throw new OnapCommandException("PR002", "No value provided for " + p.getName(), 400);
        }
      }
    }

    logger.debug("process HEAT archive {}", filename);
    File heatFile = new File(filename);
    if ((!heatFile.exists()) || (!heatFile.canRead())) {
      throw new OnapCommandException("PR007", "Unable to read heat archive " + filename, 500);
    }

    OpenCommandShellCmd heatShellCmd = (OpenCommandShellCmd) OnapCommandRegistrar.getRegistrar().get("hot-validate", "onap-dublin");
    if (heatShellCmd == null) {
      throw new OnapCommandException("PR004", "Unable to locate hot-validate / onap-vtp shell command", 500);
    }

    boolean preserveOutput = this.preserveOutput; // default from configuration...

    Map<String, OnapCommandParameter> myParams = getParametersMap();
    if (myParams.containsKey("preserveOutput")) {
      preserveOutput = "true".equalsIgnoreCase(myParams.get("preserveOutput").getValue().toString());
    }

    String outputMode = "json";
    if (myParams.containsKey("outputMode")) {
      outputMode = myParams.get("outputMode").getValue().toString();
    }

    try (HeatArchive heatArchive = new HeatArchive(filename, preserveOutput)) {
      // find the official command...
      heatShellCmd.getParameters().forEach(p -> {
        // set each of the values.
        if ("hot-folder".equals(p.getName())) {
          try {
            p.setValue(heatArchive.getExtractPath());
            logger.debug("set {} to {}", p.getName(), heatArchive.getExtractPath());
          } catch (Exception ex) {
            logger.error("failed to set param {}", p.getName(), ex);
          }
        } else {
          OnapCommandParameter param = myParams.get(p.getName());
          if (param == null) {
            logger.warn("unable to fill param {}", p.getName());
          } else {
            try {
              p.setValue(param.getValue());
              logger.debug("set {} to {}", p.getName(), param.getValue());
            } catch (Exception ex) {
              logger.error("failed to set param {}", p.getName(), ex);
            }
          }
        }
      });

      String vvpHome = System.getenv("ONAP_VVP_HOME");
      if (vvpHome == null) {
        throw new OnapCommandException("PR006", "ONAP_VVP_HOME not set in environment", 500);
      }

      String installPath = vvpHome + "/validation-scripts/ice_validator/tests";
      File ipf = new File(installPath);
      if ((!ipf.exists()) || (!ipf.canRead())) {
        logger.error("cannot read tests install path of {}", installPath);
        throw new OnapCommandException("PR006", "VVP install path not found " + installPath, 500);
      }

      logger.debug("set script-folder to {}", installPath);
      heatShellCmd.getParametersMap().get("script-folder").setValue(installPath);

      // make the existing command happy.
      heatShellCmd.setOutput("$s{file:" + heatArchive.getOutputPath() + ")");

      List<String> cmds = heatShellCmd.getCommand();
      logger.debug("original commands {}", cmds);
      String cmd = cmds.get(0);
      cmd = cmd + " --output-directory=" + heatArchive.getOutputPath();
      heatShellCmd.setCommand(Collections.singletonList(cmd));
      logger.debug("updated commands {}", cmds);

      // set the working folder so that the paths in the results results are local.
      heatShellCmd.setWd(heatArchive.getExtractPath());

      try {
        logger.debug("execute command {} in {}", heatShellCmd.getCommand(), heatShellCmd.getWd());
        heatShellCmd.execute();
        if ("json".equals(outputMode)) {
          setOnapOutput(outputParser(heatArchive.getOutputPath(), "report.json", preserveOutput));
        }
        else {
          this.getResult().setPassed(true);
          this.getResult().setType(heatShellCmd.getResult().getType());
          this.getResult().setOutput(heatShellCmd.getResult().getOutput());
        }
      }
      catch (Exception ex) {
        if ("json".equals(outputMode)) {
          // we got an exception but we should look for output anyway.
          String out = outputParser(heatArchive.getOutputPath(), "report.json", preserveOutput);
          if (out != null) {
            setOnapOutput(out);
          } else {
            // try to use the standard output of the command plugin to return error data.
            getResult().setType(OnapCommandResultType.TEXT);
            JsonObject o = new JsonObject();
            o.addProperty("code", "500");
            if (heatShellCmd.getResult().getOutput() != null) {
              o.addProperty("reason", heatShellCmd.getResult().getOutput().toString());
            }
            getResult().setOutput(new GsonBuilder().create().toJson(o));
            getResult().setPassed(false);
          }
          logger.error("command plugin threw exception", ex);
        } else {
          // stick with traditional output and just throw the exception that the command plugin issued.
          throw ex;
        }
      }
    }
    catch (OnapCommandException oe) {
      throw oe;
    }
    catch (Exception e) {
      throw new OnapCommandException("PR005", "Internal error during HEAT validation", e, 500);
    }
  }

  class HeatArchive implements AutoCloseable {
    File extractDirectory;
    File outputDirectory;
    boolean preserveOutput;

    HeatArchive(String pathToArchive, boolean preserveOutput) {
      this.preserveOutput = preserveOutput;

      extractDirectory = Files.createTempDir();
      logger.debug("new tmp directory {} created", extractDirectory);

      outputDirectory = new File(extractDirectory.toString() + "-output");
      if (!outputDirectory.exists()) {
        if (!outputDirectory.mkdirs()) {
          logger.warn("mkdirs failed for {}", outputDirectory.getName());
        }
        else {
          logger.debug("output directory {} created.", outputDirectory);
        }
      }

      byte[] buffer = new byte[1024];
      try (FileInputStream fis = new FileInputStream(pathToArchive)) {
        try (ZipInputStream zis = new ZipInputStream(fis)) {
          ZipEntry ze = zis.getNextEntry();
          while (ze != null) {
            File newFile = new File(extractDirectory.toString() + File.separator + ze.getName());
            logger.debug("extracting {}", newFile);
            try (FileOutputStream fos = new FileOutputStream(newFile)) {
              int len;
              while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
              }
            }
            zis.closeEntry();
            ze = zis.getNextEntry();
          }
        }
      } catch (IOException e) {
        logger.error("failed to extract archive {}", pathToArchive, e);
      }
    }


    String getExtractPath() {
      try {
        return extractDirectory.getCanonicalPath();
      } catch (IOException ex) {
        logger.error(getExceptionMessage(ex));
        return null;
      }
    }


    String getOutputPath() {
      try {
        return outputDirectory.getCanonicalPath();
      } catch (IOException ex) {
        logger.error(getExceptionMessage(ex));
        return null;
      }
    }

    @Override
    public void close() throws Exception {
      if (preserveOutput) {
        return;
      }
      if (extractDirectory != null) {
        FileUtils.deleteDirectory(extractDirectory);
      }
      if (outputDirectory != null) {
        FileUtils.deleteDirectory(outputDirectory);
      }
    }
  }

  private String outputParser(String pathToOutput, String outputFile, boolean outputPreserved) {
    Gson gson = new Gson();
    Output output;
    try (BufferedReader br = new BufferedReader(new FileReader(pathToOutput + "/" + outputFile))) {
      output = gson.fromJson(br, Output.class);
    } catch (IOException ex) {
      logger.error("failed to read file {}", pathToOutput);
      return null;
    }

    List<StandardTestResult> tests;
    if (output != null) {
      tests = output.getTests().stream().map(item -> {
        StandardTestResult tr = new StandardTestResult();
        tr.setResult(item.getResult());
        tr.setTestname(item.getTest_case());
        tr.setDescription(item.getRequirements().stream().map(Output.Requirements::getText).collect(Collectors.joining(",")));

        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", item.getFiles()));
        sb.append(": ");
        if ("FAIL".equals(item.getResult())) {
          sb.append(item.getError());
          sb.append(".");
        }
        sb.append("Requirements tested: ");
        sb.append(item.getRequirements().stream().map(Output.Requirements::getId).collect(Collectors.joining(",")));
        tr.setMessage(sb.toString());
        return tr;
        }).collect(Collectors.toList());
    }
    else {
      tests = Collections.emptyList();
    }

    gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(tests);
  }

  private String getExceptionMessage(Throwable ex) {
    Throwable current = ex;
    String msg = current.getMessage();
    while ((msg == null) && (current.getCause() != null)) {
      current = current.getCause();
      msg = current.getMessage();
    }
    return msg;
  }

  private void setOnapOutput(String output) throws OnapCommandException {
    if (output == null) {
      return;
    }
    try {
      this.getResult().setPassed(true);
      this.getResult().setType(OnapCommandResultType.TEXT);
      this.getResult().setOutput(output);
    } catch (Exception ex) {
      throw new OnapCommandException("PR003", getExceptionMessage(ex), ex, 500);
    }
  }

  @Override
  protected List<String> initializeProfileSchema(Map<String, ?> schemaMap, boolean validate) {
    List<String> errors = new ArrayList<>();
    logger.debug("initialize profile schema from {}", schemaMap);

    @SuppressWarnings("unchecked")
    Map<String, Object> settings = (Map<String, Object>) schemaMap.get("heat");
    if (settings == null) {
      logger.error("missing 'heat' section in config file");
    }
    else {
      Boolean tmp = (Boolean) settings.get("preserveOutput");
      if (tmp == null) {
        errors.add("Missing preserveOutput in settings");
      }
      else {
        this.preserveOutput = tmp;
      }
    }

    return errors;
  }
}
