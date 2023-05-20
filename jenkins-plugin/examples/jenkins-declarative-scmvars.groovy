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
                    scmVars: scmVars
        }
        aborted {
            buildClerk serverUrl: 'https://clerk.example.com',
                    status: 'FAILED',
                    scmVars: scmVars
        }
        failure {
            buildClerk serverUrl: 'https://clerk.example.com',
                    status: 'FAILED',
                    scmVars: scmVars
        }
        unstable {
            buildClerk serverUrl: 'https://clerk.example.com',
                    status: 'FAILED',
                    scmVars: scmVars
        }
    }
}
