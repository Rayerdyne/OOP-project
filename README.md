# Project
## INFO0062 - Object-Oriented Programming

This project is the extention of the programming project I had to make as part of the Object-Oriented Programming course, during the academic year 2019-2020.


Directories containing classes:

`./src` (Demo.java)

`./src/be/uliege/straet/oop/filters` 

`./src/be/uliege/straet/oop/loader`

`./src/be/uliege/straet/oop/gui`

--------------------
## Project structure

I organized my code in three packages, prefixed by `be.uliege.straet.oop`:
- `filters`
- `loader`
- `gui`

`filters` contains what is related to filters (O-:). 

`loader` contains the stuff to read-write a `CompositeFilter` in a xml-like file.

`gui` contains the classes related to the graphical user interface.

Also, here is for convenience ready to copy-paste commands to compile each of the package, and the `src` directory content (... ok that one is not that useful):

```
javac -d bin -cp audio.jar src/be/uliege/straet/oop/filters/*.java
javac -d bin -cp audio.jar:bin src/be/uliege/straet/oop/loader/*.java
javac -d bin -cp audio.jar:bin src/be/uliege/straet/oop/gui/*.java
javac -d bin -cp audio.jar:bin src/*.java
```

As you can see, the first package has to be built first. The `loader` package also requires the `filters` package to be built, and the `gui` package needs both.

----------------------
# `filter` package

This package contains following classes: `AdditionFilter`, `AllPassFilter`,  `CompositeFilter`, `ConvolutionFilter`, `DelayFilter`, `DifferentiatorFilter`, `FeedbackableFilter`, `GainFilter`, `IntegratorFilter`, `ReverberatorFilter`, `WFilter`, `Generator`, `NoiseGenerator`, `SineGenerator`, `SquareCenteredGenerator`, `SquareUpGenerator`, `Block`, `BlockException`, `ReadDouble`, `WriteDouble` 

This set of classes is responsible for the numerical computation of the filtered output. The `WFilter` interface ("Writeable Filter") describes any type of filter, i.e. any class that implements it is a filter. 

All filters are post-fixed by `Filter` (...). Of course, the `CompositeFilter` class here, that will be built on other filters, is particularly interesting...

The `Generator` abstract class defines a specific kind of filter, a filter that takes no inputs but outputs some function of time. Its sub-classes will be post-fixed by `Generator` (...).

----------------------
# `loader` package

This package contains following classes: `LoaderException`, `Loader`, `NodeData`, `ValueMapper`, `WriterException`, `Writer`

This set of class is responsible for the reading and writing of any given `CompositeFilter` to a parsable xml file.  

Static method `Loader.load(String fileName, HashMap<String, Double> parameters, boolean verbose)` is used to load the file at `fileName`, given a eventual parameter set, verbosely or not. It will return the `CompositeFilter` represented in that file.

Static method `Writer.writeFilter(CompositeFilter cf, String fileName)` is used to write the `CompositeFilter` to the file at `fileName`.

----------------------
# `gui` package

This package contains following classes: `AudioSequence2`, `ComputationException`, `Computer`, `Coord`, `DAdditionFilter`, `DCompositeFilter`, `DConvolutionFilter`, `DDelayFilter`, `DGainFilter`, `DInputFilter`, `DOutputFilter`, `DraggableFilter`, `Draggable`, `DVariableDeclaration`, `FixedBall`, `FMenuBar`, `FreeBall`, `Locatable`, `NothingFilter`, `Procedure`, `RetardatorFocusGiver`, `Updater`, `WindowException`, `Window`, `Wire`, `WorkSpace`, `WorkSpaceXML`

This set of class is responsible for the graphical user interface (GUI) of the program, and for the reading and writing of the positions of the filters in files in order to be able to re-open edited filters later.

The coordination is made by the `WorkSpace` class, that extends `JPanel` (and implements `KeyListener`) so that it could be integrated in some other applications (if you want so).

The full description of the GUI and its abilities can be found in [the GUI tutorial](/src/be/uliege/straet/oop/gui/README.md).

--------------------
## "Unrelated" stuff

Eeeeeh, actually, for [another program](https://github.com/Rayerdyne/FG), I were interested in drawing some arbitrary shapes, but I needed to provide all of its edges, what was exhausting to do by hand. So I just re-used what I did here to do this much more conveniently. The class corresponding to this little "hack" all have some explicit name about this...

François Straet
 