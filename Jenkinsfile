@Library('ICANN_LIB') _

final MAIN_BRANCH = 'master'
final C4_PATH = 'docs/diagrams'

properties([
    [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '8', daysToKeepStr: '7', numToKeepStr: '8']],
    parameters([
        string(defaultValue: 'false', description: 'Force publishing c4 diagrams', name: 'FORCE_C4_PUBLISH'),
    ])
])

def hasPathChanged(pathName) {
    def utils = new icann.Utilities()
    def parentCommit = utils.getGitParentCommit()
    if (parentCommit == "") {
        return true
    }
    sh "git diff --name-only HEAD^ HEAD | egrep '^${pathName}' | wc -l > folderchanged.txt"
    def matches = readFile('folderchanged.txt').trim()
    echo "folderchanged.txt value for path = ${pathName} is ${matches}"
    return matches.toInteger() > 0
}

node('docker') {

    def utils = new icann.Utilities()

    try{

        utils.notifyBuild("STARTED", 'jenkinsjobs')

        stage('Environment') {
             checkout scm
        }

        stage('Run Tests') {
            if( "${env.BRANCH_NAME}" == 'master') {
                utils.mvn(args: 'clean deploy', jdkVersion: 'jdk21', publishArtifacts: true)
            } else {
                utils.mvn(args: 'clean verify', jdkVersion: 'jdk21')
            }
        }

        stage('SonarQube Analysis') {
            withSonarQubeEnv('ICANN') {
                utils.mvn(args: 'sonar:sonar -Dsonar.java.source=21 -Dsonar.java.target=21', jdkVersion: 'jdk21')
            }
        }

        stage('Publish C4') {
            if ("${env.BRANCH_NAME}" == MAIN_BRANCH && (hasPathChanged(C4_PATH) || params.FORCE_C4_PUBLISH == 'true')) {
                utils.buildC4Diagram(path: C4_PATH, pillar: 'ts')
            } else {
                echo "Skipping C4 publish (branch=${env.BRANCH_NAME}, ${C4_PATH} unchanged, FORCE_C4_PUBLISH=${params.FORCE_C4_PUBLISH})"
            }
        }

        stage('Notify JIRA') {
          jiraSendDeploymentInfo environmentId: 'qa', environmentName: 'qa', environmentType: 'testing'
        }
     } catch (e) {
         currentBuild.result = "FAILED"
         throw e
     }
    finally {
         step([$class: 'Publisher'])
         utils.notifyBuild(currentBuild.result, 'ts-eng-builds')
    }
}
