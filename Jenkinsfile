pipeline {
    agent any
    environment {
        DOCKER_REGISTRY = "ghcr.io/imastoridis"
        IMAGE_NAME = "station-tracker-app"
        // Pulls the secret from Jenkins Credentials store
        SNCF_API_KEY = credentials('SNCF_API_KEY')
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'master',
                    url: 'https://github.com/imastoridis/stationTracker.git',
                    credentialsId: 'ghcr-credentials'
            }
        }

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
                // This pulls your GHCR username and token/password into variables
                withCredentials([usernamePassword(credentialsId: 'ghcr-credentials',
                                                  passwordVariable: 'GHCR_TOKEN',
                                                  usernameVariable: 'GHCR_USER')]) {
                    sh '''
                        # 1. Log in to GitHub Container Registry
                        echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USER --password-stdin

                        # 2. Build the image
                        docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest .

                        # 3. Push the image
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sshagent(['hetzner-server-ssh-key']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=accept-new root@77.42.32.210 "
                            export SNCF_API_KEY=$SNCF_API_KEY && \
                            cd /var/www/stationTracker && \
                            docker compose pull app && \
                            docker compose up -d postgres kafka app
                        "
                    '''
                }
            }
        }
    }
}