/**
   Jenkins Pipeline Project 
   (MULTIBRANCH - MULTIPROJECT - SADE DEPLOY - declarative)

   Builds Maven project and, unless in exclusion list, deploys it
   to SADE environment.
   
   NOTES: 
     - This pipeline contains the SCM info/url and ignores the parent's 
       scm object. This allows setting up SCM pooling on specific paths,
       only building a project if it has changes (for multi-project
       Git repo).
       (also see notes in the "Checkout" stage and getGitBranchName)
   
     - A requirement for proper multi-project functionality is that the
       Jenkins project name MUST match the project's folder name in the
       Git repository.
*/

// Git URL
def projectGitURL = 'http://tfs.intra.dmz:8080/tfs/ProjectCollection/EWS-SWE/_git/JavaTemplates'
def projectGitCredsName = 'SV-TFS2015-Build'

def projectReleaseBranchRegex = 'release-.*'  //Builds from these branches are considered "release" builds
def projectGitBranchName = getGitBranchName()
def branchIsRelease = projectGitBranchName.matches(projectReleaseBranchRegex)

def applicationName = getApplicationName(projectReleaseBranchRegex)

// Project's POM file
def projectPom = applicationName + '/pom.xml'

// Git Branch/Paths to match for changes
def projectGitWatchedPathRegex = applicationName.replaceAll("\\.", '') + '/.*'
def projectGitWatchedBranches = [[name: '*/' + projectGitBranchName]]

// SADE deployment info
def deploySADEBuildName = '/ca.gc.sade/Deployments/deploy-springboot'
def deploySADEVerticalMap = [
        //branch name -> SADE vertical map  
        'building-builds': [vertical: 'dev', stream: '3'],
        'building-builds-stream3': [vertical: 'dev', stream: '3'],
        'jenkinsfile-oas': [vertical: 'dev', stream: '3', deployAction: 'deploy'],
    ]
def deploySADEExludedProjects = [
        'pos-sop-core', 'pos-sop-event'
    ]

// Email extension plugin base parameters
def emailextConfig = [
    to: 'pierre.lupien@hrdc-drhc.net,vincent.seguin@hrdc-drhc.net', //comma-separated lits of addresses
    from: 'Jenkins-CI <jenkins-ci@jade-build.intra.dev>',
    body: '${SCRIPT, template="groovy-html.template"}', //for details on body: https://wiki.jenkins.io/display/JENKINS/Email-ext+plugin#Email-extplugin-Scriptcontent
    mimeType: 'text/html'    
    ]


def getApplicationName(currentBranchName) {
    //applicationName is derived from Jenkins project name (which is second to last in the full name)
    def jobPathElements = currentBuild.fullProjectName.split('/')
    def applicationName = jobPathElements[jobPathElements.length >= 2? jobPathElements.length-2: jobPathElements.length-1]
    
    if (getGitBranchName(false).matches(currentBranchName)) {
        //If we're processing a RELEASE, we'll be dealing with the whole set of projects (ie at the parent pom level)
        applicationName = 'gocwebtemplate-sample-jsp' //TODO: >>>>>>>>>>>>>>>>> Change this!
    }    
    
    echo('Application name: [' + applicationName + ']')
    
    return applicationName
}
    
def getGitBranchName(doEcho = true) {
    
    def branchName = env.BRANCH_NAME
    
    //If this is not run in a MULTIBRANCH pipeline, BRANCH_NAME will not
    //be available. In that case the branch name can be acquired from
    //Git (requires the "Chekcout to matching local branch" option to
    //be enabled in SCM).
    //def git = tool('git')
    //def branchName = sh(script: "'${git}' branch | grep \"\\* \"", returnStdout: true).replace("* ", "").trim()
    
    if (doEcho) echo('Detected Git Branch: [' + branchName + ']')

    return branchName
}


