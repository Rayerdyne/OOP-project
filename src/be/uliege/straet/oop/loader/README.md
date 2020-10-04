# Project
## INFO0062 - Object-Oriented Programming


The main present in the `Loader` class takes 3 arguments: 
 1) The file to load
 2) The name of the input
 3) The name of the output

If an argument is not provided, it will be set to default values, which are respectively *echo.xml*, *Source.wav* and *Filtered.wav*.

An option `-v` will print details about connection made.


Ready to use example: 
 - `example.xml`
 - `convolution.xml`
 - `reverberator.xml`

Convenience copy-paste:
> `java -cp audio.jar:bin be.uliege.straet.oop.loader.Loader src/be/uliege/straet/oop/loader/example.xml`


Example requiring parameter specification:
 - `allpass.xml`
 - `echo.xml`
 - `lowpass.xml`
 - `lowpass2.xml`
 - `allpass1nsted.xml`
 - `allpass2nseted.xml`

Parameters will be set when calling the `main` method, as follows:
`java Loader file.xml input.wav output.wav :parameter=1234`

Notice that providing all previous arguments is needed.

Convenience copy-paste:
> `java -cp audio.jar:bin be.uliege.straet.oop.loader.Loader src/be/uliege/straet/oop/loader/echo.xml Source.wav Filtered.wav :gain=0.6 :delay=10000`



## Rules for writing XML a file parsable into a filter

- ### Minimal file
All files have to begin with :

```
<?xml version="1.0" encoding="UTF-8"?>
```

Or with another valid XML file beginning. 

All file should include the CompositeFilter it will represent, which will lead to:
```
<filter id="filterId" in="1" out="1">
        <output n="nOutputToConnect" ref="filterId.filterOutputNum">

        <!-- ... -->
</filter>
```
Where the id attributes can be any valid string of character, and in and out attributes in filter, respectively the number of inputs and the number of outputs of the filter, have to be parsable integers.

In the `output` statement, the `n` attibute describes the number of the output of the "whole" filter that will be connected to the `filterOutputNum`th output of the filter of id (suprisingly) `filterId`. Notice that the numbers start at zero.

- ### Declare sub-filters
> 1) Basic sub-filter

```
<name id="subFilterId" input.inputNum="inputFilterId.inputFilterOutputNum" parameter="value"/>
```

What is very explicit. It has to be noticed that the `parameter` attribute name will depend on the filter.

For example, if I want a *GainFilter*, wich will be connected to the output of a *DelayFilter* whose id is `delay`, I will write:
```
<gain id="gain" input.0="delay.0" gain="0.4" />
```
Notice: for the particular case of the *ConvolutionFilter*, which needs an array of double, values will have to be separated by commas.

A very nice thing is that parameters can be introduced, i.e., the attribute value of `parameter` could be an expression such as `:gain`, or event a expression with sums and products, such as `:fs * 2 + -10`. A variable for pi is present by default, called `:PI`. For clarity, all parameters' names should start with '`:`'. It's a bit strange but this character is accepted.

So far, the list of all basic filter type availible is:

`gain`, `delay`, `addition`, `convolution`, `differantiator`, `integrator`.

The generators (which do not need any input) are:

`sine_generator`, `square_up_generator`, `square_centered_generator`, `noise_generator`.

I may not think to update this, so that it is possible that these lists are not exhaustive.

> 2) Composite sub-fiter

Composite filters will follow the same pattern as the "whole" filter, i.e.:
```
<composite id="composite" in="1" out="1" input.0="inputfilter.0">
    <output n="0" id="subFilterOfComposite.0">

    <!-- ... -->
</composite>
```

> 3) Sub-filter from another file

This is a very convenient feature =).
```
<filter id="fromOtherFile" src="path_to_source" input.0="idToInput.0" :someParam="0.3">
```
The thing to notice here is that it is possible to transmit value of variable parameters to the sub-filter (as much as we want, theorically). In particular, it is possible to do:
```
<filter id="echo" src="xml/echo.xml" input.0="adder.0" :delay="88200" :gain=":aParameter * 2 + 3">
```

Notice that in case of name clash, the most recent value will be taken into account.

So far, I didn't implement a mechanism to handle recursion, i.e.,  if you write in file `file.xml`:
```
<filter id="file" src="file.xml" input.0="input.0">
```
the program will attempt to add a `file` in a `file` in a `file` endlessly.

- ### Setting parameters
When using the main entry point in *Loader* class, parameters value will be read after files names, so that they will be placed after the XML file name, the source file name and the output file name. They have to be formatted as, for example: `:param=0.98`.

## Misc.

- The *Loader* class provides a static method *load(String filename, HashMap<String, Double> parameters)*, that returns the *CompositeFilter* described in the file.

- The caught exception and printed with their stacktrace.

- Names clash are not handled. The most recent name will be used.

- Some lines become unreadable if too much attributes have to be placed on a single line

- If you read this, it means you spend extra time to look at my work. Thank you a lot.

