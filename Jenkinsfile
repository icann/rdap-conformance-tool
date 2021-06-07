@Library('ICANN_LIB') _

properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '8', daysToKeepStr: '7', numToKeepStr: '8']],])

node('general') {

    def utils = new icann.Utilities()

    try{

        utils.notifyBuild("STARTED", 'jenkinsjobs')

        stage ('Checkout on Slave'){

             checkout scm

        }

        stage ('Run Tests'){

            if( "${env.BRANCH_NAME}" == 'master'){
                utils.mvn(args: 'clean deploy', jdkVersion: 'jdk11')
            }
            else{
                utils.mvn(args: 'clean test', jdkVersion: 'jdk11')
            }
        }

     }
     catch (e) {
         currentBuild.result = "FAILED"
         throw e
     }
    finally{
         step([$class: 'Publisher'])
         utils.notifyBuild(currentBuild.result, 'jenkinsjobs')
    }
}