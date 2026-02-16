pipeline {
    agent any
    environment {
        // Use your actual GitHub username here
        DOCKER_REGISTRY = "ghcr.io/imastoridis"
        IMAGE_NAME = "station-tracker-app"
        // Pulls the secret from Jenkins Credentials store
        SNCF_API_KEY = credentials('SNCF_API_KEY')
    }
    stages {
        stage('Test Connection') {
            steps {
                sshagent(['hetzner-server-ssh-key']) {
                    sh 'ssh -o StrictHostKeyChecking=no root@77.42.32.210 "hostname"'
                }
            }
        }

        stage('Maven Build') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }
        stage('Docker Build & Push') {
            steps {
                withDockerRegistry(credentialsId: 'ghcr-credentials', url: 'https://ghcr.io') {
                    sh "docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest ."
                    sh "docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest"
                }
            }
        }
        stage('Deploy') {
            steps {
                // We pass the secret directly into the compose environment
                sh """
                export SNCF_API_KEY=${SNCF_API_KEY}
                docker compose pull app
                docker compose up -d app
                """
            }
        }
    }
}