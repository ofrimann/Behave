/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ofrimann.behave;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import uk.co.caprica.vlcj.player.MediaPlayer;

/**
 *
 * @author Ofri
 */
public class VideoLogger
{
    public static class ActionEntry
    {
        public ActionEntry( boolean isState, String action )
        {
            this.isState = isState;
            this.action = action;
        }
        public boolean isState;
        public String action;

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + (this.isState ? 1 : 0);
            hash = 47 * hash + Objects.hashCode(this.action);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ActionEntry other = (ActionEntry) obj;
            if (this.isState != other.isState) {
                return false;
            }
            if (!Objects.equals(this.action, other.action)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "ActionEntry{" + "isState=" + isState + ", action=" + action + '}';
        }
    }
    
    private SortedMap<Long, ActionEntry> actions = new TreeMap<>();
    private final Ini config;
    private final MediaPlayer player;
    private File logFile;
    private File answerFile;
    private String videoFilePath;
    private String outputFilePrefix;
    private File logDir;
    private boolean gotAnswers;
    private final JDialog questionsDialog;
    private boolean isSaved = true; //empty logs do not need saving
    
    public VideoLogger(Ini loggerConfig, MediaPlayer currentPlayer, File currentFile, JDialog questionsDialog, File baseDir)
    {
        config = loggerConfig;
        player = currentPlayer;
        this.questionsDialog = questionsDialog;
                
        //log file name
        String logDirName = config.get("config","output");
        if ( logDirName == null )
        {
            JOptionPane.showMessageDialog(null, "Please define an output directory","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        logDir = new File(logDirName);
        if ( !logDir.isAbsolute() ) logDir = new File(baseDir,logDirName);
        if ( logDir.isFile() )
        {
            JOptionPane.showMessageDialog(null, "Output directory is an existing file","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        logDir.mkdirs();
        videoFilePath = currentFile.getAbsolutePath();
        outputFilePrefix = currentFile.getName();
        createOutputFiles();
    }
    
    private void createOutputFiles()
    {
        logFile = new File(logDir,outputFilePrefix+".csv");
        int suffix = 0;
        while ( logFile.exists() )
        {
            ++suffix;
            logFile = new File(logDir,outputFilePrefix+"_"+suffix+".csv");
        }
        answerFile = new File(logDir,outputFilePrefix+
                    (suffix > 0 ? "_"+suffix : "") + ".ini");
        gotAnswers = false; //reset answers file
    }

    public int rewindLog(long time)
    {
        int prevSize = actions.size();
        actions = new TreeMap<>(actions.headMap(time));
        int newSize = actions.size();
        isSaved = prevSize == newSize; //if the log size didn't change, we don't need to save it
        return newSize;
    }
    
    public long getLastTime()
    {
        return actions.lastKey();
    }
    
    public long addAction(ActionEntry action) {
        isSaved = false;
        long time = player.getTime();
        actions.put(time,action);
        return time;
    }
    
    public boolean writeLog(boolean prompt)
    {
        writeLog();
        if ( !gotAnswers ) getAnswers(true);
        //by default (no prompt) start a new log file and reset the actions
        if ( !prompt || 
             JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Start a new log file?","New Log",JOptionPane.YES_NO_OPTION) )
        {
            if ( !gotAnswers &&
                 JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "You didn't answer the questions, answer now?",
                            "Last Chance to Answer",
                            JOptionPane.YES_NO_OPTION) ) {
                getAnswers(false);
            }
            //clear the actions map - the log will start at the next action
            actions.clear();
            //create new output files
            createOutputFiles();
            return true;
        }
        return false;
    }
    
    public boolean isLogSaved()
    {
        return isSaved;
    }
    
    private void writeLog()
    {
        List<LogRecord> log = new ArrayList<>();
        LogRecord curStateRecord = null;
        for ( long startTime : actions.keySet() )
        {
            ActionEntry curAction = actions.get(startTime);
            LogRecord curRecord = new LogRecord(curAction.action,curAction.isState ? "State" : "Event", startTime);
            /* there are several options here:
            1. State with the same state active - ignore record
            2. State change - close active state
            3. New state - log the state
            3. Event inside an active state - end the active state, log the event and restart the state
            4. Event outside an active state - log the event
            */
            boolean activeState = curStateRecord != null;
            if ( curAction.isState )
            {
                if ( activeState )
                {
                    //if we are in the same state as before - ignore.
                    if ( curStateRecord.getAction().equals(curRecord.getAction()) ) continue;
                    //state change - set end time
                    curStateRecord.setEndTime(startTime);
                }
                curStateRecord = curRecord;
                log.add(curRecord);
            }
            else
            {
                //events have 0 time
                curRecord.setEndTime(startTime);
                log.add(curRecord);
                if ( activeState )
                {
                    //the current state is already logged -just set the end time
                    curStateRecord.setEndTime(startTime);
                    //now create and log a new state record
                    curStateRecord = new LogRecord(curStateRecord);
                    curStateRecord.setStartTime(startTime);
                    log.add(curStateRecord);
                }
            }
        }
        
        if ( curStateRecord != null )
        {
            //we have a state record without an end
            curStateRecord.setEndTime(player.getTime()); //get the last video time
        }
        
        try(CsvBeanWriter csvWriter = new CsvBeanWriter(new FileWriter(logFile),CsvPreference.EXCEL_PREFERENCE))
        {
            csvWriter.writeHeader(LogRecord.LOG_HEADER);
            for ( LogRecord record : log )
            {
                csvWriter.write(record,LogRecord.LOG_HEADER);
            }
            isSaved = true;
        }
        catch(IOException ioe )
        {
            JOptionPane.showMessageDialog(null, ioe,"Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void getAnswers(boolean allowLater)
    {
        Section questions = config.get("questions");
        if ( questions == null ) {
            try ( PrintWriter answers = new PrintWriter(answerFile))
            {
                answers.println("[answers]");
                answers.println("video="+videoFilePath);
                gotAnswers = true;
            }
            catch ( IOException ioe )
            {
                JOptionPane.showMessageDialog(null, ioe,"Error",JOptionPane.ERROR_MESSAGE);
                gotAnswers = false;
            }
            return;
        }
        Container questionsPane = questionsDialog.getContentPane();
        questionsPane.removeAll();
        GridLayout layout = (GridLayout)questionsPane.getLayout();
        layout.setRows(questions.size() + 1);
        Map<String,JTextField> answerMap = new HashMap<>();
        questions.keySet().stream().forEach((question) -> {
            String labelText = questions.get(question);
            JLabel label = new JLabel(labelText+":",SwingConstants.RIGHT);
            questionsPane.add(label);
            JTextField curField = new JTextField();
            questionsPane.add(curField);
            answerMap.put(question, curField);
        });
        JButton okButton = new JButton("Save Answers");
        questionsPane.add(okButton);
        okButton.addActionListener((ActionEvent e) -> {
            try ( PrintWriter answers = new PrintWriter(answerFile))
            {
                answers.println("[answers]");
                answers.println("video="+videoFilePath);
                answerMap.keySet().stream().forEach((name) -> {
                    String answer = answerMap.get(name).getText();
                    if (!( answer.isEmpty() )) {
                        answers.println(name+"="+answer);
                    }
                });
                gotAnswers = true;
            }
            catch ( IOException ioe )
            {
                JOptionPane.showMessageDialog(null, ioe,"Error",JOptionPane.ERROR_MESSAGE);
                gotAnswers = false;
            }
            questionsDialog.setVisible(false);
        });
        
        JButton cancelButton = new JButton(allowLater ? "Answer Later" : "Don't Know");
        questionsPane.add(cancelButton);
        cancelButton.addActionListener((ActionEvent e) -> {
            gotAnswers = false;
            questionsDialog.setVisible(false);
        });
        questionsDialog.pack();
        questionsDialog.setLocationRelativeTo(null);
        questionsDialog.setVisible(true);
    }
}
