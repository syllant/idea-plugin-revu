# Introduction

## What is it ?

reVu is a plugin for Intellij IDEA which helps to perform team code reviews or simply annotate your code. Intellij IDEA is great for automatic code inspections, but not at the point to make team code reviews useless... 

There's much literature about peer code review process, I'll just mention main advantages according to me: 

*   each team member knows better application code 
*   code becomes more homogeneous 
*   code is usually better not only because reviewers find potential issues or improvements, but also because authors pay more attention when they know their code will be reviewed:-) 

Some tools already exist, but they are either commercial (see [Crucible](http://www.atlassian.com/software/crucible) from Atlassian) or are not tied to IDEA (GitHub or GoogleCode provide their own review tool, Eclipse and Netbeans have their code review plugin, ...) whereas IDE integration is a key feature.

![Overview](https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/general.png)

## Key features

*   simple to setup 
    *   no server required. Review definitions are stored in XML files which you can share on your Version Control System.
*   language-agnostic
    *   you are able to review all kinds of **text** files
*   flexible, you are able to customize:
    *   users
    *   priorities
    *   your own tags used to classify tags freely
*   keep original source line
    *   if reviewed code is changed or deleted, reVu will keep issue and will mark it with a desynchronized status
*   VCS (Version Control System) integration
    *   you are able to create a review from a specific VCS revision, or for specific changesets
    *   issues are attached to VCS version (if code is under VCS of course)
    *   you can see line changes from the original revision, with committer names
*   templates management
    *   review definitions may be built from review templates wich you manage according to your needs
*   private / shared reviews
    *   each user may create its own reviews and decide to share them to team

## Installation

Plugin is available from Plugin Manager manager. No further step is required. First time you'll use it, reVu will ask you for some nickname. 

reVu plugin adds following components to IDE: 

*   a new menu in main menu 
*   a new tool window 
*   a new project view pane (like *Scopes* or *Favorites*) 
*   gutter icons and gutter menu inside editor 
*   a new global settings pane 
*   a new project settings pane 

## Review process

*   A ***review*** may be created by anyone. Creator becomes ***administrator*** for this review. 
*   The administrator configures the review: name, goal, priorities, tags, users, files selection, ... 
*   When adding users to review, the administrator configures their roles for *this* review: ***administrator***, ***reviewer*** or ***author***. 
*   Reviewers inspect code and add issues to reviews 
*   Authors fix code and resolve issues 
*   Reviewers may check resolved issues and close them 
*   The administrator may deactivate or remove review at any time 

### Creating review

You create reviews from 3 locations: 

*   from reVu project settings 
*   from reVu main menu 
*   from *Changes* view, in *Repository* tab 

First two options allow to create a blank review. Third option allows to create a review by selected the VCS revision from which you want to start the review: only files changed from this revision will be reviewed (note that this is only a shortcut: you can always change this VCS revision in review settings). 

![Create Review from Changes](https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/repositoryChanges.png)

A review is identified by its unique name. This name is used to create XML file which stores review definition. 

When you create a new review, you have to choose if this review will be **shared** with others or will only be visible by you. A shared review is referenced from project settings file, whereas a local review is referenced from workspace settings file. But you will be able to change this setting using **Share** checkbox at any time. 

NB: when you marked a review as shared, you have to make its **file** visible by others. Usually, you will store this file under project directory and commit it into VCS. 

A review definition is composed of: 

*   a goal 
*   users 
*   priorities 
*   classification tags 
*   a selection of files to review 

Users, priorities and tags are defined in a ***referential***. You'll probably use similar referentials between your reviews. In this case, you should create a **review template** with your custom data and copy or extend it for your you reviews. Extend a template means that changes performed upon template will be synchronized for reviews extending it, whereas copy duplicates review and makes it independant from template.

![Settings](https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/reviewSettings.png)

**Status** combo defines current status of review. The review administrator is in charge of changing this status according to current phase of your review: 

*   ***Draft***: review is being prepared 
*   ***Reviewing***: code is being inspected by reviewers 
*   ***Fixing***: issues are being fixed by authors 
*   ***Closed***: review is finished 
*   ***\[Template]***: special status for reviews only used as template

A review may apply to all files inside project, or only for some selected file. File selection is defined in *Files scope* tab of review. It allows to select files from their paths (using IDEA native *Scopes* definitions), or from their VCS revision. 

![Files scopes](https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/reviewFileScope.png)

### Perform review

Issues may be added as soon as the review is in *reviewing* mode. To pass a review in *reviewing* mode, just change its status in settings or click on `Start Reviewing -> \[Some Review]` menu. You may activate several reviews at the same time. By clicking on `Start Reviewing -> \[Some Review]` menu, you make the selected review the default one.

A new type of project views is available: for each active review (status = *reviewing* or *fixing*), you will be able to use a dedicated project view showing files matching selection defined in review settings. You can filter this view to show only files with issues. 

![Project view](https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/projectView.png)

When you have started a review (after clicking on `Start Reviewing -> \[Some Review]` menu), 2 new menu items are enabled in editor gutter:

*   *Annotate for \[Some Review]*
*   *Compare for \[Some Review]*

![Gutter menu](https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/gutterMenu.png)

*Annotate for \[Some Review]* adds VCS annotations inside gutter so you can view for each line the last commit information: committer, date and VCS revision. It is the same behaviour than native Annotate action of IDEA except that only relevant lines are shown. For example, if your review targets all changes done after a specific commit, only lines modified from this commit will be annotated. This make reading of annotation easier.

![Gutter annotations](https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/gutterAnnotations.png)

*Compare for \[Some Review]* launches the diff viewer between the current revision and the original revision which the review is based on. For example, if you have selected *revision 3* as the first revision to start the review (*Files scope > VCS revisions > From revision* in review settings), comparison will be done against this *revision 3* (note that if the file did not exist for selected revision, you'll see an error in message view). When no VCS revision has been set in *Files scope*, comparison is done against last revision.

#### Adding issues

To add issues to the review, just point on code in text editor and invoke `Add Issue` action (*Alt I* shortcut, or reVu menu, or gutter popup menu). Issue is attached to selected code: if code is changed, issue is marked as desynchronized but is still available (and you'll still be able to preview initial code at any time).

![Adding issue](https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/addIssue.png)

Write a summary for this item. Optionally, you may add information such as priority, a longer description or tags. 

**Tags** allow you to categorize issues according your needs. Usually, you'll use tags to set type of issue: coding standard, optimization, i18n, doc, ... But you can use them for additional classification: related business layer, resolution complexity, ... 

#### Managing existing issues

The browsing tool window allows to navigate and update existing issues. There is one tab for each *active* review. 

Left pane show issues for related review. Right pane shows all informations about selected issue. Depending on your role, you are able to update them and change issue status: *resolved*, *closed*, *reopened*. 

Note that you can also configure issue ***assignees***, i.e. author(s) which should fix issues, adding free notes and preview reviewed code. When no assignee is specified, it means all authors defined for review are concerned. 

<table border="0">
  <tr>
    <td><img alt="Issue assignees" src="https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/issueAssignees.png"/></td>
    <td><img alt="Issue notes" src="https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/issueNotes.png"/></td>
    <td><img alt="Issue preview" src="https://github.com/syllant/idea-plugin-revu/raw/master/src/main/doc/screenshots/issuePreview.png"/></td>
  </tr>
</table>

#### Issue navigation

You can navigate between issues using the **tree view**. Issues are **grouped** by some field which you can change at any time (priority, status, tag, ...). You can also **filter** issues using filtering feature, which displays a third pane on left. 

Tree view is searchable through 2 ways: 

*   using IDEA *speed search*: just type some text on tree, view will scroll to first issue whose summary match your text 
*   using search field above tree. Only issues whose summary matches your search will be visible 

## VCS integration

reVu has additional features when your project has VCS integration (such as Subversion, Git, CVS, ...). 

First, you are able to select files to review according to your VCS history. For example, you might want to review all files changed after some specific commit, or between 2 commits. This is configurable through review settings, in *Files Scope* tab. 

Then, issues are attached to the VCS revision of reviewed file. This allows later to retrieve original code when it has been changed between reviewing phase and fixing phase. The *Preview* tab in issue form shows the current preview and the original preview (when available). 

## Storage

Review definitions are stored in XML files. These files should be saved in your project tree for 2 reasons: 

*   Others should be able to access them for *shared* reviews 
*   Review files should be added to VCS 

If you store shared reviews outside from project, others will have to access them using same file paths (e.g. through a shared directory).

NB: when a review is shared and several users work at the same on this review, its XML file must be merged. Most of time, there won't be any conflict, but sometimes you'll have to perform merge. It's the price to pay for having a such simple mechanism:-) A custom merge resolution mechanism might be added in future releases. 

If you find this issue critical, you should consider using a server based tool such as [Crucible](http://www.atlassian.com/software/crucible) from Atlassian. Crucible is far more complex and must be coupled with Jira to use all features (such as workflow and assignment), but has great features, like most Atlassian products!

## Security

Each review has its own user list (but remember you should create a review template with your default users). User roles are specific to reviews: a user may be defined as reviewer in a review and author in another one. 

Users are identified by a *login* and shown in UI through a *display name*. Users must define their login using reVu *application settings* so they can be identified in reviews. Otherwise, an alert is shown and user cannot use reVu. 

NB: reVu does not manage passwords. Since user information are stored in XML files, each one could alter easily these passwords, even if they are crypted. Or it would required some protection which would degrade usability 

## Future plan

Some features I'd like to implement in future releases: 

*   show lines changes in gutter 
*   set review users from VCS committers 
*   multi-location on issues 
*   custom UI for review file merges 
*   CSV export 
*   create issues from intentions 
*   more detailed history on review or issues 
*   ... 

## Found a bug, want new feature ?

Please use [Issue tracker](https://github.com/syllant/idea-plugin-revu/issues).

## Credits

Icons come from famous FAMFAMFAM Silk icon library: <http://www.famfamfam.com/lab/icons/silk>. 

Thanks to YourKit for providing a free licence for [YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp).

*YourKit is kindly supporting open source projects with its full-featured Java Profiler.*
*YourKit, LLC is the creator of innovative and intelligent tools for profiling Java and .NET applications.*
*Take a look at !YourKit's leading software products: [YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and [YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp).*