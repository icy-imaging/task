package fr.icy.shared.task.example;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import fr.icy.shared.task.*;
import fr.icy.shared.task.gui.PipelinePanel;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * The {@code PopupPanelExample} class is a graphical user interface (GUI) implementation that
 * demonstrates the use of Swing components to create a popup-driven application. It extends
 * {@link JFrame} and implements the {@link PipelineListener} interface to interact with the pipeline
 * system managed by the {@link TaskExecutor}.
 * <p>
 * This class provides the following key features:
 * <ul>
 *     <li>A progress bar to visualize the state of active pipelines.</li>
 *     <li>A toggleable popup panel to display additional pipeline management options.</li>
 *     <li>Integration with the pipeline system to respond to addition and removal of pipelines.</li>
 *     <li>A toolbar with a button to trigger the creation of a pipeline.</li>
 *     <li>Event-driven behavior using the observer pattern for pipeline lifecycle updates.</li>
 * </ul>
 * <p>
 * The main GUI is structured with a toolbar at the bottom containing the progress bar and
 * "Create Pipeline" button, as well as a central label for general content display. The class
 * also includes functionality to visualize and position popup components relative to an anchor.
 */
public final class PopupPanelExample extends JFrame implements PipelineListener {
    /**
     * Represents a popup panel used for displaying additional UI components or
     * contextual information within the {@code PopupPanelExample} application.
     * This {@link JPanel} serves as the base container for the popup functionality
     * and is dynamically manipulated during runtime to show or hide the popup
     * relative to an anchor component.
     * <p>
     * The popup panel is styled and positioned using various helper methods, and
     * its visibility state is managed by methods like {@code togglePopup} and
     * {@code hidePopup}. It is designed to ensure proper alignment and avoid overlap
     * with other UI components in the application.
     * <p>
     * This field is initialized and configured during the application's UI
     * construction process within the {@code buildFrame} method.
     */
    private JPanel popup;
    /**
     * A progress bar component used to visually indicate progress or status updates
     * within the popup panel example application. This {@link JProgressBar} is typically
     * updated in response to pipeline events, reflecting the current state of active tasks
     * or processes.
     * <p>
     * The progress bar is configured to operate in both determinate and indeterminate modes,
     * adapting its state based on the number of active pipelines:
     * <ul>
     *     <li>Indeterminate mode is used when pipelines are actively running, providing a visual cue that processes are ongoing without specifying exact progress.</li>
     *     <li>Determinate mode is used to display specific progress values when applicable.</li>
     * </ul>
     * This component is managed, updated, and displayed as part of the application's UI,
     * and its behavior is integrated with pipeline event listeners to ensure synchronization
     * with the underlying task execution engine.
     */
    private JProgressBar progressBar;

