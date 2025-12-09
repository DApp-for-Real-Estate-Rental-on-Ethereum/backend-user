pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE_NAME = 'medgm/real-estate-user-service'
        APP_NAME = 'real-estate-user-service'
        SONARQUBE_TOKEN = credentials('sonarqube-token')  // Uncomment when SonarQube is configured
    }
    
    // No parameters needed while deploying locally only

    stages {
        stage('Checkout') {
            steps {
                script {
                    sh 'git rev-parse --short HEAD'
                }
            }
        }
        
        stage('Verify Project Layout') {
            steps {
                script {
                    sh '''
                        echo "Verifying backend-user repository layout..."
                        pwd
                        ls -la
                        echo "Contents of backend-user directory:"
                        ls -la
                        if [ ! -f pom.xml ]; then
                            echo "ERROR: pom.xml not found!"
                            echo "Available files:"
                            ls -la
                            exit 1
                        fi
                        echo "pom.xml found."
                        echo "Cleaning up any leftover files from previous builds..."
                        rm -rf backend backend@tmp target .dc-data || true
                        echo "Workspace cleaned, current contents:"
                        ls -la
                    '''
                }
            }
        }
        
        stage('Build & Test') {
            steps {
                script {
                    sh '''
                        echo "Building Spring Boot User Service..."
                        echo "Current workspace contents:"
                        ls -la "$WORKSPACE"
                        echo "Checking if pom.xml exists in workspace root:"
                        ls -la "$WORKSPACE/pom.xml"
                        
                        # Check for Maven wrapper
                        if [ -f "./mvnw" ]; then
                            chmod +x ./mvnw
                            echo "Using Maven wrapper..."
                            ./mvnw -B clean package -DskipTests
                        else
                            echo "Maven wrapper not found, using Docker Maven..."
                            # Create a fresh container and copy files
                            CONTAINER_ID=$(docker create -w /workspace maven:3.9-eclipse-temurin-17 sh -c "
                                echo 'Inside Docker container:'
                                pwd
                                ls -la
                                echo 'Running Maven build (skipping tests - no DB in CI)...'
                                mvn -B clean package -DskipTests
                            ")
                            
                            echo "Container ID: $CONTAINER_ID"
                            echo "Copying workspace files to container..."
                            docker cp "$WORKSPACE/." $CONTAINER_ID:/workspace/
                            
                            echo "Starting container..."
                            docker start -a $CONTAINER_ID
                            
                            echo "Copying build results back to workspace..."
                            docker cp $CONTAINER_ID:/workspace/target "$WORKSPACE/" || true
                            
                            echo "Cleaning up container..."
                            docker rm $CONTAINER_ID
                        fi
                    '''
                }
            }
        }
        
        stage('Publish Test Results') {
            steps {
                script {
                    sh '''
                        echo "Publishing test results..."
                        if [ -d "target/surefire-reports" ]; then
                            echo "Test reports found:"
                            ls -la target/surefire-reports/
                        else
                            echo "No test reports found (tests skipped in CI - no DB available)"
                        fi
                    '''
                }
                // Tests are skipped in CI due to missing PostgreSQL
                // junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            }
        }
        
        stage('Code Quality Analysis') {
            steps {
                script {
                    // SonarQube analysis - placeholder (enable when SonarQube is configured)
                    echo "SonarQube analysis stage - placeholder"
                    echo "Configure 'sonarqube-token' credential in Jenkins to enable"
                    // Uncomment below when SonarQube is configured:
                        sh '''
                        if [ -f "./mvnw" ]; then
                            ./mvnw sonar:sonar \
                              -Dsonar.projectKey=real-estate-user-service \
                              -Dsonar.host.url=http://localhost:9000 \
                              -Dsonar.login=${SONARQUBE_TOKEN}
                        fi
                    '''
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                script {
                    // OWASP Dependency Check - placeholder
                    echo "Security scan stage - placeholder"
                    echo "OWASP dependency check can be enabled when needed"
                    // Uncomment below to enable OWASP scanning:
                    // sh '''
                    //     if [ -f "./mvnw" ]; then
                    //         ./mvnw org.owasp:dependency-check-maven:check \
                    //           -Danalyzer.retirejs.enabled=false \
                    //           -Danalyzer.node.audit.skip=true || true
                    //     fi
                    // '''
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh '''
                        echo "Building Docker image for user service..."
                        docker build -t medgm/real-estate-user-service:${BUILD_NUMBER} .
                        docker tag medgm/real-estate-user-service:${BUILD_NUMBER} medgm/real-estate-user-service:latest
                        
                        # Get git commit short hash
                        GIT_COMMIT_SHORT=$(git rev-parse --short HEAD)
                        docker tag medgm/real-estate-user-service:${BUILD_NUMBER} medgm/real-estate-user-service:${GIT_COMMIT_SHORT}
                        
                        echo "Docker images created:"
                        docker images | grep medgm/real-estate-user-service
                    '''
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-registry-creds', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh '''
                            echo "Logging into Docker Hub..."
                            echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                            
                            # Get git commit short hash
                            GIT_COMMIT_SHORT=$(git rev-parse --short HEAD)
                            
                            echo "Pushing images to Docker Hub..."
                            docker push medgm/real-estate-user-service:${BUILD_NUMBER}
                            docker push medgm/real-estate-user-service:latest
                            docker push medgm/real-estate-user-service:${GIT_COMMIT_SHORT}
                        '''
                    }
                }
            }
        }
        
        stage('Deploy to Local Registry') {
            steps {
                script {
                    sh '''
                        echo "Tagging and pushing to local registry..."
                        docker tag medgm/real-estate-user-service:${BUILD_NUMBER} localhost:5000/real-estate-user-service:${BUILD_NUMBER}
                        docker tag medgm/real-estate-user-service:${BUILD_NUMBER} localhost:5000/real-estate-user-service:latest
                        
                        docker push localhost:5000/real-estate-user-service:${BUILD_NUMBER}
                        docker push localhost:5000/real-estate-user-service:latest
                        
                        echo "User service deployed to local registry!"
                        echo "Available images:"
                        docker images | grep real-estate-user-service
                    '''
                }
            }
        }
        
        // Deploy to Kubernetes stage removed for now (local-only testing)
        
        stage('Integration Tests') {
            steps {
                script {
                    sh '''
                        echo "Integration tests stage - placeholder for future integration testing"
                        echo "This stage can be extended when integration test infrastructure is available"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            script {
                sh '''
                    echo "Cleaning up Docker images..."
                    docker rmi medgm/real-estate-user-service:${BUILD_NUMBER} || true
                    docker rmi medgm/real-estate-user-service:latest || true
                    docker rmi localhost:5000/real-estate-user-service:${BUILD_NUMBER} || true
                '''
            }
        }
        success {
            script {
                echo "User service pipeline completed successfully! 🎉"
            }
        }
        failure {
            script {
                echo "User service pipeline failed! ❌"
            }
        }
    }
}
