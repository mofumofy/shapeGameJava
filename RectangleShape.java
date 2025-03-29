/*
 *	===============================================================================
 *	RectangleShape.java : A shape that is a rectangle.
 *  YOUR UPI: removed because github up
 *	=============================================================================== */
import java.awt.*;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

class RectangleShape extends Shape {
	public RectangleShape() {}
    public RectangleShape(Color c, Color bc, PathType pt) {super(c, bc, pt);}
	public RectangleShape(int x, int y, int w, int h, int mw, int mh, Color c, Color bc, PathType pt) {
		super(x ,y ,w, h ,mw ,mh, c, bc, pt);
	}
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x, y, width, height);
		g.setColor(borderColor);
		g.drawRect(x, y, width, height);
	}
	public boolean contains(Point mousePt) {
		return (x <= mousePt.x && mousePt.x <= (x + width + 1)	&&	y <= mousePt.y && mousePt.y <= (y + height + 1));
	}
}

class NestedShape extends RectangleShape {
    private ArrayList<Shape> innerShapes;

    public NestedShape() {
        super();
        innerShapes = new ArrayList<>(); // Initialize the innerShapes ArrayList
        createInnerShape(0, 0, getWidth() / 2, getHeight() / 2, getColor(), getBorderColor(), PathType.BOUNCING, ShapeType.RECTANGLE);
    }

    public NestedShape(int x, int y, int w, int h, int mw, int mh, Color c, Color bc, PathType pt) {
        super(x, y, w, h, mw, mh, c, bc, pt);
        innerShapes = new ArrayList<>(); // Initialize the innerShapes ArrayList
        createInnerShape(0, 0, getWidth() / 2, getHeight() / 2, getColor(), getBorderColor(), PathType.BOUNCING, ShapeType.RECTANGLE);
    }

    public NestedShape(int width, int height) {
        super(0, 0, width, height, DEFAULT_PANEL_WIDTH, DEFAULT_PANEL_HEIGHT, Color.black, Color.black, PathType.BOUNCING);
        innerShapes = new ArrayList<>(); // Initialize the innerShapes ArrayList
    }

    public Shape createInnerShape(int x, int y, int w, int h, Color c, Color bc, PathType pt, ShapeType st) {
        Shape innerShape;
        if (st == ShapeType.RECTANGLE) {
            innerShape = new RectangleShape(x, y, w, h, getWidth(), getHeight(), c, bc, pt);
        } else if (st == ShapeType.OVAL) {
            innerShape = new OvalShape(x, y, w, h, getWidth(), getHeight(), c, bc, pt);
        } else {
            innerShape = new NestedShape(x, y, w, h, getWidth(), getHeight(), c, bc, pt);
        }
        innerShape.setParent(this);
        innerShapes.add(innerShape);
        return innerShape;
    }

    public Shape createInnerShape(PathType pt, ShapeType st) {
        return createInnerShape(0, 0, getWidth() / 2, getHeight() / 2, getColor(), getBorderColor(), pt, st);
    }

    public Shape getInnerShapeAt(int index) {
        return innerShapes.get(index);
    }

    public int getSize() {
        if (this.innerShapes == null) {
            return 0;
        }
        return innerShapes.size();
    }

    @Override
    public void draw(Graphics g) {
        // Set the boundary color to black
        g.setColor(Color.black);
        g.drawRect(getX(), getY(), getWidth(), getHeight());

        // Translate the coordinate system by (x, y)
        g.translate(getX(), getY());

        // Draw inner shapes
        for (Shape innerShape : innerShapes) {
            innerShape.draw(g);

            // Draw handles if the shape is selected
            if (innerShape.isSelected()) {
                innerShape.drawHandles(g);
            }

            // Draw the label of the inner shape
            innerShape.drawString(g);
        }

        // Reset the coordinate system
        g.translate(-getX(), -getY());
    }

    @Override
    public void move() {
        super.move();

        // Move all inner shapes
        for (Shape innerShape : innerShapes) {
            innerShape.move();
        }
    }
    
    public int indexOf(Shape s) {
        return innerShapes.indexOf(s);
    }
    
    public void addInnerShape(Shape s) {
        s.setParent(this);
        innerShapes.add(s);
    }
    
    public void removeInnerShape(Shape s) {
        s.setParent(null);
        innerShapes.remove(s);
    }
    
    public void removeInnerShapeAt(int index) {
        Shape s = innerShapes.remove(index);
        s.setParent(null);
    }
    
    public ArrayList<Shape> getAllInnerShapes() {
        return innerShapes;
    }
}






