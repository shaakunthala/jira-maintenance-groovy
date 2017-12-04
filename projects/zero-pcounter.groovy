/*
    Author:       Sameera Shaakunthala
    Description:  If you empty a project by bulk deletion of issues, new issues
                  will continue issue key numbers from the last issue's number
                  even if it was deleted. There is a workaround for this by
                  setting the 'pcounter' in the database and then restarting 
                  JIRA. If you don't want to restart JIRA this is an easy way.
*/

import com.atlassian.jira.component.ComponentAccessor

def projectKey = "<PROJECT_KEY_HERE>"
def projectManager = ComponentAccessor.projectManager
def projectObj = projectManager.getProjectObjByKey (projectKey)

projectManager.setCurrentCounterForProject (projectObj, 0)
projectManager.getCurrentCounterForProject (projectObj.id)

// If result is zero, then script was successful
