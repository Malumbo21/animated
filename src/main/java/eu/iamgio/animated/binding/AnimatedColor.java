package eu.iamgio.animated.binding;

import eu.iamgio.animated.binding.property.wrapper.ObjectPropertyWrapper;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;

/**
 * Node that animates its child's color.
 * @author Giorgio Garofalo
 */
public class AnimatedColor extends Animated<Paint> {

    public AnimatedColor(Shape child) {
        super(child, new ObjectPropertyWrapper<>(child.fillProperty()));
    }
}
