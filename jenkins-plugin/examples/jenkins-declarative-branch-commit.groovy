def scmVars

pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                script {
                    // capture the checkout details for later use
                    scmVars = git url: 'https://github.com/release-tools/build-clerk.git'
                }
            }
        }
        stage('Build') {
            steps {
                echo 'Hello world!'
            }
        }
    }
    post {
        success {
            buildClerk serverUrl: 'https://clerk.example.com',
                    status: 'SUCCESS',
                    branch: scmVars.GIT_LOCAL_BRANCH,
                    commit: scmVars.GIT_COMMIT
        }
        aborted {
            buildClerk serverUrl: 'https://clerk.example.com',
                    status: 'FAILED',
                    branch: scmVars.GIT_LOCAL_BRANCH,
                    commit: scmVars.GIT_COMMIT
        }
        failure {
            buildClerk serverUrl: 'https://clerk.example.com',
                    status: 'FAILED',
                    branch: scmVars.GIT_LOCAL_BRANCH,
                    commit: scmVars.GIT_COMMIT
        }
        unstable {
            buildClerk serverUrl: 'https://clerk.example.com',
                    status: 'FAILED',
                    branch: scmVars.GIT_LOCAL_BRANCH,
                    commit: scmVars.GIT_COMMIT
        }
    }
}
