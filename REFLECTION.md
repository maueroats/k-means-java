# Reflection

Here are a few thoughts from after the project was completed.

## Overview

There was a lot I did not know how to do at the start of this project. I used Gemini Fast mode heavily.

* Graphics output from codespaces: short search online. Straightforward.
* Find a java graphics library. My search turned up `LWJGL` and I thought it was an easy-to-use wrapper library. (When I was done with the project, I came to believe this was wrong.)

  Installation was relatively trouble-free, but I had to learn a little bit about `gradle`. Copy and paste from Gemini was enough to get this working.

##  Arrays and short-lived code

I make an array of points and a separate int array to note which group each point was in. I didn't see any need for ArrayLists holding all of the points in each cluster.

This worked very well for the basic functionality that I had planned.

When I started adding code, appending two arrays was desirable and very awkward. I ended up with a bunch of low-level `System.arraycopy` and `Arrays.copyOf` calls, which I think is very low-level compared to just `+` in Python.

I would probably have been better off with ArrayLists in the end.

## Everything Changes

One of the features I added was the ability to add and remove groups. The fact that the number of groups could change introduced *most* of the bugs in the program. I hadn't thought about it at the start, and I charged in to the changes without thinking about them either... and paid for it.

## Results

* I had a hard time stepping away from writing once I began producing parts of the code myself. It would have been better to organize the application so that I could keep working on parts of it with an LLM without needing *everything* to be generated.
* This application was simple enough that I didn't have any tests prepared. Gemini pointed out the errors. I think it would have been better for me to be more careful, at least planning a few tests.
* Gemini never pointed out or corrected the error where I was printing a local variable instead of the instance variable of the same name.
* In the end, my code felt like a mess. Gemini gave good suggestions about how to clean it up. (It lead with the spot-on "God object" criticism.) I should probably have asked the LLM for higher level help planning the project.
* Working with arrays (and ArrayLists) is awkward in Java, and this project really showed that in a painful way.
* I'm definitely not sure I'm using the OpenGL functions correctly. I don't really know how to check.


## Resources

* [LWJGL](https://www.lwjgl.org/)
* [libGDX](https://libgdx.com/)
* Cay Horstmann, [Simple Java Graphics](https://horstmann.com/sjsu/graphics/). This guy has a lot of credibility as a teacher and author. Also includes links to doing the same thing with Swing with JFrame, which his library simplifies away.
