# The Step-by-Step Shader Logic

To build ripple shader, the shader needs to perform four calculations for every single pixel on the 
screen:

### Step 1: Find the Center
The Android app must pass the touch screen coordinates $(x, y)$ into the shader as a uniform (`u_touch`).

### Step 2: Calculate Distance
Every pixel needs to know how far away it is from that touch point. We use the geometric distance formula (or the `distance()` function in GLSL):

$$\text{distance} = \sqrt{(pixel.x - touch.x)^2 + (pixel.y - touch.y)^2}$$

### Step 3: Animate the Wave Front
If you just used distance, you'd get a static circle. To make it expand outward, we subtract the elapsed time (`u_time`) from the distance. This shifts the coordinates backward over time, pushing the wave crests outward.

### Step 4: The Sine Wave Modulation
We feed that moving value into a `sin()` function. To make it look like a realistic ripple rather than an infinite set of waves covering the whole screen, we use an amplitude decay function so the wave naturally dies out as it moves further away from the center.
