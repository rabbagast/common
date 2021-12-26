# Common components #

The common module contains convenicence components and classes
that may be useful in any Java program.


### Setup ###

Capture the cc library to local disk by:

```
$ git clone https://<token>@bitbucket.org/rabbagast/common
```


### Dependencies ###

common has no external dependenies. Keep it like that!


### Building cc ###

common can be built from its root folder by

```
$ make clean
$ make
$ make jar
```

The cc delivery will be the `./lib/common.jar` file.

Building with make require the make module containing the main Makefile.


### Creating Javadoc ###

Javadoc can be created by:

```
$ make javadoc
```

Entry point will be `./docs/index.html`.
