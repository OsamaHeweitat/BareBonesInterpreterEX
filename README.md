# BareBonesInterpreterEX
This is an extension to the BareBones language and hence the interpreter.
The main changes from the original are:
* Rewrote from scratch to be more efficient and cleaner.
* Added "operator commands" in in the syntax `[cmd] [var] [val1] [val2]` where `[val1]` and `[val2]` can be variables or values. The commands are:
  * `add`
  * `sub`
  * `times`
  * `div` (results in a floored integer value.)
* Added comments, they must start with "//" and end with ".".
* Added if statements, the syntax is `if [var] is [value]" or "if [var] not [value]`, the end of the if block is signified with `end` (same as while loops.)
* Allow while loops to have the syntax `while [var] is [value]`.
