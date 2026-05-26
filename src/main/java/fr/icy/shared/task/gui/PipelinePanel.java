package fr.icy.shared.task.gui;

import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import fr.icy.shared.task.Pipeline;
import fr.icy.shared.task.PipelineEvent;
import fr.icy.shared.task.PipelineListener;
import fr.icy.shared.task.TaskExecutor;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Set;

/**
 * A `PipelinePanel` is a custom Swing panel that serves as a container
 * for dynamically displaying active pipelines in a scrollable view. It implements
 * `PipelineListener` to respond to the addition or removal of pipelines.
 * <p>
 * The panel is designed with a fixed width, and its height is constrained
 * by either a specific value or a range of minimum and maximum limits. It includes
 * a custom header and a scrollable list of rows, each representing a pipeline.
 */
public class PipelinePanel extends JPanel implements PipelineListener {
    /**
     * Defines the fixed height, in pixels, of the header section within the PipelinePanel.
     * This value is used to set the vertical dimension of the panel's header,
     * ensuring consistent layout and appearance across various instances of the PipelinePanel.
     */
    public static final int HEADER_HEIGHT = 34;

    /**
     * The spacing in pixels between consecutive items in the PipelinePanel.
     * <p>
     * This constant defines the fixed vertical or horizontal gap used
     * to separate UI components such as pipeline entries. It ensures consistent
     * spacing and alignment within the panel's layout. The value directly impacts
     * the visual structure of dynamically arranged items in the `PipelinePanel`.
     */
    public static final int ITEM_GAP = 4;

    /**
     * Defines the padding value in pixels applied to the sides of the list within the PipelinePanel.
     * This constant ensures uniform spacing between the list content and its container boundaries,
     * contributing to proper layout and visual alignment.
     */
    public static final int LIST_PADDING = 8;

    //private static final int PANEL_WIDTH = 340;
    //private static final int MIN_HEIGHT = 100;
    //private static final int MAX_HEIGHT = 380;

    /**
     * Represents the fixed width of the PipelinePanel in pixels.
     * This value is used to define the horizontal dimension of the panel,
     * ensuring consistent layout and alignment of UI components within the panel.
     * It is immutable and assigned during the initialization of the PipelinePanel.
     */
    private final int panelWidth;
    /**
     * Represents the minimum allowable height of the PipelinePanel in pixels.
     * This value ensures that the panel does not shrink below a specified height,
     * maintaining visibility and usability constraints for its contained components.
     */
    private final int minHeight;
    /**
     * Defines the maximum allowable height for the PipelinePanel in pixels.
     * This value serves as an upper limit for the panel's height, ensuring it does not
     * exceed the specified constraint during dynamic adjustments. It is primarily
     * used in resizing operations and layout calculations where height restrictions
     * are necessary to maintain a consistent user interface.
     */
    private final int maxHeight;

    /**
     * A {@link JPanel} instance that serves as the primary container for displaying
     * the list of active pipelines in the {@link PipelinePanel}. This panel is dynamically populated
     * and updated as pipelines are added or removed. It is typically styled and arranged
     * based on the height constraints and layout logic defined in the enclosing class.
     * <p>
     * The listPanel ensures proper spacing and padding between pipeline items and provides
     * a scrollable view if the number of pipelines exceeds the visible height of the panel.
     * It is managed internally by the {@link PipelinePanel} to reflect real-time changes
     * in the pipeline list.
     */
    private final JPanel listPanel;
    /**
     * A JLabel instance used to display the count of active pipelines within the PipelinePanel.
     * This label is updated dynamically as pipelines are added or removed, providing
     * a visual representation of the current number of active pipelines in the header section.
     * It is positioned on the right side of the header panel and styled using custom fonts
     * and colors for consistency with the overall panel design.
     */
    private final JLabel count;

