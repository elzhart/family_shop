pipeline {
  agent any

  tools {
    jdk 'jdk21'          // Настрой в Jenkins: Manage Jenkins → Tools → JDK installations
  }

  environment {
    GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
    // Название SonarQube-сервера в Jenkins (Manage Jenkins → System → SonarQube servers)
    SONARQUBE_SERVER = 'sonarqube-server'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'chmod +x ./gradlew || true'
      }
    }

    stage('Build') {
      steps {
        sh "./gradlew --no-daemon clean assemble -x test"
      }
    }

    stage('Tests') {
      steps {
        // Юнит + интеграционные (Testcontainers)
        sh "./gradlew --no-daemon test jacocoTestReport"
      }
      post {
        always {
          junit 'build/test-results/test/*.xml'
          // Если используешь плагин JaCoCo в Jenkins — можно опубликовать:
          // jacoco execPattern: 'build/jacoco/test.exec', classPattern: 'build/classes/java/main', sourcePattern: 'src/main/java'
          archiveArtifacts artifacts: 'build/reports/**', allowEmptyArchive: true
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv("${SONARQUBE_SERVER}") {
          // Передаём URL/Token в Gradle через -D, чтобы плагин их увидел
          sh """
             ./gradlew --no-daemon sonarqube \
               -Dsonar.host.url=$SONAR_HOST_URL \
               -Dsonar.login=$SONAR_AUTH_TOKEN
          """
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 15, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Package') {
      when { branch 'main' }
      steps {
        sh "./gradlew --no-daemon clean build"
        archiveArtifacts artifacts: 'build/libs/*.jar', onlyIfSuccessful: true
      }
    }

    stage('Docker Build & Push') {
      when { allOf { branch 'main'; expression { return fileExists('Dockerfile') } } }
      steps {
        script {
          def tag = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
          sh """
            docker build -t ${IMAGE}:${tag} -t ${IMAGE}:latest .
            # docker login... (если реестр требует логин)
            # docker push ${IMAGE}:${tag}
            # docker push ${IMAGE}:latest
          """
        }
      }
    }
  }

  post {
    always {
      cleanWs(deleteDirs: true, notFailBuild: true)
    }
  }
}