    /**
     * Initializes a new instance of the PopupPanelExample class, which is a demonstration
     * of a popup panel integrated within a Swing JFrame. This constructor sets up the
     * main frame properties and builds the UI components by invoking the buildFrame method.
     * Furthermore, it registers the instance as a listener for pipeline events through
     * the TaskExecutor's pipeline listener mechanism.
     * <p>
     * Key frame properties initialized include:
     * <ul>
     *     <li>Frame title: "Popup Panel Demo"</li>
     *     <li>Default close operation: EXIT_ON_CLOSE</li>
     *     <li>Frame size: 750x480 pixels</li>
     *     <li>Frame location: Centered on the screen</li>
     * </ul>
     * <p>
     * Additional setup operations:
     * <ul>
     *     <li>The buildFrame method is called to assemble and configure the UI components in the frame.</li>
     *     <li>The current instance is registered as a {@link PipelineListener} to monitor changes in the pipeline handled by the TaskExecutor.</li>
     * </ul>
     */
    public PopupPanelExample() {
        super("Popup Panel Demo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(750, 480);
        setLocationRelativeTo(null);
        buildFrame();
        TaskExecutor.getInstance().addPipelineListener(this);
    }

    /**
     * Responds to the addition of a {@link PipelineEvent} to the system.
     * This method updates the progress bar to reflect the current state
     * of active pipelines. The progress bar will be set to indeterminate
     * if there are active pipelines; otherwise, it will display a value of 0.
     *
     * @param event the {@link PipelineEvent} instance indicating that a pipeline
     *              has been added; must not be null.
     */
    @Override
    public void onAdded(final PipelineEvent event) {
        SwingUtilities.invokeLater(() -> {
            final int n = TaskExecutor.getInstance().getActivePipelines().size();
            progressBar.setValue(0);
            progressBar.setIndeterminate(n > 0);
        });
    }

    /**
     * Handles the removal of a {@link PipelineEvent} from the system. This method updates
     * the progress bar's state based on the current number of active pipelines. The
     * progress bar will be reset to a value of 0 and set to an indeterminate mode
     * if there are still active pipelines remaining.
     *
     * @param event the {@link PipelineEvent} instance indicating that a pipeline
     *              has been removed; must not be null.
     */
    @Override
    public void onRemoved(final PipelineEvent event) {
        SwingUtilities.invokeLater(() -> {
            final int n = TaskExecutor.getInstance().getActivePipelines().size();
            progressBar.setValue(0);
            progressBar.setIndeterminate(n > 0);
        });
    }

    /**
     * Builds and configures the main user interface frame for the application.
     * This method creates and arranges components such as a progress bar, a toolbar,
     * action buttons, and various event listeners for user interaction. The frame
     * includes a bottom toolbar with a button to trigger pipeline creation and a panel
     * to display progress. Additionally, the central area of the frame shows a label
     * that represents the main content of the application.
     * <p>
     * Key functionalities include:
     * <ul>
     *     <li>A progress panel with a progress bar and interactive background color changes on mouse events.</li>
     *     <li>A toolbar configured for user actions, such as triggering a pipeline creation through the "Create Pipeline" button.</li>
     *     <li>Event listeners to handle user interactions, including toggling a popup and hiding the popup when clicking outside it.</li>
     * </ul>
     * <p>
     * The frame is organized using BorderLayout, with the toolbar positioned in the
     * SOUTH region and the main content area in the CENTER.
     */
    private void buildFrame() {
        final JPanel progressPanel = new JPanel(new GridLayout(1, 1));
        progressPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        progressPanel.setOpaque(true);

        progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(false);
        progressPanel.add(progressBar);

        final JButton triggerBtn = new JButton("Create Pipeline");
        triggerBtn.setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.BOLD, 13));
        //triggerBtn.setFocusPainted(false);
        triggerBtn.addActionListener(e -> createPipeline());

