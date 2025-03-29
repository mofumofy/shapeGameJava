/*
 *  ============================================================================================
 *  A1.java : Extends JFrame and contains a panel where shapes move around on the screen.
 *  YOUR UPI: removed because github up
 *  ============================================================================================
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.ArrayList;
import javax.swing.tree.*;

public class A3 extends JFrame {
	private AnimationViewer panel;  // panel for bouncing area
	JButton addNodeButton, removeNodeButton, fillButton, borderButton;
	JComboBox<ShapeType> shapesComboBox;
	JComboBox<PathType> pathComboBox;
	JTree tree;
	JList<Shape> shapesList;
	JTextField widthTextField, heightTextField, labelTextField;
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new A3();
			}
		});
	}
	public A3() {
		super("Bouncing Application");
		JPanel mainPanel = setUpMainPanel();
		add(mainPanel, BorderLayout.CENTER);
		add(setUpToolsPanel(), BorderLayout.NORTH);
		addComponentListener(
			new ComponentAdapter() { // resize the frame and reset all margins for all shapes
				public void componentResized(ComponentEvent componentEvent) {
					panel.resetMarginSize();
			}
		});
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100,600);
		setVisible(true);
	}
	public JPanel setUpMainPanel() {
		JPanel mainPanel = new JPanel();
		panel = new AnimationViewer();
		panel.setPreferredSize(new Dimension(Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT));
		JPanel modelPanel = setUpModelPanel();
		modelPanel.setPreferredSize(new Dimension(Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT));
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, modelPanel, panel);
		mainSplitPane.setResizeWeight(0.5);
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setContinuousLayout(true);
		mainPanel.add(mainSplitPane);
		return mainPanel;
	}
	public JPanel setUpModelPanel() {
        JPanel treePanel = new JPanel(new BorderLayout());
        JPanel listPanel = new JPanel(new BorderLayout());
        treePanel.setPreferredSize(new Dimension(Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT / 2));
        listPanel.setPreferredSize(new Dimension(Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT / 2));
        
        //JTree
        tree = new JTree(panel.model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(new TreeNodeSelectionListener());
        JScrollPane treeScrollPane = new JScrollPane(tree);
        JPanel treeButtonsPanel = new JPanel();
        addNodeButton = new JButton("Add Node");
        addNodeButton.addActionListener(new AddListener());
        removeNodeButton = new JButton("Remove Node");
        removeNodeButton.addActionListener(new RemoveListener());
        treeButtonsPanel.add(addNodeButton);
        treeButtonsPanel.add(removeNodeButton);
        treePanel.add(treeButtonsPanel, BorderLayout.NORTH);
        treePanel.add(treeScrollPane, BorderLayout.CENTER);

        //JList
        shapesList = new JList<Shape>(panel.model);
        JScrollPane listScrollPane = new JScrollPane(shapesList);
        listPanel.add(listScrollPane);

        JPanel modelPanel = new JPanel();
        JSplitPane modelSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePanel, listPanel);
        modelSplitPane.setResizeWeight(0.5);
        modelSplitPane.setOneTouchExpandable(true);
        modelSplitPane.setContinuousLayout(true);
        modelPanel.add(modelSplitPane);
        return modelPanel;
    }

	class TreeNodeSelectionListener implements TreeSelectionListener {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath selectionPath = tree.getSelectionPath();
			if (selectionPath != null) {
				Object selectedNode = selectionPath.getLastPathComponent();
				if (selectedNode instanceof NestedShape) {
					panel.model.reload((NestedShape) selectedNode);
				}
			}
		}
	}
	class AddListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath selectionPath = tree.getSelectionPath();
			if (selectionPath != null) {
				Object selectedComponent = selectionPath.getLastPathComponent();
				if (selectedComponent instanceof NestedShape) {
					NestedShape selectedNode = (NestedShape) selectedComponent;
					panel.model.addShapeNode(selectedNode);
				} else {
					JOptionPane.showMessageDialog(null, "ERROR: Must select a NestedShape node.");
				}
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No node selected.");
			}
		}
	}
	class RemoveListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath selectionPath = tree.getSelectionPath();
			if (selectionPath != null) {
				Object selectedNode = selectionPath.getLastPathComponent();
				if (selectedNode instanceof Shape) {
					Shape shapeNode = (Shape) selectedNode;
					if (shapeNode.getParent() != null) {
						panel.model.removeNodeFromParent(shapeNode);
					} else {
						JOptionPane.showMessageDialog(null, "ERROR: Must not remove the root.");
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No node selected.");
			}
		}
	}
	public JPanel setUpToolsPanel() {
		shapesComboBox = new JComboBox<ShapeType>(new DefaultComboBoxModel<ShapeType>(ShapeType.values()));
		shapesComboBox.addActionListener( new ShapeActionListener()) ;
		pathComboBox = new JComboBox<PathType>(new DefaultComboBoxModel<PathType>(PathType.values()));
		pathComboBox.addActionListener( new PathActionListener());
		labelTextField = new JTextField("0", 5);
		widthTextField = new JTextField("" + Shape.DEFAULT_WIDTH, 5);
		widthTextField.addActionListener( new WidthActionListener());
		heightTextField = new JTextField("" + Shape.DEFAULT_HEIGHT, 5);
		heightTextField.addActionListener( new HeightActionListener());
		fillButton = new JButton("Fill");
		fillButton.setBackground(panel.getCurrentColor());
		fillButton.addActionListener( new FillActionListener());
		borderButton = new JButton("Border");
		borderButton.setBackground(panel.getCurrentBorderColor());
		borderButton.addActionListener( new BorderActionListener());
		JPanel toolsPanel = new JPanel();
		toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.X_AXIS));
		toolsPanel.add(new JLabel(" Shape: ", JLabel.RIGHT));
		toolsPanel.add(shapesComboBox);
		toolsPanel.add(new JLabel(" Path: ", JLabel.RIGHT));
		toolsPanel.add(pathComboBox);
		toolsPanel.add(widthTextField);
		toolsPanel.add(heightTextField);
		toolsPanel.add(fillButton);
		toolsPanel.add(borderButton);
		toolsPanel.add( new JLabel(" Label: ", JLabel.RIGHT));
		toolsPanel.add(labelTextField);
		return toolsPanel;
	}
	class ShapeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			panel.setCurrentShapeType((ShapeType)shapesComboBox.getSelectedItem());
		}
	}
	class PathActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			panel.setCurrentPathType((PathType)pathComboBox.getSelectedItem());
		}
	}
	class FillActionListener implements ActionListener {
		public void actionPerformed( ActionEvent e) {
			Color newColor = JColorChooser.showDialog(panel, "Fill Color", panel.getCurrentColor());
			if ( newColor != null) {
				panel.setCurrentColor(newColor);
				fillButton.setBackground(newColor);
			}
		}
	}
	class BorderActionListener implements ActionListener {
		public void actionPerformed( ActionEvent e) {
			Color newColor = JColorChooser.showDialog(panel, "Border Color", panel.getCurrentColor());
			if ( newColor != null) {
				panel.setCurrentBorderColor(newColor);
				borderButton.setBackground(newColor);
			}
		}
	}
	class LabelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String text = labelTextField.getText();
			if (text.length()>0)
			panel.setCurrentLabel(text);
		}
	}
	class WidthActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int newValue = Integer.parseInt(widthTextField.getText());
				if (newValue > 0) // if the value is valid, then change the current height
					panel.setCurrentWidth(newValue);
				else
					widthTextField.setText(panel.getCurrentWidth()+""); //undo the changes
			} catch (Exception ex) {
				widthTextField.setText(panel.getCurrentWidth()+""); //if the number entered is invalid, reset it
			}
		}
	}
	class HeightActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int newValue = Integer.parseInt(heightTextField.getText());
				if (newValue > 0) // if the value is valid, then change the current height
					panel.setCurrentHeight(newValue);
				else
					heightTextField.setText(panel.getCurrentHeight()+""); //undo the changes
			} catch (Exception ex) {
				heightTextField.setText(panel.getCurrentHeight()+""); //if the number entered is invalid, reset it
			}
		}
	}
}

