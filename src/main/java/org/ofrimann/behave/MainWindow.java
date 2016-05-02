/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ofrimann.behave;

import com.sun.jna.Native;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.libvlc_log_level_e;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.filter.VideoFileFilter;
import uk.co.caprica.vlcj.filter.swing.SwingFileFilter;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 *
 * @author Ofri
 */
public class MainWindow extends javax.swing.JFrame {

    private class VideoComponent extends EmbeddedMediaPlayerComponent {

        @Override
        protected void onAfterConstruct() {
            super.onAfterConstruct();
            getMediaPlayerFactory().newLog().setLevel(libvlc_log_level_e.level(0));
        }

        @Override
        public void opening(MediaPlayer mediaPlayer) {
            //reset speed if new video
            if (playSpeedSlider.isEnabled()) {
                playSpeedSlider.setValue(5);
            } else //otherwise enable all video controls
            {
                playPauseButton.setEnabled(true);
                playSpeedLabel.setEnabled(true);
                playSpeedSlider.setEnabled(true);
                rewindButton.setEnabled(true);
                fastForwardButton.setEnabled(true);
                saveLogButton.setEnabled(true);
                videoProgressBar.setEnabled(true);
            }
        }

        @Override
        public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
            videoProgressBar.setString(getTimeString(mediaPlayer.getTime(), newLength, mediaPlayer.getRate()));
        }

