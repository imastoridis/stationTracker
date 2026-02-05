pipeline {
    agent any

    environment {
        // Define names for your docker image
        DOCKER_IMAGE = "station-tracker-app"
        REGISTRY_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                // Jenkins automatically checks out code from your linked Git repo
                echo 'Checking out source code...'
            }
        }

        stage('Maven Build & Test') {
            steps {
                // We use the Maven wrapper to ensure environment consistency
                sh './mvnw clean verify'
            }
            post {
                success {
                    // Archive the test results in Jenkins UI
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building Docker Image: ${DOCKER_IMAGE}:${REGISTRY_TAG}"
                // Build the image using the Dockerfile in the root
                sh "docker build -t ${DOCKER_IMAGE}:${REGISTRY_TAG} ."
                sh "docker tag ${DOCKER_IMAGE}:${REGISTRY_TAG} ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Deploy (Local Dev)') {
            steps {
                echo 'Restarting containers with new image...'
                // This restarts only the app service using the newly built image
                sh 'docker-compose up -d --no-deps app'
            }
        }
    }

    post {
        always {
            echo 'Cleaning up workspace...'
            // Optional: remove old build artifacts to save disk space
        }
        failure {
            echo 'Pipeline failed! Check the logs.'
        }
    }
}