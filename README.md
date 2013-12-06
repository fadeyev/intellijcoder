intellijcoder
=============

IntelliJ IDEA plugin for TopCoder

Installation
-------------------------
* Automatically: just like any other IntelliJ IDEA plugin:
`File -> Settings -> Plugins -> Available -> IntelliJCoder -> Download and install.`
* Manually: download the latest jar file: https://code.google.com/p/intellijcoder/downloads/list and place it in the plugins directory:
`<user.home>/.IntelliJIdeaXX/config/plugins/`

Usage
-------------------------
* Create new or open existing project in IntelliJ IDEA
* Press TopCoder button ![TopCoder icon](https://raw.github.com/fadeyev/intellijcoder/master/intellijcoder-idea/src/intellijcoder/idea/topcoder-icon.gif) on the main toolbar to launch Arena applet.
* Login to Arena, then select a contest and open a problem.
* IntelliJCoder will automatically create a new module with a solution stub and a test class and run tests.
* After you finished editing your code, use Compile and Sumbit buttons in Arena to submit your code to TopCoder servers.

Screenshot
-------------------------
![TopCoder icon](http://intellijcoder.googlecode.com/files/screenshot.png)

How to build (for contributors)
-------------------------
* Check out project

    ```
    git clone https://github.com/fadeyev/intellijcoder.git
    ```
* Repository contains `.idea` project files, so just open the project in IDEA:
`File -> Open`
* Configure the project:
    * Open project settings: `File -> Project Structure (Ctrl+Alt+Shift+S)`
    * Set up IntelliJ IDEA Plugin SDK:
    `Project -> Project SDK -> New... ->IntelliJ Platform Plugin SDK -> <Pick your IDEA directory> -> OK`
    * Set this SDK as SDK for modules:
    `Modules -> intellijcoder -> Dependencies -> Module SDK -> <Pick your SDK here>`
    `Modules -> intellijcoder-idea -> Dependencies -> Module SDK -> <Pick your SDK here>`
    * You also need to set up IDEA Junit plugin library location, since IntelliJCoder depends on it:
    `Libraries -> JUnit IDEA Plugin -> <If idea-junit.jar location is incorrect, please set up the correct one>`
    * Check that there are no errors at the bottom of the dialog. Press OK to close Project Settings.
* To be able to debug the plugin you may want to check out fresh IntelliJ IDEA sources: http://www.jetbrains.org/pages/viewpage.action?pageId=983225

    ```
    git clone https://github.com/JetBrains/intellij-community.git
    ```
    and then attach them to IntelliJ IDEA Plugin SDK:
`File -> Project Structure -> SDKs -> IDEA IC-*** -> Sourcepath -> Add -> <Point to the checked out folder>`
* To build a jar file, right-click on **intellijcoder-idea** module, select the option "Prepare Plugin Module intellijcoder-idea for Deployment"
