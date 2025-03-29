/*
 * ==========================================================================================
 * AnimationViewer.java : Moves shapes around on the screen according to different paths.
 * It is the main drawing area where shapes are added and manipulated.
 * YOUR UPI: removed because github up
 * ==========================================================================================
 */

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.ListDataListener;
import java.lang.reflect.Field;

class AnimationViewer extends JComponent implements Runnable {
	private Thread animationThread = null; // the thread for animation
	private static int DELAY = 120; // the current animation speed
	private ShapeType currentShapeType = Shape.DEFAULT_SHAPETYPE; // the current shape type,
	private PathType currentPathType = Shape.DEFAULT_PATHTYPE; // the current path type
	private Color currentColor = Shape.DEFAULT_COLOR; // the current fill colour of a shape
	private Color currentBorderColor = Shape.DEFAULT_BORDER_COLOR;
	private int currentPanelWidth = Shape.DEFAULT_PANEL_WIDTH, currentPanelHeight = Shape.DEFAULT_PANEL_HEIGHT,currentWidth = Shape.DEFAULT_WIDTH, currentHeight = Shape.DEFAULT_HEIGHT;
	private String currentLabel = Shape.DEFAULT_LABEL;
	
    protected NestedShape root; // define the root as a NestedShape
    protected MyModel model;

    public AnimationViewer() {
        root = new NestedShape(Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT);
        model = new MyModel();
        start();
        addMouseListener(new MyMouseAdapter());
    }

    protected void createInnerShape(int x, int y) {
        Shape innerShape = null;
        switch (currentShapeType) {
            case RECTANGLE:
                innerShape = new RectangleShape(x, y, currentWidth, currentHeight, currentPanelWidth, currentPanelHeight, currentColor, currentBorderColor, currentPathType);
                break;
            case OVAL:
                innerShape = new OvalShape(x, y, currentWidth, currentHeight, currentPanelWidth, currentPanelHeight, currentColor, currentBorderColor, currentPathType);
                break;
            case NESTED:
                innerShape = new NestedShape(x, y, currentWidth, currentHeight, currentPanelWidth, currentPanelHeight, currentColor, currentBorderColor, currentPathType);
                break;
        }
        if (innerShape != null) {
            root.addInnerShape(innerShape);
            model.insertNodeInto(innerShape, root);
        }
    }

    public void setCurrentHeight(int h) {
        currentHeight = h;
        for (Shape innerShape : root.getAllInnerShapes()) {
            innerShape.setHeight(currentHeight);
        }
    }

    public void setCurrentWidth(int w) {
        currentWidth = w;
        for (Shape innerShape : root.getAllInnerShapes()) {
            innerShape.setWidth(currentWidth);
        }
    }

    public void setCurrentColor(Color c) {
        currentColor = c;
        for (Shape innerShape : root.getAllInnerShapes()) {
            innerShape.setColor(currentColor);
        }
    }

    public void setCurrentBorderColor(Color bc) {
        currentBorderColor = bc;
        for (Shape innerShape : root.getAllInnerShapes()) {
            innerShape.setBorderColor(currentBorderColor);
        }
    }

    public void setCurrentLabel(String text) {
        currentLabel = text;
        for (Shape innerShape : root.getAllInnerShapes()) {
            innerShape.setLabel(currentLabel);
        }
    }