        final JToolBar bar = new JToolBar();
        bar.setPreferredSize(new Dimension(0, 20));
        bar.setMaximumSize(bar.getPreferredSize());
        bar.setMinimumSize(bar.getPreferredSize());
        bar.setFloatable(false);
        bar.setBorderPainted(true);
        bar.setBackground(new Color(getBackground().getRed() - 10, getBackground().getGreen() - 10, getBackground().getBlue() - 10));
        //bar.setBorder(new EmptyBorder(4, 4, 4, 8));
        bar.add(Box.createHorizontalGlue());
        bar.add(triggerBtn);
        progressPanel.setBackground(bar.getBackground());
        progressPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        progressPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                e.consume();
                togglePopup(progressPanel);
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
                e.consume();
                final Color c = bar.getBackground();
                progressPanel.setBackground(new Color(c.getRed() - 10, c.getGreen() - 10, c.getBlue() - 10));
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                e.consume();
                progressPanel.setBackground(bar.getBackground());
            }
        });
        progressPanel.setMaximumSize(new Dimension(20, bar.getPreferredSize().height));
        progressBar.setMaximumSize(new Dimension(20, bar.getPreferredSize().height));
        bar.add(progressPanel);
        bar.add(Box.createHorizontalStrut(10));
        add(bar, BorderLayout.SOUTH);

        final JLabel center = new JLabel("Main Content Area", SwingConstants.CENTER);
        center.setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, 16));
        center.setForeground(Color.GRAY);
        add(center, BorderLayout.CENTER);

        // Clicking the frame background closes the popup
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                hidePopup();
            }
        });
    }

    /**
     * Creates and executes a new processing pipeline consisting of a random number of tasks.
     * The pipeline is uniquely identified by a string based on the current system time in nanoseconds.
     * Tasks are assigned randomly generated unique identifiers and are added to the pipeline.
     * <p>
     * The method operates as follows:
     * <ol>
     *     <li>A {@link Pipeline} instance is created with a unique identifier.</li>
     *     <li>A random number of tasks (between 1 and 4) are generated using {@link RandomTimeTask}, and each task is added to the pipeline. The first task is initialized without a UID, while the following tasks use the UID of the prior task.</li>
     *     <li>The pipeline is then executed using the singleton {@link TaskExecutor}.</li>
     *     <li>Execution occurs on a separate thread to ensure that the main application flow remains unaffected.</li>
     *     <li>In the event of a {@link TaskExecutionException}, error messages for any failed subtasks are logged to the standard error output.</li>
     * </ol>
     */
    private void createPipeline() {
        final Pipeline pipeline = new Pipeline(String.valueOf(System.nanoTime()));
        final int r = new Random().nextInt(1, 5);
        long tuid = 0L;
        for (int i = 0; i < r; i++) {
            final Task t;
            if (tuid == 0L)
                t = new RandomTimeTask();
            else
                t = new RandomTimeTask(tuid);
            tuid = t.getUid();
            pipeline.addTask(t);
        }

        final TaskExecutor executor = TaskExecutor.getInstance();

        new Thread(() -> {
            try {
                executor.execute(pipeline);
            }
            catch (final TaskExecutionException e) {
                e.getFailedTasks().forEach(task ->
                        pipeline.getTasks().stream()
                                .filter(st -> st.equals(task))
                                .flatMap(st -> st.getError().stream())
                                .forEach(err -> System.err.println(task.getName() + ": " + err.getMessage())) // TODO replace with logger
                );
            }
        }).start();
    }

    /**
     * Toggles the visibility of a popup panel. If the popup is already visible, it hides
     * the popup. Otherwise, it creates and displays a new popup panel above the specified
     * anchor component.
     *
     * @param anchor the JComponent above which the popup should be positioned; must not be null
     */
    private void togglePopup(final JComponent anchor) {
        if (popup != null && popup.isVisible()) {
            hidePopup();
        }
        else {
            popup = new PipelinePanel(340, 100, 380);
            getLayeredPane().add(popup, JLayeredPane.POPUP_LAYER);
            getLayeredPane().revalidate();
            getLayeredPane().repaint();
            positionAbove(popup, anchor);
            popup.setVisible(true);
        }
    }

    /**
     * Hides the popup panel if it is currently visible. This method checks the visibility
     * state of the popup and ensures that it is hidden by setting its visibility to false.
     * <p>
     * This method assumes that the `popup` field is already initialized and manages
     * the visibility state of a JComponent used as a popup panel.
     */
    private void hidePopup() {
        if (popup != null) popup.setVisible(false);
    }

    /**
     * Positions the specified {@link JWindow} directly below the given anchor component,
     * ensuring alignment based on the anchor's screen location and dimensions.
     *
     * @param win    the {@link JWindow} to be positioned; must not be null
     * @param anchor the {@link JComponent} used as the reference point for positioning;
     *               must not be null
     */
    private void positionBelow(final @NonNull JWindow win, final @NonNull JComponent anchor) {
        final Point p = anchor.getLocationOnScreen();
        final int x = p.x + anchor.getWidth() - win.getWidth();
        final int y = p.y + anchor.getHeight() + 3;
        win.setLocation(x, y);
    }

    /**
     * Positions the specified {@link JPanel} above the given anchor component, ensuring
     * proper alignment based on the anchor's screen location and dimensions. The method
     * computes the position to avoid overlap and ensures that the panel is fully visible
     * within the parent layered pane.
     *
     * @param win    the {@link JPanel} to be positioned; must not be null
     * @param anchor the {@link JComponent} used as the reference point for positioning;
     *               must not be null
     */
    private void positionAbove(final @NonNull JPanel win, final @NonNull JComponent anchor) {
        final Point p = SwingUtilities.convertPoint(anchor, 0, 0, getLayeredPane());

        final int x = Math.max(4, p.x + anchor.getWidth() - win.getWidth());
        final int y = Math.max(0, p.y - win.getHeight() - 5);

        win.setBounds(x, y, win.getWidth(), win.getHeight());
        win.setVisible(true);

        //popupVisible = true;
    }

    /**
     * The entry point of the application. This method initializes the application's
     * look and feel settings and launches the main UI by creating and displaying
     * an instance of the {@code PopupPanelExample} class.
     *
     * @param args an array of command-line arguments passed to the application; can be empty or null
     */
    public static void main(final String[] args) {
        FlatJetBrainsMonoFont.installLazy();
        FlatLightLaf.setup();
        FlatLaf.setPreferredMonospacedFontFamily(FlatJetBrainsMonoFont.FAMILY);

        SwingUtilities.invokeLater(() -> new PopupPanelExample().setVisible(true));
    }
}
