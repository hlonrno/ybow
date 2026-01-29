# Ybow

Documentation of the language doesn't exist.

## Plans

Not many.

---
### commands

'ybc' to compile. \
'ybe' to execute. \
'yb' as a multiplexer between the two. \
Will change to only have the yb command (before v1.0.0) and there will be no multiplexing.

## About the structure

ybc/* (ybow-compile) a functional-enough template will be written in Java (will be rewritten in Ybow). \
ybe/* (ybow-execute) will be written in C. \
yb/* (the multiplexer) will be written in (idk yet, but probs) C.

## A bit of history
...(before I've forgotten it)

`Ybow` comes from S`y`m`bow`l (unintentionally misspelled), because the original
idea for this language was "The only syntax is symbowls and everything is an
expression". \
Here's an exmaple of a function\* that takes a number
and returns 1 if it's prime, 0 otherwise. `//` is a comment.

```
// "@1" is "typeof(1)" (aka 32b int)
is_prime = n: @1 -> @1   // *variable assigned to a function expression
    ...                  // loop
        ? n % i == 0     // if (n % i == 0)
            #0;          // break* returning 0
                         // *return expr "break 0", so loop will break with 0
            ;            // else nothing
        ? i < sqrt(n)    // ...if (i < sqrt(n))
            i++;         // increment i
            #1;          // else break* returning 1
        ;                // loop end (kinda return)
    ;                    // return (-s the result of the loop)

// a "better" formatted version
is_prime = n: @1 -> @1
    ...
        ? n % i == 0
            #0;;        // notice the semicolons
        ? i < sqrt(n)
            i++;
            #1;
        ;;              // also here
```
