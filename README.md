intellijcoder
=============

IntelliJ IDEA plugin for TopCoder

How to build
-------------------------
* Check out project from svn
* Repository contains .idea project files, so just open checked out project in IDEA: File -> Open
* Configure IntelliJ IDEA project:
    * Open project settings: File -> Project Structure (Ctrl+Alt+Shift+S)

    * Set up IntelliJ IDEA Plugin SDK in project settings

    ```
    Project -> Project SDK -> New... ->IntelliJ Platform Plugin SDK -> <Pick your IDEA directory> -> OK
    ```
    * Set this SDK as SDK for modules:

    ```
    Modules -> intellijcoder -> Dependencies -> Module SDK -> <Pick your SDK here>
    Modules -> intellijcoder-idea -> Dependencies -> Module SDK -> <Pick your SDK here>
    ```
    * Check that there are no errors at the bottom of the dialog. Press OK to close the Project Settings.

* You may need to manually add some libs (like junit) to IntelliJ IDEA Plugin SDK from IntelliJ IDEA folders. They are not added automatically for some reason.
* Check out fresh IntelliJ IDEA sources: http://www.jetbrains.org/pages/viewpage.action?pageId=983225 and attach them to IntelliJ IDEA Plugin SDK to be able to view and debug IDEA code.
* Right-click on intellijcoder-idea module, select option "Prepare Plugin Module intellijcoder-idea for Deployment"