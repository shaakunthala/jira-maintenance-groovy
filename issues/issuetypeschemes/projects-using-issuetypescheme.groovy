/*
  Author:       Sameera Shaakunthala
  Description:  This script is to be executed on the script runner's console.
                This is helpful when you want to get a list of JIRA projects,
                project administrators, and issue counts for a given issue type scheme.
                
                Useful when you want to contact project leads/ administrators when you
                want to switch the issue type scheme to a different one.
*/

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.bc.issue.search.SearchService

def issueTypeSchemeName = "<ISSUE TYPE SCHEME NAME HERE>"

def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def roleMgr = ComponentAccessor.getComponent(ProjectRoleManager.class)
def searchSvc = ComponentAccessor.getComponent(SearchService.class)
def adminRole = roleMgr.getProjectRole("Administrators")
def admins = null

def scheme = ComponentAccessor.getIssueTypeSchemeManager().getAllSchemes().find { it.name == issueTypeSchemeName }
def rows = scheme.getAssociatedProjectObjects().collect {
    def lead = it.getProjectLead()
    admins = roleMgr.getProjectRoleActors(adminRole, it).getRoleActorsByType("atlassian-user-role-actor").collect { 
        def u = it.getUsers().first()
        u.getDisplayName() + " &lt;" + u.getEmailAddress() + "&gt;"
    }.sort().join (", ")
    
    def parseResult = searchSvc.parseQuery (user, "project = \"" + it.key + "\"")
    def defectCount = null
    if ( parseResult.isValid() ) {
        defectCount = searchSvc.searchCount (user, parseResult.getQuery())
    } else {
        log.warn ("Unable to get the issue count for project " + it.key)
        defectCount = "?"
    }
    
    "<tr><td>" + ( [it.key, it.name, lead.getDisplayName(), lead.getEmailAddress(), admins, defectCount] ).join( "</td><td>" ) + "</td></tr>"
}.sort()
def h = "<tr><th>" + ( ["Key", "Name", "Lead", "Lead Contact", "Admins", "Issue Count"].join ( "</th><th>" ) ) + "</th></tr>"
def html = "<table border=\"1\" cellspacing=\"1\" cellpadding=\"1\">" + h + rows.join ("") + "</table>"

