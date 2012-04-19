simple-user-repository
======================

A simple web application that allows the management of user information in our OpenID IdP

Build & Implementation Notes
============================

* You will need to link in OpenID-Connect-Java-Spring-Server manually if you are not a committer using the https:// read 
only link rather than using the automatic git submodule handling. See github for the latest link. You can still use the
automatic git submodule update --init behavior inside of OpenID-Connect.. to initialize the spring module inside of the 
common module.

* Make sure to define an https proxy on the maven command line or the build will not work. The build must have the https 
proxy defined (-Dhttps.proxyHost= -Dhttps.proxyPort=) in order to make it through your firewall. You can define the regular
http proxy in your maven settings file.


