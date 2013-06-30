Ellison
=======

What is Ellison?
----------------
Ellison is a [SonarQube](http://www.sonarqube.org/) plugin that checks your
[JSF (JavaServer Faces) 1.2](http://jcp.org/en/jsr/detail?id=252) pages for potential errors in the [Expression
Language (part of JSP 2.1)](http://jcp.org/aboutJava/communityprocess/final/jsr245/index.html) expressions.

What problems with Expression Language expressions does it check?
-----------------------------------------------------------------
Ellison checks the following things:

  - Syntax errors in EL expressions
  - References to undeclared named beans

    This means that it will raise a SonarQube violation when an expression refers to a bean that cannot be found. The
    type of beans that Ellison can find are the ones declared with:

      - @ManagedBean attributes (with and without explicit naming)
      - faces-config.xml (the JSF configuration file)
      - pseudo-beans from properties files (as declared in faces-config.xml)

How do I install Ellison?
-------------------------
To install Ellison, you must first have installed SonarQube itself. You can obtain a copy of SonarQube from its
[website](http://www.sonarqube.org/).

The versions of SonarQube Ellison has been tested against are:

  - 2.14
  - 3.1.1
  - 3.3.2
  - 3.4.1

It may or may not work with other versions.

Once you have SonarQube running, get a copy of this repository. From within the directory you cloned this repository
to, do the following:
    mvn package

Once this command has completed, copy `ellison-sonar-plugin/ellison-sonar-plugin-1.0-SNAPSHOT.jar` to the following
directory within the SonarQube installation directory: `extensions/plugins`.

Restart SonarQube, and Ellison should be installed. All you need to do now is add the Ellison rules to your quality
profile, and analyze your project with SonarQube!

How do I get support?
---------------------
There is official support channel for Ellison, but you can still get some help through one of the following ways:

  - The SonarQube [user mailing list](http://sonar.15.x6.nabble.com/Sonar-user-f3159782.html) (make sure to subscribe!)
  - The Github [issues for this project](https://github.com/infosupport/SonarJSFPlugin/issues)
