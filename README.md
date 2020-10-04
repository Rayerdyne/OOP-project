# Project
## INFO0062 - Object-Oriented Programming


Directory containing required classes for the project and the bonus:

`./src` (Demo.java)

`./src/be/uliege/straet/oop/filters`

Additional stuff: 

`./src/be/uliege/straet/oop/additionalfilters`

`./src/be/uliege/straet/oop/loader`

--------------------
## Project structure

I organized my code in three packages, prefixed by `be.uliege.straet.oop`:
- `filters`
- `additionalfilters`
- `loader`

The first one contains what is related to filters (O-:), the two other are described here after. The example program implementing the echo filter is `./src/ApplyEcho.java`

Also, here is for convenience ready to copy-paste commands to compile each of the package, and the `src` directory content (... ok that one is not that useful):

```
javac -d bin -cp audio.jar src/be/uliege/straet/oop/filters/*.java
javac -d bin -cp audio.jar:bin src/be/uliege/straet/oop/additionalfilters/*.java
javac -d bin -cp audio.jar:bin src/be/uliege/straet/oop/loader/*.java
javac -d bin -cp audio.jar:bin src/*.java
```

As you can see, the first package has to be built first. The `loader` package also requires the `additionalfilters` package to be built.

----------------------
### Additionnal basic blocks
The course *SYST-0002 - Signaux et systèmes* greatly motivated me to implement simple versions of an integrator block (`IntegratorFilter`) a differentiator (surprisingly, `DifferentiatorFilter`) and a `ConvolutionFilter`. The integration is done by the trapeze method, and differentiation by the simplest form (dividing the difference between to consecutive element by the step.)

I also made some sound generators (`SineGenerator`, `SquareUpGenerator`, `SquareCenteredGenerator` and `NoiseGenerator`) that implements the abstract class `Generator`.

----------------------
### File loading feature

I also added a class (`Loader`) that is designed to read a file and build a `CompositeFilter` based on it, then apply it to the input.

As this is of course not part of the project, the only reason why I putted this is *why not ?*. 

Files follows a xml-like convention, and should match a specific pattern. More specific details and examples are availible in sub-folder `./src/be/uliege/straet/oop/loader`.

François Straet
 