    /**
     * Constructs a new PipelinePanel with the specified dimensions and initializes its components.
     * The panel serves as a container for dynamically displaying active pipelines in a scrollable view,
     * constrained by the provided height limits.
     *
     * @param width     The fixed width of the panel in pixels.
     * @param minHeight The minimum allowable height of the panel in pixels.
     * @param maxHeight The maximum allowable height of the panel in pixels.
     */
    public PipelinePanel(final int width, final int minHeight, final int maxHeight) {
        super(new BorderLayout());

        this.panelWidth = width;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;

        setDoubleBuffered(true);

        // scrollable list of item rows
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(listPanel.getBackground().brighter());
        listPanel.setBorder(new EmptyBorder(LIST_PADDING / 2, 5, LIST_PADDING / 2, 5));

        count = new JLabel("0 items");

        buildContainer(true);

        final JScrollPane scroll = new JScrollPane(
                listPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        /* ---- outer container ------------------------------------------------
         *  ┌─────────────────────────────────┐
         *  │  Dark header                    │  ← HEADER_H px
         *  ├─────────────────────────────────┤
         *  │  JScrollPane                    │
         *  │    ┌──────────────────────────┐ │
         *  │    │ [Progress] [Button]      │ │  ← ITEM_HEIGHT px each
         *  │    │ [Progress] [Button]      │ │
         *  │    │       …                  │ │
         *  │    └──────────────────────────┘ │
         *  └─────────────────────────────────┘
         */
        setBorder(new LineBorder(new Color(110, 110, 110), 1));
        add(buildHeader(), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        TaskExecutor.getInstance().addPipelineListener(this);
    }

    /**
     * Constructs a new PipelinePanel with the specified dimensions and initializes its components.
     * The panel serves as a container for dynamically displaying active pipelines in a scrollable view.
     * The height of the panel is constrained to the value provided as `height` for both minimum and
     * maximum limits.
     *
     * @param width  The fixed width of the panel in pixels.
     * @param height The height of the panel in pixels, which is used as both the minimum
     *               and maximum allowable height constraint.
     */
    public PipelinePanel(final int width, final int height) {
        this(width, height, height);
    }

    /**
     * Handles the addition of a pipeline event to the PipelinePanel. This method is invoked
     * when a {@link PipelineEvent} is added to the associated {@link PipelineListener}.
     * It ensures the user interface is updated by rebuilding the container view asynchronously
     * on the Event Dispatch Thread.
     *
     * @param event the {@link PipelineEvent} that triggered this method; represents
     *              the pipeline that was added and must not be null.
     */
    @Override
    public void onAdded(final PipelineEvent event) {
        SwingUtilities.invokeLater(() -> buildContainer(false));
    }

    /**
     * Handles the removal of a pipeline event from the PipelinePanel. This method is invoked
     * when a {@link PipelineEvent} is removed via the associated {@link PipelineListener}.
     * It is intended to trigger necessary UI updates, ensuring the container reflects
     * the current state of active pipelines.
     *
     * @param event the {@link PipelineEvent} that triggered this method; represents
     *              the pipeline that was removed and must not be null.
     */
    @Override
    public void onRemoved(final PipelineEvent event) {
        //SwingUtilities.invokeLater(() -> buildContainer(false));
    }

    /**
     * Builds and populates the container view for the active pipelines within the PipelinePanel.
     * Depending on the number of active pipelines, it dynamically creates and arranges UI components.
     * Handles resizing of the container if the specified flag is enabled.
     *
     * @param resize A boolean flag indicating whether the container should be resized
     *               based on the number of active pipelines and defined height constraints.
     */
    private void buildContainer(final boolean resize) {
        final Set<Pipeline> pipelines = TaskExecutor.getInstance().getActivePipelines();
        final int itemCount = pipelines.size();

        count.setText(itemCount + " items");

        listPanel.removeAll();

        if (itemCount == 0) {
            final JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(224, 224, 224), 1),
                    new EmptyBorder(7, 10, 7, 10))
            );
            final JLabel label = new JLabel("No active pipelines");
            label.setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.BOLD, 12));
            panel.add(label, BorderLayout.CENTER);
            listPanel.add(panel);
        }

        else {
            int i = 0;
            for (final Pipeline pipeline : pipelines) {
                listPanel.add(new PipelineRow(pipeline, panelWidth));
                if (i < itemCount - 1)
                    listPanel.add(Box.createRigidArea(new Dimension(0, ITEM_GAP)));
                i++;
            }
        }

        if (resize) {
            // variable height: clamp between MIN_HEIGHT and MAX_HEIGHT
            final int rowsH = itemCount * PipelineRow.ITEM_HEIGHT + Math.max(0, itemCount - 1) * ITEM_GAP + LIST_PADDING;
            final int total = Math.max(minHeight, Math.min(maxHeight, rowsH + HEADER_HEIGHT + 2));
            this.setSize(panelWidth, total);
        }

        this.revalidate();
    }

    /**
     * Builds and returns the header panel for the PipelinePanel.
     * The header includes a title on the left and a count label on the right,
     * both styled with custom fonts and colors.
     *
     * @return A non-null JPanel instance representing the header section of the PipelinePanel.
     */
    private @NonNull JPanel buildHeader() {
        final JPanel header = new JPanel(new BorderLayout());
        //header.setBackground(new Color(55, 58, 60));
        header.setBackground(header.getBackground().darker());
        header.setPreferredSize(new Dimension(panelWidth, HEADER_HEIGHT));
        header.setBorder(new EmptyBorder(0, 10, 0, 10));

        final JLabel title = new JLabel("Active Pipelines");
        //title.setForeground(Color.WHITE);
        title.setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.BOLD, 12));

        //count.setForeground(new Color(180, 180, 180));
        count.setForeground(count.getForeground().darker());
        count.setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, 11));

        header.add(title, BorderLayout.WEST);
        header.add(count, BorderLayout.EAST);
        return header;
    }
}
