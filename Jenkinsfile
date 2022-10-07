// Project's POM file
def applicationName = 'JavaTemplates'
def projectPom = './pom.xml'

//TODO: Add to email recipients, set versions, actually deploy, deploy archetypes

// Email extension plugin base parameters
def emailextConfig = [
    to: 'pierre.lupien@hrsdc-rhdcc.gc.ca', //comma-separated lits of addresses
    from: 'Jenkins-CI <jenkins-ci@jade-build.intra.dev>',
    body: '${SCRIPT, template="groovy-html.template"}', //for details on body: https://wiki.jenkins.io/display/JENKINS/Email-ext+plugin#Email-extplugin-Scriptcontent
    mimeType: 'text/html'    
    ]

pipeline {
    
    agent any; //any: Run on any available agent - agent is same for all stages
               //none: No top-level setting.  Each stage must declare its own agent. Needed for non-blocking input.
    
    options {
        buildDiscarder logRotator(artifactNumToKeepStr: '5', numToKeepStr: '10')
        disableConcurrentBuilds()
        timestamps()
    }
    
    tools {
        maven('maven')
        git('git')
        ant('Ant')
        jdk('JDK11')
    }
   
    parameters {
      string(defaultValue: '', description: 'Deploy version (e.g. 2.0.1)', name: 'DEPLOY_VERSION', trim: true)
    }    
    
    stages {
        stage('Project Artifact Version Check') {
            steps {
                script {
                    def projectModel = readMavenPom(file: projectPom)
                    
                    if (DEPLOY_VERSION.contains(" ")) {
                        currentBuild.result = 'ABORTED'
                        error('DEPLOY_VERSION must not contain spaces.')
                    }
                    
                    if (!DEPLOY_VERSION.toUpperCase().endsWith('-SNAPSHOT')) {
                        //RELEASE build: make sure our dependencies are not snapshots
                        def mavenDesc = Artifactory.mavenDescriptor()
                        
                        mavenDesc.pomFile = projectPom
                        if (mavenDesc.hasSnapshots()) {
                            currentBuild.result = 'ABORTED'
                            error('Snapshot(s) detected in dependencies.  This is a release build.  Snapshot dependencies are not allowed.')
                        }
                    }
                } //of script
            } //of steps
        } //of stage
        
        stage('Build and Deploy to Artifactory') {
            input {
                message "About to deploy version [${DEPLOY_VERSION}] to Artifactory, are you sure?"
                ok 'Yes, deploy!'
                submitterParameter 'DEPLOY_SUBMITTER'
            }
            steps {
                script {
                    def git = tool('git')
                    def gitCommitId = sh(script: "'${git}' rev-parse HEAD", returnStdout: true).trim()
                    
                    withMaven(maven: 'maven') {
                        sh(script: "mvn --batch-mode --errors --update-snapshots -Dbuild_number=${BUILD_NUMBER} -Dbuild_git_commitid=${gitCommitId} --file ${projectPom} clean package")
                    }
                }
            }
        }
    }

    post {
        always { //Always run, regardless of build status
            archiveArtifacts(artifacts: "${applicationName}/target/*.?ar", allowEmptyArchive: true, fingerprint: true)
            
            junit(testResults: "${applicationName}/target/surefire-reports/TEST-*.xml", allowEmptyResults: true)
            
            emailext(to: emailextConfig.to,
                     from: emailextConfig.from,
                     body: emailextConfig.body,
                     mimeType: emailextConfig.mimeType,
                     subject: "Jenkins Build [${currentBuild.fullDisplayName}] Built - [${currentBuild.result}] for application [${applicationName}]")
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
