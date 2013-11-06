yoloninja - A Nokia 6212 NFC cardreader and maybe more
=========

Developed as part of the exercises of the "(Mobile) Network Services and Applications" lecture at Vienna University of Technology (TU Vienna) by Michael Borkowski and Alexander Falb.

Native requirements
---------

The module responsible for rxtx communication (rxtx-tcp) requires native rxtx libraries to be installed at the target computer. They can either be installed at a system-wide path or passed to the runtime via `-Djava.library.path=/path/to/jni/libraries`.