/* Returns the specified projectModel's version by looking 
   first to see if a version is defined, and if not go to 
   the parent's version

   Fails build is no version is found at all.
*/
def getEffectiveVersionFromProjectModel(projectModel) {
    
    def projectVersion = projectModel.version
    
    if (!projectVersion) { //version is null, get from parent (if any) 
        if (projectModel.parent) projectVersion = projectModel.parent.version
    }
    
    if (!projectVersion) { //still no version... fail
        error('Could not find version in project model. Make sure pom.xml specifies a version or a parent pom version.')
    }
    
    return projectVersion
}

/* Returns the specified projectModel's groupId by looking 
   first to see if a groupId is defined, and if not go to 
   the parent's groupId

   Fails build is no groupId is found at all.
*/
def getEffectiveGroupIdFromProjectModel(projectModel) {
    
    def projectGroupId = projectModel.groupId
    
    if (!projectGroupId) { //groupId is null, get from parent (if any) 
        if (projectModel.parent) projectGroupId = projectModel.parent.groupId
    }
    
    if (!projectGroupId) { //still no groupId... fail
        error('Could not find groupId in project model. Make sure pom.xml specifies a groupId or a parent pom groupId.')
    }
    
    return projectGroupId
}


pipeline {
    
    agent any; //any: Run on any available agent - agent is same for all stages
               //none: No top-level setting.  Each stage must declare its own agent. Needed for non-blocking input.

//TODO: Cleanup               
/*               
    parameters {
        choice name: 'SADE_VERTICAL', choices: ['', 'dev', 'int', 'sys'], description: 'SADE vertical to target for deployment. (Default when blank:' + (deploySADEVerticalMap[projectGitBranchName]?.vertical ?: '') + ')' 
        choice name: 'SADE_STREAM', choices: ['', '1', '2', '3'], description: 'SADE stream to target for deployment. (Default when blank:' + (deploySADEVerticalMap[projectGitBranchName]?.stream ?: '') + ')' 
		choice name: 'DEPLOY_ACTION', choices: ['', 'deploy', 'stop', 'start', 'restart'], description: 'SADE deploy action. (Default when blank:' + (deploySADEVerticalMap[projectGitBranchName]?.deployAction ?: '') + ')' 
    }               
*/
    options {
        buildDiscarder logRotator(artifactNumToKeepStr: '2', numToKeepStr: '10')
        disableConcurrentBuilds()
        timestamps()
        skipDefaultCheckout() //skip SCM pull before first stage, we'll do our own
    }
    
    tools {
        maven('maven')
        git('git')
    }
    
    triggers {
        //pollSCM 'H/30 * * * *' //every 30 minutes
        pollSCM(branchIsRelease? '' : 'H * * * *') //never for release, every hour for others
        snapshotDependencies()
    }    
    
    stages {
        stage('Checkout from SCM') {
            steps {
                //We won't be using the simple "checkout(scm)" here because we want
                //to setup our own path restriction and that is not supported
                //by the default multibranch git provider.
                //checkout(scm)
                
                checkout([$class: 'GitSCM', 
                    userRemoteConfigs: [[credentialsId: projectGitCredsName, 
                                         url: projectGitURL]],
                    branches: projectGitWatchedBranches, 
                    extensions: [[$class: 'PathRestriction', //SCM poll filter by path
                                    excludedRegions: '', 
                                    includedRegions: projectGitWatchedPathRegex],
                                 [$class: 'LocalBranch',  //Checkout as named local branch, required for our getGitBranchName to work on non-multibranch pipeline
                                    localBranch: '**']]])
            }
        }
        
        stage('Project Artifact Version Check') {
            //Could only use "when branch" in a multibranch build
            //when {branch('master')}
            //Could also use "when expression" for the "if", but we have an "else" and don't want to create two stages for this
            //when { expression {return getGitBranchName().matches(projectReleaseBranchRegex)} }

            steps {
                script {
                    def projectModel = readMavenPom(file: projectPom)
                    
                    if (branchIsRelease) {
                        //RELEASE build: make sure our dependencies are not snapshots
                        def mavenDesc = Artifactory.mavenDescriptor()
                        
                        mavenDesc.pomFile = projectPom
                        if (mavenDesc.hasSnapshots()) {
                            currentBuild.result = 'ABORTED'
                            error('Snapshot(s) detected in dependencies.  Based on the branch, this is a release build.  Snapshot dependencies are not allowed.')
                        }
                        
                        // Also make sure our own version is not a snapshot
                        if (getEffectiveVersionFromProjectModel(projectModel).toUpperCase().endsWith('-SNAPSHOT')) {
                            currentBuild.result = 'ABORTED'
                            error('Trying to build a SNAPSHOT project version from a release branch.  Please update the pom.xml')
                        }
                    } else {
                        //NOT a release build: Warn if building a release version.
                        if (!getEffectiveVersionFromProjectModel(projectModel).toUpperCase().endsWith('-SNAPSHOT')) {
                            currentBuild.result = 'ABORTED' //'UNSTABLE'
                            error('Trying to build RELEASE (ie non-SNAPSHOT) project version from a non-release branch.  Are you sure this is what you want? If not you will want to update the pom.xml.')
                        }
                    }
                } //of script
            } //of steps
        } //of stage
        
        stage('Prepare for Release') {
            when { expression {return getGitBranchName().matches(projectReleaseBranchRegex)} }
            steps {
                script {
                    def git = tool('git')

                    //---[ A little value gymnastic
                    def projectModel = readMavenPom(file: projectPom)
                    def version = projectModel.version.replaceAll('-SNAPSHOT', '') 
                    def tagName = "v" + version                     
                    def revNumPos = version.lastIndexOf('.') + 1
                    def revNum = Integer.parseInt(version.substring(revNumPos)) + 1
                    def nextVersion = version.substring(0, revNumPos) + revNum + '-SNAPSHOT'

                    def gitURLWithCreds = projectGitURL.replaceAll('//', '//${CREDENTIALS}@')

                    //---[ Remove "-SNAPSHOT" from version
                    withMaven(maven: 'maven', publisherStrategy: 'EXPLICIT') { //turn off publishers for this invocation
                        sh(script: "mvn --batch-mode --errors versions:set -DnewVersion=${version}")
                    }
                    
                    //---[ Commit release version changes and create tag
                    withCredentials([usernameColonPassword(credentialsId: projectGitCredsName, variable: 'CREDENTIALS')]) {
                        sh(script: "'${git}' config --local user.name Jenkins-CI")
                        sh(script: "'${git}' config --local user.email jenkins@jade-build.intra.dev")
                        
                        sh(script: "'${git}' commit --all --message 'Jenkins build ${currentBuild.fullProjectName} #${BUILD_NUMBER} preparing release ${version} on ${java.time.LocalDateTime.now()}'")
                        
                        sh(script: "'${git}' tag --annotate release/${tagName} --message 'Release ${version} (Jenkins build ${currentBuild.fullProjectName} #${BUILD_NUMBER})'")
                    }
                    
                    //---[ Set next version right away 
                    withMaven(maven: 'maven', publisherStrategy: 'EXPLICIT') { //turn off publishers for this invocation
                        sh(script: "mvn --batch-mode --errors versions:set -DnewVersion=${nextVersion}")
                    }
                    
                    //---[ Commit next version, push everything and checkout newly created tag for building
                    withCredentials([usernameColonPassword(credentialsId: projectGitCredsName, variable: 'CREDENTIALS')]) {
                        sh(script: "'${git}' commit --all --message 'Jenkins build ${currentBuild.fullProjectName} #${BUILD_NUMBER} updating pom to next version ${version} on ${java.time.LocalDateTime.now()}'")
                    
                        sh(script: "'${git}' push ${gitURLWithCreds}")
                        sh(script: "'${git}' push ${gitURLWithCreds} tag release/${tagName}")
                        
                        sh(script: "'${git}' checkout release/${tagName}")
                    }                    
                } //of script
            } //of steps
        } //of stage        
        
        stage('Build and Deploy to Artifactory') {
            steps {
                script {
                    def git = tool('git')
                    def gitCommitId = sh(script: "'${git}' rev-parse HEAD", returnStdout: true).trim()

                    withMaven(maven: 'maven') {
                        sh(script: "mvn --batch-mode --errors --update-snapshots -Dbuild_number=${BUILD_NUMBER} -Dbuild_githash=${gitCommitId} -f ${projectPom} clean install") //TODO: >>>>>>>>> CHANGE THIS
                    }
                }
            }
        }
        
        stage("Deployment to SADE") {
            when {
                expression {                
                    //only for NON-release branches AND projects that are NOT in the exluded list
                    return !(branchIsRelease || deploySADEExludedProjects.contains(applicationName))
                }
            }
            steps {
                script {
                  
                    def sadeVerticalName = (deploySADEVerticalMap[projectGitBranchName]?.vertical ?: '')
                    def sadeStreamName = (deploySADEVerticalMap[projectGitBranchName]?.stream ?: '')
                    def sadeDeployAction = (deploySADEVerticalMap[projectGitBranchName]?.deployAction ?: '')
                                        
                    if (sadeVerticalName && sadeStreamName) { //do we have a SADE_VERTICAL+STREAM value?
                        def projectModel = readMavenPom(file: projectPom)
                        
                        echo("${new java.util.Date()}: Invoking SADE Deploy Build")
                        
                        //NOTE: Setting wait: true would be ideal (so we'd know if the deploy failed), but it creates bottlenecks in Jenkins
                        build(job: deploySADEBuildName, wait: false, propagate: true,
                            parameters: [
                                string(name: 'DEPLOY_TARGET_VERTICAL', value: sadeVerticalName), 
                                string(name: 'DEPLOY_TARGET_STREAM', value: sadeStreamName), 
                                string(name: 'DEPLOY_ACTION', value: sadeDeployAction), 
                                string(name: 'DEPLOY_GROUPID', value: getEffectiveGroupIdFromProjectModel(projectModel)), 
                                string(name: 'DEPLOY_ARTIFACTID', value: projectModel.artifactId), 
                                string(name: 'DEPLOY_ARTIFACT_VERSION', value: getEffectiveVersionFromProjectModel(projectModel))
                                ])
                    } else {
                        //currentBuild.result = 'UNSTABLE'
                        echo('WARNING: SADE Vertical name is blank, skipping deployment.')
                    }
                } //of script
            } //of steps
        } //of stage
    }

    post {
        always { //Always run, regardless of build status
            //archiveArtifacts(artifacts: "${applicationName}/target/*.?ar", allowEmptyArchive: true, fingerprint: true)
            junit(testResults: "${applicationName}/target/surefire-reports/TEST-*.xml", allowEmptyResults: true)
            script {
                if (branchIsRelease) {
                    emailext(to: emailextConfig.to,
                             from: emailextConfig.from,
                             body: emailextConfig.body,
                             mimeType: emailextConfig.mimeType,
                             subject: "Jenkins Build [${currentBuild.fullDisplayName}] RELEASE BUILD STATUS - [${currentBuild.result}]")
                }
            }
        }
        regression { //Run if the current builds status is worse than the previous builds status
            emailext(to: emailextConfig.to,
                     from: emailextConfig.from,
                     body: emailextConfig.body,
                     mimeType: emailextConfig.mimeType,
                     subject: "Jenkins Build [${currentBuild.fullDisplayName}] Regression - [${currentBuild.result}] for application [${applicationName}]")
        } 
        fixed { //Run if the previous build was not successful and the current builds status is "Success"
            emailext(to: emailextConfig.to,
                     from: emailextConfig.from,
                     body: emailextConfig.body,
                     mimeType: emailextConfig.mimeType,
                     subject: "Jenkins Build [${currentBuild.fullDisplayName}] Success - [${currentBuild.result}] for application [${applicationName}]")
        }
    }
}
