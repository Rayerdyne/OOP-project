# Project
## INFO0062 - Object-Oriented Programming

## Rules for constructing XML a file parsable into a filter

Examples can be found in this directory (**./xml**)

- ### Minimal file
All files have to begin with :

```<?xml version="1.0" encoding="UTF-8"?>```

Or with another valid XML file beginning. 

All file should include the CompositeFilter it will represent, which will lead to:
```
<filter id="filterId" in="1" out="1">
        <output n="nOutputToConnect" ref="filterId.filterOutputNum">

        (...)
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

A very nice thing is that variable parameters can be introduced, i.e., the attribute value of `parameter` could be an expression such as `:gain`, or event a expression with sums and products, such as `:fs * 2 + -10`. A variable for pi is present by default, called `:PI`. For clarity, variable parameters' will all begin by "`:`".

For example, if I want a *GainFilter*, wich will be connected to the output of a *DelayFilter* whose id is `delay`, I will write:
```
<gain id="gain" input.0="delay.0" gain="0.4" />
```
Notice: for the particular case of the *ConvolutionFilter*, which nneds an array of double, values will have to be separated by commas.

So far, the list of all basic filter type availible is:

`gain`, `delay`, `addition`, `convolution`, `differantiator`, `integrator`.

The generators (which do not need any input) are:

`sine_generator`, `square_up_generator`, `square_centered_generator`, `noise_generator`.

I may not think to update this, so that it is possible that these lists are not exhaustive.

> 2) Composite sub-fiter

Composite filters will follow the same pattern as the "whole" filter, i.e.:
```
<composite id="composite" in="2" out="1">
    <output n="0" id="subFilterOfComposite.0">

    (...)
</composite>
```

> 3) Sub-filter from another file

This is a very convenient feature =).
```
<filter id="fromOtherFile" src="path_to_source" input.0="idToInput.0" :someParam="0.3">
```
The thing to notice here is that it is possible to transmit value of variable parameters to the sub-filter (as much as we want, theorically). In particular, it is possible to do:
```
<filter id="echo" src="xml/echo.xml" input.0="adder.0" :delay="88200" :gain=":aVariable * 2 + 3">
```
So far, I didn't implement a mechanism to handle recursion. 

- ### Introduce variable parameters
When using the main entry point in *Loader* class, variable parameters value will be read after files names, so that they will be placed after the XML file name, the source file name and the output file name. They have to be formatted as, for example: `:param=0.98`.

If you use the *Loader.load(String filename, HashMap<String, Double> parameters)* method, (ok you see).

