pipeline {
    agent any
    stages {
        stage('Example Build') {
            steps {
                echo 'Hello world!'
            }
        }
    }
    post {
        always {
            buildClerk 'https://jenkins.example.com/'
        }
    }
}