        @Override
        public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
            videoProgressBar.setString(getTimeString(newTime, mediaPlayer.getLength(), mediaPlayer.getRate()));
            if ( newTime >= logger.getLastTime() ) return;
            int newSize = logger.rewindLog(newTime);
            logTableModel.setRowCount(newSize);
            LogTable.changeSelection(newSize - 1, 0, false, false);
        }

        @Override
        public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
            
            videoProgressBar.setValue(Float.valueOf(newPosition*videoProgressBar.getMaximum()).intValue());
        }

        private String getTimeString(long time, long length, float rate) {
            time = time / 1000;
            length = length / 1000;
            return MessageFormat.format("{0,number,00}:{1,number,00}/{2,number,00}:{3,number,00} [x{4}]",
                    time / 60, time % 60,
                    length / 60, length % 60,
                    rate);
        }

        @Override
        public void paused(MediaPlayer mediaPlayer) {
            playPauseButton.setIcon(playIcon);
        }

        @Override
        public void playing(MediaPlayer mediaPlayer) {
            playPauseButton.setIcon(pauseIcon);
        }

        @Override
        public void finished(MediaPlayer mediaPlayer) {
            logger.writeLog(false); //do not prompt for a reset, a new log file will be automatically created
            logTableModel.setNumRows(0);
            mediaPlayer.stop();
            playPauseButton.setIcon(playIcon);
        }

        @Override
        public void keyPressed(KeyEvent ke) {
            switch (ke.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_KP_LEFT:
                    if (rewindButton.isEnabled()) {
                        rewind();
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_KP_RIGHT:
                    if (fastForwardButton.isEnabled()) {
                        fastForward();
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    if (playPauseButton.isEnabled()) {
                        playPause();
                    }
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_KP_DOWN:
                    if (playSpeedSlider.isEnabled() && playSpeedSlider.getMinimum() < playSpeedSlider.getValue()) {
                        playSpeedSlider.setValue(playSpeedSlider.getValue() - 1);
                    }
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_KP_UP:
                    if (playSpeedSlider.isEnabled() && playSpeedSlider.getMaximum() > playSpeedSlider.getValue()) {
                        playSpeedSlider.setValue(playSpeedSlider.getValue() + 1);
                    }
                    break;
            }
        }
    }

    public MainWindow(String[] args) {
        try {
        setIconImage(ImageIO.read(getClass().getResourceAsStream("/fishicon.png")));
        } catch ( IOException ignore ){}
        
        initComponents();
        logTableModel = (DefaultTableModel) LogTable.getModel();
        playerComp = new VideoComponent();
        //set the minimum size to enable the divider to move freely
        playerComp.setMinimumSize(new Dimension(0, 0));
        videoLogSplitPane.setTopComponent(playerComp);
        player = playerComp.getMediaPlayer();
        videoFileChooser.setFileFilter(new SwingFileFilter("Video Files", VideoFileFilter.INSTANCE));
        configFileChooser.setFileFilter(new FileNameExtensionFilter("INI Files", "ini"));
        baseDir = configFileChooser.getFileSystemView().getDefaultDirectory();
        
        //open config and video files, if passed as arguments
        if ( args.length < 1 ) return;
        File configFile = new File(args[0]);
        if ( ! configFile.isFile() ) return;
        openConfigFile(configFile);
        if ( args.length < 2 ) return;
        File videoFile = new File(args[1]);
        if ( ! videoFile.isFile() ) return;
        openVideoFile(videoFile);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        videoFileChooser = new javax.swing.JFileChooser();
        configFileChooser = new javax.swing.JFileChooser();
        QuestionsDialog = new javax.swing.JDialog();
        tableVideoSplitPane = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        KeyTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        playSpeedLabel = new javax.swing.JLabel();
        playSpeedSlider = new javax.swing.JSlider();
        rewindButton = new javax.swing.JButton();
        playPauseButton = new javax.swing.JButton();
        fastForwardButton = new javax.swing.JButton();
        videoProgressBar = new javax.swing.JProgressBar();
        saveLogButton = new javax.swing.JButton();
        videoLogSplitPane = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        LogTable = new javax.swing.JTable();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        openConfigMenuItem = new javax.swing.JMenuItem();
        openVideoMenuItem = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        QuestionsDialog.setTitle("Summary information");
        QuestionsDialog.setAlwaysOnTop(true);
        QuestionsDialog.setModal(true);
        QuestionsDialog.setName("questionsDialog"); // NOI18N
        QuestionsDialog.getContentPane().setLayout(new java.awt.GridLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Behave Video Scoring");
        setPreferredSize(new java.awt.Dimension(1024, 768));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        tableVideoSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(452, 100));

        KeyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Key", "Action", "Type"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        KeyTable.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(KeyTable);
        if (KeyTable.getColumnModel().getColumnCount() > 0) {
            KeyTable.getColumnModel().getColumn(0).setHeaderValue("Time");
            KeyTable.getColumnModel().getColumn(1).setHeaderValue("Action");
            KeyTable.getColumnModel().getColumn(2).setHeaderValue("Type");
        }

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        playSpeedLabel.setText("Play Speed:");
        playSpeedLabel.setEnabled(false);
        jPanel3.add(playSpeedLabel);

        playSpeedSlider.setMaximum(10);
        playSpeedSlider.setToolTipText("");
        playSpeedSlider.setValue(5);
        playSpeedSlider.setEnabled(false);
        playSpeedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                playSpeedSliderStateChanged(evt);
            }
        });
        jPanel3.add(playSpeedSlider);

        rewindButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rewind.png"))); // NOI18N
        rewindButton.setEnabled(false);
        rewindButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rewindButtonActionPerformed(evt);
            }
        });
        jPanel3.add(rewindButton);

        playPauseButton.setIcon(playIcon);
        playPauseButton.setEnabled(false);
        playPauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playPauseButtonActionPerformed(evt);
            }
        });
        jPanel3.add(playPauseButton);

        fastForwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ff.png"))); // NOI18N
        fastForwardButton.setEnabled(false);
        fastForwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastForwardButtonActionPerformed(evt);
            }
        });
        jPanel3.add(fastForwardButton);

        videoProgressBar.setMaximum(1000);
        videoProgressBar.setEnabled(false);
        videoProgressBar.setString("00:00/00:00 [x1]");
        videoProgressBar.setStringPainted(true);
        videoProgressBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                videoProgressBarMouseClicked(evt);
            }
        });
        jPanel3.add(videoProgressBar);

        saveLogButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/save.png"))); // NOI18N
        saveLogButton.setEnabled(false);
        saveLogButton.setFocusable(false);
        saveLogButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveLogButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveLogButtonActionPerformed(evt);
            }
        });
        jPanel3.add(saveLogButton);

        jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        tableVideoSplitPane.setLeftComponent(jPanel1);

        videoLogSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        videoLogSplitPane.setResizeWeight(1.0);
        videoLogSplitPane.setToolTipText("");

        jPanel2.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setPreferredSize(new java.awt.Dimension(452, 100));

        LogTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Time", "Action", "State"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        LogTable.setFillsViewportHeight(true);
        jScrollPane2.setViewportView(LogTable);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        videoLogSplitPane.setBottomComponent(jPanel2);

        tableVideoSplitPane.setRightComponent(videoLogSplitPane);

        getContentPane().add(tableVideoSplitPane, java.awt.BorderLayout.CENTER);

        jMenu3.setText("File");

        openConfigMenuItem.setText("Open Configuration");
        openConfigMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openConfigMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(openConfigMenuItem);

        openVideoMenuItem.setText("Open Video");
        openVideoMenuItem.setEnabled(false);
        openVideoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openVideoMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(openVideoMenuItem);

        jMenuItem2.setText("Exit");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        jMenuBar2.add(jMenu3);

        setJMenuBar(jMenuBar2);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private VideoLogger logger;
    private final File baseDir;
    private final DefaultTableModel logTableModel;
    private final ImageIcon playIcon = new ImageIcon(getClass().getResource("/play.png"));
    private final ImageIcon pauseIcon = new ImageIcon(getClass().getResource("/pause.png"));
    
    private void openVideoFile(File f)
    {
        player.startMedia(f.getAbsolutePath());
        player.pause();
        videoProgressBar.setValue(0);
        //initialize the logger
        logger = new VideoLogger(config, player, f, QuestionsDialog, baseDir);
        logTableModel.setNumRows(0);
        setTitle(f.getAbsolutePath());
        ActionMap actions = getRootPane().getActionMap();
        actions.clear();
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        for (KeyStroke key : inputMap.keys()) {
            final Object actionObj = inputMap.get(key);
            if (actionObj instanceof VideoLogger.ActionEntry) {
                actions.put(actionObj, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        VideoLogger.ActionEntry entry = (VideoLogger.ActionEntry) actionObj;
                        long time = logger.addAction(entry);
                        Object[] row = {time, entry.action, entry.isState};
                        logTableModel.addRow(row);
                        LogTable.changeSelection(logTableModel.getRowCount() - 1, 0, false, false);
                    }
                });
            }
        }
    }
    private void openVideoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openVideoMenuItemActionPerformed
        int result = videoFileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        openVideoFile(videoFileChooser.getSelectedFile());
    }//GEN-LAST:event_openVideoMenuItemActionPerformed

    private void rewind() {
        player.skip(-1000);
    }

    private void rewindButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rewindButtonActionPerformed
        rewind();
    }//GEN-LAST:event_rewindButtonActionPerformed

    private void fastForward() {
        player.skip(1000); //skip 1 second
    }
    private void fastForwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastForwardButtonActionPerformed
        fastForward();
    }//GEN-LAST:event_fastForwardButtonActionPerformed

    private void playPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    private void playPauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playPauseButtonActionPerformed
        playPause();
    }//GEN-LAST:event_playPauseButtonActionPerformed

    private void playSpeedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_playSpeedSliderStateChanged
        player.setRate((float) Math.pow(2, playSpeedSlider.getValue() - 5));
    }//GEN-LAST:event_playSpeedSliderStateChanged

    private static void addKeyBinds(DefaultTableModel table, Section config, boolean isState, InputMap inputMap) {
        //a section can be null, which means no bindings are defined for it
        if (config == null) {
            return;
        }
        for (String key : config.keySet()) {
            Vector curRow = new Vector();
            curRow.add(key);
            String action = config.get(key);
            inputMap.put(KeyStroke.getKeyStroke(key.charAt(0)), new VideoLogger.ActionEntry(isState, action));
            curRow.add(config.get(key));
            curRow.add(isState ? "State" : "Event");
            table.addRow(curRow);
        }
    }
    
    private Ini config = null;
    private void openConfigFile(File f)
    {
        try {
            config = new Ini(f);
            DefaultTableModel table = (DefaultTableModel) KeyTable.getModel();
            table.setRowCount(0); //reset the model
            InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.clear();
            addKeyBinds(table, config.get("states"), true, inputMap);
            addKeyBinds(table, config.get("events"), false, inputMap);
            String videoPath = config.get("config", "videos");
            if (videoPath != null) {
                videoFileChooser.setCurrentDirectory(new File(videoPath));
            }
            openVideoMenuItem.setEnabled(true);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, ioe, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openConfigMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openConfigMenuItemActionPerformed
        int result = configFileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        openConfigFile(configFileChooser.getSelectedFile());
    }//GEN-LAST:event_openConfigMenuItemActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void saveLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveLogButtonActionPerformed
        boolean wasPlaying = player.isPlaying();
        if (wasPlaying) {
            player.pause();
        }
        if ( logger.writeLog(true) ) logTableModel.setNumRows(0);
        if (wasPlaying) {
            player.play();
        }
    }//GEN-LAST:event_saveLogButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if ( logger != null && !logger.isLogSaved()
                && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Save log before exit?","Save Log",JOptionPane.YES_NO_OPTION) )
        {
                logger.writeLog(false);
        }
        if (playerComp != null) {
            playerComp.release(true);
        }
    }//GEN-LAST:event_formWindowClosing

    private void videoProgressBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_videoProgressBarMouseClicked
        float newValue = (float)evt.getX() / (float)videoProgressBar.getWidth();
        player.setPosition(newValue);
    }//GEN-LAST:event_videoProgressBarMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //before we load the main window try to load the VLC library
        try {
            Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        } catch ( Throwable t ) {
            JOptionPane.showMessageDialog(null, 
                    "Could not load the VLC library\n"
                    + "Error Message:\n"
                    + t.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainWindow(args).setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable KeyTable;
    private javax.swing.JTable LogTable;
    private javax.swing.JDialog QuestionsDialog;
    private javax.swing.JFileChooser configFileChooser;
    private javax.swing.JButton fastForwardButton;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuItem openConfigMenuItem;
    private javax.swing.JMenuItem openVideoMenuItem;
    private javax.swing.JButton playPauseButton;
    private javax.swing.JLabel playSpeedLabel;
    private javax.swing.JSlider playSpeedSlider;
    private javax.swing.JButton rewindButton;
    private javax.swing.JButton saveLogButton;
    private javax.swing.JSplitPane tableVideoSplitPane;
    private javax.swing.JFileChooser videoFileChooser;
    private javax.swing.JSplitPane videoLogSplitPane;
    private javax.swing.JProgressBar videoProgressBar;
    // End of variables declaration//GEN-END:variables

    private final EmbeddedMediaPlayerComponent playerComp;
    private final MediaPlayer player;
}
