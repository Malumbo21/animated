# animated

**animated** introduces **implicit animations**, a completely new concept in JavaFX strongly inspired by [Flutter's animations and motion widgets](https://flutter.dev/docs/development/ui/widgets/animation).

### Index
1. [Getting started](#getting-started)
2. [Implicit animations](#implicit-animations)
3. [Animated containers](#animated-containers)
4. [Animated switchers](#animated-switchers)
5. [Animated theme switch](#animated-theme-switch)
6. [Other examples](#other-examples)
7. [FXML](#fxml)
8. [Kotlin extensions](#kotlin-extensions)

## Getting started

Maven:
```xml
<dependency>
    <groupId>eu.iamgio</groupId>
    <artifactId>animated</artifactId>
    <version>1.0.0</version>
</dependency>
```

Gradle:
```gradle
allprojects {
    repositories {
        mavenCentral()
    }
}
dependencies {
    implementation 'eu.iamgio:animated:1.0.0'
}
```

> **Note**: v1.0.0 brought several important changes, including a different project structure,
> that may cause compilation errors when upgrading from 0.x versions.  
> Please check its changelog (TODO URL) to see what changed and quickly learn how to fix those errors.

<br/>

---

<br/>

## Implicit animations

Forget about timelines, explicit animations and other stuff that pollutes your code. This animation system will provide versatility to your code and interface.

![Demo](https://i.imgur.com/TKXA8de.gif)  
**[Code](src/test/java/eu/iamgio/animatedtest/AnimatedTest.java)**

```java
Animated<Double> animated = new Animated<>(child, PropertyWrapper.of(child.opacityProperty()));
root.getChildren().add(animated);

// Later...
child.setOpacity(0.5); // Plays the transition
```  

This approach instantiates an `Animated` node that contains one child and is bound to a property.  
Now that we have set an animated bound, we'll see that `child.setOpacity(someValue)` creates a transition between the initial and final value.

> `PropertyWrapper.of` automatically finds out the best kind of wrapper for a given property.  
> Currently supported wrappers are `DoublePropertyWrapper` and `ObjectPropertyWrapper<T>`.

There are some pre-made animated nodes that take the child as an argument as well (list will expand):
- `AnimatedBlur`
- `AnimatedDropShadow`
- `AnimatedColor` (shapes only)
- `AnimatedOpacity`
- `AnimatedPosition`
- `AnimatedRotation`
- `AnimatedSize`
- `AnimatedScale`
- `AnimatedLayout`

### Multiple animations at once

In case you need to animate more than one property of a single node, `AnimatedMulti` comes to the rescue. At this time it only takes properties as arguments, so it won't be possible to use pre-made nodes (list above).

```java
AnimatedMulti animated = new AnimatedMulti(child,
    PropertyWrapper.of(child.opacityProperty()),
    PropertyWrapper.of(child.prefWidthProperty()),
    PropertyWrapper.of(child.prefHeightProperty())
);
root.getChildren().add(animated);

// Later...
child.setOpacity(0.5);   // Plays the transition
child.setPrefWidth(100); // Plays the transition
child.setPrefHeight(50); // Plays the transition
```  

### Independent animations

`Animated` and `AnimatedMulti` are nodes that have to be added to the scene in order to work.  
Here is a different approach that is independent from the scene:

```java
PropertyWrapper.of(node.opacityProperty()).registerAnimation();

// Later...
node.setOpacity(0.5); // Plays the transition
```

### Custom animations

The default animation is linear and lasts 1 second.
It can be customized by calling `withSettings(AnimationSettings settings)` or `custom(Function<AnimationSettings, AnimationSettings> settings)`,
both methods available on animated nodes, property wrappers and animation properties.

Examples:
```java
AnimatedOpacity animated = new AnimatedOpacity(child)
    .custom(settings -> settings.withDuration(Duration.seconds(.5)).withCurve(Curve.EASE_IN_OUT));
```  

```java
AnimatedMulti animated = new AnimatedMulti(child,
    PropertyWrapper.of(child.opacityProperty())
        .custom(settings -> settings.withDuration(Duration.seconds(.8))),
    PropertyWrapper.of(child.rotateProperty())
        .custom(settings -> settings.withDuration(Duration.seconds(.5)),
).custom(settings -> settings.withCurve(Curve.EASE_OUT)); // 'custom' applies only these settings to the properties.
                                                          // 'withSettings' overrides all instead.
root.getChildren().add(animated);
```
<br/>

---

<br/>

## Animated containers

**animated** provides custom implementations of `VBox` and `HBox` that animate their content whenever their children are affected by a change.  
This feature is based on animations from [AnimateFX](https://github.com/Typhon0/AnimateFX).

![Demo](https://i.imgur.com/jqm9KDA.gif)  
**[Code](src/test/java/eu/iamgio/animatedtest/AnimatedContainerTest.java)**

**Constructors**:
- `Animation in, Animation out` wraps two `AnimateFX` objects into customizable `animated` objects;
- `AnimationFX in, AnimationFX out` takes two raw AnimateFX animations that cannot be customized;
- `AnimationPair animation` takes a pair of animations, mostly used with pre-made pairs (e.g. `AnimationPair.fade()`).

`pause()` and `resume()` allow disabling/enabling animations so that you can switch back to the regular implementation and forth.

Example:
```java
AnimatedVBox vBox = new AnimatedVBox(AnimationPair.fade());

// Later...
vBox.getChildren().add(someNode);    // someNode fades in
vBox.getChildren().remove(someNode); // someNode fades out
```

<br/>

---

<br/>

## Animated switchers

The library also provides an `AnimatedSwitcher` node that creates a transition whenever its child changes.  
As for animated containers, this feature relies on AnimateFX.

![Demo](https://i.imgur.com/8v2Wn0a.gif)  
**[Code](src/test/java/eu/iamgio/animatedtest/AnimatedSwitcherTest.java)**

See [animated containers](#animated-containers) for information about constructors.  
Right after the instantiation, calling `of(Node child)` will set the initial child without any animation played.

Example:
```java
AnimatedSwitcher switcher = new AnimatedSwitcher(
    new Animation(new FadeInDown()).setSpeed(2), 
    new Animation(new FadeOutDown()).setSpeed(1.5)
).of(firstChild);
root.getChildren().add(switcher);

// Later...
switcher.setChild(secondChild); // Plays the transition
```

### Related: animated text

`AnimatedLabel` uses a switcher to animate text.

Example:
```java
AnimatedLabel label = new AnimatedLabel("Text", AnimationPair.fade());

// Later...
label.setText("New text"); // Plays the transition
```

<br/>

---

<br/>

## Animated theme switch

It is possible to create a transition whenever the stylesheets of the scene change via `AnimatedThemeSwitcher`, based on AnimateFX.


![Theme](https://i.imgur.com/0PnOYbc.gif)  
**[Code](src/test/java/eu/iamgio/animatedtest/AnimatedThemeTest.java)**

```java
scene.getStylesheets().setAll("/light.css"); // Initial theme
AnimatedThemeSwitcher themeSwitcher = new AnimatedThemeSwitcher(scene, new FadeOut());
themeSwitcher.init(); // Required!

// Later...
scene.getStylesheets().setAll("/dark.css"); // Plays the transition

// This also works with add, set, remove and other List methods.
```

> **Note** that not every type of root can be animated properly, such as `VBox` and `HBox`.
> Parents that allow overlapping children, i.e. `Pane`, are suggested. 

<br/>

---

<br/>

## Other examples

![Button](https://i.imgur.com/mVGkKcx.gif)  
**[Button color and border](src/test/java/eu/iamgio/animatedtest/AnimatedButtonTest.java)**

![Shadow](https://i.imgur.com/jd8Bbr4.gif)  
**[Drop shadows + label](src/test/java/eu/iamgio/animatedtest/AnimatedShadowTest.java)**

![Root switch](https://i.imgur.com/cYkSu9z.gif)  
**[Root switch](src/test/java/eu/iamgio/animatedtest/AnimatedRootSwitchTest.java)**

![Layout alignment](https://i.imgur.com/xNRltwq.gif)  
**[Layout alignment](src/test/java/eu/iamgio/animatedtest/AnimatedLayoutTest.java)** (inspired by the Edge home page)

<br/>

---

<br/>

## FXML
Extended FXML support is available with the new 1.0.0 version. 

- **Animated**
  ```xml
  <?import eu.iamgio.animated.binding.Animated?>
  <?import eu.iamgio.animated.binding.AnimationSettings?>
  <?import eu.iamgio.animated.binding.presets.AnimatedOpacity?>
  
  <Animated>
        <child>
            <MyNode/>
        </child>
        <targetProperties>
            <!-- Properties to animate, related to the current child -->
            <AnimatedOpacity>
                <settings>
                    <AnimationSettings duration="1500ms" curve="EASE_IN_OUT"/>
                </settings>
            </AnimatedOpacity>
            <!-- Multiple properties are supported -->
        </targetProperties>
    </Animated>
  ```

- **AnimatedContainer**
  ```xml
  <?import eu.iamgio.animated.transition.container.AnimatedHBox?>
  <?import eu.iamgio.animated.transition.Animation?>

  <AnimatedHBox>
      <!-- Optional: defaults to FadeIn -->
      <in>
          <Animation type="ZoomIn"/>
      </in>
      <!-- Optional: defaults to FadeOut -->
      <out>
          <Animation type="ZoomOut"/>
      </out>
      <!-- Optional initial children -->
      <MyNode1/>      
      <MyNode2/>
      <!-- ... -->      
  </AnimatedHBox>
  ```

- **AnimatedSwitcher**  
  ```xml
  <?import eu.iamgio.animated.transition.AnimatedSwitcher?>
  <?import eu.iamgio.animated.transition.Animation?>
  
  <AnimatedSwitcher>
      <!-- Optional: defaults to FadeIn -->
      <in>
          <Animation type="SlideInUp" speed="0.5"/>
      </in>
      <!-- Optional: defaults to FadeOut -->
      <out>
          <Animation type="SlideOutUp"/>
      </out>
      <!-- Optional initial child -->
      <child>
          <InitialChild/>
      </child>
  </AnimatedSwitcher>
  ```
  
- **AnimatedLabel**  
  ```xml
  <?import eu.iamgio.animated.transition.AnimatedLabel?>
  <?import eu.iamgio.animated.transition.Animation?>
  
  <AnimatedLabel text="Initial text">
      <!-- Optional: defaults to FadeIn -->
      <in>
          <Animation type="BounceIn"/>
      </in>
      <!-- Optional: defaults to FadeOut -->
      <in>
          <Animation type="BounceOut"/>
      </in>
  </AnimatedLabel>
  ```

<br/>

---

<br/>

## Kotlin extensions

[Extension functions](src/main/java/eu/iamgio/animated/AnimatedExtensions.kt) make the library less verbose with Kotlin.  
Example:
```kotlin
val animated: Animated = Animated(child, child.someProperty().animated())
val pair: AnimationPair = FadeIn().options(speed = 1.5) outTo FadeOut()
```