    class MyMouseAdapter extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            boolean found = false;
            for (Shape innerShape : root.getAllInnerShapes()) {
                if (innerShape.contains(e.getPoint())) {
                    innerShape.setSelected(!innerShape.isSelected());
                    found = true;
                }
            }
            if (!found) {
                createInnerShape(e.getX(), e.getY());
            }
        }
    }

    public final void paintComponent(Graphics g) {
	    super.paintComponent(g);
		for (Shape innerShape : root.getAllInnerShapes()) {
		    innerShape.move();
			innerShape.draw(g);
			innerShape.drawHandles(g);
			innerShape.drawString(g);
		}
	}

    public void resetMarginSize() {
        currentPanelWidth = getWidth();
        currentPanelHeight = getHeight();
        root.resetPanelSize(currentPanelWidth, currentPanelHeight);
    }

    class MyModel extends AbstractListModel<Shape> implements TreeModel {
        protected ArrayList<TreeModelListener> treeModelListeners = new ArrayList<>();
        private ArrayList<Shape> selectedShapes;
    
        @Override
        public Shape getRoot() {
            return root;
        }
        
        public MyModel() {
            if (root != null){
                selectedShapes = root.getAllInnerShapes();}
        }
        
        public int getSize() {
            return selectedShapes.size();
        }
    
        public Shape getElementAt(int index) {
            return selectedShapes.get(index);
        }
    
        public void reload(NestedShape selected) {
            selectedShapes = selected.getAllInnerShapes();
        }
    
        @Override
        public boolean isLeaf(Object node) {
            return !(node instanceof NestedShape);
        }
    
        public boolean isRoot(Shape selectedNode) {
            return selectedNode == root;
        }
    
        @Override
        public Object getChild(Object parent, int index) {
            if (parent instanceof NestedShape) {
                NestedShape nestedShape = (NestedShape) parent;
                ArrayList<Shape> children = nestedShape.getAllInnerShapes();
                if (index >= 0 && index < children.size()) {
                    return children.get(index);
                }
            }
            return null;
        }
    
        @Override
        public int getChildCount(Object parent) {
            if (parent instanceof NestedShape) {
                NestedShape nestedShape = (NestedShape) parent;
                return nestedShape.getAllInnerShapes().size();
            }
            return 0;
        }
    
        @Override
        public int getIndexOfChild(Object parent, Object child) {
            if (parent instanceof NestedShape && child instanceof Shape) {
                NestedShape nestedShape = (NestedShape) parent;
                Shape innerShape = (Shape) child;
                ArrayList<Shape> children = nestedShape.getAllInnerShapes();
                return children.indexOf(innerShape);
            }
            return -1;
        }
    
        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            // Empty method
        }
    
        @Override
        public void addTreeModelListener(final TreeModelListener tml) {
            treeModelListeners.add(tml);
        }
    
        @Override
        public void removeTreeModelListener(final TreeModelListener tml) {
            treeModelListeners.remove(tml);
        }
    
        public void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
            System.out.printf("Called fireTreeNodesInserted: path=%s, childIndices=%s, children=%s\n",
                    Arrays.toString(path), Arrays.toString(childIndices), Arrays.toString(children));
            TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
            for (TreeModelListener listener : treeModelListeners) {
                listener.treeNodesInserted(event);
            }
        }
    
        public void insertNodeInto(Shape newChild, NestedShape parent) {
            if (parent.getAllInnerShapes().contains(newChild)) {
                Object[] path = getPath(parent);
                int[] childIndices = {parent.getAllInnerShapes().size() - 1};
                Object[] children = {newChild};
                fireTreeNodesInserted(this, path, childIndices, children);
                fireIntervalAdded(this, parent.getAllInnerShapes().size()-1, parent.getAllInnerShapes().size());
            }
        }
    
        private Object[] getPath(Shape node) {
            ArrayList<Object> path = new ArrayList<>();
            while (node != null) {
                path.add(0, node);
                if (node.getParent() instanceof NestedShape) {
                    node = (NestedShape) node.getParent();
                } else {
                    node = null;
                }
            }
            return path.toArray();
        }
        public void addShapeNode(NestedShape selectedNode) {
            Shape newInnerShape;
            if (isRoot(selectedNode)) {
                if (currentShapeType == ShapeType.RECTANGLE) {
                newInnerShape = new RectangleShape(0, 0, currentWidth, currentHeight, currentPanelWidth, currentPanelHeight, currentColor, currentBorderColor, currentPathType);
                } else if (currentShapeType == ShapeType.NESTED) {
                    newInnerShape = new NestedShape(0, 0, currentWidth, currentHeight, currentPanelWidth, currentPanelHeight, currentColor, currentBorderColor, currentPathType);
                } else if (currentShapeType == ShapeType.OVAL) {
                    newInnerShape = new OvalShape(0, 0, currentWidth, currentHeight, currentPanelWidth, currentPanelHeight, currentColor, currentBorderColor, currentPathType);
                } else {
                    throw new IllegalArgumentException("Invalid currentShapeType");
                }
            } else {
                int parentWidth = selectedNode.getWidth();
                int parentHeight = selectedNode.getHeight();
                if (currentShapeType == ShapeType.RECTANGLE) {
                    newInnerShape = new RectangleShape(0, 0, parentWidth / 2, parentHeight / 2, parentWidth, parentHeight, selectedNode.getColor(), selectedNode.getBorderColor(), currentPathType);
                } else if (currentShapeType == ShapeType.NESTED) {
                    newInnerShape = new NestedShape(0, 0, parentWidth / 2, parentHeight / 2, parentWidth, parentHeight, selectedNode.getColor(), selectedNode.getBorderColor(), currentPathType);
                } else if (currentShapeType == ShapeType.OVAL) {
                    newInnerShape = new OvalShape(0, 0, parentWidth / 2, parentHeight / 2, parentWidth, parentHeight, selectedNode.getColor(), selectedNode.getBorderColor(), currentPathType);
                } else {
                    throw new IllegalArgumentException("Invalid currentShapeType");
                }
            }
            selectedNode.addInnerShape(newInnerShape);
            insertNodeInto(newInnerShape, selectedNode);
        }
        
        public void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
            System.out.printf("Called fireTreeNodesRemoved: path=%s, childIndices=%s, children=%s\n",
                    Arrays.toString(path), Arrays.toString(childIndices), Arrays.toString(children));
            TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
            for (TreeModelListener listener : treeModelListeners) {
                listener.treeNodesRemoved(event);
            }
        }
        
        public void removeNodeFromParent(Shape selectedNode) {
            NestedShape parentNode = selectedNode.getParent();
            int index = getIndexOfChild(parentNode, selectedNode);
        
            if (parentNode != null && index != -1) {
                parentNode.removeInnerShape(selectedNode);
                Object[] path = getPath(parentNode);
                int[] childIndices = {index};
                Object[] children = {selectedNode};
                fireTreeNodesRemoved(this, path, childIndices, children);
                fireIntervalRemoved(this, index, index-1);
            }
        }

    }    

	// you don't need to make any changes after this line ______________
	public String getCurrentLabel() {return currentLabel;}
	public int getCurrentHeight() { return currentHeight; }
	public int getCurrentWidth() { return currentWidth; }
	public Color getCurrentColor() { return currentColor; }
	public Color getCurrentBorderColor() { return currentBorderColor; }
	public void setCurrentShapeType(ShapeType value) {currentShapeType = value;}
	public void setCurrentPathType(PathType value) {currentPathType = value;}
	public ShapeType getCurrentShapeType() {return currentShapeType;}
	public PathType getCurrentPathType() {return currentPathType;}
	public void update(Graphics g) {
		paint(g);
	}
	public void start() {
		animationThread = new Thread(this);
		animationThread.start();
	}
	public void stop() {
		if (animationThread != null) {
			animationThread = null;
		}
	}
	public void run() {
		Thread myThread = Thread.currentThread();
		while (animationThread == myThread) {
			repaint();
			pause(DELAY);
		}
	}
	private void pause(int milliseconds) {
		try {
			Thread.sleep((long) milliseconds);
		} catch (InterruptedException ie) {}
	}
}
