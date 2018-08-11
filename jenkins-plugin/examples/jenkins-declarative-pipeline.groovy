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
            buildBouncer 'https://jenkins.example.com/'
        }
    }
}
