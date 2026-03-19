import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MusicPlayerGui extends JFrame implements ActionListener {

  // Image centered
  BufferedImage image = null;

  // Colors
  public static final Color BG_Color = Color.black;
  public static final Color FG_Color = Color.white;

  // Song
  JLabel songImage;
  JLabel songTitle;
  JSlider playbackSlider;

  // Toolbar
  JToolBar toolbar;
  JMenuBar menuBar;
  JMenu songMenu;
  JMenuItem loadMusic;
  JMenuItem loadPlaylist;

  // Button
  JPanel playbackBtns;
  JButton prevButton;
  JButton playButton;
  JButton pauseButton;
  JButton nextButton;

  // Music stuff
  Stack<Clip> clips = new Stack<>();
  int next;
  int prev;
  long frames;
  int current = 0;
  float frameRate;
  AudioFormat format;
  boolean play = false;
  double durationInSeconds;
  long seconds;
  boolean playlist;
  ArrayList<String> names = new ArrayList<>();

  public MusicPlayerGui() {
    super("Music Player");
    setSize(400, 600);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);
    setLayout(null);

    getContentPane().setBackground(BG_Color);
    addUiComponents();
  }

  private void addUiComponents() {
    addToolbar();
    addPlaybackBtns();

    songImage = new JLabel(loadImage("./res/record.png"));
    songImage.setBounds(0, 50, getWidth() - 20, 225);
    add(songImage);

    songTitle = new JLabel("Song title");
    songTitle.setBounds(0, 285, getWidth() - 10, 30);
    songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
    songTitle.setForeground(FG_Color);
    songTitle.setHorizontalAlignment(SwingConstants.CENTER);
    add(songTitle);

    playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
    playbackSlider.setBounds(getWidth() / 2 - 300 / 2, 365, 300, 40);
    playbackSlider.setBackground(null);
    add(playbackSlider);
  }

  private void addToolbar() {
    toolbar = new JToolBar();
    toolbar.setBounds(0, 0, getWidth(), 30);
    toolbar.setFloatable(false);
    add(toolbar);

    menuBar = new JMenuBar();
    toolbar.add(menuBar);

    songMenu = new JMenu("Song");
    menuBar.add(songMenu);

    loadMusic = new JMenuItem("Load music from directory");
    loadMusic.addActionListener(this);
    songMenu.add(loadMusic);

    loadPlaylist = new JMenuItem("Load playlist from directory");
    loadPlaylist.addActionListener(this);
    songMenu.add(loadPlaylist);
  }

  private void addPlaybackBtns() {
    playbackBtns = new JPanel();
    playbackBtns.setBounds(0, 435, getWidth() - 10, 80);
    playbackBtns.setBackground(null);

    prevButton = new JButton(loadImage("./res/previous.png"));
    prevButton.setBorderPainted(false);
    prevButton.setBackground(null);
    prevButton.addActionListener(this);
    playbackBtns.add(prevButton);

    playButton = new JButton(loadImage("./res/play.png"));
    playButton.setBorderPainted(false);
    playButton.setBackground(null);
    playButton.addActionListener(this);
    playbackBtns.add(playButton);

    pauseButton = new JButton(loadImage("./res/pause.png"));
    pauseButton.setBorderPainted(false);
    pauseButton.setBackground(null);
    pauseButton.setVisible(false);
    pauseButton.addActionListener(this);
    playbackBtns.add(pauseButton);

    nextButton = new JButton(loadImage("./res/next.png"));
    nextButton.setBorderPainted(false);
    nextButton.setBackground(null);
    nextButton.addActionListener(this);
    playbackBtns.add(nextButton);

    add(playbackBtns);
  }

  private ImageIcon loadImage(String imagePath) {
    try {
      BufferedImage image = ImageIO.read(new File(imagePath));
      return new ImageIcon(image);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == loadMusic) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileFilter(new FileNameExtensionFilter("Music Files", "wav"));
      int response = fileChooser.showOpenDialog(null);
      if (response == JFileChooser.APPROVE_OPTION) {
        File file = new File(fileChooser.getSelectedFile().getAbsolutePath());

        try (AudioInputStream aS = AudioSystem.getAudioInputStream(file)) {
          if (!clips.isEmpty() && clips.get(current).isActive()) {
            clips.get(current).stop();
          }

          Clip clip = AudioSystem.getClip();

          System.out.println(file.getName());
          names.add(file.getName().substring(0, file.getName().indexOf(".")));
          songTitle.setText(names.get(0));

          clip.open(aS);
          clips.push(clip);
          current = clips.size() - 1;
          clips.peek().start();
          enablePauseButton();

          updatePlayBackSlider(clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
          ex.printStackTrace();
        }
      }
    }

    if (e.getSource() == loadPlaylist) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      int response = fileChooser.showOpenDialog(null);
      if (response == JFileChooser.APPROVE_OPTION) {
        File files = new File(fileChooser.getSelectedFile().getAbsolutePath());

        for (File file : files.listFiles()) {

          try (AudioInputStream aS = AudioSystem.getAudioInputStream(file)) {
            if (!clips.isEmpty() && clips.get(current).isActive()) {

              clips.get(current).stop();
            }

            Clip clip = AudioSystem.getClip();
            System.out.println(file.getName());
            names.add(file.getName().substring(0, file.getName().indexOf(".")));
            songTitle.setText(names.get(0));

            playlist = true;
            clip.open(aS);
            clips.push(clip);
            current = clips.size() - 1;
            clips.peek().start();
            enablePauseButton();

            updatePlayBackSlider(clip);
          } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
          }
        }
      }
    }

    if (e.getSource() == playButton) {
      enablePauseButton();
      clips.get(current).start();
    }

    if (e.getSource() == pauseButton) {
      enablePlayButton();
      clips.get(current).stop();
    }

    if (e.getSource() == prevButton) {
      if (!clips.isEmpty() && current - 1 >= 0) {
        if (clips.get(current).isActive()) {
          clips.get(current).stop();
          clips.get(current).setMicrosecondPosition(0);
          clips.get(current - 1).start();
          songTitle.setText(names.get(current - 1));
        }
        current = current - 1;
      }
    }

    if (e.getSource() == nextButton) {
      if (!clips.isEmpty() && current + 1 < clips.size()) {
        if (clips.get(current).isActive()) {
          clips.get(current).stop();
          clips.get(current).setMicrosecondPosition(0);
          clips.get(current + 1).start();
          songTitle.setText(names.get(current + 1));
        }
        current = current + 1;
      }
    }
  }

  void enablePauseButton() {
    JButton playBtn = (JButton) playbackBtns.getComponent(1);
    JButton pauseBtn = (JButton) playbackBtns.getComponent(2);

    playBtn.setVisible(false);
    playBtn.setEnabled(false);

    pauseBtn.setVisible(true);
    pauseBtn.setEnabled(true);
    play = true;
  }

  void enablePlayButton() {
    JButton playBtn = (JButton) playbackBtns.getComponent(1);
    JButton pauseBtn = (JButton) playbackBtns.getComponent(2);

    playBtn.setVisible(true);
    playBtn.setEnabled(true);

    pauseBtn.setVisible(false);
    pauseBtn.setEnabled(false);
    play = false;
  }

  void updatePlayBackSlider(Clip clip) {}
}
