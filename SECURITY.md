Akka Security
====================
This document contains description and links related to 
setting up secure transports in Akka.

Intro
====================
Akka provides security mechanisms out of the box. The ones
we use in this project are:

* Secure cookie for authentication
* SSL for transport encryption

Below we describe how we set this up.

Generate SSL certificate
====================
Use 'keytool' to generate secure keystore, provide password to protect it:

    keytool -genkeypair -alias my_akka -keyalg RSA -validity 1500 -keystore keystore

Check that you can view certificate:

    keytool -list -v -keystore keystore

Export the certificate:

    keytool -export -alias my_akka -keystore keystore -rfc -file my_akka.cer

Import it into the truststore:

    keytool -import -alias my_akka_cert -file my_akka.cer -keystore truststore

Check that you can view it:

    keytool -list -v -keystore truststore

Use keystore and its password on the server side, and truststore with its password
on the client side. File path to these files should be properly configured - see
application.conf and References for more info.

References
====================
Here are some links with more details:

* [Step by step instructions](http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html#CreateKeystore)
* [Reference guide](http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html)
* [Manual](http://docs.oracle.com/javase/7/docs/technotes/tools/solaris/keytool.html)
* [Akka remoting](http://doc.akka.io/docs/akka/snapshot/scala/remoting.html#remote-configuration-scala)
* [More Akka remoting](http://doc.akka.io/docs/akka/2.1.0-RC2/scala/remoting.html#SSL)
* [Simple example config](http://letitcrash.com/post/36064300296/2-1-spotlight-ssl-support)

Testing
====================
Fireup Wireshark and capture network trafic. See if you can grab that secure cookie with
and without SSL encryption.

Akka application config
====================
Related section of application.conf:

    # Cookie
    remote.netty.secure-cookie = "0B0B0B0207040507080E0C070E0A04050E05060D"
    remote.netty.require-cookie = on

    # Secure transport
    remote.enabled-transports = ["akka.remote.netty.ssl"]
    remote.netty.ssl = {
        enable = on
        key-store = "pathto/keystore"
        key-store-password = "???"
        trust-store = "pathto/truststore"
        trust-store-password = "???"
        protocol = "TLSv1"
        random-number-generator = "AES128CounterSecureRNG"
        enabled-algorithms = ["TLS_RSA_WITH_AES_128_CBC_SHA"]
    }

Obviously various settings like encryption algorithm, rand number generator, and protocol
can be configured.

