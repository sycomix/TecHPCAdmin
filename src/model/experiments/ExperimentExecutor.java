/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experiments;

import common.CommonFunctions;
import files.DirectoryManager;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Observable;
import model.Experiment;
import model.ExperimentExecution;
import model.UserExperimentMapping;

/**
 * This class does all the tasks related to the execution of the experiments.
 * It includes starting and stopping the experiment. It also checks if the
 * experiment execution finished.
 * @author cfernandez
 */
public class ExperimentExecutor {
    // Attributes
    // -------------------------------------------------------------------------

    private Experiment executedExperiment;
    private Process experimentProcess;
    private ProcessVerifier verifier;
    private Date startDate;

    // Constructor
    // -------------------------------------------------------------------------
    public ExperimentExecutor(Experiment experimentToExecute) {
        executedExperiment = experimentToExecute;
    }

    /**
     * Gets the experiment that is is being executed by this instance.
     * @return Experiment that was put under execution by this instance.
     */
    public Experiment getExecutedExperiment() {
        return executedExperiment;
    }

    /**
     * Date at which the experiment was put under execution.
     * @return Date of the execution start.
     */
    public Date getStartDate() {
        return startDate;
    }

    public ExperimentExecution GenerateExperimentExecution(final int userId)
    {
        return new ExperimentExecution(-1, startDate, new Date(),
                GenerateExperimentOutput(userId), verifier.getUsedMemoryPercentage(),
                verifier.getCPUUsagePercentage(), verifier.getCPUTimeSeconds());
    }

    private String GenerateExperimentOutput(final int userId)
    {
        DirectoryManager dirMan = DirectoryManager.GetInstance();
        String baseFilePathname = dirMan.GetPathForExperimentOutput(
                userId, executedExperiment.getId());
        String startTimeText = CommonFunctions.GetDateText(startDate);
        String outputDir = baseFilePathname + startTimeText;
        dirMan.CreateDirectory(outputDir);
        GenerateStandardOutputFile(userId, outputDir);
        GenerateErrorOutputFile(userId, outputDir);
        dirMan.CreateCompressedDir(startTimeText, baseFilePathname);
        dirMan.DeleteFile(outputDir);
        return startTimeText + ".tar.gz";
    }

    /**
     * Standard output of the experiment execution.
     * @userId Id of the user that executed the experiment.
     * @return File in which the standard output was saved.
     */
    private void GenerateStandardOutputFile(final int userId, String stdOutFilePathname)
    {
        InputStream standardOutput = experimentProcess.getInputStream();
        String startTimeText = CommonFunctions.GetDateText(startDate);
        stdOutFilePathname += "/StdOut - ";
        stdOutFilePathname += executedExperiment.getName() + " - " + startTimeText + ".txt";
        File stdOutFile = new File(stdOutFilePathname);
        SaveInputToFile(stdOutFile, standardOutput);
    }

    /**
     * Error output of the experiment execution.
     * @userId Id of the user that executed the experiment.
     * @return File in which the standard output was saved.
     */
    private void GenerateErrorOutputFile(final int userId, String errOutFilePathname)
    {
        InputStream errorOutput = experimentProcess.getInputStream();
        errOutFilePathname += "/Error - ";
        String startTimeText = CommonFunctions.GetDateText(startDate);
        errOutFilePathname += executedExperiment.getName() + " - " + startTimeText + ".txt";
        File errOutFile = new File(errOutFilePathname);
        SaveInputToFile(errOutFile, errorOutput);
    }

    /**
     * Saves an Input Stream to the contents of a non-existent file. Which is
     * created in this beginning. If the file already exists, nothing happens
     * @file File to which the input is going to be saved.
     * @input Input stream to save on a file.
     */
    private void SaveInputToFile(final File file, final InputStream input) {
        try {
            if (!file.exists()) {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                byte[] inputBuffer = new byte[1024];
                int readAmount;
                int totalRead = 0;
                while ((readAmount = input.read(inputBuffer)) > 0)
                {
                    writer.write(new String(inputBuffer), totalRead, readAmount);
                    totalRead += readAmount;
                }
                writer.flush();
                writer.close();
            }
        } catch (IOException ex) {
        }
    }

    // Instance methods
    // -------------------------------------------------------------------------
    /**
     * Starts to run a experiment previously configured by the user
     * @param obs Object to notify when the experiment execution finishes.
     * @param userId User that is executing the experiment.
     * @return Indicates whether the experiment started successfully or not.
     */
    public Boolean RunExperiment(Observable obs, int userId) {
        try {
            // Address of the executable file of the experiment.
            String execAddr = DirectoryManager.GetInstance().
                    GetPathForExperimentExecution(userId, executedExperiment.getId())
                    + executedExperiment.getExecutablePath()
                    + " " + executedExperiment.getInputParametersLine();
            experimentProcess = Runtime.getRuntime().exec(execAddr);
            startDate = new Date();
            verifier = new ProcessVerifier(experimentProcess, obs,
                    new UserExperimentMapping(executedExperiment.getId(), userId));
            verifier.start();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * Stops the execution of the experiment ONLY if was previously running
     * @return Indicates whether the experiment started successfully or not.
     */
    public Boolean StopExperiment() {
        if (experimentProcess != null) {
            verifier.interrupt();
            experimentProcess = null;
            verifier = null;
            return true;
        } else {
            return false;
        }
    }
}
