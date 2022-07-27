package eu.iamgio.animated;

import animatefx.animation.AnimationFX;
import animatefx.animation.FadeOut;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AnimatedThemeSwitcher} provides animated transitions that are played when changing the stylesheets of a {@link Scene}.
 * @author Giorgio Garofalo
 */
public class AnimatedThemeSwitcher {

    private final Scene scene;
    private final SimpleObjectProperty<Animation> animation = new SimpleObjectProperty<>(new Animation(new FadeOut()));

    // Whether the changes to the stylesheets should be handled
    private boolean handleChanges = true;

    // TODO implement pause/resume methods

    private AnimatedThemeSwitcher(Scene scene) {
        this.scene = scene;

        // Set-up stylesheets listener
        scene.getStylesheets().addListener((ListChangeListener<? super String>) change -> {
            while(change.next() && handleChanges) {

                // TODO keep added/removed indexes

                final ObservableList<String> stylesheets = scene.getStylesheets();

                // Copy changes (to avoid ConcurrentModificationException)
                final List<? extends String> added = new ArrayList<>(change.getAddedSubList());
                final List<? extends String> removed = new ArrayList<>(change.getRemoved());

                System.out.println("Added " + added);
                System.out.println("Removed " + removed);
                System.out.println();

                handleChanges = false; // Puts the listener on hold

                // Revert changes
                stylesheets.removeAll(added);
                stylesheets.addAll(removed);

                // Screenshot the scene with the old theme applied and play the out animation
                overlapSnapshot();

                // Reapply changes
                stylesheets.addAll(added);
                stylesheets.removeAll(removed);

                handleChanges = true; // Resumes the listener
            }
        });
    }

    /**
     * Gets an {@link AnimatedThemeSwitcher} that wraps a {@link Scene} and registers a listener to its stylesheets.
     *
     * @param scene JavaFX scene to affect
     * @return new {@link AnimatedThemeSwitcher} for the given {@link Scene}
     * @throws IllegalStateException if the root of the scene is not suitable for the transition (e.g. VBox and HBox)
     */
    public static AnimatedThemeSwitcher of(Scene scene) {
        Parent root = scene.getRoot();

        if(!(root instanceof Pane)) {
            throw new IllegalStateException("The root node is not a Pane (or subclass).");
        }
        if(root instanceof VBox || root instanceof HBox) {
            throw new IllegalStateException("The root node cannot be a VBox or HBox.");
        }

        return new AnimatedThemeSwitcher(scene);
    }

    /**
     * @return the animation played during the transition
     */
    public SimpleObjectProperty<Animation> animationProperty() {
        return animation;
    }

    /**
     * @return the exit animation played during the transition, defaults to {@link FadeOut}
     */
    public Animation getAnimation() {
        return animation.get();
    }

    /**
     * @param animationOut exit animation to be played during the transition.
     * @throws IllegalArgumentException if the animation is not an exit animation.
     */
    public void setAnimation(Animation animationOut) {
        if(!animationOut.getAnimationFX().toString().contains("Out")) {
            throw new IllegalArgumentException(
                    "AnimatedThemeSwitcher should feature an exit animation, such as FadeOut.");
        }
        this.animation.set(animationOut);
    }

    /**
     * @param animationOut raw exit animation to be played during the transition.
     * @throws IllegalArgumentException if the animation is not an exit animation.
     */
    public void setAnimation(AnimationFX animationOut) {
        setAnimation(new Animation(animationOut));
    }

    /**
     *  and plays the exit animation.
     */
    private void overlapSnapshot() {
        // Takes a screenshot
        Image snapshot = scene.snapshot(null);
        Pane root = (Pane) scene.getRoot();

        // Adds the image on top of the root
        ImageView imageView = new ImageView(snapshot);
        root.getChildren().add(imageView);

        // Plays the exit animation and removes the image after the transition ends.
        getAnimation().playOut(imageView, root.getChildren());
    }
}
