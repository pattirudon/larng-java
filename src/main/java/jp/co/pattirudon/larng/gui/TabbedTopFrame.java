package jp.co.pattirudon.larng.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import jp.co.pattirudon.larng.pokemon.Gender;

public class TabbedTopFrame extends JFrame implements ActionListener {

    private final static String[] COLUMNS = { "id", "path", "nature", "gender", "ability", "h", "a", "b", "c", "d", "s",
            "shiny", "group seed", "spawner seed", "fixed seed", "ec", "tid", "pid" };
    private final static int[] COLUMN_WIDTHS = { 80, 130, 100, 100, 60, 50, 50, 50, 50, 50, 50, 100, 130, 130, 130, 90, 90,
            90 };
    JTabbedPane tabPane = new JTabbedPane(1, JTabbedPane.SCROLL_TAB_LAYOUT);
    JSplitPane splitPane = new JSplitPane();
    JScrollPane leftScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JPanel topForm = new JPanel();
    JPanel scrollablePane = new JPanel();
    JTextField textFieldSeed = new JTextField(18);
    JTextField textFieldShinyRolls = new JTextField(10);
    JTextField textFieldPaths = new JTextField(18);
//    JComboBox<Gender.Ratio> textFieldGenderRatio = new JComboBox<>(Gender.Ratio.values());
    JScrollPane rightScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    DefaultTableModel model = new DefaultTableModel(COLUMNS, 0);
    JTable rightTable = new JTable(model);
    private JTextField textFieldNumSpawns;
    private JTextField textFieldMaxDepth;
    private JCheckBox checkBoxShinyOnly;
    private JRadioButton radioButtonShowPath;
    private JRadioButton radioButtonShowMult;
    private JComboBox<Gender.Ratio> comboBoxGenderRatio;

    public TabbedTopFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1280, 800));
        setMinimumSize(new Dimension(500, 400));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(tabPane);
        tabPane.add(splitPane, "Pokemon View");
        splitPane.setLeftComponent(leftScroll);
        {
            leftScroll.setViewportView(topForm);
            leftScroll.setPreferredSize(new Dimension(340, 0));
            leftScroll.setMinimumSize(new Dimension(200, 0));
            topForm.setLayout(new FormLayout(new ColumnSpec[] {
                    ColumnSpec.decode("107px"),
                    FormSpecs.RELATED_GAP_COLSPEC,
                    ColumnSpec.decode("114px:grow"),},
                new RowSpec[] {
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.DEFAULT_ROWSPEC,
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.DEFAULT_ROWSPEC,
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.DEFAULT_ROWSPEC,
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.DEFAULT_ROWSPEC,
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.DEFAULT_ROWSPEC,
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.DEFAULT_ROWSPEC,
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.DEFAULT_ROWSPEC,
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.DEFAULT_ROWSPEC,
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.LINE_GAP_ROWSPEC,
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.BUTTON_ROWSPEC,
                    FormSpecs.RELATED_GAP_ROWSPEC,
                    FormSpecs.DEFAULT_ROWSPEC,}));
            {
                JLabel l = new JLabel("Group seed");
                topForm.add(l, "1, 2, right, center");
                topForm.add(textFieldSeed, "3, 2, left, default");
            }
            {
                JLabel l = new JLabel("Shiny rolls");
                topForm.add(l, "1, 4, right, center");
                topForm.add(textFieldShinyRolls, "3, 4, left, default");
            }
            {
                JLabel l_1 = new JLabel("Gender ratio");
                topForm.add(l_1, "1, 6, right, center");

                comboBoxGenderRatio = new JComboBox<>(Gender.Ratio.values());
                topForm.add(comboBoxGenderRatio, "3, 6, left, default");

                radioButtonShowPath = new JRadioButton("Designate a path");
                radioButtonShowPath.setSelected(true);
                topForm.add(radioButtonShowPath, "1, 8, 3, 1");
                JLabel l = new JLabel("Path");
                topForm.add(l, "1, 10, right, center");
                topForm.add(textFieldPaths, "3, 10, left, default");

                radioButtonShowMult = new JRadioButton("Search paths");
                topForm.add(radioButtonShowMult, "1, 12, 3, 1");
                
                ButtonGroup group = new ButtonGroup();
                group.add(radioButtonShowPath);
                group.add(radioButtonShowMult);
                
                JLabel lblNumSpawns = new JLabel("Number of spawns");
                topForm.add(lblNumSpawns, "1, 14, right, default");

                textFieldNumSpawns = new JTextField();
                topForm.add(textFieldNumSpawns, "3, 14, left, default");
                textFieldNumSpawns.setColumns(10);

                JLabel lblNewLabel_1 = new JLabel("Max depth");
                topForm.add(lblNewLabel_1, "1, 16, right, default");

                textFieldMaxDepth = new JTextField();
                topForm.add(textFieldMaxDepth, "3, 16, left, default");
                textFieldMaxDepth.setColumns(10);

                JButton button = new JButton("Show");
                topForm.add(button, "1, 20, center, center");
                button.addActionListener(this);

                checkBoxShinyOnly = new JCheckBox("shiny only");
                topForm.add(checkBoxShinyOnly, "3, 20");
            }
        }
        for (int i = 0; i < COLUMNS.length; i++) {
            rightTable.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
        }
        rightTable.setRowSelectionAllowed(true);
        rightTable.setColumnSelectionAllowed(false);

        splitPane.setRightComponent(rightScroll);
        rightScroll.setViewportView(rightTable);
        rightScroll.setPreferredSize(new Dimension(0, 0));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GenerateTableLogic logic = new GenerateTableLogic(model);
        String seedStr = textFieldSeed.getText().strip();
        logic.setSeedStr(seedStr);
        String shinyRollsStr = textFieldShinyRolls.getText().strip();
        logic.setShinyRollsStr(shinyRollsStr);
        int genderRatioInd = comboBoxGenderRatio.getSelectedIndex();
        logic.setGenderRatioInd(genderRatioInd);
        boolean showPath = radioButtonShowPath.isSelected();
        logic.setShowPath(showPath);
        String pathsStr = textFieldPaths.getText().strip();
        logic.setPathsStr(pathsStr);
        boolean showMult = radioButtonShowMult.isSelected();
        logic.setShowMult(showMult);
        String numSpawnsStr = textFieldNumSpawns.getText().strip();
        logic.setNumSpawnsStr(numSpawnsStr);
        String maxDepthStr = textFieldMaxDepth.getText().strip();
        logic.setMaxDepthStr(maxDepthStr);
        boolean shinyOnly = checkBoxShinyOnly.isSelected();
        logic.setShinyOnly(shinyOnly);
        SwingUtilities.invokeLater(logic);
    }

}
