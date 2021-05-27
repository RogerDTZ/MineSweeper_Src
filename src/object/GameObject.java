/**
 * @Author: RogerDTZ
 * @FileName: GameObject.java
 */

package object;

import component.Component;
import datatype.Transform;
import datatype.Vector2;
import graphics.Sprite;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Comparator;

public class GameObject {

    protected String id;

    protected GameObject parentObject;
    protected ArrayList<GameObject> subObjects;

    protected Vector2 objSize;
    protected Sprite sprite;
    protected Transform transform;
    private boolean absoluteTransform;

    protected double alpha;
    protected boolean visible;
    private double renderPriority;

    private ArrayList<Component> components;


    public GameObject(String id, Sprite sprite) {
        this.id = id;

        this.parentObject = null;
        this.subObjects = new ArrayList<>();

        this.sprite = sprite;
        if (this.sprite != null)
            this.objSize = this.sprite.getUnitSize();

        this.transform = new Transform();
        this.absoluteTransform = false;

        this.alpha = 1.0;
        this.visible = true;
        this.renderPriority = 0.0;

        this.components = new ArrayList<>();
    }

    public GameObject(String id) {
        this(id, null);
    }

    public String getID() {
        return this.id;
    }

    // Tree Relationship [Start]

    public GameObject getParentObject() {
        return this.parentObject;
    }

    public ArrayList<GameObject> getSubObjects() {
        return this.subObjects;
    }

    public int getSubObjectsNum() {
        return this.subObjects.size();
    }

    public void setParentObject(GameObject parentObject) {
        this.parentObject = parentObject;
    }

    public boolean addObject(GameObject object) {
        if (object == null || object.getParentObject() != null)
            return false;
        for (GameObject subObject : this.subObjects)
            if (subObject.getID().equals(object.getID()))
                return false;
        this.subObjects.add(object);
        object.setParentObject(this);
        return true;
    }

    public boolean deleteObject(String id) {
        for (GameObject object : this.subObjects)
            if (object.getID().equals(id)) {
                this.subObjects.remove(object);
                return true;
            }
        return false;
    }

    public void detachSubObjects() {
        for (GameObject object : this.subObjects)
            object.setParentObject(null);
        this.subObjects.clear();
    }

    public void detach() {
        if (this.parentObject != null)
            this.parentObject.deleteObject(this.id);
        this.parentObject = null;
    }

    public GameObject findObject(String id) {
        for (GameObject object : this.subObjects)
            if (object.getID().equals(id))
                return object;
        for (GameObject object : this.subObjects) {
            GameObject res = object.findObject(id);
            if (res != null)
                return res;
        }
        return null;
    }

    public boolean removeObject(String id) {
        int index = 0;
        for (GameObject subObject : this.subObjects) {
            if (subObject.getID().equals(id)) {
                subObjects.remove(index);
                return true;
            }
            ++index;
        }
        return false;
    }

    // Tree Relationship [End]

    public AffineTransform getAbsoluteTransform() {
        ArrayList<GameObject> path = new ArrayList<>();
        GameObject u;
        for (u = this; u != null; u = u.getParentObject())
            path.add(u);
        Object[] list = path.toArray();
        AffineTransform res = new AffineTransform();
        for (int i = path.size() - 1; i >= 0; --i)
            res = ((GameObject)list[i]).transform.concatenate(res);
        return res;
    }

    protected void addComponent(Component component) {
        this.components.add(component);
    }

    public void update(double dt) {
        this.components.removeIf(Component::isDestroy);
        for (Component component : this.components)
            component.update(dt);
        for (GameObject gameObject : this.subObjects)
            gameObject.update(dt);
    }

    public AffineTransform render(Graphics2D g2d, AffineTransform parentTransform, double alpha) {
        if (!this.visible)
            return null;
        AffineTransform at;
        if (!this.absoluteTransform)
            at = (AffineTransform) parentTransform.clone();
        else
            at = new AffineTransform();
        at = this.transform.concatenate(at);
        alpha *= this.alpha;

        ArrayList<GameObject> list = new ArrayList<>();
        for (GameObject gameObject : this.subObjects) {
            if (gameObject.renderPriority < 0)
                list.add(gameObject);
        }
        list.sort(Comparator.comparingDouble(o -> o.renderPriority));
        for (GameObject gameObject : list)
            gameObject.render(g2d, at, alpha);

        if (this.sprite != null)
            this.sprite.draw(g2d, at, this.transform.scale, alpha);

        list.clear();
        for (GameObject gameObject : this.subObjects) {
            if (gameObject.renderPriority >= 0)
                list.add(gameObject);
        }
        list.sort(Comparator.comparingDouble(o -> o.renderPriority));
        for (GameObject gameObject : list)
            gameObject.render(g2d, at, alpha);

        return at;
    }

    public AffineTransform render(Graphics2D g2d) {
        this.render(g2d, new AffineTransform(), 1.0);
        return new AffineTransform();
    }

    public void setRenderPriority(double renderPriority) {
        this.renderPriority = renderPriority;
    }

    public void setSprite(Sprite sprite) {
        if (this.sprite != null) {
            this.sprite = sprite;
            if (this.sprite != null)
                this.sprite.resizeTo(this.objSize);
        } else {
            this.sprite = sprite;
            if (this.sprite != null)
                this.objSize = this.sprite.getUnitSize();
        }
    }

    public Sprite getSprite() {
        return this.sprite;
    }

    public void setAlpha(double a) {
        this.alpha = a;
        if (this.sprite != null) {
            this.sprite.setColor(new Color(255, 255, 255, (int)(Math.round(a * 255))));
        }
    }

    public void setVisible(boolean flag) {
        this.visible = flag;
        if (this.sprite != null)
            this.sprite.setVisible(flag);
    }

    public void setColor(Color color) {
        this.sprite.setColor(color);
    }

    public void setAbsoluteTransform(boolean flag) {
        this.absoluteTransform = flag;
    }

    public void resizeTo(Vector2 rect) {
        this.objSize = rect;
        if (this.sprite != null)
            this.sprite.resizeTo(rect);
    }

    public void resizeTo(double x, double y) {
        this.resizeTo(new Vector2(x, y));
    }

    public Vector2 getObjSize() {
        return new Vector2(this.objSize.x, this.objSize.y);
    }

    public Transform getTransform() {
        return this.transform;
    }

    public void setPosition(Vector2 position) {
        this.transform.position = position;
    }

    public void setPosition(double x, double y) {
        this.setPosition(new Vector2(x, y));
    }

    public void move(Vector2 replacement) {
        this.transform.translate(replacement);
    }

    public void move(double x, double y) {
        this.move(new Vector2(x, y));
    }

    public void setRotation(double rotation) {
        this.transform.rotation = rotation;
    }

    public void rotate(double angle) {
        this.transform.rotate(angle);
    }

    public void setScale(Vector2 scale) {
        this.transform.scale = scale;
    }

    public void setScale(double x, double y) {
        this.setScale(new Vector2(x, y));
    }

    public void setScale(double scalar) {
        this.setScale(new Vector2(scalar, scalar));
    }

}
