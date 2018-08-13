def scmVars

pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                script {
                    // capture the checkout details for later use
                    scmVars = git url: 'https://github.com/outofcoffee/build-clerk.git'
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
        always {
            buildClerk serverUrl: 'http://b2c409e2.ngrok.io',
                    branch: scmVars.GIT_LOCAL_BRANCH,
                    commit: scmVars.GIT_COMMIT
        }
    }
}
