# GUI

Here is a not exhaustive (idk, I'm not sure it will be but I'll try) manual of my program - its GUI.

<p align="center">
    <img src="Screenshot_uptodate.png" />
</p>

### The workspace
This is the zone in white. This zone will contain the actual filter representation, that is, symbols representing other filters and connections between them.

### The tool bar
This is the light gray zone, just above the workspace. There you will find buttons to add some filter to the workspace.

### The menu bar
This is the uppermost zone of the window, with four menus: **File**, **View**, **Run**, and **Add**. There you will find all the utilities in order to manipulate the workspace and to use the represented filter.

## 1- Manipulating filters
| Action | How to |
|--------|--------|
| Add filter | Click the corresponding button in the tool bar, or the corresponding menu item in **Add** menu. Click on the workspace to place it |
| Drag filter | Click the filter, hold the button down when dragging, then release it when placed |
| Rotate filter | Press '`r`' key when dragging the filter |
| Delete filter | Press '`d`' key when dragging the filter |
| Edit filter   | Press '`e`' key when dragging the filter. Note that editing the filter properties depends on the filter's type |
| Connect filters | Click the `Connection` button in the tool bar, or press '`space`' to begin the drawing of a connection | 

## 2- Connecting filters
Of course, to construct the filter, you will need to connect the sub-filters between them. To do so, click the `Connection` button in the tool bar, or simply press '`space`' to begin the drawing of a new connection.

Then, all the inputs/outputs that are ok to be connected will be filled. Note that, which respect to the filters, an input is green and an output is blue. You can select any availible input/output by clicking it.

When the first end of the wire is set, you can either directly connect it to another input/output (but of the other type, as connecting two inputs is impossible), or click in "the void" in the workspace. This will add a "checkpoint" in the wire. You can add as many checkpoints as you want.

The drawing of the connection is finished once you have selected the second end of the wire.

When the wire is drawn, you can still move the checkpoints by dragging them, as for the filters.

To delete a wire, you have press '`d`' key when dragging one of its checkpoints. This means it's impossible to delete a wire that have no checkpoint without deleting an adjacent filter.

## 3- Special filters

- Input/output filters

These filter represents an input or an output of the filter. This can be a *wav* or a *csv* file. (in *csv* files, the k-th value is the value of the k/2 sample, in left channel if k is even, right channel otherwise).

When editing them, you will have to select a file (it should exist if you select an input).

- Parameters declaration

These are not filters, but declaration of some variables. When editing a declaration, you will have to set the parameter's name (e.g. `frequency`) and it's definition (e.g. `440`).

The parameter's definition, as well as all filter's parameters that are numerical values, should be valid. It is able to parse simple combination of sums and products, that is, for example, `3 * 6 + 8` is correct, but not `3 * 6 - 7`, what can be replaced by `3 * 6 + -7`, as we can parse negative values =). We can also include parameters in the definitions: `2 * frequency + 2`, one single definition may contain more than one parameter.

- Convolution filter

As the convolution require a vector as variable, you'll have to specify it when editing (...). To do so, when editing such a filter, you will have to enter all the values, separated by a '`,`'.

Also, as this vector might be quite large, it is also possible to specify a *csv* file that contains this vector. This is done by answering "Yes" when it asks after pressing '`e`' for editing.

[More on convolutions](https://en.wikipedia.org/wiki/Convolution), [[Fr]](https://fr.wikipedia.org/wiki/Produit_de_convolution)

- Composite filter

This filter import another filter that has **one** input and **one** output in the filter. Thus, editing requires you to select another xml file that represents a valid filter.

The limitation to filters with one input and one input is only because I didn't implemented mechaninsm to handle this, I plan to do this for future versions.

## 4- Actions
I.e. all the action you can perform on the workspace.

| Action | Menu | What it does |
|--------|--------|--------------|
| Save   | File   | Save current workspace, save as if not saved before. |
 Save as | File   | Save the current workspace in a new xml file. |
| Open   | File   | Open an existing filter in the workspace. The xml file should be valid and specifiy filters positions. |
| Export standalone filter | File | Export the filter currently represented by the workspace as a standalone filter. I.e., the composite filters in it are included and there is no need to re-read the file describing them. |
| Quit   | File   | Quit the program. Shortcut: `Ctrl`+`Q`. |
| Zoom in | View  | Zooms in in the workspace. |
| Zoom out | View | Zooms out in the workspace. |
| Play result | Run | If the filter in the workspace has only one output, build the filter and play that single output. If it already has been lauched and paused, resume. |
| Pause | Run | Pauses the playing of the computed output. |
| End  | Run | Ends the playing of the computed output. |
| Build filter | Run | Constructs the composite filter represented by the workspace. It will thus detect if something goes wrong. |
| Build output file | Run | Apply the composite filter to its specified inputs and outputs (with input and output filters). |
| Apply to voice | Run | If the filter has one input and one output, get a sample of sound from the microphone and apply the represented filter to that sample. |
| smurf filter | Add | Starts the placing (creates and drag until mouse click) of a smurf filter. |