simple-user-repository
======================

A simple web application that allows the management of user information in our OpenID IdP

Build & Implementation Notes
============================

* Do not build with Java 7, it will not work properly

* You will need to link in OpenID-Connect-Java-Spring-Server manually if you are not a committer using the https:// read 
only link rather than using the automatic git submodule handling. See github for the latest link. You can still use the
automatic git submodule update --init behavior inside of OpenID-Connect.. to initialize the spring module inside of the 
common module.

* Make sure to define an https proxy on the maven command line or the build will not work. The build must have the https 
proxy defined (-Dhttps.proxyHost= -Dhttps.proxyPort=) in order to make it through your firewall. You can define the regular
http proxy in your maven settings file.

Installation Notes
==================

* Start by setting up your MySQL db with the ddl files in simple-db-repository/db, first with init.ddl, then schema.ddl. Use your favorite mysql utility.

* The build uses maven. You can do an optional "mvn clean" if the project has been built before, or just do a "mvn package" to build 
the entire thing if you trust whatever has been already built or you have done the mvn clean step. You'll generally want to do a "-DskipTests" on the maven 
command line as well. Example:

	mvn package -Dhttps.proxyHost=yourproxy -Dhttps.proxyPort=80 -DskipTests

* The results of the build are found in simple-db-repository/target. The result is simpledb-openid-connect-server.war. The context should
really be redone to yield a more typeable URL, but that's what's there right now. This was should deploy to any compatable J2EE 
application server, but it has only been tested against Tomcat 6. If you try something else YMMV.

* As packaged, the software will automatically create an admin user with the username "admin" and a password of "PassWord". This can be changed 
by editing tomcat/webapps/simpledb-openid-connect-server/WEB-INF/local-config.xml. Change the following lines to contain the desired values and
restart the application. Make sure to remove the admin user before restarting either by deleting in the UI or removing from MySQL directly.

		<property name="defaultAdminUserEmail" value="drand@mitre.org" />
		<property name="defaultAdminUserName" value="admin" />
		<property name="defaultAdminUserPassword" value="xyzzy" />
		
License
=======

Copyright 2012 The MITRE Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.