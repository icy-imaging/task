package fr.icy.shared.task.gui;

import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import fr.icy.shared.task.*;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Represents a UI component for displaying and managing the progress of a {@code Pipeline}.
 * This class extends {@code JPanel} and implements {@code ProgressListener} to allow real-time
 * updates of pipeline progress and status.
 * <p>
 * The {@code PipelineRow} is designed to be used within a container to visually represent
 * the progress of multiple pipelines. It includes a progress bar and a cancel button,
 * and optionally displays task-specific messages.
 */
public class PipelineRow extends JPanel implements ProgressListener {
    /**
     * Represents the fixed height for an individual row in the PipelineRow component.
     * This value is used to ensure consistent vertical sizing of each row within the
     * UI layout, allowing for uniform display and alignment of pipeline rows. The height
     * is specified in pixels.
     */
    public static final int ITEM_HEIGHT = 62;

    /**
     * Represents the associated pipeline for this row component.
     * The pipeline tracks the progress of a sequence of tasks and provides
     * updates on its current state, such as completed, in progress, or failed.
     * The `pipeline` is integral to managing and visually representing the
     * progress updates within this component.
     * <p>
     * This variable is final, ensuring that the pipeline reference cannot
     * be reassigned after initialization, maintaining consistency throughout
     * the lifecycle of the `PipelineRow` instance.
     */
    private final Pipeline pipeline;
    /**
     * A flag indicating whether task-specific messages should be displayed for the pipeline.
     * <p>
     * This variable determines if the component associated with the pipeline should render
     * additional information about the tasks being executed, such as status or progress messages.
     * When set to {@code true}, task-related messages are shown; when {@code false}, they are hidden.
     * <p>
     * It is initialized during the construction of the {@code PipelineRow} and impacts
     * the behavior of progress updates and the visual representation in the UI.
     */
    private final boolean showTaskMessages;

    /**
     * A progress bar component used to visually represent the progress of a pipeline task.
     * <p>
     * This progress bar is updated dynamically based on the progress events of the associated
     * pipeline. It serves as a key visual indicator for tracking the pipeline's completion,
     * failure, or current progress status.
     * <p>
     * The component's appearance (e.g., color, value) reflects the state of the pipeline, such as
     * successful completion (dark green) or failure (dark red). The updates to this component are
     * managed within the context of the PipelineRow class.
     * <p>
     * This variable is final, ensuring that the progress bar instance remains immutable during
     * the lifecycle of the PipelineRow.
     */
    private final JProgressBar progressBar;

    /**
     * Constructs a new PipelineRow component that visually represents the progress of a given pipeline.
     * This constructor initializes the row with a default configuration and sets up the layout,
     * progress bar, and cancel functionality.
     *
     * @param pipeline         The pipeline whose progress this row represents. Must not be null.
     * @param parentWidth      The width of the parent container, used to calculate the row's size.
     * @param showTaskMessages A boolean indicating whether task-specific messages should be displayed.
     */
    public PipelineRow(final @NonNull Pipeline pipeline, final int parentWidth, final boolean showTaskMessages) {
        super(new BorderLayout(10, 0));
        this.pipeline = pipeline;
        this.showTaskMessages = showTaskMessages;

        setDoubleBuffered(true);

        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(224, 224, 224), 1),
                new EmptyBorder(7, 10, 7, 10)
        ));

        // Enforce fixed height so every row is the same
        setPreferredSize(new Dimension(parentWidth - 12, ITEM_HEIGHT));
        setMinimumSize(new Dimension(0, ITEM_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, ITEM_HEIGHT));

        final JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        final JLabel nameLabel = new JLabel(pipeline.getName());
        nameLabel.setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.BOLD, 12));

        final int progressValue = pipeline.getTotalProgress();
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(progressValue);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, 10));
        paintProgressBar();

        left.add(nameLabel);
        left.add(progressBar);

        final JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, 11));
        cancelBtn.setPreferredSize(new Dimension(72, 32));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                e.consume();
                new Thread(() -> TaskExecutor.getInstance().stop(pipeline)).start();
            }
        });

        add(left, BorderLayout.CENTER);
        add(cancelBtn, BorderLayout.EAST);

        for (final Task task : pipeline.getTasks())
            task.addProgressListener(this);
    }

    /**
     * Constructs a new PipelineRow component that visually represents the progress of a given pipeline.
     * This constructor initializes the row without task-specific messages and sets up the layout,
     * progress bar, and cancel functionality.
     *
     * @param pipeline    The pipeline whose progress this row represents. Must not be null.
     * @param parentWidth The width of the parent container, used to calculate the row's size.
     */
    public PipelineRow(final @NonNull Pipeline pipeline, final int parentWidth) {
        this(pipeline, parentWidth, false);
    }

    /**
     * Handles progress updates for the pipeline by updating the progress bar and
     * optionally displaying task-specific messages.
     *
     * @param event The progress event containing details such as the updated progress
     *              value, task status, and an optional message to be displayed.
     */
    @Override
    public void onProgress(final @NonNull ProgressEvent event) {
        progressBar.setValue(pipeline.getTotalProgress());
        if (showTaskMessages)
            progressBar.setString(event.getMessage());
        paintProgressBar();
    }

    /**
     * Updates the visual appearance of the progress bar based on the current state of the pipeline.
     * <p>
     * If the pipeline has completed all tasks, the progress bar is set to a dark green color
     * to indicate success. If any task in the pipeline has failed, the progress bar is set
     * to a dark red color to indicate failure.
     * <p>
     * This method is invoked as part of the progress listener updates or during initialization
     * to reflect the initial state of the pipeline on the progress bar.
     */
    private void paintProgressBar() {
        if (pipeline.isCompleted())
            progressBar.setForeground(Color.GREEN.darker());
        else if (pipeline.isFailed())
            progressBar.setForeground(Color.RED.darker());
    }
}
